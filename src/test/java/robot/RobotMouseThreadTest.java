package robot;


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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the mouse moves and button interaction
 * 
 * ----------------------------------------------------------------------------
 * NOTE This test will fail in a Linux environment that does not have a windows
 * manager. For example WSL on windows which is just Linux without a GUI
 * component or when using ssl. This is because it has no mouse to move around!
 *
 * Build on windows using cmd (Windows command line).
 *
 * gradlew.bat clean build.
 *
 * Throws an exception: Caused by: java.awt.AWTException: headless environment
 * at java.desktop/java.awt.Robot.<init>(Robot.java:94) at
 * robot.RobotMouseThread.<init>(RobotMouseThread.java:80)
 * ----------------------------------------------------------------------------
 */

public class RobotMouseThreadTest {

    @Test
    public void testCanMoveTheMouse() {
        RobotMouseEventListener listener = createListener();
        RobotMouseThread robotMouseThread = new RobotMouseThread(listener, new Rectangle(0, 0, 1000, 1000));
        robotMouseThread.start();
        robotMouseThread.connect();

        robotMouseThread.moveMouseAbs(0, 0);
        delay(100); // Thread loops every 30ms so give it time!
        Point p2 = MouseInfo.getPointerInfo().getLocation();
        robotMouseThread.moveMouseAbs(p2.x, p2.y);
        delay(100); // Thread loops every 30ms so give it time!
        Point p3 = MouseInfo.getPointerInfo().getLocation();
        assertEquals(p2, p3);

        robotMouseThread.moveMouseAbs(50, 50);
        delay(100); // Thread loops every 30ms so give it time!
        p3 = MouseInfo.getPointerInfo().getLocation();
        assertEquals(50, p3.x);
        assertEquals(50, p3.y);

        robotMouseThread.moveMouseAbs(-50, 50);
        delay(100); // Thread loops every 30ms so give it time!
        p3 = MouseInfo.getPointerInfo().getLocation();
        assertEquals(0, p3.x);
        assertEquals(50, p3.y);

        robotMouseThread.moveMouseAbs(50, -50);
        delay(100); // Thread loops every 30ms so give it time!
        p3 = MouseInfo.getPointerInfo().getLocation();
        assertEquals(50, p3.x);
        assertEquals(0, p3.y);

        robotMouseThread.moveMouseAbs(1010, 1010);
        delay(100); // Thread loops every 30ms so give it time!
        p3 = MouseInfo.getPointerInfo().getLocation();
        assertEquals(1000, p3.x);
        assertEquals(1000, p3.y);

        robotMouseThread.disConnect();
        robotMouseThread.close();
        delay(100); // Thread loops every 30ms so give it time!
    }

    @Test
    public void testLeftAndRightKeyPressed() {
        RobotMouseEventListener listener = createListener();
        RobotMouseThread robotMouseThread = new RobotMouseThread(listener, new Rectangle(0, 0, 1000, 1000));
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        /*
        Check button cannot be pressed if not running. Only goes to DOWN not IS_DOWN
         */
        robotMouseThread.leftButtonPress(); // request left press
        delay(100); // Thread loops every 30ms so give it time!
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.REQUEST_DOWN)); // press requested
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP)); // no press requested

        /*
        Check button cannot be pressed if not running. Only goes to DOWN nor IS_DOWN
         */
        robotMouseThread.rightButtonPress(); // request right press
        delay(100); // Thread loops every 30ms so give it time!
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.REQUEST_DOWN)); // press requested
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.REQUEST_DOWN)); // no press requested

        try {
            /*
            Start the thread running. Button state should not change to IS_DOWN untill connected
             */
            robotMouseThread.start(); // Starting thread should not change anything
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.REQUEST_DOWN));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.REQUEST_DOWN));

            /*
            Connected. Button state should now change to IS_DOWN
             */
            robotMouseThread.connect();  // Will make the thread press the buttons
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_DOWN));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_DOWN));

            /*
            Connected. Left Button state should now change to IS_UP
             */
            robotMouseThread.leftButtonRelease();// request left release
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_DOWN));
            /*
            Connected. Right Button state should now change to IS_UP
             */
            robotMouseThread.rightButtonRelease();// request left release
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));

            robotMouseThread.leftButtonPress(); // request left press
            robotMouseThread.rightButtonPress(); // request right press
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_DOWN));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_DOWN));

            robotMouseThread.disConnect(); // Test it will release the buttons on 
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
            robotMouseThread.close();
            delay(100); // Thread loops every 30ms so give it time!
            assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
            assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
            assertEquals("connectedMouse:disConnectedMouse:", listener.toString());
        } finally {
            /*
            If you dont do this the mouse button can be stuck down if a test fails!
             */
            robotMouseThread.close();
            delay(100);
        }
    }

    /**
     * Test that through a life cycle the state is correct.
     *
     * No mouse or button changes.
     */
    @Test
    public void testConnectDisConnect() {
        RobotMouseEventListener listener = createListener();
        RobotMouseThread robotMouseThread = new RobotMouseThread(listener, new Rectangle(0, 0, 1000, 1000));
        assertFalse(robotMouseThread.isConnected());
        assertFalse(robotMouseThread.isAlive());

        robotMouseThread.start();
        delay(100); // Thread loops every 30ms so give it time!  
        assertFalse(robotMouseThread.isConnected());
        assertTrue(robotMouseThread.isAlive());
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));

        robotMouseThread.connect();
        assertTrue(robotMouseThread.isConnected());
        assertTrue(robotMouseThread.isAlive());
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        delay(100); // Thread loops every 30ms so give it time!

        robotMouseThread.disConnect();
        delay(100); // Thread loops every 30ms so give it time!        
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertFalse(robotMouseThread.isConnected());
        assertTrue(robotMouseThread.isAlive());

        robotMouseThread.close();
        delay(100); // Thread loops every 30ms so give it time!        
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertFalse(robotMouseThread.isConnected());
        assertFalse(robotMouseThread.isAlive());
        assertEquals("connectedMouse:disConnectedMouse:", listener.toString());
    }

    @Test
    public void testNoActivity() {
        RobotMouseEventListener listener = createListener();
        RobotMouseThread robotMouseThread = new RobotMouseThread(listener, new Rectangle(0, 0, 1000, 1000));
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        robotMouseThread.start();
        delay(100); // Thread loops every 30ms so give it time!

        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertFalse(robotMouseThread.isConnected());
        assertTrue(robotMouseThread.isAlive());
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));

        robotMouseThread.close();
        delay(100); // Thread loops every 30ms so give it time!
        assertTrue(robotMouseThread.getButtonLeftState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertTrue(robotMouseThread.getButtonRightState().equals(RobotMouseThread.ButtonState.IS_UP));
        assertFalse(robotMouseThread.isAlive());
        assertFalse(robotMouseThread.isConnected());

        assertEquals("", listener.toString());
        System.out.println(listener.toString());
    }

    private RobotMouseEventListener createListener() {

        return new RobotMouseEventListener() {
            StringBuilder log = new StringBuilder();

            @Override
            public String toString() {
                return log.toString();
            }

            @Override
            public void mouseNotInPosition(Point expected, Point actual, int count) {
                log.append("mouseNotInPosition:");
            }

            @Override
            public void connectedMouse() {
                log.append("connectedMouse:");

            }

            @Override
            public void disConnectedMouse() {
                log.append("disConnectedMouse:");
            }
        };
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace(); // This is OK as it is VERY unlikely to be inturrnpted and we dont really care!
        }
    }

}
