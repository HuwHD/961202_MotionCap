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

/**
 * When a mouse event occurs the class with this interface will be caused.
 */
public interface RobotMouseEventListener {
    /**
     * When the mouse is not in the correct position control is taken from the 
     * mouse controller and passed to the user.
     * 
     * @param expected The expected position
     * @param actual The actual position
     * @param count The consecutive number of times it was out of position
     */
    void mouseNotInPosition(Point expected, Point actual, int count);
    /**
     * The mouse in connected. This means that control of the mouse is being 
     * driven by the sensor.
     */
    void connectedMouse();
    /**
     * The mouse in disconnected. This means that control of the mouse is being 
     * driven by the real mouse.
     */
    void disConnectedMouse();
}
