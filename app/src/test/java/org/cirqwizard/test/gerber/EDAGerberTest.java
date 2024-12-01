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


package org.cirqwizard.test.gerber;

import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.gerber.appertures.RectangularAperture;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EDAGerberTest
{

    @Test
    public void testEagleFile() throws IOException
    {
        String fileContent = "G75*\n" +
                "G70*\n" +
                "%OFA0B0*%\n" +
                "%FSLAX24Y24*%\n" +
                "%IPPOS*%\n" +
                "%LPD*%\n" +
                "%AMOC8*\n" +
                "5,1,8,0,0,1.08239X$1,22.5*\n" +
                "%\n" +
                "%ADD10C,0.0000*%\n" +
                "%ADD11R,0.0591X0.0197*%\n" +
                "%ADD16C,0.0740*%\n" +
                "%ADD22C,0.0236*%\n" +
                "D10*\n" +
                "X000100Y000100D02*\n" +
                "X000100Y012305D01*\n" +
                "X012108Y012305D01*\n" +
                "D11*\n" +
                "X006181Y005549D03*\n" +
                "X006181Y006179D03*\n" +
                "D16*\n" +
                "X003624Y010901D03*\n" +
                "X002624Y010901D03*\n" +
                "D22*\n" +
                "X004594Y008561D02*\n" +
                "X003214Y008561D01*\n" +
                "X002874Y008901D01*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(8, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(0, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(254, 254), l.getFrom());
        assertEquals(new Point(254, 31254), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(0, p.getAperture().getWidth());
        assertEquals(new Point(254, 31254), l.getFrom());
        assertEquals(new Point(30754, 31254), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1501, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(500, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(15699, 14094), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1501, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(500, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(15699, 15694), f.getPoint());

        p = elements.get(4);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1879, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(9204, 27688), f.getPoint());

        p = elements.get(5);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1879, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(6664, 27688), f.getPoint());

        p = elements.get(6);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(599, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(11668, 21744), l.getFrom());
        assertEquals(new Point(8163, 21744), l.getTo());

        p = elements.get(7);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(599, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(8163, 21744), l.getFrom());
        assertEquals(new Point(7299, 22608), l.getTo());
    }

    @Test
    public void testOrCADFile() throws IOException
    {
        String fileContent = "*\n" +
                "G04 Mass Parameters ***\n" +
                "*\n" +
                "G04 Image ***\n" +
                "*\n" +
                "%IND:\\FILENAME*%\n" +
                "%ICAS*%\n" +
                "%MOIN*%\n" +
                "%IPPOS*%\n" +
                "%ASAXBY*%\n" +
                "G74*%FSLAN2X34Y34*%\n" +
                "*\n" +
                "G04 Aperture Definitions ***\n" +
                "*\n" +
                "%ADD10R,0.0500X0.0600*%\n" +
                "%ADD16C,0.0600*%\n" +
                "%ADD25C,0.0100*%\n" +
                "*\n" +
                "G04 Plot Data ***\n" +
                "*\n" +
                "G54D25*\n" +
                 "G01X0005590Y0015160D02*\n" +
                "Y0014340D01*\n" +
                "X0006410D02*\n" +
                "X0005590D01*\n" +
                "G54D10*\n" +
                "X0019600Y0023250D03*\n" +
                "X0018400D03*\n" +
                "G54D16*\n" +
                "X0034750Y0023250D03*\n" +
                "Y0018250D03*\n" +
                "%LPD*%\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(6, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(254, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(14198, 38506), l.getFrom());
        assertEquals(new Point(14198, 36423), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(254, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(16281, 36423), l.getFrom());
        assertEquals(new Point(14198, 36423), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1270, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(49784, 59055), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1270, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(46736, 59055), f.getPoint());

        p = elements.get(4);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1524, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(88265, 59055), f.getPoint());

        p = elements.get(5);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1524, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(88265, 46355), f.getPoint());

    }

    @Test
    public void testKiCAD() throws IOException
    {
        String fileContent = "G04 (created by PCBNEW (2013-07-07 BZR 4022)-stable) date 23/01/2014 11:32:09*\n" +
                "%MOIN*%\n" +
                "G04 Gerber Fmt 3.4, Leading zero omitted, Abs format*\n" +
                "%FSLAX34Y34*%\n" +
                "G01*\n" +
                "G70*\n" +
                "G90*\n" +
                "G04 APERTURE LIST*\n" +
                "%ADD12C,0.055*%\n" +
                "%ADD13R,0.144X0.08*%\n" +
                "%ADD39C,0.012*%\n" +
                "G04 APERTURE END LIST*\n" +
                "G54D12*\n" +
                "X29724Y-52649D03*\n" +
                "G54D13*\n" +
                "X34842Y-57796D03*\n" +
                "G54D39*\n" +
                "X30905Y-49428D02*\n" +
                "X30905Y-49094D01*\n" +
                "X31023Y-47755D02*\n" +
                "X30433Y-47755D01*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1397, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(75498, -133728), f.getPoint());

        p = elements.get(1);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(3657, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(2032, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(88498, -146801), f.getPoint());

        p = elements.get(2);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(304, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(78498, -125547), l.getFrom());
        assertEquals(new Point(78498, -124698), l.getTo());

        p = elements.get(3);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(304, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(78798, -121297), l.getFrom());
        assertEquals(new Point(77299, -121297), l.getTo());

    }

    @Test
    public void testSprintLayoutFile() throws IOException
    {
        String fileContent = "%FSLAX32Y32*%\n" +
                "%MOMM*%\n" +
                "%LNKUPFERSEITE2*%\n" +
                "G71*\n" +
                "G01*\n" +
                "%ADD10C, 0.25*%\n" +
                "%ADD11C, 1.80*%\n" +
                "%ADD12C, 2.00*%\n" +
                "%LPD*%\n" +
                "G36*\n" +
                "X654Y852D02*\n" +
                "X654Y822D01*\n" +
                "X534Y822D01*\n" +
                "X534Y852D01*\n" +
                "X654Y852D01*\n" +
                "G37*\n" +
                "G54D10*\n" +
                "X1474Y1163D02*\n" +
                "X1474Y1263D01*\n" +
                "G54D11*\n" +
                "D03*\n" +
                "X1103Y438D02*\n" +
                "G54D12*\n" +
                "D03*\n" +
                "X2126Y1233D02*\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Region.class, p.getClass());
        Region r = (Region) p;
        List<GerberPrimitive> s = r.getSegments();
        assertEquals(4, r.getSegments().size());
        assertEquals(5340, r.getMin().getX());
        assertEquals(8220, r.getMin().getY());
        LinearShape l = (LinearShape) s.get(0);
        assertEquals(new Point(6540, 8520), l.getFrom());
        assertEquals(new Point(6540, 8220), l.getTo());
        l = (LinearShape) s.get(1);
        assertEquals(new Point(6540, 8220), l.getFrom());
        assertEquals(new Point(5340, 8220), l.getTo());
        l = (LinearShape) s.get(2);
        assertEquals(new Point(5340, 8220), l.getFrom());
        assertEquals(new Point(5340, 8520), l.getTo());
        l = (LinearShape) s.get(3);
        assertEquals(new Point(5340, 8520), l.getFrom());
        assertEquals(new Point(6540, 8520), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(250, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(14740, 11630), l.getFrom());
        assertEquals(new Point(14740, 12630), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(1800, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(14740, 12630), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(2000, ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(11030, 4380), f.getPoint());
    }

    @Test
    public void testDesignSparkFile() throws IOException
    {
        String fileContent = "%FSLAX23Y23*%\n" +
                "%MOMM*%\n" +
                "G04 EasyPC Gerber Version 16.0.6 Build 3249 *\n" +
                "%ADD23R,1.52400X1.52400*%\n" +
                "%ADD13R,1.87960X1.87960*%\n" +
                "%ADD14C,1.87960*%\n" +
                "X0Y0D02*\n" +
                "D02*\n" +
                "D13*\n" +
                "X17844Y25718D03*\n" +
                "D02*\n" +
                "D14*\n" +
                "X20384D02*\n" +
                "X22924D01*\n" +
                "D02*\n" +
                "D23*\n" +
                "X17844Y7049D03*\n" +
                "Y25464D03*\n" +
                "X0Y0D02*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1879, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(1879, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(17844, 25718), f.getPoint());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(1879, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(20384, 25718), l.getFrom());
        assertEquals(new Point(22924, 25718), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(17844, 7049), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(1524, ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(17844, 25464), f.getPoint());
    }

    @Test
    public void testProteusFile() throws IOException
    {
        String fileContent = "G04 PROTEUS RS274X GERBER FILE*\n" +
                "%FSLAX24Y24*%\n" +
                "%MOIN*%\n" +
                "%ADD11C,0.0080*%\n" +
                "G54D11*\n" +
                "X+3077Y-16191D02*\n" +
                "X+8457Y-16191D01*\n" +
                "X-44265Y+11501D01*\n" +
                "M00*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(2, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(203, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(7815, -41125), l.getFrom());
        assertEquals(new Point(21480, -41125), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(203, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(21480, -41125), l.getFrom());
        assertEquals(new Point(-112433, 29212), l.getTo());

    }

    @Test
    public void testEasyPCFile() throws IOException
    {
        String fileContent = "%FSLAX23Y23*%\n" +
                "%MOIN*%\n" +
                "G04 EasyPC Gerber Version 17.0 Build 3379 *\n" +
                "%ADD102R,0.06000X0.06000*%\n" +
                "%ADD27C,0.00800*%\n" +
                "%ADD17R,0.07800X0.02200*%\n" +
                "%ADD114R,0.12598X0.03937*%\n" +
                "%ADD115R,0.06890X0.05906*%\n" +
                "%ADD103R,0.07090X0.06300*%\n" +
                "%ADD113C,0.01654*%\n" +
                "%ADD112C,0.05906*%\n" +
                "%ADD116C,0.06299*%\n" +
                "D113*\n" +
                "X203Y186D02*\n" +
                "G75*\n" +
                "G02X224Y224I81J-22D01*\n" +
                "G01*\n" +
                "X181*\n" +
                "G75*\n" +
                "G02X203Y186I-59J-59*\n" +
                "G01*\n" +
                "G36*\n" +
                "G75*\n" +
                "G02X224Y224I81J-22*\n" +
                "G01*\n" +
                "X181*\n" +
                "G75*\n" +
                "G02X203Y186I-59J-59*\n" +
                "G01*\n" +
                "G37*\n" +
                "Y654D02*\n" +
                "X254Y602D01*\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(5, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(CircularShape.class, p.getClass());
        CircularShape c = (CircularShape) p;
        Arc arc = c.getArc();
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(420, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(5156, 4724), arc.getFrom());
        assertEquals(new Point(5689, 5689), arc.getTo());
        assertEquals(new Point(7213, 4166), arc.getCenter());
        assertEquals(true, arc.isClockwise());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(420, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(5689, 5689), l.getFrom());
        assertEquals(new Point(4597, 5689), l.getTo());

        p = elements.get(2);
        assertEquals(CircularShape.class, p.getClass());
        c = (CircularShape) p;
        arc = c.getArc();
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(420, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(4597, 5689), arc.getFrom());
        assertEquals(new Point(5156, 4724), arc.getTo());
        assertEquals(new Point(3099, 4191), arc.getCenter());
        assertEquals(true, arc.isClockwise());

        p = elements.get(3);
        assertEquals(Region.class, p.getClass());

        Region region = (Region) p;
        assertEquals(3, region.getSegments().size());

        p = elements.get(4);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(420, ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(5156, 16611), l.getFrom());
        assertEquals(new Point(6451, 15290), l.getTo());
    }

    @Test
    public void testAllegroFile() throws IOException
    {
        String fileContent = "%FSLAX55Y55*MOIN*%\n" +
                "%IR0*IPPOS*OFA0.00000B0.00000*MIA0B0*SFA1.00000B1.00000*%\n" +
                "%ADD14C,.024*%\n" +
                "%ADD11C,.06*%\n" +
                "%ADD10C,.025*%\n" +
                "%ADD13C,.035*%\n" +
                "%ADD12C,.065*%\n" +
                "%ADD15C,.015*%\n" +
                "%ADD16C,.070004*%\n" +
                "%ADD17C,.075004*%\n" +
                "G75*\n" +
                "%LPD*%\n" +
                "G75*\n" +
                "G36*\n" +
                "G01X90625Y7880D02*\n" +
                "Y94565D01*\n" +
                "X86684Y98505D01*\n" +
                "Y141847D01*\n" +
                "X82744D01*\n" +
                "Y149728D01*\n" +
                "X90625D01*\n" +
                "X94565Y153668D01*\n" +
                "Y173369D01*\n" +
                "X94094Y173840D01*\n" +
                "X94084Y173905D01*\n" +
                "G03X91160Y176828I-3459J-536D01*\n" +
                "G01X91096Y176838D01*\n" +
                "X90625Y177309D01*\n" +
                "X82744D01*\n" +
                "Y189130D01*\n" +
                "X94565D01*\n" +
                "Y193070D01*\n" +
                "X86684D01*\n" +
                "Y216711D01*\n" +
                "X102445D01*\n" +
                "Y203200D01*\n" +
                "X93306D01*\n" +
                "G03Y198700I-2681J-2250D01*\n" +
                "G01X100436D01*\n" +
                "G02X100719Y198017I0J-400D01*\n" +
                "G01X100195Y197493D01*\n" +
                "Y139597D01*\n" +
                "X94565Y133967D01*\n" +
                "Y106385D01*\n" +
                "X98505Y102445D01*\n" +
                "Y19701D01*\n" +
                "X102445Y15761D01*\n" +
                "X122904D01*\n" +
                "X125094Y13570D01*\n" +
                "X133350D01*\n" +
                "X134774Y12147D01*\n" +
                "X144220D01*\n" +
                "X147834Y15761D01*\n" +
                "X232472D01*\n" +
                "X236412Y19701D01*\n" +
                "Y55163D01*\n" +
                "X232267Y59308D01*\n" +
                "X232261Y59382D01*\n" +
                "G03X228807Y62836I-3739J-285D01*\n" +
                "G01X228733Y62842D01*\n" +
                "X224591Y66983D01*\n" +
                "X212771D01*\n" +
                "X208831Y63043D01*\n" +
                "Y55163D01*\n" +
                "X200950Y47282D01*\n" +
                "X197010D01*\n" +
                "X193070Y51223D01*\n" +
                "Y59103D01*\n" +
                "X200950Y66983D01*\n" +
                "Y80557D01*\n" +
                "G02X201690Y80767I400J0D01*\n" +
                "G03X206828Y85942I3191J1970D01*\n" +
                "G02X207036Y86684I208J342D01*\n" +
                "G01X226367D01*\n" +
                "G02X226575Y85942I0J-400D01*\n" +
                "G03X230468I1947J-3205D01*\n" +
                "G02X230676Y86684I208J342D01*" +
                "G01X241110D01*\n" +
                "X255181Y72614D01*\n" +
                "X296447D01*\n" +
                "X310518Y86684D01*\n" +
                "X316987D01*\n" +
                "G02X317194Y85942I0J-400D01*\n" +
                "G03X321088I1947J-3205D01*\n" +
                "G02X321296Y86684I208J342D01*\n" +
                "G01X334917D01*\n" +
                "Y63043D01*\n" +
                "X248233D01*\n" +
                "Y7880D01*\n" +
                "X90625D01*\n" +
                "G37*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());
        GerberPrimitive p = elements.get(0);
        assertEquals(Region.class, p.getClass());
        Region region = (Region) p;
        assertEquals(75, region.getSegments().size());
        assertEquals(new Point(21016, 2001), region.getMin());
        assertEquals(new Point(85068, 55044), region.getMax());
    }

    @Test
    public void testCQ208() throws IOException
    {
        String fileContent = "%FSLAX25Y25*MOMM*%\n" +
                "%IR0*IPPOS*OFA0.00000B0.00000*MIA0B0*SFA1.00000B1.00000*%\n" +
                "%ADD31R,1.25X1.*%\n" +
                "G54D31*\n" +
                "X2815250Y1849500D03*\n" +
                "Y2050000D03*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(2, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(28152, f.getPoint().getX());
        assertEquals(18495, f.getPoint().getY());
        assertEquals(1250, f.getAperture().getWidth());
        assertEquals(1000, f.getAperture().getHeight());

        p = elements.get(1);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(28152, f.getPoint().getX());
        assertEquals(20500, f.getPoint().getY());
        assertEquals(1250, f.getAperture().getWidth());
        assertEquals(1000, f.getAperture().getHeight());
    }
}
