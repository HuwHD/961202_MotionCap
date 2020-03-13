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
package robot;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Move the mouse in the background.
 */
public class RobotMouseThread extends Thread {

    private final Robot robot;
    private final RobotMouseEventListener listener;
    private final int moveDelay;
    private boolean canRun = true;
    /*
    Doubles used to keep track of mouse position.
    Actual mouse pos is integer. This means we would loose precision if we 
        read the mouse position then move 1/2 a pixel.
     */
    private double speedX;
    private double speedY;
    private double mouseX;
    private double mouseY;
    private boolean hasSpeed = false;

    private Rectangle screenBounds;

    /**
     * @param listener     Listen to events caused by the robot mouse movement.
     * @param screenBounds The limits we can move the mouse
     * @param moveDelay    To slow things down if required.
     */
    public RobotMouseThread(RobotMouseEventListener listener, Rectangle screenBounds, int moveDelay) {
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(0);
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException awt) {
            throw new RobotMouseException("Mouse Robot creation failed:", awt);
        }
        this.listener = listener;
        this.screenBounds = screenBounds;
        this.moveDelay = moveDelay;
        this.stopMouse();
    }

    public final void stopMouse() {
        setSpeed(0.0, 0.0);
        Point p = MouseInfo.getPointerInfo().getLocation();
        mouseX = p.x;
        mouseY = p.y;
    }

    protected final void moveMouseAbs(double x, double y) {
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

        if ((p.getX() != x) || (p.getY() != y)) {
            if (listener != null) {
                listener.mouseNotInPosition(new Point((int) x, (int) y), p);
            }
        }
        mouseX = x;
        mouseY = y;
        robot.mouseMove((int) x, (int) y);
    }

    protected final void moveMouseRel(double dx, double dy) {
        moveMouseAbs(mouseX + dx, mouseY + dy);
    }

    public void delay(int ms) {
        robot.delay(ms);
    }

    protected void setSpeed(double x, double y) {
        this.hasSpeed = ((Math.abs(x) > 0.0001) || (Math.abs(y) > 0.0001));
        this.speedX = x;
        this.speedY = y;
    }

    protected double getSpeedY() {
        return speedY;
    }

    protected double getSpeedX() {
        return speedX;
    }

    /**
     * Stop the thread and any mouse movement
     */
    public void close() {
        stopMouse();
        canRun = false;
    }

    /**
     * Click and Release the LEFT mouse button
     */
    public void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Click and Release the RIGHT mouse button
     */
    public void rightClick() {
        robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
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
     * Sleep until we have a destination
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
            if (canRun) {
                robot.delay(50);
            }
        }
    }

}
