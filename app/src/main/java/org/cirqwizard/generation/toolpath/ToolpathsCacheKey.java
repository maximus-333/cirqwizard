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

package org.cirqwizard.generation.toolpath;


import java.io.Serializable;


public class ToolpathsCacheKey implements Serializable
{
    private int layerId;
    private int toolDiameter;

    // Insulation milling parameters
    private int additionalPasses;
    private int additionalPassesOverlap;
    private boolean additionalPassesAroundPadsOnly;

    // Rub out parameters
    private int rubOutInitialOffset;
    private int rubOutOverlap;

    public ToolpathsCacheKey(int layerId, int toolDiameter, int additionalPasses, int additionalPassesOverlap, boolean additionalPassesAroundPadsOnly,
                             int rubOutInitialOffset, int rubOutOverlap)
    {
        this.layerId = layerId;
        this.toolDiameter = toolDiameter;
        this.additionalPasses = additionalPasses;
        this.additionalPassesOverlap = additionalPassesOverlap;
        this.additionalPassesAroundPadsOnly = additionalPassesAroundPadsOnly;
        this.rubOutInitialOffset = rubOutInitialOffset;
        this.rubOutOverlap = rubOutOverlap;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToolpathsCacheKey that = (ToolpathsCacheKey) o;

        if (additionalPasses != that.additionalPasses) return false;
        if (additionalPassesAroundPadsOnly != that.additionalPassesAroundPadsOnly) return false;
        if (additionalPassesOverlap != that.additionalPassesOverlap) return false;
        if (layerId != that.layerId) return false;
        if (rubOutInitialOffset != that.rubOutInitialOffset) return false;
        if (rubOutOverlap != that.rubOutOverlap) return false;
        if (toolDiameter != that.toolDiameter) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = layerId;
        result = 31 * result + toolDiameter;
        result = 31 * result + additionalPasses;
        result = 31 * result + additionalPassesOverlap;
        result = 31 * result + (additionalPassesAroundPadsOnly ? 1 : 0);
        result = 31 * result + rubOutInitialOffset;
        result = 31 * result + rubOutOverlap;
        return result;
    }
}
