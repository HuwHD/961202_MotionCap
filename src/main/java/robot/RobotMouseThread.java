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
package robot;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Move the mouse in the background using the robot package.
 *
 * All mouse moves and button presses occur in the thread (run method).
 *
 * There were issues when the mouse button methods were called in another thread
 * so all calls to the robot API were moved to the thread (run method).
 *
 * I think this is an issue with the way the robot API works.
 *
 */
public class RobotMouseThread extends Thread implements RobotMouseThreadInterface {

    /**
     * The State of a given button.
     * 
     * REQUEST_DOWN - Requested to press button down. 
     * IS_DOWN - Thread has set button DOWN.
     * REQUEST_UP - Requested to release button. 
     * IS_UP - Thread has set button UP.
     * 
     * The thread MUST be running and connected for REQUEST_DOWN to 
     * change to IS_DOWN and REQUEST_UP to change to IS_UP.
     */
    public static enum ButtonState {
        REQUEST_DOWN, IS_DOWN, REQUEST_UP, IS_UP
    }

    private final static double MOUSE_NOT_IN_POSITION_TOLLERANCE = 20.0;
    private final static int MOUSE_NOT_IN_POSITION_REPEATS = 2;

    private final Robot robotMouse;
    private final RobotMouseEventListener listener;
    private final Rectangle screenBounds;

    private boolean canRun = true;
    private int outOfPositionCounts = 0;
    /*
    Doubles used to keep track of mouse position.
    Actual mouse pos is integer but this means we would loose precision if we 
        read the mouse position then move 1/2 a pixel.
     */
    private double speedX;
    private double speedY;
    private double mouseX;
    private double mouseY;
    private boolean hasSpeed = false;
    private boolean connected = false;
    private ButtonState buttonLeftState;
    private ButtonState buttonRightState;

    /**
     * @param listener Listen to events caused by the robot mouse movement.
     * @param screenBounds The limits we can move the mouse
     */
    public RobotMouseThread(RobotMouseEventListener listener, Rectangle screenBounds) {
        try {
            this.robotMouse = new Robot();
            this.robotMouse.setAutoDelay(0);
            this.robotMouse.setAutoWaitForIdle(false);
        } catch (AWTException awt) {
            throw new RobotMouseException("Mouse Robot creation failed:", awt);
        }
        this.listener = listener;
        this.screenBounds = screenBounds;
        /*
        Assume the buttons are UP when we start!
        */
        this.stopMouse();
        buttonLeftState = ButtonState.IS_UP;
        buttonRightState = ButtonState.IS_UP;
     }

    /**
     * Stop the mouse. This also releases the button state 
     * 
     * Note this only happens as long as the we are still connected and the thread is running.
     */
    public final void stopMouse() {
        setSpeedX(0.0);
        setSpeedY(0.0);
        Point p = MouseInfo.getPointerInfo().getLocation();
        mouseX = p.x;
        mouseY = p.y;
        outOfPositionCounts = 0;
        buttonLeftState = ButtonState.REQUEST_UP;
        buttonRightState = ButtonState.REQUEST_UP;
    }

    @Override
    public final void moveMouseAbs(double x, double y) {
        if (x < screenBounds.getMinX()) {
            x = screenBounds.getMinX();
        } else if (x > screenBounds.getMaxX()) {
            x = screenBounds.getMaxX();
        }
        if (y < screenBounds.getMinX()) {
            y = screenBounds.getMinX();
        } else if (y > screenBounds.getMaxY()) {
            y = screenBounds.getMaxY();
        }

        Point p = MouseInfo.getPointerInfo().getLocation();
        if (roughlyNotEqual(p.getX(), x, MOUSE_NOT_IN_POSITION_TOLLERANCE) || roughlyNotEqual(p.getY(), y, MOUSE_NOT_IN_POSITION_TOLLERANCE)) {
            outOfPositionCounts++;
            if (outOfPositionCounts > MOUSE_NOT_IN_POSITION_REPEATS) {
                outOfPositionCounts = 0;
                if (listener != null) {
                    listener.mouseNotInPosition(new Point((int) x, (int) y), p, outOfPositionCounts);
                }
            }
        } else {
            outOfPositionCounts = 0;
        }
        mouseX = x;
        mouseY = y;
        if (connected) {
            robotMouse.mouseMove((int) x, (int) y);
        }
    }

    public static boolean roughlyNotEqual(double a, double b, double tollerance) {
        double diff = Math.abs(a - b);
        return (diff > Math.abs(tollerance + 0.000001d));
    }

    public final void moveMouseRel(double dx, double dy) {
        moveMouseAbs(mouseX + dx, mouseY + dy);
    }

    public void delay(int ms) {
        robotMouse.delay(ms);
    }

    @Override
    public void setSpeedY(double y) {
        this.hasSpeed = ((Math.abs(this.speedX) > 0.0001) || (Math.abs(y) > 0.0001));
        this.speedY = y;
    }

    @Override
    public void setSpeedX(double x) {
        this.hasSpeed = ((Math.abs(x) > 0.0001) || (Math.abs(this.speedY) > 0.0001));
        this.speedX = x;
    }

    public double getSpeedY() {
        return speedY;
    }

    public double getSpeedX() {
        return speedX;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void disConnect() {
        stopMouse();
        if (listener != null) {
            listener.disConnectedMouse();
        }
        /*
        Before we can disconnect we need to wait for the buttons to come up!
        stopMouse() releases each button but the thread needs to run to actually
        release them so we cannot disconnect untill both are UP.
        
        Count the number of loops so we dont wait forever and lock up the program.
        1 second should be loads of time.
         */
        int count = 0;
        while ((!buttonLeftState.equals(ButtonState.IS_UP)) || (!buttonRightState.equals(ButtonState.IS_UP))) {
            try {
                sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace(); // This is OK as it is VERY unlikely to be inturrnpted!
            }
            count++;
            if (count > 100) {
                break;
            }
        }
        this.connected = false;

    }

    public ButtonState getButtonLeftState() {
        return buttonLeftState;
    }

    public ButtonState getButtonRightState() {
        return buttonRightState;
    }

    @Override
    public void connect() {
        this.connected = true;
        if (listener != null) {
            listener.connectedMouse();
        }
    }

    /**
     * Stop the thread and any mouse movement
     */
    public void close() {
        stopMouse();
        canRun = false;
    }

    @Override
    public boolean isLeftButtonPressed() {
        return buttonLeftState.equals(ButtonState.IS_DOWN);
    }

    @Override
    public boolean isRightButtonPressed() {
        return buttonRightState.equals(ButtonState.IS_DOWN);
    }

    /**
     * Press the LEFT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread
     * (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state
     * changes.
     */
    @Override
    public void leftButtonPress() {
        switch (buttonLeftState) {
            case IS_UP:
            case REQUEST_UP:
                buttonLeftState = ButtonState.REQUEST_DOWN;
        }
    }

    /**
     * Release the LEFT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread
     * (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state
     * changes.
     */
    @Override
    public void leftButtonRelease() {
        switch (buttonLeftState) {
            case IS_DOWN:
            case REQUEST_DOWN:
                buttonLeftState = ButtonState.REQUEST_UP;
        }
    }

    /**
     * Press the RIGHT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread
     * (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state
     * changes.
     */
    @Override
    public void rightButtonPress() {
        switch (buttonRightState) {
            case IS_UP:
            case REQUEST_UP:
                buttonRightState = ButtonState.REQUEST_DOWN;
        }
    }

    /**
     * Release the RIGHT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread
     * (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state
     * changes.
     */
    @Override
    public void rightButtonRelease() {
        switch (buttonRightState) {
            case IS_DOWN:
            case REQUEST_DOWN:
                buttonRightState = ButtonState.REQUEST_UP;
        }
    }

    /**
     * Type a character
     *
     * @param i the int value if the key to be typed
     */
    public void type(int i) {
        if (connected) {
            robotMouse.delay(40);
            robotMouse.keyPress(i);
            robotMouse.keyRelease(i);
        }
    }

    public void type(String s) {
        byte[] bytes = s.getBytes();
        for (byte b : bytes) {
            int code = b;
            // keycode only handles [A-Z] (which is ASCII decimal [65-90])
            if (code > 96 && code < 123) {
                code = code - 32;
            }
            type(code);
        }
    }

    /**
     * Sleep until we have a speed
     */
    @Override
    public void run() {
        long timeNow = System.currentTimeMillis();
        long lastTimeMoved = timeNow;
        double time;
        try {
            while (canRun) {
                if (hasSpeed) {
                    timeNow = System.currentTimeMillis();
                    time = (timeNow - lastTimeMoved) / (1000.0);
                    moveMouseRel(speedX * time, speedY * time);
                    lastTimeMoved = timeNow;
                } else {
                    lastTimeMoved = System.currentTimeMillis();
                }
                /*
            Press or release the LEFT button once when the it's state changes
            State 1: IS_UP - The steady released state. No action required here.
            State 2: DOWN - set by calling leftPress() above
            State 3: IS_DOWN - Set after robot.mousePress is called.
            ...
            State 1: IS_DOWN - The steady pressed state. No action required here.
            State 2: UP - set by calling leftRelease() above
            State 3: IS_UP - Set after robot.mouseRelease is called.
            ...
                 */
                if (connected) {
                    switch (buttonLeftState) {
                        case REQUEST_UP:
                            robotMouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                            buttonLeftState = ButtonState.IS_UP;
                            break;
                        case REQUEST_DOWN:
                            robotMouse.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            buttonLeftState = ButtonState.IS_DOWN;
                            break;
                    }
                    /*
            Press or release the RIGHT button once when the it's state changes
            Same logic as LEFT button
                     */
                    switch (buttonRightState) {
                        case REQUEST_UP:
                            robotMouse.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                            buttonRightState = ButtonState.IS_UP;
                            break;
                        case REQUEST_DOWN:
                            robotMouse.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                            buttonRightState = ButtonState.IS_DOWN;
                            break;
                    }
                }
                if (canRun) {
                    robotMouse.delay(30);
                }
            }
        } finally {
            /*
            Always release the mouse buttons!
             */
            robotMouse.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            buttonRightState = ButtonState.IS_UP;
            robotMouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            buttonLeftState = ButtonState.IS_UP;
        }
    }
}
