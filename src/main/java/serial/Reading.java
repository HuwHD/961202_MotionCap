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
 * References:
 *  Compass Code from MicroBit Git Repository. 
 *  Used as a refernce to implement tiltCompensatedBearing code in Java.
 *      https://github.com/lancaster-university/microbit-dal/blob/master/source/drivers/MicroBitCompass.cpp
 *  Accelerometer code from MicroBit Git Repository.
 *  Used as a refernce to implement tiltCompensatedBearing code in Java.
 *      https://github.com/lancaster-university/microbit-dal/blob/master/source/drivers/MicroBitAccelerometer.cpp
 */
package serial;

import java.text.DecimalFormat;

/**
 * Read from the sensor data String
 */
public class Reading {

    private static final double PI = Math.PI;
    private static final double TO_DEGREES = 180.0 / PI;

    private static final int SERIES_X = 0;
    private static final int SERIES_Y = 1;
    private static final int SERIES_NS = 2;
    private static final int SERIES_WE = 3;
    private static final int SERIES_UD = 4;
    private static final int SERIES_H = 5;
    private static final int SERIES_BUTTON_A = 6;
    private static final int SERIES_BUTTON_B = 7;
    private final double x;
    private final double y;
    private final double ns;
    private final double we;
    private final double ud;
    private final double heading;

    private final boolean b1;
    private final boolean b2;
    private final long timestamp;

    public static Reading parse(String data) {
        System.out.println(data);
        if (data == null) {
            return null;
        }
        String[] values = data.trim().split("\\,");
        if (values.length != 8) {
            System.err.println("Invalid sensor data [" + data + "]");
            return null;
        }
        try {
            double x = parseNum(values[SERIES_X], "X", data);
            double y = parseNum(values[SERIES_Y], "Y", data);
            double ns = parseNum(values[SERIES_NS], "NS", data);
            double we = parseNum(values[SERIES_WE], "WE", data);
            double ud = parseNum(values[SERIES_UD], "UD", data);
            double h = parseNum(values[SERIES_H], "H", data);
            boolean b1 = parseBool(values[SERIES_BUTTON_A]);
            boolean b2 = parseBool(values[SERIES_BUTTON_B]);
            return new Reading(x, y, ns, we, ud, h, b1, b2);
        } catch (ReadingException ex) {
            System.err.println("Invalid sensor data [" + data + "] " + ex.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "Reading{" 
                + "x=" + df2.format(getX()) 
                + ", y=" + df2.format(getY()) 
                + ", NS=" + df2.format(getNS()) 
                + ", WE=" + df2.format(getWE()) 
                + ", UD=" + df2.format(getUD()) 
                + ", h=" + getHeading() 
                + ", b1=" + b1 
                + ", b2=" + b2 
                + "}";
    }

    private static DecimalFormat df1 = new DecimalFormat("0.000000");
    private static DecimalFormat df2 = new DecimalFormat("000000.0");


    private Reading(double x, double y, double ns, double we, double ud, double heading, boolean b1, boolean b2) {
        this.x = x;
        this.y = y;
        this.ns = ns;
        this.we = we;
        this.ud = ud;
        this.heading = heading;
        this.b1 = b1;
        this.b2 = b2;
        this.timestamp = System.currentTimeMillis();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getNS() {
        return ns;
    }

    public double getWE() {
        return we;
    }

    public double getUD() {
        return ud;
    }

    public long tiltCompensatedBearing() {
        // Precompute the tilt compensation parameters to improve readability.
        double phi = getX();    // getRollRadians();
        double theta = getY();  // getPitchRadians();

        // Convert to floating point to reduce rounding errors
        //    Sample3D cs = this->getSample(NORTH_EAST_DOWN);
        double x = getWE();     // float x = (float) cs.x;
        double y = getNS();     // float y = (float) cs.y;
        double z = getUD();     //float z = (float) cs.z;

        // Precompute cos and sin of pitch and roll angles to make the calculation a little more efficient.
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        // Calculate the tilt compensated bearing, and convert to degrees.
        double bearing = (360 * Math.atan2(x * cosTheta + y * sinTheta * sinPhi + z * sinTheta * cosPhi, z * sinPhi - y * cosPhi)) / (2 * PI);

        // Handle the 90 degree offset caused by the NORTH_EAST_DOWN based calculation.
//        bearing = 90 - bearing;
//
//        // Ensure the calculated bearing is in the 0..359 degree range.
//        if (bearing < 0) {
//            bearing += 360.0f;
//        }

        return Math.round(bearing);
    }

    /**
     * Read the heading in degrees.
     *
     * This is the heading computed from Polar (NS, WE) to degrees.
     *
     * @return
     */
    public long getHeading() {
        return Math.round(heading);
    }

    public boolean isB1() {
        return b1;
    }

    public boolean isB2() {
        return b2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private static double parseNum(String value, String data, String name) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException nfe) {
            throw new ReadingException("Invalid number read: value[" + value + "] name[" + name + "] data[" + data + "]", nfe);
        }
    }

    private static boolean parseBool(String value) {
        if (value.trim().startsWith("1")) {
            return true;
        }
        return false;
    }

}
