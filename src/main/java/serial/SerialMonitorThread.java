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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import purejavacomm.*;

public class SerialMonitorThread extends Thread {

    private final SerialPortListener serialPortListener;

    private final SerialPort serialPort;
    private final InputStream portInStream;
    private final String deviceName;
    private final String devicePort;
    private final int deviceBaud;
    private boolean canRun = true;
    private boolean running = false;
    private boolean swapLR = false;
    private boolean debug = false;

    /**
     * Connect to the serial port
     *
     * @param devicePort The name of the port
     * @param deviceBaud The speed of the port (baud rate)
     * @param serialPortListener A listener for events that can occur
     * @param deviceName The (human readable) name of the port.
     * @throws serial.SerialMonitorException when connection fails.
     */
    public SerialMonitorThread(String devicePort, int deviceBaud, SerialPortListener serialPortListener, String deviceName, boolean swapLR, boolean debug) throws SerialMonitorException {
        this.debug = debug;
        this.swapLR = swapLR;
        this.deviceName = deviceName;
        this.devicePort = devicePort;
        this.deviceBaud = deviceBaud;
        try {
            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(devicePort).open("abc", 0);
        } catch (NoSuchPortException ex) {
            throw new SerialMonitorException("Port not found: port[" + devicePort + "] baud[" + deviceBaud + "] name[" + deviceName + "]. Available ports are: " + getPortListAsString(), ex);
        } catch (PortInUseException ex) {
            throw new SerialMonitorException("Port is already open: port[" + devicePort + "] baud[" + deviceBaud + "] name[" + deviceName + "]", ex);
        }
        try {
            serialPort.setSerialPortParams(deviceBaud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new SerialMonitorException("Failed to configure port[" + devicePort + "] baud[" + deviceBaud + "] name[" + deviceName + "]", ex);
        }
        try {
            portInStream = serialPort.getInputStream();
        } catch (IOException ex) {
            throw new SerialMonitorException("Failed connect to port[" + devicePort + "] baud[" + deviceBaud + "] name[" + deviceName + "]", ex);
        }
        this.serialPortListener = serialPortListener;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDevicePort() {
        return devicePort;
    }

    public int getDeviceBaud() {
        return deviceBaud;
    }

    /**
     * Stop the thread running
     */
    public void close() {
        /*
        Ensure the thread exits
         */
        canRun = false;
    }

    @Override
    public void run() {
        if (debug) {
            System.out.println("DEBUG SENSOR DATA");
        }
        running = true;
        if (serialPortListener != null) {
            serialPortListener.connected(getDevicePort(), getDeviceBaud(), getDeviceName());
        }
        StringBuilder sb = new StringBuilder();
        try {
            int b = portInStream.read();
            while (canRun) {
                if (debug) {
                    System.out.print((char)b);;
                }
                if (b == ':') {
                    if (debug) {
                        System.out.println();;
                    }
                    /*
                    Beware if you throw an exception in his method the SerialMonitior thread will terminate
                     */
                    if (serialPortListener != null) {
                        String data = sb.toString();
                        /*
                        If raw data returns true then we are done. Dont call reading
                         */
                        if (!serialPortListener.rawData(data)) {
                            /*
                            Parse the data and call reading
                             */
                            try {
                                Reading reading = Reading.parse(data,swapLR);
                                if (reading != null) {
                                    serialPortListener.reading(reading);
                                }
                            } catch (Exception e) {
                                if (debug) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                    sb.setLength(0);
                } else {
                    sb.append((char) b);
                }
                if (canRun) {
                    b = portInStream.read();
                }
            }
        } catch (Exception io) {
            io.printStackTrace();
            /*
            If an error occured here we cannot do a lot about it so
            we notify the action listener. Perhaps it can do somthing!
            This will kill the SerialMonitor!
             */
            if (serialPortListener != null) {
                serialPortListener.fail(io);
            } else {
                io.printStackTrace();
            }
        } finally {
            /*
            Ensure serial port is freed!
             */
            if (serialPort != null) {
                serialPort.close();
            }
            running = false;
            if (serialPortListener != null) {
                serialPortListener.disConnected(getDevicePort(), getDeviceName());
            }

        }
    }

    /**
     * Return the list of available ports as a String so we can tell the user
     * what is valid.
     *
     * Mark is used to remove the last comma.
     *
     * @return A CSV list of available ports
     */
    public static String getPortListAsString() {
        StringBuilder sb = new StringBuilder();
        int mark = 0;
        for (String s : getPortList()) {
            sb.append(s);
            mark = sb.length();
            sb.append(',');
        }
        sb.setLength(mark);
        return sb.toString();
    }

    /**
     * Use purejavacomm to Find a list of all available ports;
     *
     * @return A list of ports
     */
    public static List<String> getPortList() {
        List<String> portList = new ArrayList<>();
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) e.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portList.add(portId.getName());
            }
        }
        return portList;
    }

    public boolean swapLR() {
        this.swapLR = !swapLR;
        return this.swapLR;
    }
    
    public boolean isRunning() {
        return running;
    }

}
