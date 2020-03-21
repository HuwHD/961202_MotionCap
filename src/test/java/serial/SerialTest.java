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
package serial;

/**
 *
 * @author dev
 */
public class SerialTest {

    private SerialMonitorThread serialMonitorThread;

//    @Test
//    public void serialTest() throws InterruptedException {
//        serialMonitor = new SerialMonitor("/dev/ttyACM0", 115200, new SerialPortAction() {
//            @Override
//            public boolean action(String s) {
//                return true;
//            }
//
//            @Override
//            public void close(String s) {
//            }
//
//            @Override
//            public void start(String s) {
//            }
//        }, "ttyACM0");
//        serialMonitor.start();
//        try {
//            System.out.println("START");
//            sm.start();
//            Thread.sleep(5000);
//        } finally {
//            System.out.println("CLOSE");
//            sm.close();
//        }
//    }
}
