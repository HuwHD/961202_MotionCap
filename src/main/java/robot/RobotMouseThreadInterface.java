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

/**
 *
 * @author Huw
 * 
 * This interface is used so that different implementation of the RobotMouseThread
 * can be used with the MouseController.
 * 
 * Currently there is only one RobotMouseThread.
 * 
 * The only other use of this interface in for testing the MouseController with a
 * Mock RobotMouseThread.
 * 
 * See: test/java/mouse/MouseControllerTest
 * 
 */
public interface RobotMouseThreadInterface {

    /**
     * Called when the MouseController is connected to the RobotMouseThread.
     * 
     * This sets a local flag and calls connectedMouse() on the listener.
     * 
     */
    void connect();

    /**
     * Called when the MouseController is dis-connected from the RobotMouseThread.
     * 
     * This sets a local flag and calls disConnectedMouse() on the listener.
     * 
     */
    void disConnect();

    /**
     * @return the local 'connected' flag.
     */
    boolean isConnected();

    /**
     * Press the LEFT mouse button
     */
    void leftButtonPress();

    /**
     * Release the LEFT mouse button
     */
    void leftButtonRelease();

    /**
     * Press the RIGHT mouse button
     */
    void rightButtonPress();

    /**
     * Release the RIGHT mouse button
    */
    void rightButtonRelease();

    /**
     * return the state of the left mouse button
    */
    boolean isLeftButtonPressed();

    /**
     * return the state of the right mouse button
    */
    boolean isRightButtonPressed();

    /**
     * Set the speed of the mouse in the X direction. 
     * negative values moves mouse left
     * positive moves mouse right.
     * 
     * @param x The speed 
     */
    void setSpeedX(double x);

    /**
     * Set the speed of the mouse in the Y direction. 
     * negative values moves mouse up
     * positive moves mouse down
     * 
     * @param y The speed 
     */
    void setSpeedY(double y);

    /**
     * Move the mouse to a point on the screen.
     * @param x The x value. Zero is the LHS of the screen
     * @param y The y value. Zero is the TOP of the screen
     */
    void moveMouseAbs(double x, double y);
    
}
