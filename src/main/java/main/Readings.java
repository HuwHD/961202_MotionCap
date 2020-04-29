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
package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import serial.Reading;

/**
 * A list of Reading(s) for use by the FXMLDocumentController to draw a graph 
 * of the last N readings.
 */
public class Readings {

    private final ConcurrentLinkedQueue<Reading> readings;
    private final int capacity;
    private Reading lastReading;
    private Reading firstReading;

    /**
     * Create with a given capacity.
     * 
     * @param capacity The number of readings to be displayed
     */
    public Readings(int capacity) {
        readings = new ConcurrentLinkedQueue();
        this.capacity = capacity;
    }

    /**
     * Return a list of the readings from  the queue;
     * @return A list of readings
     */
    public List<Reading> readings() {
        List<Reading> l = new ArrayList<>();
        Iterator<Reading> ite = readings.iterator();
        while (ite.hasNext()) {
            l.add(ite.next());
        }
        return l;
    }

    /**
     * Add a reading, keeping the first and last reading in sync
     * @param r The reading to be added.
     * @return the number of readings
     */
    public int add(Reading r) {
        if (firstReading == null) {
            firstReading = r;
        }
        lastReading = r;
        readings.add(r);
        if (readings.size() > capacity) {
            firstReading = readings.poll();
        }
        return readings.size();
    }

    /**
     * Get the average time between readings.
     * @return the latency of the sensor readings in milli seconds
     */
    public long getLatency() {
        return (lastReading.getTimestamp() - firstReading.getTimestamp()) / readings.size();
    }

    /**
     * Read a reading from the front of the queue
     * @return The next reading.
     */
    public Reading get() {
        return readings.poll();
    }

    /**
     * Get the maximum number of readings
     * @return the maximum number of readings
     */
    public int capacity() {
        return capacity;
    }

    /**
     * is there a 'last' reading.
     * 
     * There will not be a reading if the sensor is not connected or the first
     * one has not been received.
     * 
     * @return true if there is a last reading.
     */
    public boolean hasLastReading() {
        return lastReading != null;
    }
    
    /**
     * Get the last reading to be added.
     * @return the last reading to be added.
     */
    public Reading getLastReading() {
        return lastReading;
    }

    /**
     * Get the actual number of readings. May be less than capacity.
     * @return the actual number of readings
     */
    public int size() {
        return readings.size();
    }

    /**
     * clear all of the readings and clear the first and last.
     */
    void clear() {
        readings.clear();
        lastReading = null;
        firstReading = null;
    }
}
