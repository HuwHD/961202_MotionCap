package robot;


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
import org.junit.Test;
import static org.junit.Assert.*;

public class RoughlyNotEqualTest {
    
    @Test
    public void test() {
        roughlyEqual(1.00, 1.09, 0.1);
        roughlyEqual(1.00, 1.1, 0.1);
        roughlyNotEqual(1.00, 1.11, 0.1);
        roughlyNotEqual(1.00, 1.12, 0.1);
        
        roughlyEqual(-1.00, -1.09, 0.1);
        roughlyEqual(-1.00, -1.1, 0.1);
        roughlyNotEqual(-1.00, -1.11, 0.1);
        roughlyNotEqual(-1.00, -1.12, 0.1);
        
        roughlyEqual(-0.05, 0.05, 0.1);
        roughlyNotEqual(-0.05, 0.051, 0.1);
    }
    
    private void roughlyNotEqual(double a, double b, double tollerance) {
        assertTrue(RobotMouseThread.roughlyNotEqual(a, b, tollerance));
        assertTrue(RobotMouseThread.roughlyNotEqual(b, a, tollerance));
    }
    private void roughlyEqual(double a, double b, double tollerance) {
        assertFalse(RobotMouseThread.roughlyNotEqual(a, b, tollerance));
        assertFalse(RobotMouseThread.roughlyNotEqual(b, a, tollerance));
    }
 
}
