/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 *
 * @author dev
 */
public class SerialMonitor extends Thread {

    private final SerialPortAction action;

    private SerialPort serialPort = null;
    private InputStream portInStream = null;
    private boolean canRun = true;
    private String name;

    public SerialMonitor(String devicePort, int baud, SerialPortAction action, String name) {
        this.name = name;
        try {
            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(devicePort).open("abc", 0);
        } catch (NoSuchPortException ex) {
            throw new SerialMonitorException("Port not found: port[" + devicePort + "] baud[" + baud + "] name[" + name + "]", ex);
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

    public void close() {
        /*
        Ensure the thread exits
         */
        canRun = false;
        /*
        If the thread is waiting for a character is will block and never test canRun!
        
        If we force the serialPort to close this interupts the wait and the thread exits normally.
         */
        if (serialPort != null) {
            serialPort.close();
        }
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
}
