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
package mouse;

import robot.RobotMouseThread;
import serial.Reading;
import serial.SerialPortListener;

public class MouseController implements SerialPortListener {
    private final RobotMouseThread robotMouseThread;
    
    public MouseController(RobotMouseThread robotMouseThread) {
        this.robotMouseThread = robotMouseThread;
    }

    @Override
    public void reading(Reading s) {

    }

    @Override
    public void fail(Exception e) {
        robotMouseThread.stopMouse();
    }

    @Override
    public void connected(String devicePort, int baud, String name) {
        robotMouseThread.stopMouse();
    }

}
