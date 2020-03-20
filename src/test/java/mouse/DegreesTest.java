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
package mouse;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Stuart
 */
public class DegreesTest {
    @Test
    /*
    Use https://duckduckgo.com/?q=radians+to+degrees&t=ha&ia=answer 
    to calculate the answers
    */
    public void testRadians() {
        Degrees d = new Degrees(0.0);
        assertEquals(0, d.getDegrees()); // 0
        d = new Degrees(1.0);
        assertEquals(57, d.getDegrees()); // 57.29578
        assertEquals(0.99, d.getRadians(), 0.01); // Close enough
        d = new Degrees(2.0);
        assertEquals(115, d.getDegrees()); // 114.5916
        d = new Degrees(3.0);
        assertEquals(172, d.getDegrees()); // 171.8873
        d = new Degrees(4.0);
        assertEquals(229, d.getDegrees()); // 229.1831
        assertEquals(3.99, d.getRadians(), 0.01); // Close enough
        d = new Degrees(5.0);
        assertEquals(286, d.getDegrees()); // 286.4789
        d = new Degrees(6.0);
        assertEquals(344, d.getDegrees()); // 343.7747
        d = new Degrees(7.0);
        assertEquals(41, d.getDegrees()); // 401.0705 - 360
        d = new Degrees(8.0);
        assertEquals(98, d.getDegrees()); // 458.3662 - 360
        d = new Degrees(100.0);
        assertEquals(330, d.getDegrees()); // 5729.578 - (360 * 15)
        assertEquals(5.76, d.getRadians(), 0.01); // Close enough
    }
    
    @Test 
    public void testDiffClockwise() {
        Degrees dd = new Degrees(341);
        assertEquals(350, dd.diffClockwise(new Degrees(351))); 
        assertEquals(360-19, dd.diffClockwise(new Degrees(0))); 
        assertEquals(360-20, dd.diffClockwise(new Degrees(1))); 
        assertEquals(360-30, dd.diffClockwise(new Degrees(11))); 
        assertEquals(360-110, dd.diffClockwise(new Degrees(91))); 
        assertEquals(360-0, dd.diffClockwise(new Degrees(341))); 
        assertEquals(360-350, dd.diffClockwise(new Degrees(331))); 
    }
    
    @Test 
    public void testDiffAntiClockwise() {
        Degrees dd = new Degrees(341);
        assertEquals(10, dd.diffAntiClockwise(new Degrees(351))); 
        assertEquals(19, dd.diffAntiClockwise(new Degrees(0))); 
        assertEquals(20, dd.diffAntiClockwise(new Degrees(1))); 
        assertEquals(30, dd.diffAntiClockwise(new Degrees(11))); 
        assertEquals(110, dd.diffAntiClockwise(new Degrees(91))); 
        assertEquals(0, dd.diffAntiClockwise(new Degrees(341))); 
        assertEquals(350, dd.diffAntiClockwise(new Degrees(331))); 
    }
    
    @Test
    public void testBasic() {
        Degrees d = new Degrees(90);
        assertEquals(90, d.getDegrees());
    }

    @Test
    public void testSub() {
        Degrees d = new Degrees(90);
        assertEquals(90, d.getDegrees());
        d = d.sub(90);
        assertEquals(0, d.getDegrees());
        d = d.sub(90);
        assertEquals(270, d.getDegrees());
        d = d.sub(270);
        assertEquals(0, d.getDegrees());
        d = d.sub(360);
        assertEquals(0, d.getDegrees());
        d = d.sub(360 * 7);
        assertEquals(0, d.getDegrees());

        d = d.sub(1);
        assertEquals(359, d.getDegrees());
        d = d.sub(360 * 7);
        assertEquals(359, d.getDegrees());
        d = d.sub(180 * 4);
        assertEquals(359, d.getDegrees());
        d = d.sub(90 * 9);
        assertEquals(359-90, d.getDegrees());
    }
    @Test
    public void testAdd() {
        Degrees d = new Degrees(90);
        assertEquals(90, d.getDegrees());
        d = d.add(90);
        assertEquals(180, d.getDegrees());
        d = d.add(90);
        assertEquals(270, d.getDegrees());
        d = d.add(90);
        assertEquals(0, d.getDegrees());
        d = d.add(90);
        assertEquals(90, d.getDegrees());
        d = d.add(360 * 7);
        assertEquals(90, d.getDegrees());
        d = d.add(270);
        assertEquals(0, d.getDegrees());
        d = d.add(1);
        assertEquals(1, d.getDegrees());
        d = d.add(360 * 99);
        assertEquals(1, d.getDegrees());
    }

}
