package main;

import config.ConfigData;
import serial.Reading;
import serial.SerialMonitorException;
import serial.SerialMonitorThread;
import serial.SerialPortListener;

/**
 * This class is just used to test the data coming from the sensor.
 * <p>
 * This is used to help understand the data and helped when trying to use the microbit radio.
 */
public class ListenToDevice {
    private static boolean canRun;
    private static SerialMonitorThread serialMonitorThread;

    public static void main(String[] args) {
        ConfigData.load("config.properties");

        canRun = true;
        final String port = ConfigData.getDefaultPort();
        try {
            serialMonitorThread = new SerialMonitorThread(port, ConfigData.getDefaultBaud(), new SerialPortListener() {
                @Override
                public boolean rawData(String s) {
                    System.out.println(port + ": " + s);
                    if ((s.endsWith("1,0")) || (s.endsWith("1,1"))){
                        canRun = false;
                    }
                    return true;
                }

                @Override
                public void reading(Reading reading) {
                }

                @Override
                public void fail(Exception e) {
                     e.printStackTrace();
                    canRun = false;
                }

                @Override
                public void connected(String devicePort, int baud, String name) {
                    System.out.println(port + ": " + "Connected:");
                }

                @Override
                public void disConnected(String devicePort, String name) {
                    System.out.println(port + ": " + "Dis-Connected:");
                    canRun = false;
                }
            }, ConfigData.getValue(ConfigData.SENSOR_NAME, "Sensor"));
        } catch (SerialMonitorException sme) {
            sme.printStackTrace();
            canRun = false;
        }

        /*
        Start the serial port monitor
         */
        serialMonitorThread.start();
        /*
        Wait forever if we can run!
         */
        try {
            while (canRun) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            /*
            Kill the thread.
             */
            serialMonitorThread.close();
        }

    }

    private static void test() {

    }
}
