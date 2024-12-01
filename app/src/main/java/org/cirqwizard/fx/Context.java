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

package org.cirqwizard.fx;

import org.cirqwizard.layers.Panel;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.settings.ToolSettings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Context
{
    public enum PcbPlacement {FACE_UP, FACE_DOWN, FACE_UP_SPACER}

    private PcbPlacement pcbPlacement;
    private Tool insertedTool;
    private ToolSettings currentMillingTool;
    private int currentMillingToolIndex;
    private Integer g54X;
    private Integer g54Y;
    private Integer g54Z;

    private int currentDrill;

    private ComponentId currentComponent;
    private Feeder feeder;
    private int feederRow;
    private Integer componentPitch;
    private Map<String, Integer> pitchCache = new HashMap<>();

    private Panel panel;
    private File panelFile;

    public PcbPlacement getPcbPlacement()
    {
        return pcbPlacement;
    }

    public void setPcbPlacement(PcbPlacement pcbPlacement)
    {
        this.pcbPlacement = pcbPlacement;
    }

    public Tool getInsertedTool()
    {
        return insertedTool;
    }

    public void setInsertedTool(Tool insertedTool)
    {
        this.insertedTool = insertedTool;
    }

    public ToolSettings getCurrentMillingTool()
    {
        return currentMillingTool;
    }

    public void setCurrentMillingTool(ToolSettings currentMillingTool)
    {
        this.currentMillingTool = currentMillingTool;
    }

    public int getCurrentMillingToolIndex()
    {
        return currentMillingToolIndex;
    }

    public void setCurrentMillingToolIndex(int currentMillingToolIndex)
    {
        this.currentMillingToolIndex = currentMillingToolIndex;
    }

    public Integer getG54X()
    {
        return g54X;
    }

    public void setG54X(Integer g54X)
    {
        this.g54X = g54X;
    }

    public Integer getG54Y()
    {
        return g54Y;
    }

    public void setG54Y(Integer g54Y)
    {
        this.g54Y = g54Y;
    }

    public Integer getG54Z()
    {
        return g54Z;
    }

    public void setG54Z(Integer g54Z)
    {
        this.g54Z = g54Z;
    }

    public int getCurrentDrill()
    {
        return currentDrill;
    }

    public void setCurrentDrill(int currentDrill)
    {
        this.currentDrill = currentDrill;
    }

    public ComponentId getCurrentComponent()
    {
        return currentComponent;
    }

    public void setCurrentComponent(ComponentId currentComponent)
    {
        this.currentComponent = currentComponent;
    }

    public Feeder getFeeder()
    {
        return feeder;
    }

    public void setFeeder(Feeder feeder)
    {
        this.feeder = feeder;
    }

    public int getFeederRow()
    {
        return feederRow;
    }

    public void setFeederRow(int feederRow)
    {
        this.feederRow = feederRow;
    }

    public Integer getComponentPitch()
    {
        return componentPitch;
    }

    public void setComponentPitch(int componentPitch)
    {
        this.componentPitch = componentPitch;
    }

    public Integer getPitchFromCache(String componentPackage)
    {
        return pitchCache.get(componentPackage);
    }

    public void savePitchToCache(String componentPackage, Integer pitch)
    {
        pitchCache.put(componentPackage, pitch);
    }

    public Panel getPanel()
    {
        return panel;
    }

    public void setPanel(Panel panel)
    {
        this.panel = panel;
    }

    public File getPanelFile()
    {
        return panelFile;
    }

    public void setPanelFile(File panelFile)
    {
        this.panelFile = panelFile;
    }
}
