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
    private long mouseHeadingOffset;

    private final long verticalMin;
    private final long verticalMax;
    private final long verticalLimitMin;
    private final long verticalLimitMax;
    private final long verticalOffset;
    private final long maxVerticalWidth1;
    private final long minVerticalWidth1;
    private final long maxVerticalWidth2;
    private final long minVerticalWidth2;
    private long mouseVerticalOffset;

    private MouseState mouseHeadingState;
    private MouseState mouseVerticalState;

    public MouseController(RobotMouseThread robotMouseThread, long[] headingData, long[] verticalData) {
        this.mouseHeadingState = MouseState.DISCONNECTED;
        this.mouseVerticalState = MouseState.DISCONNECTED;
        /*
        Set up the Heading (Horizontal) boundries
         */
        this.headingOffset = new Degrees(headingData[0]);
        this.minHeadingWidth1 = headingData[1];
        this.maxHeadingWidth1 = headingData[2];
        this.minHeadingWidth2 = minHeadingWidth1 * 2;
        this.maxHeadingWidth2 = maxHeadingWidth1 * 2;
        this.headingMin = headingOffset.sub(minHeadingWidth1);
        this.headingMax = headingOffset.add(minHeadingWidth1);
        this.headingLimitMin = headingOffset.sub(maxHeadingWidth1);
        this.headingLimitMax = headingOffset.add(maxHeadingWidth1);
        /*
        Set up the Up/Down (Vertical) boundries
         */
        this.verticalOffset = verticalData[0];
        this.minVerticalWidth1 = verticalData[1];
        this.maxVerticalWidth1 = verticalData[2];
        this.minVerticalWidth2 = minVerticalWidth1 * 2;
        this.maxVerticalWidth2 = maxVerticalWidth1 * 2;
        this.verticalMin = verticalOffset - minVerticalWidth1;
        this.verticalMax = verticalOffset + minVerticalWidth1;
        this.verticalLimitMin = verticalOffset - maxVerticalWidth1;
        this.verticalLimitMax = verticalOffset + maxVerticalWidth1;

        this.robotMouseThread = robotMouseThread;

    }

    /**
     * Start moving the mouse.
     * <p>
     * Setting movingTheMouse = true will mean that when a reading is received
     * from the SerialPortThread the mouse will be moved.
     */
    public final void connectTheMouse() {
        mouseHeadingState = MouseState.NULL_ZONE;
        mouseVerticalState = MouseState.NULL_ZONE;
        mouseHeadingOffset = 0;
        mouseVerticalOffset = 0;
        robotMouseThread.connect();
    }

    /**
     * stop moving the mouse and hand control back to the normal mouse
     */
    public final void disConnectTheMouse() {
        robotMouseThread.disConnect();
        mouseHeadingState = MouseState.DISCONNECTED;
        mouseVerticalState = MouseState.DISCONNECTED;
    }

    /**
     * Message received from SerialPortThread when it gets a reading from the
     * serial port
     * <p>
     * Get the readings and workout what to send the Mouse Robot to make it
     * move.
     * <p>
     * If movingTheMouse is false we do all the work just don't send it to the
     * mouse robot. This means the GUI can still plot changes.
     *
     * @param r - The readings from the device
     */
    @Override
    public void reading(Reading r) {
        if (r != null) {
            processHeadingData(r.getHeading());
            processVerticalData(Math.round(r.getY()));
            processSensorButtons(r);
        }
    }

    private void processSensorButtons(Reading r) {
        if (robotMouseThread.isConnected()) {
            if (r.isB2S()) {
                robotMouseThread.leftButtonPress();
            } else {
                robotMouseThread.leftButtonRelease();
            }
            if (r.isB1S()) {
                robotMouseThread.rightButtonPress();
            } else {
                robotMouseThread.rightButtonRelease();
            }
        }
    }

    private void processVerticalData(long y) {
        long diffLimitMin = y - verticalLimitMin;
        long diffMin = y - verticalMin;
        if ((diffLimitMin > 0) && (diffLimitMin < maxVerticalWidth2)) {
            if ((diffMin > 0) && (diffMin < minVerticalWidth2)) {
                inNullVerticalZone(diffMin - minVerticalWidth1);
            } else {
                inActiveVerticalZone(diffLimitMin - maxVerticalWidth1);
            }
        } else {
            outsideVerticalZone(diffLimitMin - maxVerticalWidth1);
        }
    }

    private void processHeadingData(long heading) {
        Degrees d = new Degrees(heading);
        long diffLimitMax = d.diffAntiClockwise(headingLimitMax);
        long diffMax = d.diffAntiClockwise(headingMax);
        if (diffLimitMax <= maxHeadingWidth2) {
            if (diffMax <= minHeadingWidth2) {
                inNullHeadingZone(diffMax - minHeadingWidth1);
            } else {
                inActiveHeadingZone(diffLimitMax - maxHeadingWidth1);
            }
        } else {
            outsideHeadingZone(diffLimitMax - maxHeadingWidth1);
        }
    }

    /**
     * Message received from SerialPortThread When a failure is detected and the
     * thread ends.
     * <p>
     * There is nothing to do here except to stop moving the mouse and hand
     * control back to the normal mouse
     *
     * @param e - What went wrong!
     */
    @Override
    public void fail(Exception e) {
        disConnectTheMouse();
    }

    /**
     * Message received from SerialPortThread When the device connects and
     * reading are available.
     * <p>
     * There is nothing to do here. We have to wait for the
     * startMovingTheMouse() method to be called before we start moving the
     * mouse.
     *
     * @param devicePort The port
     * @param baud The port speed
     * @param name The port name
     */
    @Override
    public void connectedSensor(String devicePort, int baud, String name) {
    }

    /**
     * Message received from SerialPortThread When the device dis-connects and
     * reading are NOT available.
     * <p>
     * There is nothing to do here except to stop moving the mouse and hand
     * control back to the normal mouse
     *
     * @param devicePort The port
     * @param name The port name
     */
    @Override
    public void disConnectedSensor(String devicePort, String name) {
        disConnectTheMouse();
    }

    @Override
    public boolean rawData(String s) {
        return false;
    }

    public boolean isConnectedToMouse() {
        return robotMouseThread.isConnected();
    }

    public long getVerticalMin() {
        return verticalMin;
    }

    public long getVerticalMax() {
        return verticalMax;
    }

    public long getVerticalLimitMin() {
        return verticalLimitMin;
    }

    public long getVerticalLimitMax() {
        return verticalLimitMax;
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

    public MouseState getMouseHeadingState() {
        return mouseHeadingState;
    }

    public MouseState getMouseVerticalState() {
        return mouseVerticalState;
    }

    public long getMouseHeadingOffset() {
        return mouseHeadingOffset;
    }

    public long getMouseVerticalOffset() {
        return mouseVerticalOffset;
    }

    /*
    (swapLR?1:-1) *
     */
    private void inActiveHeadingZone(long heading) {
        mouseHeadingOffset = heading;
        mouseHeadingState = MouseState.ACTIVE;
        if (robotMouseThread.isConnected()) {
            robotMouseThread.setSpeedX(-(heading * 5));
        }
    }

    private void inNullHeadingZone(long heading) {
        mouseHeadingOffset = heading;
        mouseHeadingState = MouseState.NULL_ZONE;
        if (robotMouseThread.isConnected()) {
            robotMouseThread.setSpeedX(0);
        }
    }

    private void inNullVerticalZone(long l) {
        mouseVerticalOffset = l;
        mouseVerticalState = MouseState.NULL_ZONE;
        if (robotMouseThread.isConnected()) {
            robotMouseThread.setSpeedY(0);
        }
    }

    private void inActiveVerticalZone(long l) {
        mouseVerticalOffset = l;
        mouseVerticalState = MouseState.ACTIVE;
        if (robotMouseThread.isConnected()) {
            robotMouseThread.setSpeedY(l / 2.0);
        }
    }

    private void outsideVerticalZone(long l) {
        mouseVerticalOffset = l;
        mouseVerticalState = MouseState.INACTIVE;
    }

    private void outsideHeadingZone(long l) {
        mouseHeadingOffset = l;
        mouseHeadingState = MouseState.INACTIVE;
    }

    public boolean isLeftButtonPressed() {
        return robotMouseThread.isLeftButtonPressed();
    }

    public boolean isRightButtonPressed() {
        return robotMouseThread.isRightButtonPressed();
    }
}
