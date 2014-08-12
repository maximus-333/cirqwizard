/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.fx.machining;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.AdditionalToolpathGenerator;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.ApplicationSettings;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;

public class TraceMillingToolpathGenerationService extends ToolpathGenerationService
{
    private Layer layer;
    private int cacheLayerId;
    private long layerModificationDate;
    private GenerationKey generationKey;

    public TraceMillingToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                                 StringProperty estimatedMachiningTimeProperty,
                                                 Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty);
        this.layer = layer;
        this.cacheLayerId = cacheLayerId;
        this.layerModificationDate = layerModificationDate;
    }

    private class GenerationKey
    {
        private int toolDiameter;
        private int additionalToolpaths;
        private int additionalToolpathsOverlap;
        private boolean additionalToolpathsAroundPadsOnly;

        private GenerationKey(int toolDiameter, int additionalToolpaths, int additionalToolpathsOverlap, boolean additionalToolpathsAroundPadsOnly)
        {
            this.toolDiameter = toolDiameter;
            this.additionalToolpaths = additionalToolpaths;
            this.additionalToolpathsOverlap = additionalToolpathsOverlap;
            this.additionalToolpathsAroundPadsOnly = additionalToolpathsAroundPadsOnly;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenerationKey that = (GenerationKey) o;

            if (additionalToolpaths != that.additionalToolpaths) return false;
            if (additionalToolpathsAroundPadsOnly != that.additionalToolpathsAroundPadsOnly) return false;
            if (additionalToolpathsOverlap != that.additionalToolpathsOverlap) return false;
            if (toolDiameter != that.toolDiameter) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = toolDiameter;
            result = 31 * result + additionalToolpaths;
            result = 31 * result + additionalToolpathsOverlap;
            result = 31 * result + (additionalToolpathsAroundPadsOnly ? 1 : 0);
            return result;
        }
    }

    @Override
    protected Task<ObservableList<Toolpath>> createTask()
    {
        return new Task<ObservableList<Toolpath>>()
        {
            @Override
            protected ObservableList<Toolpath> call() throws Exception
            {
                try
                {
                    InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
                    GenerationKey newKey = new GenerationKey(settings.getToolDiameter().getValue(), settings.getAdditionalPasses().getValue(),
                            settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
                    if (generationKey != null && generationKey.equals(newKey))
                        return null;

                    overallProgressProperty.unbind();
                    generationStageProperty.unbind();
                    estimatedMachiningTimeProperty.unbind();

                    int diameter = settings.getToolDiameter().getValue();
                    ToolpathsCacheKey cacheKey = new ToolpathsCacheKey(cacheLayerId, context.getPcbLayout().getAngle(), diameter, settings.getAdditionalPasses().getValue(),
                            settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
                    ToolpathsCache cache = null;
                    try
                    {
                        cache = ToolpathsPersistor.loadFromFile(context.getPcbLayout().getFileName() + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
                    }

                    TraceLayer traceLayer = (TraceLayer) layer;
                    if (cache != null && cache.hasValidData(context.getPcbLayout().getFile().lastModified()))
                    {
                        if (cache.getToolpaths(cacheKey) != null)
                        {
                            traceLayer.setToolpaths(cache.getToolpaths(cacheKey));
                            return FXCollections.observableArrayList(cache.getToolpaths(cacheKey));
                        }
                    }
                    else
                        cache = new ToolpathsCache();

                    final ToolpathGenerator generator = new ToolpathGenerator();
                    ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();
                    generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                            diameter / 2, diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue());
                    Platform.runLater(() ->
                    {
                        generationStageProperty.setValue("Generating tool paths...");
                        overallProgressProperty.bind(generator.progressProperty());
                        estimatedMachiningTimeProperty.setValue("");
                    });

                    List<Toolpath> toolpaths = generator.generate();
                    if (toolpaths == null || toolpaths.size() == 0)
                        return null;
                    final int mergeTolerance = diameter / 4;
                    toolpaths = new ToolpathMerger(toolpaths, mergeTolerance).merge();

                    if (!settings.getAdditionalPassesPadsOnly().getValue())
                    {
                        Platform.runLater(() -> generationStageProperty.setValue("Generating additional passes..."));
                        for (int i = 0 ; i < settings.getAdditionalPasses().getValue(); i++)
                        {
                            int offset = diameter * (100 - settings.getAdditionalPassesOverlap().getValue()) / 100;
                            generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                                    diameter / 2 + offset * (i + 1), diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue());
                            List<Toolpath> additionalToolpaths = generator.generate();
                            if (additionalToolpaths == null || additionalToolpaths.size() == 0)
                                continue;
                            toolpaths.addAll(new ToolpathMerger(additionalToolpaths, mergeTolerance).merge());
                        }
                    }
                    else if (settings.getAdditionalPasses().getValue() > 0)
                    {
                        final AdditionalToolpathGenerator additionalGenerator = new AdditionalToolpathGenerator(mainApplication.getContext().getBoardWidth() + 1,
                                mainApplication.getContext().getBoardHeight() + 1, settings.getAdditionalPasses().getValue(),
                                settings.getAdditionalPassesOverlap().getValue(), diameter, applicationSettings.getProcessingThreads().getValue(), traceLayer.getElements());
                        Platform.runLater(() ->
                        {
                            generationStageProperty.setValue("Generating additional passes...");
                            overallProgressProperty.bind(additionalGenerator.progressProperty());
                        });
                        toolpaths.addAll(new ToolpathMerger(additionalGenerator.generate(), mergeTolerance).merge());
                    }


                    List<Chain> chains = new ChainDetector(toolpaths).detect();


                    final Optimizer optimizer = new Optimizer(chains, convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                            convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                            convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()), mergeTolerance);
                    Platform.runLater(() ->
                    {
                        generationStageProperty.setValue("Optimizing milling time...");
                        overallProgressProperty.unbind();
                        overallProgressProperty.bind(optimizer.progressProperty());
                    });

                    final DecimalFormat format = new DecimalFormat("00");
                    Platform.runLater(() ->
                    {
                        estimatedMachiningTimeProperty.bind(Bindings.createStringBinding(() ->
                        {
                            long totalDuration = (long) TimeEstimator.calculateTotalDuration(optimizer.getCurrentBestSolution(),
                                    convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                                    convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                                    convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()),
                                    true, mergeTolerance);
                            String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                                    ":" + format.format(totalDuration % 60);
                            return "Estimated machining time: " + time;
                        }, optimizer.currentBestSolutionProperty()));
                    });
                    chains = optimizer.optimize();

                    toolpaths.clear();
                    for (Chain p : chains)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);

                    cache.setToolpaths(cacheKey, toolpaths);
                    cache.setLastModified(layerModificationDate);
                    generationKey = newKey;

                    try
                    {
                        ToolpathsPersistor.saveToFile(cache, context.getPcbLayout().getFileName()  + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
                    }

                    return FXCollections.observableArrayList(toolpaths);
                }
                catch (NumberFormatException e)
                {
                    LoggerFactory.getApplicationLogger().log(Level.WARNING, "Could not parse tool diameter", e);
                    throw e;
                }
                catch (Exception e)
                {
                    LoggerFactory.logException("Error generating toolpaths", e);
                    throw e;
                }
            }
        };
    }

    private double convertToDouble(Integer i)
    {
        return i.doubleValue() / ApplicationConstants.RESOLUTION;
    }
}
