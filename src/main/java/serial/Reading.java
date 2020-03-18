/*
 * Copyright (C) 2019 Huw Hudson-Davies
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

    private static final int SERIES_X = 0;
    private static final int SERIES_Y = 1;
    private static final int SERIES_NS = 2;
    private static final int SERIES_WE = 3;
    private static final int SERIES_H = 4;
    private final double x;
    private final double y;
    private final double ns;
    private final double we;
    private final double heading;

    private final boolean b1;
    private final boolean b2;
    private final long timestamp;

    public static Reading parse(String data) {
        if (data == null) {
            return null;
        }
        String[] values = data.trim().split("\\,");
        if (values.length != 7) {
            System.err.println("Invalid sensor data [" + data + "]");
            return null;
        }
        try {
            double x = parseNum(values[SERIES_X], "X", data);
            double y = parseNum(values[SERIES_Y], "Y", data);
            double ns = parseNum(values[SERIES_NS], "NS", data);
            double we = parseNum(values[SERIES_WE], "WE", data);
            double h = parseNum(values[SERIES_H], "H", data);
            boolean b1 = parseBool(values[5]);
            boolean b2 = parseBool(values[6]);
            return new Reading(x, y, ns, we, h, b1, b2);
        } catch (ReadingException ex) {
            System.err.println("Invalid sensor data [" + data + "] " + ex.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "Reading{" + "x=" + format(getX()) + ", y=" + format(getY()) + ", NS=" + format(getNS()) + ", WE=" + format(getWE()) + ", h=" + format(getHeading()) + " b1=" + b1 + ", b2=" + b2 + "}";
    }

    private static DecimalFormat df = new DecimalFormat("00000.0");

    private String format(double d) {
        return df.format(d);
    }

    private Reading(double x, double y, double ns, double we, double heading, boolean b1, boolean b2) {
        this.x = x;
        this.y = y;
        this.ns = ns;
        this.we = we;
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

    public double getHeading() {
        return heading;
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
            throw new ReadingException("Invalid integer read: value[" + value + "] name[" + name + "] data[" + data + "]", nfe);
        }
    }

    private static boolean parseBool(String value) {
        if (value.trim().startsWith("1")) {
            return true;
        }
        return false;
    }

}
