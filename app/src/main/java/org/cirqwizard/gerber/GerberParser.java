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

package org.cirqwizard.gerber;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.gerber.appertures.*;
import org.cirqwizard.gerber.appertures.macro.*;
import org.cirqwizard.geom.Point;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GerberParser
{
    private ArrayList<GerberPrimitive> elements = new ArrayList<>();

    private boolean parameterMode = false;
    private ApertureMacro apertureMacro = null;
    private HashMap<String, ApertureMacro> apertureMacros = new HashMap<>();
    private Region region = null;
    private HashMap<Integer, Aperture> apertures = new HashMap<>();

    private static final int MM_RATIO = 1 * ApplicationConstants.RESOLUTION;
    private static final int INCHES_RATIO = (int)(25.4 * ApplicationConstants.RESOLUTION);
    private int unitConversionRatio = MM_RATIO;

    private boolean omitLeadingZeros = true;
    private int integerPlaces = 2;
    private int decimalPlaces = 4;

    private Reader reader;

    private enum InterpolationMode
    {
        LINEAR,
        CLOCKWISE_CIRCULAR,
        COUNTERCLOCKWISE_CIRCULAR
    }

    private enum ArcQuadrantMode
    {
        SINGLE_QUADRANT, MULTI_QUADRANT
    }

    private InterpolationMode currentInterpolationMode = InterpolationMode.LINEAR;
    private ArcQuadrantMode arcQuadrantMode;
    private GerberPrimitive.Polarity polarity = GerberPrimitive.Polarity.DARK;

    private int x = 0;
    private int y = 0;

    private enum ExposureMode
    {
        ON,
        OFF,
        FLASH
    }

    private ExposureMode exposureMode = ExposureMode.OFF;

    private Aperture aperture = null;

    public GerberParser(Reader reader)
    {
        this.reader = reader;
    }

    public List<GerberPrimitive> parse() throws IOException
    {
        String str;
        while ((str = readDataBlock()) != null)
        {
            try
            {
                if (parameterMode)
                    parseParameter(str);
                else
                    processDataBlock(parseDataBlock(str));
            }
            catch (GerberParsingException e)
            {
                LoggerFactory.getApplicationLogger().log(Level.FINE, "Unparsable gerber element", e);
            }
        }

        return elements;
    }

    private String readDataBlock() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int i;
        boolean inCommentSection = false;
        while ((i = reader.read()) != -1)
        {
            if (i == '%' && !inCommentSection)
            {
                parameterMode = !parameterMode;
                apertureMacro = null;
            }
            else if (i == '*')
            {
                if (sb.length() > 0)
                    break;
            }
            else if (!Character.isWhitespace(i))
                sb.append((char)i);

            if (sb.length() == 3 && sb.toString().equals("G04"))
                inCommentSection = true;
        }
        if (sb.length() == 0)
            return null;
        return sb.toString();
    }

    private void parseParameter(String parameter) throws GerberParsingException
    {
        if (apertureMacro != null)
            parseApertureMacroDefinition(parameter);
        else if (parameter.startsWith("AD"))
            parseApertureDefinition(parameter.substring(2));
        else if (parameter.startsWith("OF") || parameter.startsWith("IP"))
            LoggerFactory.getApplicationLogger().log(Level.FINE, "Ignoring obsolete gerber parameter");
        else if (parameter.startsWith("FS"))
            parseCoordinateFormatSpecification(parameter);
        else if (parameter.startsWith("MO"))
            parseMeasurementUnits(parameter.substring(2, parameter.length()));
        else if (parameter.startsWith("AM"))
            parseApertureMacro(parameter);
        else if (parameter.startsWith("LP"))
            parseLevelPolarity(parameter);
        else
            throw new GerberParsingException("Unknown parameter: " + parameter);
    }

    private void parseMeasurementUnits(String str)
    {
        if (str.equals("IN"))
            unitConversionRatio = INCHES_RATIO;
        else if (str.equals("MM"))
            unitConversionRatio = MM_RATIO;
    }

    private void parseCoordinateFormatSpecification(String str)
    {
        omitLeadingZeros = str.charAt(2) == 'L';
        integerPlaces = str.charAt(str.indexOf('X') + 1) - '0';
        decimalPlaces = str.charAt(str.indexOf('X') + 2) - '0';
    }

    private void parseLevelPolarity(String str) throws GerberParsingException
    {
        if (str.charAt(2) == 'C')
            polarity = GerberPrimitive.Polarity.CLEAR;
        else if (str.charAt(2) == 'D')
            polarity = GerberPrimitive.Polarity.DARK;
        else
            throw new GerberParsingException("Unknown polarity specified: " + str);
    }

    private void parseApertureMacro(String str)
    {
        String macroName = str.substring(2);
        apertureMacro = new ApertureMacro();
        apertureMacros.put(macroName, apertureMacro);
    }

    private final static Pattern PATTERN_MACRO_CIRCLE =
            Pattern.compile("1,(1|0),(?<diameter>\\d*.\\d*),(?<x>-?\\d*.\\d*),(?<y>-?\\d*.?\\d*)");
    private final static Pattern PATTERN_MACRO_OUTLINE =
            Pattern.compile("4,(1|0),(?<count>\\d*),(?<vertices>.*,)(?<angle>-?\\d*.?\\d*)");
    private final static Pattern PATTERN_MACRO_OUTLINE_COORDINATE_PAIR =
            Pattern.compile("(?<x>-?\\d*.?\\d*),(?<y>-?\\d*.?\\d*),");
    private final static Pattern PATTERN_MACRO_VECTOR_LINE =
            Pattern.compile("20,(1|0),(?<width>\\d*.\\d*),(?<fromX>-?\\d*.\\d*),(?<fromY>-?\\d*.?\\d*),(?<toX>-?\\d*.?\\d*),(?<toY>-?\\d*.?\\d*),(?<angle>-?\\d*.?\\d*)");
    private final static Pattern PATTERN_MACRO_CENTER_LINE =
            Pattern.compile("21,(1|0),(\\d*.\\d*),(\\d*.\\d*),(-?\\d*.?\\d*),(-?\\d*.?\\d*),(-?\\d*.?\\d*)");
    private final static Pattern PATTERN_MACRO_POLYGON =
            Pattern.compile("5,(1|0),(?<vertices>\\d+),(?<x>-?\\d*.?\\d*),(?<y>-?\\d*.?\\d*),(?<diameter>\\d*.?\\d*),(?<angle>-?\\d*.?\\d*)");

    private void parseApertureMacroDefinition(String str)
    {
        Matcher matcher = PATTERN_MACRO_CENTER_LINE.matcher(str);
        if (matcher.find())
        {
            MacroCenterLine centerLine = new MacroCenterLine((int) (Double.valueOf(matcher.group(2)) * unitConversionRatio),
                    (int) (Double.valueOf(matcher.group(3)) * unitConversionRatio),
                    new Point((int) (Double.valueOf(matcher.group(4)) * unitConversionRatio), (int) (Double.valueOf(matcher.group(5)) * unitConversionRatio)),
                    new Double(matcher.group(6)).intValue());
            apertureMacro.addPrimitive(centerLine);
            return;
        }

        matcher = PATTERN_MACRO_CIRCLE.matcher(str);
        if (matcher.find())
        {
            MacroCircle circle = new MacroCircle((int) (Double.valueOf(matcher.group("diameter")) * unitConversionRatio),
                    new Point((int) (Double.valueOf(matcher.group("x")) * unitConversionRatio),
                            (int) (Double.valueOf(matcher.group("y")) * unitConversionRatio)));
            apertureMacro.addPrimitive(circle);
            return;
        }

        matcher = PATTERN_MACRO_VECTOR_LINE.matcher(str);
        if (matcher.find())
        {
            MacroVectorLine vectorLine = new MacroVectorLine((int) (Double.valueOf(matcher.group("width")) * unitConversionRatio),
                    new Point((int) (Double.valueOf(matcher.group("fromX")) * unitConversionRatio),
                            (int)(Double.valueOf(matcher.group("fromY")) * unitConversionRatio)),
                    new Point((int) (Double.valueOf(matcher.group("toX")) * unitConversionRatio),
                            (int)(Double.valueOf(matcher.group("toY")) * unitConversionRatio)),
                    (int)(Double.valueOf(matcher.group("angle")).doubleValue()));
            apertureMacro.addPrimitive(vectorLine);
            return;
        }

        matcher = PATTERN_MACRO_POLYGON.matcher(str);
        if (matcher.find())
        {
            MacroPolygon polygon = new MacroPolygon(Integer.valueOf(matcher.group("vertices")),
                    new Point((int)(Double.valueOf(matcher.group("x")) * unitConversionRatio),
                            (int)(Double.valueOf(matcher.group("y")) * unitConversionRatio)),
                    (int)(Double.valueOf(matcher.group("diameter")) * unitConversionRatio),
                    (int)(Double.valueOf(matcher.group("angle")).doubleValue()));
            apertureMacro.addPrimitive(polygon);
            return;
        }

        matcher = PATTERN_MACRO_OUTLINE.matcher(str);
        if (matcher.find())
        {
            int verticesCount = Integer.valueOf(matcher.group("count"));
            MacroOutline outline = new MacroOutline();
            outline.setRotationAngle(new Double(matcher.group("angle")).intValue());
            matcher = PATTERN_MACRO_OUTLINE_COORDINATE_PAIR.matcher(matcher.group("vertices"));
            while (matcher.find())
                outline.addPoint(new Point((int) (Double.valueOf(matcher.group("x")) * unitConversionRatio),
                        (int) (Double.valueOf(matcher.group("y")) * unitConversionRatio)));
            if (verticesCount != outline.getPoints().size() - 1)
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Aperture macro vertices count does not match supplied coordinates: " + str);
            if (!outline.getPoints().get(0).equals(outline.getPoints().get(outline.getPoints().size() - 1)))
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Aperture macro does not define enclosed area: " + str);
            outline.getPoints().remove(outline.getPoints().size() - 1);
            apertureMacro.addPrimitive(outline);
        }
    }

    private void parseApertureDefinition(String str) throws GerberParsingException
    {
        if (!str.startsWith("D"))
            throw new GerberParsingException("Invalid aperture definition: " + str);

        str = str.substring(1);
        Pattern pattern = Pattern.compile("(\\d+)(.*)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
        {
            ApertureMacro macro = apertureMacros.get(matcher.group(2));
            if (macro != null)
            {
                apertures.put(Integer.valueOf(matcher.group(1)), macro);
                return;
            }
        }

        pattern = Pattern.compile("(\\d+)([CORP8]+)");
        matcher = pattern.matcher(str);
        if (!matcher.find())
            throw new GerberParsingException("Aperture definition incorrectly formatted: " + str);

        int apertureNumber = Integer.parseInt(matcher.group(1));
        String aperture = matcher.group(2);

        if (aperture.equals("C"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d*)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of circular aperture");
            int diameter = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            apertures.put(apertureNumber, new CircularAperture(diameter));
        }
        else if (aperture.equals("R"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d*)X(\\d*.\\d*)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of rectangular aperture");
            int width = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            int height = (int)(Double.valueOf(matcher.group(2)) * unitConversionRatio);
            apertures.put(apertureNumber, new RectangularAperture(width, height));
        }
        else if (aperture.equals("OC8"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d*)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of octagonal aperture");
            int diameter = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            apertures.put(apertureNumber, new OctagonalAperture(diameter));
        }
        else if (aperture.equals("O"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d*)X(\\d*.\\d*)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of oval aperture");
            int width = (int) (Double.valueOf(matcher.group(1)) * unitConversionRatio);
            int height = (int) (Double.valueOf(matcher.group(2)) * unitConversionRatio);
            apertures.put(apertureNumber, new OvalAperture(width, height));
        }
        else if (aperture.equals("P"))
        {
            System.out.println("Polygon aperture");
        }
        else
            throw new GerberParsingException("Unknown aperture");
    }

    private DataBlock parseDataBlock(String str)
    {
        DataBlock dataBlock = new DataBlock();
        Pattern pattern = Pattern.compile("([GMDXYIJ])([+-]?\\d+)");
        Matcher matcher = pattern.matcher(str);
        int i = 0;
        while (matcher.find(i))
        {
            switch (matcher.group(1).charAt(0))
            {
                case 'G': dataBlock.setG(Integer.parseInt(matcher.group(2))); break;
                case 'M': dataBlock.setM(Integer.parseInt(matcher.group(2))); break;
                case 'D': dataBlock.setD(Integer.parseInt(matcher.group(2))); break;
                case 'X': dataBlock.setX(convertCoordinates(matcher.group(2))); break;
                case 'Y': dataBlock.setY(convertCoordinates(matcher.group(2))); break;
                case 'I': dataBlock.setI(convertCoordinates(matcher.group(2))); break;
                case 'J': dataBlock.setJ(convertCoordinates(matcher.group(2))); break;
            }
            i = matcher.end();
        }
        return dataBlock;
    }

    private int convertCoordinates(String str)
    {
        boolean negative = str.startsWith("-");
        if (str.startsWith("-") || str.startsWith("+"))
            str = str.substring(1);
        while (str.length() < integerPlaces + decimalPlaces)
            str = omitLeadingZeros ? '0' + str : str + '0';
        str = str.substring(0, str.length() - decimalPlaces) + "." + str.substring(str.length() - decimalPlaces, str.length());

        return (int)(Double.valueOf(str) * unitConversionRatio) * (negative ? -1 : 1);
    }

    private void processDataBlock(DataBlock dataBlock) throws GerberParsingException
    {
        if (dataBlock.getG() != null)
        {
            switch (dataBlock.getG())
            {
                case  1:
                    currentInterpolationMode = InterpolationMode.LINEAR;
                break;
                case 2:
                    currentInterpolationMode = InterpolationMode.CLOCKWISE_CIRCULAR;
                break;
                case 3:
                    currentInterpolationMode = InterpolationMode.COUNTERCLOCKWISE_CIRCULAR;
                break;
                case  4: return;
                case 36:
                    region = new Region(polarity);
                break;
                case 37:
                    if (!region.getSegments().isEmpty())
                        elements.add(region);
                    region = null;
                break;
                case 54: break;
                case 70:
                    unitConversionRatio = INCHES_RATIO;
                break;
                case 71:
                    unitConversionRatio = MM_RATIO;
                break;
                case 74:
                    arcQuadrantMode = ArcQuadrantMode.SINGLE_QUADRANT;
                break;
                case 75:
                    arcQuadrantMode = ArcQuadrantMode.MULTI_QUADRANT;
                break;
                default:
                    throw new GerberParsingException("Unknown gcode: " + dataBlock.getG());
            }
        }
        if (dataBlock.getM() != null)
        {
            switch (dataBlock.getM())
            {
                case 2: return;
                default:
                    throw new GerberParsingException("Unknown mcode: " + dataBlock.getM());
            }
        }
        if (dataBlock.getD() != null)
        {
            switch (dataBlock.getD())
            {
                case 1: exposureMode = ExposureMode.ON; break;
                case 2: exposureMode = ExposureMode.OFF; break;
                case 3: exposureMode = ExposureMode.FLASH; break;
                default:
                    aperture = apertures.get(dataBlock.getD());
                    if (aperture == null)
                        throw new GerberParsingException("Undefined aperture used: " + dataBlock.getD());
                    return;
            }
        }

        if (dataBlock.getX() == null && dataBlock.getY() == null && dataBlock.getD() == null)
            return;
        Integer newX = x;
        if (dataBlock.getX() != null)
            newX = dataBlock.getX();
        Integer newY = y;
        if (dataBlock.getY() != null)
            newY = dataBlock.getY();

        GerberPrimitive primitive = null;
        if(exposureMode == ExposureMode.FLASH)
            primitive = new Flash(newX, newY, aperture, polarity);
        else if (exposureMode == ExposureMode.ON)
        {
            if (currentInterpolationMode == InterpolationMode.LINEAR)
                primitive = new LinearShape(x, y, newX, newY, aperture, polarity);
            else
            {
                Integer i = dataBlock.getI() == null ? 0 : dataBlock.getI();
                Integer j = dataBlock.getJ() == null ? 0 : dataBlock.getJ();
                Point center = new Point(x + i, y + j);
                if (arcQuadrantMode == ArcQuadrantMode.SINGLE_QUADRANT)
                {
                    Point from = new Point(x, y);
                    Point to = new Point(newX, newY);
                    Point[] centers = new Point[] {new Point(x + i, y + j), new Point(x + i, y - j),
                            new Point(x - i, y + j), new Point(x - i, y - j)};
                    for (Point p : centers)
                    {
                        if (Math.abs(1 - p.distanceTo(from) / p.distanceTo(to)) > 0.1)
                            continue; // Radii are too different - that's not a valid arc
                        double angle = new Arc(from, to, p, (int) from.distanceTo(center),
                                currentInterpolationMode == InterpolationMode.CLOCKWISE_CIRCULAR).getAngle();
                        if (angle <= Math.PI)
                        {
                            center = p;
                            break;
                        }

                    }
                }
                primitive = new CircularShape(x, y, newX, newY,  center.getX(), center.getY(),
                        currentInterpolationMode == InterpolationMode.CLOCKWISE_CIRCULAR, aperture, polarity);
            }
        }

        if (region != null)
        {
            if (exposureMode == ExposureMode.ON && (!newX.equals(x) || !newY.equals(y)))
                region.addSegment(primitive);
        }
        else if (aperture != null)
        {
            if (exposureMode == ExposureMode.FLASH || exposureMode == ExposureMode.ON)
                elements.add(primitive);
        }

        x = newX;
        y = newY;
    }

}
