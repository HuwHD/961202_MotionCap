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
package mouse;

import robot.RobotMouseThread;
import serial.Reading;
import serial.SerialPortListener;

public class MouseController implements SerialPortListener {

    private final RobotMouseThread robotMouseThread;
    private final long headingMin;
    private final long headingMax;
    private final long headingLimitMin;
    private final long headingLimitMax;
    private final long headingOffset;
    private final long limitWidth;

    public MouseController(RobotMouseThread robotMouseThread, long[] headingData) {
        this.robotMouseThread = robotMouseThread;
        this.headingOffset = headingData[0];
        this.headingMin = sub(headingOffset, headingData[1]);
        this.headingMax = add(headingOffset, headingData[1]);
        this.limitWidth = headingData[2] * 2;
        this.headingLimitMin = sub(headingOffset, headingData[2]);
        this.headingLimitMax = add(headingOffset, headingData[2]);
    }

    private long sub(long deg, long val) {
        deg = deg - val;
        while (deg < 0) {
            deg = deg + 360;
        }
        return deg;
    }

    private long add(long deg, long val) {
        deg = deg + val;
        while (deg > 359) {
            deg = deg - 360;
        }
        return deg;
    }

    @Override
    public void reading(Reading r) {
        if (r != null) {
            long heading = (long) r.getHeading();
            long beMax = belowOrEqualToMax(heading);
            long aeMin = aboveOrEqualToMin(heading);
            int c = 0;
            if ((beMax >= 0) && (aeMin >= 0)) {
                System.out.print(c+": beMax:" + beMax + " aeMin:" + aeMin + " ");
                if ((beMax == headingMax) && (aeMin == headingMin)) {
                    System.out.print("MID");
                }
                if ((beMax == headingMin) && (aeMin == headingLimitMin)) {
                    System.out.print("MIN");
                }
                if ((beMax == headingLimitMax) && (aeMin == headingMax)) {
                    System.out.print("MAX");
                }
                System.out.println("");
                c++;
            }
        }
    }

    private long belowOrEqualToMax(long h) {
        for (int i = 0; i < limitWidth; i++) {
            if ((h == headingMax) || (h == headingLimitMax) || (h == headingMin) || (h == headingLimitMin)) {
                return h;
            }
            h = h + 1;
            if (h == 360) {
                h = 0;
            }
        }
        return -1;
    }

    private long aboveOrEqualToMin(long h) {
        for (int i = 0; i < limitWidth; i++) {
            if ((h == headingMin) || (h == headingLimitMin) || (h == headingMax) || (h == headingLimitMax)) {
                return h;
            }
            h = h - 1;
            if (h == 0) {
                h = 359;
            }
        }
        return -1;
    }

    @Override
    public void fail(Exception e) {
        robotMouseThread.stopMouse();
    }

    @Override
    public void connected(String devicePort, int baud, String name) {
        robotMouseThread.stopMouse();
    }

    @Override
    public void disConnected(String devicePort, String name) {
        robotMouseThread.stopMouse();
    }

    public long getHeadingMin() {
        return headingMin;
    }

    public long getHeadingMax() {
        return headingMax;
    }

    public long getHeadingLimitMin() {
        return headingLimitMin;
    }

    public long getHeadingLimitMax() {
        return headingLimitMax;
    }

    private void moveLeft(double d) {
        System.out.println("LEFT:" + d);
    }

    private void moveRight(double d) {
        System.out.println("RIGHT:" + d);
    }

}
