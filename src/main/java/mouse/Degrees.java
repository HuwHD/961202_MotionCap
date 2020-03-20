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

/**
 *
 * @author Stuart
 */
public class Degrees {

    public static final int DEG = 360;
    public static final double PI = Math.PI;
    public static final double TO_RADIANS = PI / 180.0;
    public static final double TO_DEGREES = 180.0 / PI;

    private final long degrees;

    public Degrees(int degrees) {
        this.degrees = degrees;
    }
    
    public Degrees(double radians) {
        this.degrees = Math.round(radians * TO_DEGREES) % DEG;
    }

    public double getRadians() {
        return degrees * TO_RADIANS;
    }
    
    public long getDegrees() {
        return degrees;
    }

    public Degrees add(int d) {
        return new Degrees(antiClockwise(this.degrees, d));
    }

    public Degrees sub(int d) {
        return new Degrees(clockwise(this.degrees, d));
    }

    public long diffAntiClockwise(Degrees s) {
        long dd = this.getDegrees();
        long ds = s.getDegrees();
        return antiClockwise(ds, DEG-dd);
    }
    
    public long diffClockwise(Degrees s) {
        return (360-diffAntiClockwise(s));
    }
    
    private long antiClockwise(long d1, long d2) {
        return (d1 + d2) % DEG;
    }

    private long clockwise(long d1, long d2) {
        long x = DEG - (d2 % DEG);
        return (d1 + x) % DEG;
    }

}
