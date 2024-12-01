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

package org.cirqwizard.generation;

import javafx.application.Platform;
import org.cirqwizard.geom.Circle;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.generation.toolpath.CuttingToolpath;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AdditionalToolpathGenerator extends AbstractToolpathGenerator
{
    private static final int MIN_LENGTH = 250;

    private int width;
    private int height;
    private int passes;
    private int overlap;
    private int toolDiameter;

    public AdditionalToolpathGenerator(int width, int height, int passes, int overlap, int toolDiameter, List<GerberPrimitive> primitives)
    {
        this.width = width;
        this.height = height;
        this.passes = passes;
        this.overlap = overlap;
        this.toolDiameter = toolDiameter;
        this.primitives = primitives;
    }

    public List<Toolpath> generate()
    {
        long tt = System.currentTimeMillis();
        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(SettingsFactory.getApplicationSettings().getProcessingThreads().getValue());
        progressProperty.setValue(0);
        final double progressIncrement = 1.0 / primitives.size();

        for (final GerberPrimitive primitive : primitives)
        {
            pool.submit(() ->
            {
                try
                {
                    Platform.runLater(() -> progressProperty.setValue(progressProperty.getValue() + progressIncrement));
                    if (!(primitive instanceof Flash))
                        return null;

                    Flash flash = (Flash) primitive;
                    ArrayList<GerberPrimitive> primitivesCopy = new ArrayList<>(primitives);
                    primitivesCopy.remove(flash);
                    int windowSize = (flash.getAperture().getCircumRadius() + (inflation * (passes + 1) * overlap / 100)) * 2;
                    int x = flash.getX() - windowSize;
                    int y = flash.getY() - windowSize;
                    x = Math.max(0, x);
                    y = Math.max(0, y);
                    Point windowOffset = new Point(x, y);
                    int windowWidth = Math.min(windowSize * 2, width - x);
                    int windowHeight = Math.min(windowSize * 2, height - y);
                    for (int i = 0; i < passes; i++)
                    {
                        RasterWindow window = new RasterWindow(new Point(x, y), windowWidth, windowHeight);
                        window.render(primitivesCopy, toolDiameter / 2);
                        int inflation = toolDiameter / 2 + toolDiameter * (100 - overlap) / 100 * (1 + i);
                        window.render(Arrays.asList((GerberPrimitive) flash), inflation);
                        SimpleEdgeDetector detector = new SimpleEdgeDetector(window.getBufferedImage());
                        window = null; // Helping GC to reclaim memory consumed by rendered image
                        detector.process();
                        if (detector.getOutput() != null)
                        {
                            List<Circle> knownCircles = translateKnownCircles(windowOffset, 1, getKnownCircles(inflation));
                            List<Toolpath> toolpaths =
                                    new Tracer(detector.getOutput(), windowWidth, windowHeight, toolDiameter, knownCircles).process();
                            detector = null;  // Helping GC to reclaim memory consumed by processed image
                            for (Toolpath t : toolpaths)
                            {
                                Point from = ((CuttingToolpath)t).getCurve().getFrom();
                                Point to = ((CuttingToolpath)t).getCurve().getTo();
                                if ((t instanceof LinearToolpath) && from.distanceTo(to) < MIN_LENGTH)
                                    continue;
                                Point centerPoint = translateToWindowCoordinates(flash.getPoint(), windowOffset, 1);
                                int threshold = flash.getAperture().getCircumRadius() + (int)Math.sqrt(inflation * inflation * 2) + 10;
                                if (from.distanceTo(centerPoint) < threshold && to.distanceTo(centerPoint) < threshold)
                                    segments.addAll(translateToolpaths(Arrays.asList(t), windowOffset, 1));
                            }
                        }
                    }
                }
                catch (Throwable e)
                {
                    LoggerFactory.logException("Exception caught while generating additional passes", e);
                }
                return null;
            });
        }
        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e) {}

        tt = System.currentTimeMillis() - tt;
        System.out.println("Addtional passes generation time: " + tt);

        return segments;
    }
}
