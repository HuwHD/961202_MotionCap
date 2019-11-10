/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author dev
 */
public class SerialTest {

    private SerialMonitor serialMonitor;

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
