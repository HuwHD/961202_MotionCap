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
    private static FXMLDocumentController controller;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLDocument.fxml"));
        Parent root = loader.load();
        /*
        Save a reference to the controller for later.
        This is so the serial monitor can pass action messages to it.
         */
        controller = loader.getController();
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
        if (serialMonitorThread != null) {
            serialMonitorThread.close();
        }
        if (robotMouseThread != null) {
            robotMouseThread.close();
        }
        if (mainStage != null) {
            Platform.exit();
        }
        System.exit(returnCode);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exitProgramWithHelp("Requires the name of a configuration data file. For example config.properties", null);
        }
        try {
            ConfigData.load(args[0]);
        } catch (ConfigException ce) {
            exitProgramWithHelp("Configuration data '" + args[0] + "' could not be loaded", ce);
        }

        robotMouseThread = new RobotMouseThread(new RobotMouseEventListener() {
            @Override
            public void mouseNotInPosition(Point expected, Point actual) {
                System.out.println("Mouse out of position");
            }
        }, getScreenRectangle(), 0);

        try {
            serialMonitorThread = new SerialMonitorThread(ConfigData.getDefaultPort(), ConfigData.getDefaultBaud(), new SerialPortListener() {
                @Override
                public boolean reading(Reading s) {
                    /*
                    The action MUST be in a JavaFX Thread so we must use runLater.
                     */
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                        /*
                        Set the label on the controller via the action method
                         */
                            if (controller != null) {
                                controller.action("Received:" + s.toString());
                            }
                        }
                    });
                    return true;
                }

                @Override
                public void fail(Exception s) {
                    exitProgramWithHelp("Serial port monitor failed. Program cannot continue.", s);
                }
            }, "Sensor Port");
        } catch (SerialMonitorException sme) {
            exitProgramWithHelp("Serial port monitor could not be started.", sme);
        }

        robotMouseThread.start();
        serialMonitorThread.start();
        launch(args);
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
     * This method returns the rectangle that is the effective application
     * window (inside the borders)
     * <p>
     * The values are screen coordinates not application coordinates
     *
     * @return a rectangle
     */
    public static Rectangle getScreenRectangle() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle(0,  0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

}
