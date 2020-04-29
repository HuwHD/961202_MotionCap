/*
 * Copyright (C) 2020 Huw Hudson-Davies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package readings;

import static org.junit.Assert.*;
import org.junit.Test;
import serial.Reading;
import serial.ReadingException;

public class ReadingTest {

    /**
     * Test that only the sensor buttons (b1S and b2S) are swapped
     */
    @Test
    public void testRangeSwapLR() {
        Reading r;
        r = Reading.parse("10,143,1,1,0,1,0:", true, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=1, b1S=false, b2S=true, b1R=true, b2R=false}", r.toString());
 
        r = Reading.parse("10,143,1,1,0,0,1:", true, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=1, b1S=false, b2S=true, b1R=false, b2R=true}", r.toString());
    }

    /**
     * Test that up down movement are swapped y --> sign changed
     */
    @Test
    public void testRangeSwapUD() {
        Reading r;
        r = Reading.parse("10,143,1,1,0,1,0:", false, true);
        assertEquals("Reading{x=000010.0, y=-000143.0, h=1, b1S=true, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10,-143,1,1,0,1,0:", false, true);
        assertEquals("Reading{x=000010.0, y=000143.0, h=1, b1S=true, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10,143,1,1,0,1,0:", true, true);
        assertEquals("Reading{x=000010.0, y=-000143.0, h=1, b1S=false, b2S=true, b1R=true, b2R=false}", r.toString());
    }

    /**
     * Test that double values are read correctly
     */
    @Test
    public void testRangeDouble() {
        Reading r;
        r = Reading.parse("10.456,143,0.5,0,0,0,0:", false, false);
        assertEquals("Reading{x=000010.5, y=000143.0, h=1, b1S=false, b2S=false, b1R=false, b2R=false}", r.toString());

        r = Reading.parse("10.456,143,-0.5,0,0,1,0:", false, false);
        assertEquals("Reading{x=000010.5, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10.456,143.99,-2,0,0,1,0:", false, false);
        assertEquals("Reading{x=000010.5, y=000144.0, h=-2, b1S=false, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10.456,143,0,0,0,1,0,", false, false);
        assertEquals("Reading{x=000010.5, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10.456,143,0,0,0,0,1,", false, false);
        assertEquals("Reading{x=000010.5, y=000143.0, h=0, b1S=false, b2S=false, b1R=false, b2R=true}", r.toString());

        r = Reading.parse("10.456,143,0,0,0,1,1:", false, false);
        assertEquals("Reading{x=000010.5, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=true}", r.toString());

        testParseFail("10.456,143,-2,-16,20.5,1:", false, false);

        testParseFail("10.456,143,-2a,-16,1,0:", false, false);

        testParseFail(",143,-2,-16,1,0:", false, false);

        testParseFail(",,143,-2,-16,1,0:", false, false);
    }

    @Test
    public void testRangeInt() {
        Reading r;
        r = Reading.parse("10,143,1,1,0,0,0:", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=1, b1S=true, b2S=false, b1R=false, b2R=false}", r.toString());

        r = Reading.parse("10,143,0,1,0,1,0:", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=0, b1S=true, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10,143,-2,0,0,1,0:", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=-2, b1S=false, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10,143,0,0,0,1,0,", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=false}", r.toString());

        r = Reading.parse("10,143,0,0,0,1,1,", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=true}", r.toString());

        r = Reading.parse("10,143,0,0,0,1,1:", false, false);
        assertEquals("Reading{x=000010.0, y=000143.0, h=0, b1S=false, b2S=false, b1R=true, b2R=true}", r.toString());

        testParseFail("10,143,-2,-16,0,1:", false, false);

        testParseFail("10,143,-2a,-16,0,1,0:", false, false);

        testParseFail(",143,-2,-16,1,0:", false, false);

        testParseFail(",,143,-2,-16,1,0:", false, false);

    }

    private void testParseFail(String s, boolean lr, boolean ud) {
        try {
            Reading r = Reading.parse(s, lr, ud);
        } catch (ReadingException re) {
            return;
        }
        fail("Should throw a ReadingException exception");
    }
}
