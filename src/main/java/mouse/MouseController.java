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
    private boolean movingTheMouse = false;

    public MouseController(RobotMouseThread robotMouseThread, long[] headingData) {
        movingTheMouse = false;
        mouseHeadingState = MouseHeadingState.DISCONNECTED;
        
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

    /**
     * Start moving the mouse.
     *
     * Setting movingTheMouse = true will mean that when a reading is received
     * from the SerialPortThread the mouse will be moved.
     */
    public final void startMovingTheMouse() {
        mouseHeadingState = MouseHeadingState.NULL_ZONE;
        mouseHeadingOffset = 0;
        movingTheMouse = true;
    }

    /**
     * stop moving the mouse and hand control back to the normal mouse
     */
    public final void stopMovingTheMouse() {
        movingTheMouse = false;
        robotMouseThread.stopMouse();
        mouseHeadingState = MouseHeadingState.DISCONNECTED;
    }

    /**
     * Message received from SerialPortThread when it gets a reading from the
     * serial port
     *
     * Get the readings and workout what to send the Mouse Robot to make it
     * move.
     *
     * If movingTheMouse is false we do all the work just don't send it to the
     * mouse robot. This means the GUI can still plot changes.
     *
     * @param r - The readings from the device
     */
    @Override
    public void reading(Reading r) {
        if (r != null) {
            Degrees d = new Degrees(r.getHeading());
            long diffLimitMax = d.diffAntiClockwise(headingLimitMax);
            long diffMax = d.diffAntiClockwise(headingMax);
            if (diffLimitMax <= maxHeadingWidth2) {
                if (diffMax < minHeadingWidth2) {
                    inNullHeadingZone(diffMax - minHeadingWidth1);
                } else {
                    inActiveHeadingZone(diffLimitMax - maxHeadingWidth1);
                }
            } else {
                inNullHeadingZone(mouseHeadingOffset);
            }
        }
    }

    /**
     * Message received from SerialPortThread When a failure is detected and the
     * thread ends.
     *
     * There is nothing to do here except to stop moving the mouse and hand
     * control back to the normal mouse
     *
     * @param e - What went wrong!
     */
    @Override
    public void fail(Exception e) {
        stopMovingTheMouse();
    }

    /**
     * Message received from SerialPortThread When the device connects and
     * reading are available.
     *
     * There is nothing to do here. We have to wait for the
     * startMovingTheMouse() method to be called before we start moving the
     * mouse.
     *
     * @param devicePort The port
     * @param baud The port speed
     * @param name The port name
     */
    @Override
    public void connected(String devicePort, int baud, String name) {
    }

    /**
     * Message received from SerialPortThread When the device dis-connects and
     * reading are NOT available.
     *
     * There is nothing to do here except to stop moving the mouse and hand
     * control back to the normal mouse
     *
     * @param devicePort The port
     * @param name The port name
     */
    @Override
    public void disConnected(String devicePort, String name) {
        stopMovingTheMouse();
    }

    public boolean isMovingTheMouse() {
        return movingTheMouse;
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

    private void inActiveHeadingZone(long heading) {
        mouseHeadingOffset = heading;
        if (movingTheMouse) {
            mouseHeadingState = MouseHeadingState.ACTIVE;
            robotMouseThread.setSpeedX(heading);
        } else {
            mouseHeadingState = MouseHeadingState.DISCONNECTED;
        }
    }

    private void inNullHeadingZone(long heading) {
        mouseHeadingOffset = heading;
        if (movingTheMouse) {
            mouseHeadingState = MouseHeadingState.NULL_ZONE;
        } else {
            mouseHeadingState = MouseHeadingState.DISCONNECTED;
        }
    }
}
