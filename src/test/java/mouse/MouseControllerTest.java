package mouse;

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
import org.junit.Test;
import static org.junit.Assert.*;
import robot.RobotMouseThreadInterface;
import serial.Reading;

/**
 *
 * Test the MouseController behaviour.
 * 
 * Create a MOCK RobotMouseThread from the RobotMouseThreadInterface.
 * This logs calls made to it in a StringBuilder. 
 * 
 * The log is then used to assert that the correct operations were called 
 * on the RobotMouseThread.
 * 
 * @author Huw
 */
public class MouseControllerTest {

    @Test
    public void testConnectedMouse_outsideActiveZone() {
        RobotMouseThreadInterface robot = createMockRobotMouseThread();
        MouseController mc = new MouseController(robot, new long[]{0, 10, 20}, 1, new long[]{0, 20, 30}, 1);
        mc.connectTheMouse();
        assertTrue(mc.isConnectedToMouse());
        mc.reading(Reading.parse("0,-35,-25,1,0,1,1:", true, true));
        mc.disConnectTheMouse();
        assertFalse(mc.isConnectedToMouse());

        System.out.println(robot.toString());
        assertFalse(robot.toString().contains("setSpeed"));  // Set speed should not be called
        assertTrue(robot.toString().contains("leftButtonRelease"));
        assertTrue(robot.toString().contains("rightButtonPress"));
        assertTrue(robot.toString().contains("connect"));
        assertTrue(robot.toString().contains("disConnect"));
    }

    @Test
    public void testConnectedMouse_inNullZone() {
        RobotMouseThreadInterface robot = createMockRobotMouseThread();
        MouseController mc = new MouseController(robot, new long[]{0, 10, 20}, 1, new long[]{0, 20, 30}, 1);
        mc.connectTheMouse();
        assertTrue(mc.isConnectedToMouse());
        mc.reading(Reading.parse("0,0,0,1,1,1,1:", true, true));
        mc.disConnectTheMouse();
        assertFalse(mc.isConnectedToMouse());

        System.out.println(robot.toString());
        assertTrue(robot.toString().contains("setSpeedX(0.0)"));
        assertTrue(robot.toString().contains("setSpeedY(0.0)"));
        assertTrue(robot.toString().contains("leftButtonPress"));
        assertTrue(robot.toString().contains("rightButtonPress"));
        assertTrue(robot.toString().contains("connect"));
        assertTrue(robot.toString().contains("disConnect"));
    }
    
    @Test
    public void testConnectedMouse_inActiveZoneWithSpeed() {
        RobotMouseThreadInterface robot = createMockRobotMouseThread();
        MouseController mc = new MouseController(robot, new long[]{0, 10, 20}, 0.5, new long[]{0, 20, 30}, 2);
        mc.connectTheMouse();
        assertTrue(mc.isConnectedToMouse());
        mc.reading(Reading.parse("0,25,15,0,0,1,1:", true, true));
        mc.disConnectTheMouse();
        assertFalse(mc.isConnectedToMouse());

        System.out.println(robot.toString());
        assertTrue(robot.toString().contains("setSpeedX(7.5)"));  // Speed should be halved
        assertTrue(robot.toString().contains("setSpeedY(50.0)")); // Speed should be doubled
        assertTrue(robot.toString().contains("leftButtonRelease"));
        assertTrue(robot.toString().contains("rightButtonRelease"));
        assertTrue(robot.toString().contains("connect"));
        assertTrue(robot.toString().contains("disConnect"));
    }

    @Test
    public void testConnectedMouse_inActiveZone() {
        RobotMouseThreadInterface robot = createMockRobotMouseThread();
        MouseController mc = new MouseController(robot, new long[]{0, 10, 20}, 1, new long[]{0, 20, 30}, 1);
        mc.connectTheMouse();
        assertTrue(mc.isConnectedToMouse());
        mc.reading(Reading.parse("0,25,15,0,1,1,1:", true, true));
        mc.disConnectTheMouse();
        assertFalse(mc.isConnectedToMouse());

        System.out.println(robot.toString());
        assertTrue(robot.toString().contains("setSpeedX(15.0)"));
        assertTrue(robot.toString().contains("setSpeedY(25.0)"));
        assertTrue(robot.toString().contains("leftButtonPress"));
        assertTrue(robot.toString().contains("rightButtonRelease"));
        assertTrue(robot.toString().contains("connect"));
        assertTrue(robot.toString().contains("disConnect"));
    }

    @Test
    public void testDisconnectedMouse() {
        RobotMouseThreadInterface robot = createMockRobotMouseThread();
        MouseController mc = new MouseController(robot, new long[]{0, 10, 20}, 1, new long[]{0, 20, 30}, 1);
        mc.reading(Reading.parse("0,0,0,1,1,1,1:", true, true));
        System.out.println(robot.toString());
        assertFalse(robot.toString().contains("setSpeed"));
        assertFalse(robot.toString().contains("Button"));
    }

    /**
     * Create a MOCK RobotMouseThread to log calls
     * @return A MOCK RobotMouseThread
     */
    private RobotMouseThreadInterface createMockRobotMouseThread() {
        return new RobotMouseThreadInterface() {
            private StringBuilder log = new StringBuilder();
            private boolean connected = false;
            private boolean leftButtonPress = false;
            private boolean rightButtonPress = false;

            @Override
            public String toString() {
                return log.toString();
            }

            @Override
            public void connect() {
                connected = true;
                log.append("connect:");
            }

            @Override
            public void setSpeedX(double x) {
                log.append("setSpeedX(").append(x).append("):");
            }

            @Override
            public void setSpeedY(double y) {
                log.append("setSpeedY(").append(y).append("):");
            }

            @Override
            public void disConnect() {
                connected = false;
                log.append("disConnect:");
            }

            @Override
            public boolean isConnected() {
                return connected;
            }

            @Override
            public void leftButtonPress() {
                leftButtonPress = true;
                log.append("leftButtonPress:");
            }

            @Override
            public void leftButtonRelease() {
                leftButtonPress = false;
                log.append("leftButtonRelease:");
            }

            @Override
            public void rightButtonPress() {
                rightButtonPress = true;
                log.append("rightButtonPress:");
            }

            @Override
            public void rightButtonRelease() {
                rightButtonPress = false;
                log.append("rightButtonRelease:");
            }

            @Override
            public boolean isLeftButtonPressed() {
                return leftButtonPress;
            }

            @Override
            public boolean isRightButtonPressed() {
                return rightButtonPress;
            }

            @Override
            public void moveMouseAbs(double x, double y) {
                log.append("moveMouseAbs(").append(y).append(',').append(y).append("):");
            }
        };
    }

}
