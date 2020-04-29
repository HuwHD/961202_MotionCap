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
package main;

import config.ConfigData;
import config.ConfigException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mouse.MouseController;
import robot.RobotMouseEventListener;
import robot.RobotMouseThread;
import serial.Reading;
import serial.SerialMonitorException;
import serial.SerialMonitorThread;
import serial.SerialPortListener;

import java.awt.*;

public class Main extends Application {

    private static Stage mainStage;
    private static Scene mainScene;
    private static FXMLDocumentController guiController;
    private static MouseController mouseController;

    private static SerialMonitorThread serialMonitorThread;
    private static RobotMouseThread robotMouseThread;

    /**
     * Start the application.
     * <p>
     * We store stage and scene for later so we can get size and position data
     * from them.
     *
     * @param stage The Stage generated by JavaFX
     * @throws Exception if any thing goes bad!
     */
    @Override
    public void start(Stage stage) throws Exception {
        /*
        Save the reference to the stage
         */
        mainStage = stage;
        /*
        Set what happens when an 'application close' event is triggered.
         */
        mainStage.setOnCloseRequest(new EventHandler<>() {
            @Override
            public void handle(WindowEvent event) {
                /*
                Clean up and exit the application
                 */
                closeApplication(0);
            }
        });
        /*
        Use the loader to load the window controls
         */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLMotionCap.fxml"));
        Parent root = loader.load();
        /*
        Save a reference to the controller for later.
        This is so the serial monitor can pass action messages to it.
         */
        guiController = loader.getController();
        /*
        Save a reference to the scene for later.
         */
        mainScene = new Scene(root);

        stage.setScene(mainScene);
        stage.show();
    }

    /**
     * Close the application.
     * <p>
     * The serial port monitor and robot mouse should be closed properly.
     * <p>
     * Then the Platform (JavaFX) must be told to exit
     * <p>
     * Then we terminate the Java VM with a specific return code
     */
    public static void closeApplication(int returnCode) {
        disConnectSensor();
        if (robotMouseThread != null) {
            robotMouseThread.close();
        }
        if (mainStage != null) {
            Platform.exit();
        }
        System.exit(returnCode);
    }

    /**
     * Disconnect the sensor thread.
     *
     * Set the ref to null ASAP to indicate disconnect. Then stop the thread.
     *
     */
    public static void disConnectSensor() {
        if (serialMonitorThread != null) {
            SerialMonitorThread temp = serialMonitorThread;
            serialMonitorThread = null;
            temp.close();
            while (temp.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // dont care if interrupted
                }
            }
        }
    }

    public static boolean isConnectedToSensor() {
        if (serialMonitorThread != null) {
            return serialMonitorThread.isRunning();
        }
        return false;
    }

    public static boolean isConnectedToMouse() {
        if (mouseController != null) {
            return mouseController.isConnectedToMouse();
        }
        return false;
    }

    /**
     * Connect to the sensor.
     * <p>
     * If configuration data ConfigData.CONNECT_ON_LOAD is true then this is
     * called by the Main class. If configuration data
     * ConfigData.CONNECT_ON_LOAD is false then this is called by the GUI
     * Controller class when the connect button is pressed.
     * <p>
     * It forwards messages to the GUI controller if it has been set up. It
     * forwards messages to the Mouse controller if it has been set up.
     */
    public static void connectSensor(String port) {
        /*
        Start the serial port reader thread and add a listener for any events
         */
        try {
            serialMonitorThread = new SerialMonitorThread(port, ConfigData.getDefaultBaud(), new SerialPortListener() {
                @Override
                public void reading(Reading reading) {
                    /*
                    The reading is passed to thw controller if one exists.
                     */
                    if (guiController != null) {
                        guiController.reading(reading);
                    }
                    if (mouseController != null) {
                        mouseController.reading(reading);
                    }
                }

                @Override
                public void fail(Exception s) {
                    exitProgramWithHelp("Serial port monitor failed. Program cannot continue.", s);
                }

                @Override
                public void connectedSensor(String devicePort, int baud, String name) {
                    if (guiController != null) {
                        guiController.connectedSensor(devicePort, baud, name);
                    }
                    if (mouseController != null) {
                        mouseController.connectedSensor(devicePort, baud, name);
                    }
                    ConfigData.set(ConfigData.DEFAULT_PORT, devicePort);
                }

                @Override
                public void disConnectedSensor(String devicePort, String name) {
                    if (guiController != null) {
                        guiController.disConnectedSensor(devicePort, name);
                    }
                    if (mouseController != null) {
                        mouseController.disConnectedSensor(devicePort, name);
                    }
                }

                @Override
                public boolean rawData(String s) {
                    return false;
                }

            }, ConfigData.getValue(ConfigData.SENSOR_NAME, "Sensor"),
                    ConfigData.getBoolean(ConfigData.CALIB_SWAP_LR, false),
                    ConfigData.getBoolean(ConfigData.DEBUG_SENSOR_DATA, false));
        } catch (SerialMonitorException sme) {
            /*
            If the GUI is running just display the error message
             */
            if (guiController != null) {
                guiController.fail(sme);
            } else {
                /*
                Otherwise we need to exit!
                 */
                throw new SerialMonitorException("Could not connect to Serial port '" + port + "'");
            }
        }
        if (serialMonitorThread != null) {
            serialMonitorThread.start();
        }
    }

    public static void initMouseController() {
        mouseController = new MouseController(robotMouseThread,
                ConfigData.getLongs(ConfigData.CALIB_HEADING_DATA, 3),
                ConfigData.getLongs(ConfigData.CALIB_VERTICAL_DATA, 3)
        );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exitProgramWithHelp("Requires the name of a configuration data file. For example config.properties", null);
        }
        /*
        Load the configuration file.
         */
        try {
            ConfigData.load(args[0]);
        } catch (ConfigException ce) {
            exitProgramWithHelp("Configuration data '" + args[0] + "' could not be loaded", ce);
        }

        if (ConfigData.getBoolean(ConfigData.CONNECT_ON_LOAD, true)) {
            try {
                connectSensor(ConfigData.getDefaultPort());
            } catch (SerialMonitorException e) {
                if (ConfigData.getBoolean(ConfigData.LAUNCH_GUI, true)) {
                    e.printStackTrace();
                } else {
                    exitProgramWithHelp("Failed to connect to Sensor", e);
                }
            }
        }
        /*
        Start the robot mouse thread and ad a listener for any events
         */
        robotMouseThread = new RobotMouseThread(new RobotMouseEventListener() {
            @Override
            public void mouseNotInPosition(Point expected, Point actual, int count) {
                if (mouseController != null) {
                    mouseController.disConnectTheMouse();
                }
                if (guiController != null) {
                    guiController.mouseNotInPosition(expected, actual, count);
                } else {
                    for (int i = 5; i > 0; i--) {
                        System.out.println("Mouse is out of position. Restarting in [" + i + "]");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mouseController.connectTheMouse();
                }
            }

            @Override
            public void connectedMouse() {
                if (guiController != null) {
                    guiController.connectedMouse();
                }
            }

            @Override
            public void disConnectedMouse() {
                if (guiController != null) {
                    guiController.disConnectedMouse();
                }

            }
        }, getScreenRectangle());

        robotMouseThread.start();

        /*
        Create the mouse controller.. This receives messages from the Sensor 
        an decides what to do with the mouse via the robotMouseThread
         */
        try {
            initMouseController();
        } catch (ConfigException ce) {
            exitProgramWithHelp("Configuration data '" + args[0] + "' could not be loaded", ce);
        }

        if (ConfigData.getBoolean(ConfigData.LAUNCH_GUI, true)) {
            launch(args);
        } else {
            if (serialMonitorThread == null) {
                exitProgramWithHelp("If you dont load the GUI (" + ConfigData.LAUNCH_GUI + "=false) you MUST start the SerialMonitor on Load (" + ConfigData.CONNECT_ON_LOAD + "=true)", null);
            }
            /*
            If running without GUI then start the mouse moving.
             */
            if (mouseController != null) {
                mouseController.connectTheMouse();
            }
            /*
            Not running with the GUI so wait for the sensor thread to stop. 
             */
            System.out.println("Waiting for Sensor monitor to terminate");
            /*
            Keep checking the sensor thread. Sleep releases the thread so other 
            processes can continue.
             */
            while (serialMonitorThread.isRunning()) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException ex) {
                    // Do Nothing. Just keep waiting.
                }
            }
            /*
            Close the application properly!
             */
            closeApplication(0);
        }
    }

    private static void exitProgramWithHelp(String message, Throwable ex) {
        System.err.println("Program terminated!\n    Details: " + message);
        if (ex != null) {
            System.err.println("    Reason: " + ex.getMessage());
            System.err.println("    Full Stack Trace follows:");
            ex.printStackTrace(System.err);
        }
        closeApplication(1);
    }

    /**
     * This method returns the rectangle that is the effective screen size.
     *
     * @return a rectangle
     */
    public static Rectangle getScreenRectangle() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

    /*
    Here so that the controller can get details of the Stage
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /*
   Here so that the controller can get details of the Scene
     */
    public static Scene getMainScene() {
        return mainScene;
    }

    public static MouseController getMouseController() {
        return mouseController;
    }

    public static SerialMonitorThread getSerialMonitorThread() {
        return serialMonitorThread;
    }

    public static RobotMouseThread getRobotMouseThread() {
        return robotMouseThread;
    }

}
