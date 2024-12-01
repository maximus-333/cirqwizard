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
import javafx.beans.property.BooleanProperty;
import org.cirqwizard.geom.Circle;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ToolpathGenerator extends AbstractToolpathGenerator
{
    private final static int WINDOW_SIZE = 5000;
    private final static int WINDOWS_OVERLAP = 5;
    private final static int OOM_RETRY_DELAY = 1000;

    private int width;
    private int height;
    private int toolDiameter;
    private List<Circle> knownCircles;
    private BooleanProperty cancelledProperty;
    private double scale = 1;   // It has to go

    public void init(int width, int height, int inflation, int toolDiameter, List<GerberPrimitive> primitives,
                     BooleanProperty cancelledProperty)
    {
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.primitives = primitives;
        this.cancelledProperty = cancelledProperty;
        knownCircles = getKnownCircles(inflation);
    }

    public List<Toolpath> generate()
    {
        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(SettingsFactory.getApplicationSettings().getProcessingThreads().getValue());
        for (int x = 0; x < width; x += WINDOW_SIZE)
            for (int y = 0; y < height; y += WINDOW_SIZE)
                pool.submit(new WindowGeneratorThread(x > WINDOWS_OVERLAP ? x - WINDOWS_OVERLAP : x, y > WINDOWS_OVERLAP ? y - WINDOWS_OVERLAP : y, segments));

        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
        }

        return segments;
    }

    private class WindowGeneratorThread implements Runnable
    {
        private int x;
        private int y;
        private Vector<Toolpath> segments;

        private WindowGeneratorThread(int x, int y, Vector<Toolpath> segments)
        {
            this.x = x;
            this.y = y;
            this.segments = segments;
        }

        @Override
        public void run()
        {
            if (cancelledProperty.get())
                return;

            try
            {
                Platform.runLater(() -> progressProperty.set(((double) y * WINDOW_SIZE + (double) x * height) / ((double) width * height)));

                Point offset = new Point(x, y);
                List<Circle> translatedCircles = translateKnownCircles(offset, scale, knownCircles);

                int windowWidth = Math.min(WINDOW_SIZE + 2 * WINDOWS_OVERLAP, width - x);
                int windowHeight = Math.min(WINDOW_SIZE + 2 * WINDOWS_OVERLAP, height - y);
                RasterWindow window = new RasterWindow(new Point(x, y), windowWidth, windowHeight, scale);
                window.render(primitives, inflation);
                SimpleEdgeDetector detector = new SimpleEdgeDetector(window.getBufferedImage());
                window = null; // Helping GC to reclaim memory consumed by rendered image
                detector.process();
                if (detector.getOutput() != null)
                {
                    java.util.List<Toolpath> toolpaths =
                            new Tracer(detector.getOutput(), (int)(scale * windowWidth), (int)(scale * windowHeight), toolDiameter, translatedCircles).process();
                    detector = null;  // Helping GC to reclaim memory consumed by processed image
                    segments.addAll(translateToolpaths(toolpaths, offset, scale));
                }
            }
            catch (OutOfMemoryError e)
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Out of memory caught while generating tool paths. Retrying", e);
                try
                {
                    Thread.sleep((int)((1.0 + Math.random()) * OOM_RETRY_DELAY));
                }
                catch (InterruptedException e1)  {}

                run();
            }
            catch (Throwable e)
            {
                LoggerFactory.logException("Error while generating tool paths", e);
            }
        }
    }



}
