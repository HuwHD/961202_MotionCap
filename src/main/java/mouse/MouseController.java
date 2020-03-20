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

import robot.RobotMouseThread;
import serial.Reading;
import serial.SerialPortListener;

public class MouseController implements SerialPortListener {

    private final RobotMouseThread robotMouseThread;
    private final Degrees headingMin;
    private final Degrees headingMax;
    private final Degrees headingLimitMin;
    private final Degrees headingLimitMax;
    private final Degrees headingOffset;
    private final long maxHeadingWidth1;
    private final long minHeadingWidth1;
    private final long maxHeadingWidth2;
    private final long minHeadingWidth2;
    private MouseHeadingState mouseHeadingState;
    private long mouseHeadingOffset;
    private boolean connected = false;

    public MouseController(RobotMouseThread robotMouseThread, long[] headingData) {
        stop();
        minHeadingWidth1 = headingData[1];
        maxHeadingWidth1 = headingData[2];
        minHeadingWidth2 = minHeadingWidth1 * 2;
        maxHeadingWidth2 = maxHeadingWidth1 * 2;

        this.robotMouseThread = robotMouseThread;
        this.headingOffset = new Degrees(headingData[0]);
        this.headingMin = headingOffset.sub(minHeadingWidth1);
        this.headingMax = headingOffset.add(minHeadingWidth1);
        this.headingLimitMin = headingOffset.sub(maxHeadingWidth1);
        this.headingLimitMax = headingOffset.add(maxHeadingWidth1);
    }

    public final void start() {
        mouseHeadingState = MouseHeadingState.NULL_ZONE;
        connected = true;
    }

    public final void stop() {
        mouseHeadingState = MouseHeadingState.INACTIVE;
        connected = false;
    }

    @Override
    public void reading(Reading r) {
        if (r != null) {
            Degrees d = new Degrees(r.getHeading());
            long diffLimitMax = d.diffAntiClockwise(headingLimitMax);
            long diffMax = d.diffAntiClockwise(headingMax);
            if (diffLimitMax <= maxHeadingWidth2) {
                if (diffMax < minHeadingWidth2) {
                    nullZone(diffMax - minHeadingWidth1);
                 } else {
                    activeZone(diffLimitMax - maxHeadingWidth1);
                }
            } else {
                mouseHeadingOffset = 0;
                mouseHeadingState = MouseHeadingState.INACTIVE;
            }
        }
    }
    
    private void activeZone(long amount) {
        mouseHeadingOffset = amount;
        mouseHeadingState = MouseHeadingState.ACTIVE;
    }
    
    private void nullZone(long amount) {
        mouseHeadingOffset = amount;
        mouseHeadingState = MouseHeadingState.NULL_ZONE;
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
        return headingMin.getDegrees();
    }

    public long getHeadingMax() {
        return headingMax.getDegrees();
    }

    public long getHeadingLimitMin() {
        return headingLimitMin.getDegrees();
    }

    public long getHeadingLimitMax() {
        return headingLimitMax.getDegrees();
    }

    public MouseHeadingState getMouseHeadingState() {
        return mouseHeadingState;
    }

    public long getMouseHeadingOffset() {
        return mouseHeadingOffset;
    }

}
