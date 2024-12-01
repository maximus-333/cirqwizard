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
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RubOutToolpathGenerator extends AbstractToolpathGenerator
{
    private final static int WINDOW_SIZE = 5000;
    private final static int WINDOWS_HORIZONTAL_OVERLAP = 5;
    private final static int WINDOWS_VERTICAL_OVERLAP = 500;
    private final static int OOM_RETRY_DELAY = 1000;

    private int x;
    private int y;
    private int width;
    private int height;
    private int toolDiameter;
    private int overlap;
    private int threadCount;
    private double scale = 1; // It has to go
    private BooleanProperty cancelledProperty;

    public void init(int x, int y, int width, int height, int inflation, int toolDiameter, int overlap, List<GerberPrimitive> primitives,
                     int threadCount, BooleanProperty cancelledProperty)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.overlap = overlap;
        this.primitives = primitives;
        this.threadCount = threadCount;
        this.cancelledProperty = cancelledProperty;
    }

    public List<Toolpath> generate()
    {
        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int x = this.x; x < this.x + width; x += WINDOW_SIZE)
            for (int y = this.y; y < this.y + height; y += WINDOW_SIZE)
                pool.submit(new WindowGeneratorThread(x > WINDOWS_HORIZONTAL_OVERLAP ? x - WINDOWS_HORIZONTAL_OVERLAP : x, y > WINDOWS_VERTICAL_OVERLAP ? y - WINDOWS_VERTICAL_OVERLAP : y, segments));

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

                int windowWidth = Math.min(WINDOW_SIZE + 2 * WINDOWS_HORIZONTAL_OVERLAP, width);
                int windowHeight = Math.min(WINDOW_SIZE + 2 * WINDOWS_VERTICAL_OVERLAP, height);
                RasterWindow window = new RasterWindow(new Point(x, y), windowWidth, windowHeight, scale);
                window.render(primitives, inflation + toolDiameter / 2);

                RubOutGenerator g = new RubOutGenerator(window.getBufferedImage(), toolDiameter, overlap);
                window = null;
                segments.addAll(translateToolpaths(g.process(), offset, scale));
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
