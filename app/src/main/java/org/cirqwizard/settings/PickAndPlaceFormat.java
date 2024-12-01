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
package org.cirqwizard.settings;

public enum PickAndPlaceFormat
{
    EAGLE("(?<name>\\S+)\\s+(?<x>-?\\d+.?\\d*)\\s+(?<y>-?\\d+.?\\d*)\\s+(?<angle>-?\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?",
            "(?<name>\\S+)\\s+(?<x>-?\\d+.?\\d*)\\s+(?<y>-?\\d+.?\\d*)\\s+(?<angle>-?\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?",
            "Eagle"),
    ALTIUM("(?<name>\\S+)\\s+(?<package>\\S+)\\s+(?<x>-?\\d+.?\\d*)mm\\s+(?<y>-?\\d+.?\\d*)mm\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(?<angle>-?\\d+.\\d*)\\s+(?<value>\\S+)\\s*",
            "(?<name>\\S+)\\s+(?<package>\\S+)\\s+(?<x>-?\\d+.?\\d*)mm\\s+(?<y>-?\\d+.?\\d*)mm\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(?<angle>-?\\d+.\\d*)\\s+(?<value>\\S+)\\s*",
            "Altium Designer"),
    ULTIBOARD("\"(?<name>\\S+)\",\"(?<value>\\S+)\",\"(?<package>\\S+)\",\"(?<x>-?\\d+.?\\d*)\",\"(?<y>-?\\d+.?\\d*)\",\"(?<angle>-?\\d+)\",\"TOP\",\"SMD\"",
            "\"(?<name>\\S+)\",\"(?<value>\\S+)\",\"(?<package>\\S+)\",\"(?<x>-?\\d+.?\\d*)\",\"(?<y>-?\\d+.?\\d*)\",\"(?<angle>-?\\d+)\",\"BOTTOM\",\"SMD\"",
            "UltiBoard"),
    DESIGNSPARK("\"(?<name>\\S+)\",\"(?<package>\\S+)\",\"Top\",\"(?<x>-?\\d+.?\\d*)\",\"(?<y>-?\\d+.?\\d*)\",\"(?<angle>-?\\d+.?\\d*)\",\"(?<value>.*)\"",
            "\"(?<name>\\S+)\",\"(?<package>\\S+)\",\"Bottom\",\"(?<x>-?\\d+.?\\d*)\",\"(?<y>-?\\d+.?\\d*)\",\"(?<angle>-?\\d+.?\\d*)\",\"(?<value>.*)\"",
            "DesignSpark"),
    KICAD("(?<name>\\S+),.*,(?<value>.+?),(?<package>\\S+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*)",
            "(?<name>\\S+),.*,(?<value>.+?),(?<package>\\S+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*)",
            "KiCAD"),
    DIPTRACE("(?<name>\\S+),(?<package>.+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),Top,(?<angle>-?\\d+.?\\d*),(?<value>\\S*)",
            "(?<name>\\S+),(?<package>.+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),Bottom,(?<angle>-?\\d+.?\\d*),(?<value>\\S*)",
            "DipTrace"),
    EASYPC("(?<name>\\S+),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*),(?<value>\\S*),(?<package>.*)",
            "(?<name>\\S+),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*),(?<value>\\S*),(?<package>.*)",
            "EasyPC"),
    PROTEUS("\"(?<name>\\S+)\",\"(?<value>\\S*)\",\"(?<package>.*)\",TOP,(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*)",
            "\"(?<name>\\S+)\",\"(?<value>\\S*)\",\"(?<package>.*)\",BOTTOM,(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>-?\\d+.?\\d*)",
            "Proteus");

    private String topRegex;
    private String bottomRegex;
    private String name;

    PickAndPlaceFormat(String topRegex, String bottomRegex, String name)
    {
        this.topRegex = topRegex;
        this.bottomRegex = bottomRegex;
        this.name = name;
    }

    public String getTopRegex()
    {
        return topRegex;
    }

    public String getBottomRegex()
    {
        return bottomRegex;
    }

    public String getName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return getName();
    }

    public static PickAndPlaceFormat forName(String name)
    {
        for (PickAndPlaceFormat f : values())
            if (f.getName().equals(name))
                return f;
        throw new IllegalArgumentException("No such format: " + name);
    }
}
