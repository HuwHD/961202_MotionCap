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
 * Move the mouse in the background.
 */
public class RobotMouseThread extends Thread {

    private enum ButtonState {
        DOWN, IS_DOWN, UP, IS_UP
    }

    private final static double MOUSE_NOT_IN_POSITION_TOLLERANCE = 20.0;
    private final static int MOUSE_NOT_IN_POSITION_REPEATS = 2;

    private final Robot robot;
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

    private ButtonState buttonLeftPressed;
    private ButtonState buttonRightPressed;


    /**
     * @param listener     Listen to events caused by the robot mouse movement.
     * @param screenBounds The limits we can move the mouse
     */
    public RobotMouseThread(RobotMouseEventListener listener, Rectangle screenBounds) {
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(0);
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException awt) {
            throw new RobotMouseException("Mouse Robot creation failed:", awt);
        }
        this.listener = listener;
        this.screenBounds = screenBounds;
        this.stopMouse();
    }

    public final void stopMouse() {
        setSpeedX(0.0);
        setSpeedY(0.0);
        Point p = MouseInfo.getPointerInfo().getLocation();
        mouseX = p.x;
        mouseY = p.y;
        outOfPositionCounts = 0;
        buttonLeftPressed = ButtonState.IS_UP;
        buttonRightPressed = ButtonState.IS_UP;
    }

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
        robot.mouseMove((int) x, (int) y);
    }

    public static boolean roughlyNotEqual(double a, double b, double tollerance) {
        double diff = Math.abs(a - b);
        return (diff > Math.abs(tollerance + 0.000001d));
    }

    public final void moveMouseRel(double dx, double dy) {
        moveMouseAbs(mouseX + dx, mouseY + dy);
    }

    public void delay(int ms) {
        robot.delay(ms);
    }

    public void setSpeedY(double y) {
        this.hasSpeed = ((Math.abs(this.speedX) > 0.0001) || (Math.abs(y) > 0.0001));
        this.speedY = y;
    }

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

    /**
     * Stop the thread and any mouse movement
     */
    public void close() {
        stopMouse();
        canRun = false;
    }

    public boolean isLeftButtonPressed() {
        return buttonLeftPressed.equals(ButtonState.IS_DOWN);
    }

    public boolean isRightButtonPressed() {
        return buttonRightPressed.equals(ButtonState.IS_DOWN);
    }

    /**
     * Press the LEFT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state changes.
     */
    public void leftButtonPress() {
        switch (buttonLeftPressed) {
            case IS_UP:
            case UP:
                buttonLeftPressed = ButtonState.DOWN;
        }
    }

    /**
     * Release the LEFT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state changes.
     */
    public void leftButtonRelease() {
        switch (buttonLeftPressed) {
            case IS_DOWN:
            case DOWN:
                buttonLeftPressed = ButtonState.UP;
        }
    }

    /**
     * Press the RIGHT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state changes.
     */
    public void rightButtonPress() {
        switch (buttonRightPressed) {
            case IS_UP:
            case UP:
                buttonRightPressed = ButtonState.DOWN;
        }
    }

    /**
     * Release the RIGHT mouse button
     *
     * Note the actual call to the robot mouse api is done in the main thread (run method)
     *
     * ButtonState ensures that the call to the robot only occurs when the state changes.
     */
    public void rightButtonRelease() {
        switch (buttonRightPressed) {
            case IS_DOWN:
            case DOWN:
                buttonRightPressed = ButtonState.UP;
        }
    }

    /**
     * Type a character
     *
     * @param i the int value if the key to be typed
     */
    public void type(int i) {
        robot.delay(40);
        robot.keyPress(i);
        robot.keyRelease(i);
    }

    public void type(String s) {
        byte[] bytes = s.getBytes();
        for (byte b : bytes) {
            int code = b;
            // keycode only handles [A-Z] (which is ASCII decimal [65-90])
            if (code > 96 && code < 123) {
                code = code - 32;
            }
            robot.delay(40);
            robot.keyPress(code);
            robot.keyRelease(code);
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
            switch (buttonLeftPressed) {
                case UP:
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    buttonLeftPressed = ButtonState.IS_UP;
                    break;
                case DOWN:
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    buttonLeftPressed = ButtonState.IS_DOWN;
                    break;
            }
            /*
            Press or release the RIGHT button once when the it's state changes
            Same logic as LEFT button
             */
            switch (buttonRightPressed) {
                case UP:
                    robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                    buttonRightPressed = ButtonState.IS_UP;
                    break;
                case DOWN:
                    robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                    buttonRightPressed = ButtonState.IS_DOWN;
                    break;
            }

            if (canRun) {
                robot.delay(50);
            }
        }
    }
}
