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
package serial;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author dev
 */
public class SerialMonitorThread extends Thread {

    private final SerialPortListener action;

    private final SerialPort serialPort;
    private final InputStream portInStream;
    private final String name;
    private boolean canRun = true;

    /**
     * Connect to the serial port
     * @param devicePort The name of the port
     * @param baud The speed of the port (baud rate)
     * @param action A listener for events that can occur
     * @param name The (human readable) name of the port.
     */
    public SerialMonitorThread(String devicePort, int baud, SerialPortListener action, String name) {
        this.name = name;
        try {
            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(devicePort).open("abc", 0);
        } catch (NoSuchPortException ex) {
            throw new SerialMonitorException("Port not found: port[" + devicePort + "] baud[" + baud + "] name[" + name + "]. Available ports are: "+getPortListAsString(), ex);
        } catch (PortInUseException ex) {
            throw new SerialMonitorException("Port is already open: port[" + devicePort + "] baud[" + baud + "] name[" + name + "]", ex);
        }
        try {
            serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new SerialMonitorException("Failed to configure port[" + devicePort + "] baud[" + baud + "] name[" + name + "]", ex);
        }
        try {
            portInStream = serialPort.getInputStream();
        } catch (IOException ex) {
            throw new SerialMonitorException("Failed connect to port[" + devicePort + "] baud[" + baud + "] name[" + name + "]", ex);
        }
        this.action = action;
    }

    public String getPortName() {
        return name;
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
        StringBuilder sb = new StringBuilder();
        try {
            int b = portInStream.read();
            while (canRun) {
                if (b == ':') {
                    if (action != null) {
                        /*
                        Beware if you throw an exception in his method the SerialMonitior thread will terminate
                         */
                        canRun = action.action(sb.toString());
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char) b);
                }
                if (canRun) {
                    b = portInStream.read();
                }
            }
        } catch (IOException io) {
            /*
            If an error occured here we cannot do a lot about it so
            we notify the action listener. Perhaps it can do somthing!
            This will kill the SerialMonitor!
             */
            if (action != null) {
                action.fail(io);
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
        }
    }

    /**
     * Return the list of available ports as a String so we can tell the user what is valid.
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

}
