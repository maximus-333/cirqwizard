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
package org.cirqwizard.layers;

import org.cirqwizard.generation.outline.OutlineGenerator;
import org.cirqwizard.logging.LoggerFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PanelBoard
{
    @Element
    private String filename;
    @Element
    private int x;
    @Element
    private int y;
    @Element
    private int angle;
    @Element
    private boolean generateOutline;
    @Element(required = false)
    private Date topLayerTimestamp;
    @Element(required = false)
    private Date bottomLayerTimestamp;
    @Transient
    private Board board;

    public PanelBoard()
    {
    }

    public PanelBoard(String filename, int x, int y)
    {
        this.filename = filename;
        this.x = x;
        this.y = y;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getAngle()
    {
        return angle;
    }

    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    public boolean isGenerateOutline()
    {
        return generateOutline;
    }

    public void setGenerateOutline(boolean generateOutline)
    {
        this.generateOutline = generateOutline;
    }

    public void resetCacheTimestamps()
    {
        topLayerTimestamp = null;
        bottomLayerTimestamp = null;
    }

    public void updateCacheTimestamps()
    {
        File topLayer = new File(filename + ".cmp");
        topLayerTimestamp = topLayer.exists() ? new Date(topLayer.lastModified()) : null;
        File bottomLayer = new File(filename + ".sol");
        bottomLayerTimestamp = bottomLayer.exists() ? new Date(bottomLayer.lastModified()) : null;
    }

    private boolean validateTimestamp(Date timestamp, File file)
    {
        if (timestamp == null && !file.exists())
            return true;
        if (timestamp == null && file.exists())
            return false;
        if (!file.exists())
            return false;
        return timestamp.compareTo(new Date(file.lastModified())) >= 0;
    }

    public boolean validateCacheTimestamps()
    {
        if (!validateTimestamp(topLayerTimestamp, new File(filename + ".cmp")))
            return false;
        return validateTimestamp(bottomLayerTimestamp, new File(filename + ".sol"));
    }

    public void rotate(boolean clockwise)
    {
        if (generateOutline)
        {
            try
            {
                loadBoard(true);
            }
            catch (IOException e)
            {
                LoggerFactory.logException("Could not load board files", e);
            }
        }
        angle += clockwise ? 90 : -90;
        angle %= 360;
        board.rotate(clockwise);
        if (generateOutline)
            new OutlineGenerator(this).generate();
    }

    public Board getBoard()
    {
        return board;
    }

    public void loadBoard() throws IOException
    {
        loadBoard(false);
    }

    private void loadBoard(boolean omitOutlineGeneration) throws IOException
    {
        board = new Board();
        board.loadLayers(filename);
        if (!board.hasLayers())
            return;
        int rotations = angle / 90;
        while (rotations != 0)
        {
            board.rotate(angle > 0);
            rotations += rotations > 0 ? -1 : 1;
        }
        if (generateOutline && !omitOutlineGeneration)
            new OutlineGenerator(this).generate();
    }

    public void centerInPanel(Panel panel)
    {
        x = (panel.getSize().getWidth() - board.getWidth()) / 2;
        y = (panel.getSize().getHeight() - board.getHeight()) / 2;
    }


}
