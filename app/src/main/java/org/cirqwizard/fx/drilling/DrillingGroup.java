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

package org.cirqwizard.fx.drilling;

import org.cirqwizard.fx.OperationsScreenGroup;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.ScreenGroup;
import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.layers.Board;
import org.cirqwizard.settings.ApplicationConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrillingGroup extends OperationsScreenGroup
{
    public DrillingGroup(String name)
    {
        super(name);
    }

    @Override
    protected boolean isEnabled()
    {
        return super.isEnabled() && !getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.DRILLING).isEmpty();
    }

    @Override
    public List<ScreenController> getChildren()
    {
        List<ScreenController> children = new ArrayList<>(super.getChildren());
        if (!isEnabled())
            return children;

        List<DrillPoint> drillPoints = (List<DrillPoint>) getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.DRILLING);
        List<Integer> drillDiameters = drillPoints.stream().
                map(DrillPoint::getToolDiameter).
                distinct().
                sorted().
                collect(Collectors.toList());
        for (int d : drillDiameters)
        {
            ScreenGroup group = new ScreenGroup("Drilling " + ApplicationConstants.formatToolDiameter(d) + "mm")
            {
                @Override
                public void select()
                {
                    getMainApplication().getContext().setCurrentDrill(d);
                    super.select();
                }
            };
            group.setParent(this);
            children.add(group.setMainApplication(getMainApplication()).
                    addChild(new InsertDrill().setMainApplication(getMainApplication())).
                    addChild(new Drilling().setMainApplication(getMainApplication())));
        }

        return children;
    }

}
