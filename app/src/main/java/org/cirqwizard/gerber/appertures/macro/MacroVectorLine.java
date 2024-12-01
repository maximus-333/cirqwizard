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

package org.cirqwizard.gerber.appertures.macro;

import org.cirqwizard.geom.Point;

public class MacroVectorLine extends MacroPrimitive
{
    private int width;
    private Point start;
    private Point end;

    public MacroVectorLine(int width, Point start, Point end, int rotationAngle)
    {
        super(rotationAngle);
        this.width = width;
        this.start = start;
        this.end = end;
    }

    public int getWidth()
    {
        return width;
    }

    public Point getStart()
    {
        return start;
    }

    public Point getEnd()
    {
        return end;
    }

    public Point getTranslatedStart()
    {
        return translate(start);
    }

    public Point getTranslatedEnd()
    {
        return translate(end);
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroVectorLine(width, start, end, getRotationAngle());
    }
}
