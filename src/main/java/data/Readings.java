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
package data;

import serial.Reading;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Readings {
    private final ConcurrentLinkedQueue<Reading> readings;
    private final int capacity;
    private Reading lastReading;

    public Readings(int capacity) {
        readings = new ConcurrentLinkedQueue();
        this.capacity = capacity;
    }

    public int add(Reading r, long latency) {
        lastReading = r;
        readings.add(r);
        if (readings.size() > capacity) {
            readings.poll();
        }
        return readings.size();
    }

    public Reading getLastReading() {
        return lastReading;
    }

    public Reading get() {
        return readings.poll();
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return readings.size();
    }
}