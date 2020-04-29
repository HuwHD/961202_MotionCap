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
package serial;

import java.text.DecimalFormat;

/**
 * Read from the sensor data String
 */
public class Reading {

    private static final double PI = Math.PI;

    private static final int SERIES_X = 0;
    private static final int SERIES_Y = 1;
    private static final int SERIES_H = 2;
    private static final int SERIES_BUTTON_AS = 3;
    private static final int SERIES_BUTTON_BS = 4;
    private static final int SERIES_BUTTON_AR = 5;
    private static final int SERIES_BUTTON_BR = 6;
    private final double x;
    private final double y;
    private final double heading;

    private final boolean b1S;
    private final boolean b2S;
    private final boolean b1R;
    private final boolean b2R;
    private final long timestamp;

    /**
     * Factory method to return a reading from the sensor data.
     * @param data the sensor data
     * @param swapLR flag if need to swap left and right buttons.
     * @param swapUD flag if need to swap up and down movement.
     * @return The Reading.
     * @Throws ReadingException is the data is not valid
     */
    public static Reading parse(String data, boolean swapLR, boolean swapUD) {
        if (data == null) {
            return null;
        }
        /*
        There MUST be 7 values for it to be valid
        */
        String[] values = data.trim().split("\\,");
        if (values.length != 7) {
            throw new ReadingException("Invalid sensor data [" + data + "]");
        }
        try {
            double x = parseNum(values[SERIES_X], "X", data);
            double y = parseNum(values[SERIES_Y], "Y", data);
            double h = parseNum(values[SERIES_H], "H", data);
            boolean b1S = parseBool(values[SERIES_BUTTON_AS]);
            boolean b2S = parseBool(values[SERIES_BUTTON_BS]);
            boolean b1R = parseBool(values[SERIES_BUTTON_AR]);
            boolean b2R = parseBool(values[SERIES_BUTTON_BR]);
            if (swapUD) {
                y = -y;
            }
            if (swapLR) {
                return new Reading(x, -y, h, b1S, b2S, b1R, b2R);
            } else {
                return new Reading(x, -y, h, b2S, b1S, b1R, b2R);
            }
        } catch (Exception ex) {
            throw new ReadingException(("Failed to read ["+data+"]"), ex);
        }
    }

    /**
     * Return a JSON like string of the readings
     * @return a String
     */
    @Override
    public String toString() {
        return "Reading{" 
                + "x=" + df2.format(getX()) 
                + ", y=" + df2.format(getY()) 
                + ", h=" + getHeading()
                + ", b1S=" + b1S
                + ", b2S=" + b2S
                + ", b1R=" + b1R
                + ", b2R=" + b2R
                + "}";
    }

    private static DecimalFormat df2 = new DecimalFormat("000000.0");

/**
 * Create a Reading
 * @param x The X value (tilt)
 * @param y The Y value (Roll)
 * @param heading The heading in degrees
 * @param b1S The A|B button state on the sensor (A if not swapped)
 * @param b2S The B|A button state on the sensor (B if not swapped)
 * @param b1R The A|B button state on the receiver (A if not swapped, false if no receiver)
 * @param b2R The B|A button state on the receiver (A if not swapped, false if no receiver)
 */
    private Reading(double x, double y, double heading, boolean b1S, boolean b2S, boolean b1R, boolean b2R) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.b1S = b1S;
        this.b2S = b2S;
        this.b1R = b1R;
        this.b2R = b2R;
        this.timestamp = System.currentTimeMillis();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public long getHeading() {
        return Math.round(heading);
    }

    public boolean isB1S() {
        return b1S;
    }

    public boolean isB2S() {
        return b2S;
    }

    public boolean isB1R() {
        return b1R;
    }

    public boolean isB2R() {
        return b2R;
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
