/*
 * Copyright (C) 2019 Huw Hudson-Davies (961202)
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

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class PocRobot {

    private Robot robot;

    /**
     * Create a robot class and set some default values
     *
     * @throws AWTException
     */
    public void init() throws AWTException {
        robot = new Robot();
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);
    }

    /**
     * Move the mouse to an absolute position of the screen
     *
     * @param x The x position
     * @param y The y position
     */
    public void moveMouseAbs(int x, int y) {
        robot.mouseMove(x, y);
    }

    /**
     * Move the mouse to an absolute position of the screen
     *
     * @param p The point on the screen
     */
    public void moveMouseAbs(Point p) {
        robot.mouseMove(p.x, p.y);
    }

    /**
     * Move the mouse relative to it's current position
     *
     * @param x The amount in the x direction +x to the right -x to the left
     * @param y The amount in the y direction +y down -y up
     */
    public void moveMouseRel(int x, int y) {
        Point p = getMousePos();
        robot.mouseMove(p.x + x, p.y + y);
    }

    /**
     * Return the screen position of the mouse
     *
     * @return The Point on screen
     */
    public Point getMousePos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * Delay in microseconds
     *
     * @param ms the delay
     */
    public void delay(int ms) {
        robot.delay(ms);
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
     * @param i 
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
     */
    /**
     * Move the mouse along a straight line between two points
     *
     * Original Code From -
     * https://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
     *
     * @param delay - A short delay after each move. High value moves slower
     * @param x1 - The from x
     * @param y1 - The from y
     * @param x2 - The to x
     * @param y2 - The to y
     */
    public void moveLine(int delay, int x1, int y1, int x2, int y2) {
        // delta of exact value and rounded value of the dependent variable
        int d = 0;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point

        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        int x = x1;
        int y = y1;

        if (dx >= dy) {
            while (true) {
                /*
                Move the mouse and delay
                 */
                moveMouseAbs(x, y);
                if (delay > 0) {
                    delay(delay);
                }
                if (x == x2) {
                    break;
                }
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                moveMouseAbs(x, y);
                if (delay > 0) {
                    delay(delay);
                }
                if (y == y2) {
                    break;
                }
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }

}
