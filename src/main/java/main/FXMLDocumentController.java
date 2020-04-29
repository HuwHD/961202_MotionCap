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
import java.awt.Point;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import serial.Reading;
import serial.SerialMonitorThread;
import serial.SerialPortListener;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import robot.RobotMouseEventListener;
/**
 * @author huw
 * 
 * Note on event handling in the UI.
 * 
 * Long-running operations must not be run on the JavaFX application thread, 
 * since this prevents JavaFX from updating the UI, resulting in a frozen UI.
 * 
 * Furthermore any change to a Node that is part of a "live" scene graph must 
 * happen on the JavaFX application thread. <b>Platform.runLater</b> can be used to 
 * execute those updates on the JavaFX application thread.
 * 
 * @See: <a href="https://riptutorial.com/javafx/example/7291/updating-the-ui-using-platform-runlater"/>
 * 
 */
public class FXMLDocumentController implements Initializable, SerialPortListener, RobotMouseEventListener {

    private static final int READINGS_SIZE = 50;
    private static final double TO_RADIANS = Math.PI / 180.0;
    private final Timer displayTimer = new Timer();
    private GraphicsContext canvasGraphics;
    private double canvasWidth;
    private double canvasHeight;
    private String rightButtonLabel;
    private String leftButtonLabel;
    /*
    A list (queue) of the last N readings. This is so we can plot the reading on the canvas
     */
    private final static Readings readings = new Readings(READINGS_SIZE);

    @FXML
    private ChoiceBox choiceBoxPortList;

    @FXML
    private Button buttonConnect;

    @FXML
    private Button buttonMouseMotion;

    @FXML
    private Button buttonCalibrateVertical;

    @FXML
    private Button buttonSwapLR;

    @FXML
    private Button buttonCalibrateHeading;

    @FXML
    private Canvas mainCanvas;
    /*
    Needed so we can alculate the height of the canvas when the mainBorderPane changes size
     */
    @FXML
    private AnchorPane statusAnchorPane;
    /*
    Needed so we can calculate the height of the canvas when the mainBorderPane changes size
     */
    @FXML
    private FlowPane buttonFlowPane;
    /*
    Needed so we can calculate the height of the canvas when the mainBorderPane changes size
     */
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label status1;

    @FXML
    private Label status2;

    @FXML
    private Button buttonFinish;
    /*
    ----------------------------------------------------------------------------
    Section implements button handlers
    ----------------------------------------------------------------------------
     */
    @FXML
    private void handleButtonMouseMotion(ActionEvent event) {
        switchMouseState();
    }

    @FXML
    private void handleButtonFinish(ActionEvent event) {
        Main.closeApplication(0);
    }

    @FXML
    private void handleButtonConnect() {
        if (isConnectedToSensor()) {
            Main.disConnectSensor();
        } else {
            Main.connectSensor(choiceBoxPortList.getSelectionModel().getSelectedItem().toString());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initButtonState();
            }
        });
    }

    @FXML
    private void handleButtonCalibrateVertical(ActionEvent event) {
        calibrateVerticle();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.initMouseController();
            }
        });
    }

    @FXML
    private void handleButtonCalibrateHeading() {
        calibrateHeading();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.initMouseController();
            }
        });
    }

    @FXML
    private void handleButtonSwapLR(ActionEvent event) {
        calibrateHeading();
        calibrateVerticle();
        boolean swap = Main.getSerialMonitorThread().swapLR();
        ConfigData.set(ConfigData.CALIB_SWAP_LR, String.valueOf(swap));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buttonSwapLR.setText("<- Swap " + (swap ? "ON " : "OFF") + " ->");
                Main.initMouseController();
            }
        });

    }

    /*
    ----------------------------------------------------------------------------
    Section implements the SerialPortListener interface
    
    All events must be wrapped in Platform.runLater. This is because you 
    cannot update the GUI in a thread that is not a JavaFX managed thread.
    ----------------------------------------------------------------------------
     */
    @Override
    public void reading(Reading reading) {
        /*
        Add the reading to the list of readings.
         */
        readings.add(reading);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*
                Set the label to display the raw data
                 */
                status1.setText(readings.getLatency() + "ms [" + readings.size() + "]" + ": " + reading.toString());
            }
        });
    }

    @Override
    public void fail(Exception e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                status2.setText(e.getMessage());
            }
        });
    }

    @Override
    public void connectedSensor(String devicePort, int baud, String name) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initButtonState();
            }
        });
    }

    @Override
    public void disConnectedSensor(String devicePort, String name) {
        readings.clear();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initButtonState();
            }
        });
    }

    @Override
    public boolean rawData(String s) {
        return false;
    }

    /*
    ----------------------------------------------------------------------------
    Section implements the RobotMouseEventListener interface
    
    All events must be wrapped in Platform.runLater. This is because you 
    cannot update the GUI in a thread that is not a JavaFX managed thread.
    ----------------------------------------------------------------------------
     */
    @Override
    public void mouseNotInPosition(Point expected, Point actual, int count) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                status2.setText("ALERT: Mouse is out of position [" + count + "]!");
                setMouseConnectButtonState();
                alertOk("Mouse Movement Aborted", "Mouse is out of position.\nDid you move the mouse?", "Ok ot continue");
            }
        });
    }

    @Override
    public void connectedMouse() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initButtonState();
            }
        });
    }

    @Override
    public void disConnectedMouse() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initButtonState();
            }
        });
    }
    /*
    ----------------------------------------------------------------------------
    Section implements the Java FX and Graphical data display
    ----------------------------------------------------------------------------
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initConnections();
        initTheCanvas();
        buttonSwapLR.setText("<- Swap " + (ConfigData.getBoolean(ConfigData.CALIB_SWAP_LR) ? "ON " : "OFF") + " ->");
        displayTimer.scheduleAtFixedRate(displayTimerTask, 1, 333);
    }

    /**
     * TimerTask is an instance of Runnable (has a run method). The run method
     * is invoked by the JAVA FX Timer (displayTimer) started in the initialize
     * method.
     * <p>
     * Currently invoked every 200 MS
     */
    TimerTask displayTimerTask = new TimerTask() {
        @Override
        public void run() {
            /*
            Only draw if connected to sensor
             */
            canvasGraphics = mainCanvas.getGraphicsContext2D();
            try {
                /*
                Init the canvas with a background colour and fill it!
                 */
                canvasGraphics.setFill(Color.AQUA);
                canvasGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
                canvasGraphics.setLineWidth(1);
                canvasGraphics.setFont(new Font(15));
                canvasGraphics.setStroke(Color.BLACK);
                double yOrg = canvasHeight / 2; // Center of the canvas
                double xOrg = canvasWidth / 2;  // Center of the canvas
                if (!isConnectedToSensor()) {
                    /*
                    Sensor is not connected.
                     */
                    canvasGraphics.setStroke(Color.RED);
                    canvasGraphics.strokeText("Sensor is not connected", xOrg - 100, 50);
                } else {
                    /*
                    Given the size of the canvas. Calculate each X step and the origin line. Also the tick height.
                     */
                    double xStep = canvasWidth / (readings.capacity() - 1);
                    double tickHeight = canvasHeight / 80;
                    double radius = Math.min(canvasHeight, canvasWidth) / 4;
                    double radius2 = Math.min(canvasHeight, canvasWidth) / 3;
                    double scaleY = canvasHeight / 3000;
                    double scale = canvasHeight / 1000;
                    double xPos;
                    double yPos;
                    double yPosPrev;
                    double lastPlotReading;
                    /*
                    Set the line width to 1. Draw a bounding box and origin line in black
                     */
                    canvasGraphics.strokeRect(0, 0, canvasWidth, canvasHeight);
                    canvasGraphics.strokeLine(0, yOrg, canvasWidth, yOrg);
                    /*
                    Draw the ticks. Each 10th goes below the line
                     */
                    for (int t = 0; t < readings.capacity(); t++) {
                        if ((t % 10) == 0) {
                            canvasGraphics.strokeLine(t * xStep, yOrg - tickHeight, t * xStep, yOrg + tickHeight);
                        } else {
                            canvasGraphics.strokeLine(t * xStep, yOrg, t * xStep, yOrg - tickHeight);
                        }
                    }
                    /*
                    Draw the vertical boundaries.
                     */
                    drawVerticalLimitLine(yOrg, scaleY, Color.BLUE, Main.getMouseController().getVerticalMin(), "MIN:");
                    drawVerticalLimitLine(yOrg, scaleY, Color.BLUE, Main.getMouseController().getVerticalMax(), "MAX:");

                    drawVerticalLimitLine(yOrg, scaleY, Color.RED, Main.getMouseController().getVerticalLimitMin(), "MIN:");
                    drawVerticalLimitLine(yOrg, scaleY, Color.RED, Main.getMouseController().getVerticalLimitMax(), "MAX:");

                    /*
                    If there are ANY values in the readings
                     */
                    canvasGraphics.setLineWidth(2);
                    canvasGraphics.setFont(new Font(20));

                    if (readings.size() > 0) {
                        List<Reading> list = readings.readings();

                        switch (Main.getMouseController().getMouseVerticalState()) {
                            case INACTIVE:
                                canvasGraphics.setStroke(Color.DARKGRAY);
                                break;
                            case ACTIVE:
                                canvasGraphics.setStroke(Color.GREEN);
                                break;
                            case NULL_ZONE:
                                canvasGraphics.setStroke(Color.YELLOW);
                                break;
                            default:
                                canvasGraphics.setStroke(Color.RED);
                                break;
                        }

                        xPos = -(xStep * 2);
                        yPos = yOrg;
                        yPosPrev = yPos;
                        lastPlotReading = 0;
                        for (Reading reading : list) {
                            lastPlotReading = reading.getY();
                            xPos = xPos + xStep;
                            yPos = yOrg + (lastPlotReading * scaleY);
                            canvasGraphics.strokeLine(xPos, yPosPrev, xPos + xStep, yPos);
                            yPosPrev = yPos;
                        }
                        canvasGraphics.strokeText("" + lastPlotReading, xPos - 80, yPos + 20);
                        canvasGraphics.strokeText("" + Main.getMouseController().getMouseVerticalOffset(), xPos - 80, yPos - 20);

                        Reading lastReading = readings.getLastReading();
                        if (lastReading != null) {
                            canvasGraphics.setStroke(Color.BLACK);
                            canvasGraphics.strokeOval(xOrg - radius, yOrg - radius, radius * 2, radius * 2);
                            drawClockHand(xOrg, yOrg, radius, 0, Color.YELLOW, 1, "North");
                            drawClockHand(xOrg, yOrg, radius2, Main.getMouseController().getHeadingMin(), Color.BLUE, 1, "Min:" + Main.getMouseController().getHeadingMin());
                            drawClockHand(xOrg, yOrg, radius2, Main.getMouseController().getHeadingMax(), Color.BLUE, 1, "Max:" + Main.getMouseController().getHeadingMax());
                            drawClockHand(xOrg, yOrg, radius, Main.getMouseController().getHeadingLimitMin(), Color.RED, 1, "Min:" + Main.getMouseController().getHeadingLimitMin());
                            drawClockHand(xOrg, yOrg, radius, Main.getMouseController().getHeadingLimitMax(), Color.RED, 1, "Max:" + Main.getMouseController().getHeadingLimitMax());
                            Color col;

                            String offsetHeading = String.valueOf(Main.getMouseController().getMouseHeadingOffset());
                            String actualHeading = String.valueOf(lastReading.getHeading());
                            String displayHeading;
                            switch (Main.getMouseController().getMouseHeadingState()) {
                                case INACTIVE:
                                    col = Color.DARKGREY;
                                    displayHeading = actualHeading + "[D]";
                                    break;
                                case ACTIVE:
                                    col = Color.GREEN;
                                    displayHeading = actualHeading + "[" + offsetHeading + "]";
                                    break;
                                case NULL_ZONE:
                                    displayHeading = actualHeading + "[" + offsetHeading + "]";
                                    col = Color.YELLOW;
                                    break;
                                default:
                                    displayHeading = actualHeading + "[?]";
                                    col = Color.RED;
                            }
                            if (Main.getSerialMonitorThread().isSwapLR()) {
                                rightButtonLabel = "A";
                                leftButtonLabel = "B";
                            } else {
                                rightButtonLabel = "B";
                                leftButtonLabel = "A";                                
                            }
                            drawClockHand(xOrg, yOrg, radius2, (long) lastReading.getHeading(), col, 2, displayHeading);
                            drawButton(50, 50, 80 * scale, Main.getMouseController().isLeftButtonPressed(), lastReading.isB2S(), lastReading.isB2R(), leftButtonLabel);
                            drawButton(canvasWidth - 50, 50, 80 * scale, Main.getMouseController().isRightButtonPressed(), lastReading.isB1S(), lastReading.isB1R(), rightButtonLabel);
                        }
                    } else {
                        canvasGraphics.setLineWidth(1);
                        canvasGraphics.setStroke(Color.RED);
                        canvasGraphics.strokeText("No readings have been received", xOrg - 120, 50);
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    };

    private void drawButton(double x, double y, double size, boolean mousePressed, boolean pressed1, boolean pressed2, String marker) {
        canvasGraphics.setStroke(Color.CYAN);
        if (pressed2) {
            canvasGraphics.setFill(Color.RED);
            double pSize = size + 20;
            canvasGraphics.fillOval(x - (pSize / 2), y - (pSize / 2), pSize, pSize);
        }
        if (pressed1) {
            canvasGraphics.setFill(Color.BLUE);
            double pSize = size + 10;
            canvasGraphics.fillOval(x - (pSize / 2), y - (pSize / 2), pSize, pSize);
        }
        if (mousePressed) {
            canvasGraphics.setFill(Color.GREEN);
        } else {
            canvasGraphics.setFill(Color.PINK);
        }
        canvasGraphics.fillOval(x - (size / 2), y - (size / 2), size, size);
        canvasGraphics.strokeText(marker, x - (size / 8), y + (size / 8));
    }

    /**
     * This is a horizontal line marking a vertical limit
     *
     * @param yOrg Zero on the canvas
     * @param scale Scale for display only
     * @param colour The line colour
     * @param line The line value
     * @param marker The text to annotate the line with
     */
    private void drawVerticalLimitLine(double yOrg, double scale, Color colour, double line, String marker) {
        canvasGraphics.setStroke(colour);
        double d = yOrg + (line * scale);
        canvasGraphics.strokeLine(110, d, canvasWidth - 110, d);
        canvasGraphics.strokeText(marker + (long) line, 10, d + 5);
    }

    /**
     * Draw a line on the 'clock face'
     *
     * @param xOrg The centre of the clock X
     * @param yOrg The centre of the clock X
     * @param radius The effective length of the line (from the centre
     * @param degrees The angle (ZERO) is from centre vertcally UP.
     * @param colour The colour of the line
     * @param lineWidth The thickness of the line
     * @param marker The Text to draw at the end of the line.
     */
    private void drawClockHand(double xOrg, double yOrg, double radius, long degrees, Color colour, double lineWidth, String marker) {
        canvasGraphics.setLineWidth(lineWidth);
        canvasGraphics.setStroke(colour);
        double rr = (degrees + 90) * TO_RADIANS;
        double yy = radius * Math.sin(rr);
        double xx = radius * Math.cos(rr);
        canvasGraphics.strokeLine(xOrg, yOrg, xOrg + xx, yOrg - yy);
        yy = (radius + 30) * Math.sin(rr);
        xx = (radius + 30) * Math.cos(rr);
        canvasGraphics.strokeText(marker, (xOrg + xx) - 20, (yOrg - yy) + 10);
    }

    /**
     * The canvas (graph plot) is contained inside connectionsAnchorPane.
     * statusAnchorPane has a height and a width property that can be listened
     * to. This method sets up the listeners.
     * <p>
     * Not the canvas sits inside an Anchor Pane but this would only respond to
     * increases in size. Shrinking the window was ignored.
     * <p>
     * Using statusAnchorPane worked but included the button bar
     * (buttonFlowPane) and Status panel (statusAnchorPane) so these sizes are
     * used to calculate the canvas size
     */
    private void initTheCanvas() {
        /*
        Get statusAnchorPane and buttonFlowPane heights
         */
        double hightAdjust = statusAnchorPane.getPrefHeight() + buttonFlowPane.getPrefHeight();
        /*
        Add a listener to the Width property that sets the canvas to the same width
         */
        mainBorderPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                /*
                Width has changed. Update the canvas width!
                 */
                try {
                    canvasWidth = mainBorderPane.getWidth();
                    mainCanvas.setWidth(canvasWidth);
                    canvasGraphics = mainCanvas.getGraphicsContext2D();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        /*
        Add a listener to the height property that sets the canvas to the same height
         */
        mainBorderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            /*
            Height has changed. Update the canvas height!
             */
            try {
                canvasHeight = mainBorderPane.getHeight() - hightAdjust;
                mainCanvas.setHeight(canvasHeight);
                canvasGraphics = mainCanvas.getGraphicsContext2D();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /*
    ----------------------------------------------------------------------------
    Section implements utility methods
    ----------------------------------------------------------------------------
     */
    /**
     * Populate the Connections Drop down
     *
     * Calls the SerialMonitorThread to get a list of ports
     */
    private void initConnections() {
        choiceBoxPortList.setItems(FXCollections.observableArrayList(SerialMonitorThread.getPortList()));
        choiceBoxPortList.getSelectionModel().select(ConfigData.getDefaultPort());
        if (choiceBoxPortList.getSelectionModel().getSelectedIndex() < 0) {
            choiceBoxPortList.getSelectionModel().select(0);
        }
        buttonConnect.setText(isConnectedToSensor() ? "Dis-Connect" : "Connect");
    }

    private void initButtonState() {
        setSensorConnectButtonState();
        setMouseConnectButtonState();
    }

    private void switchMouseState() {
        if (isConnectedToSensor()) {
            if (isConnectedToMouse()) {
                Main.getMouseController().disConnectTheMouse();
            } else {
                Main.getMouseController().connectTheMouse();
            }
        }
        setMouseConnectButtonState();
    }

    private boolean isConnectedToSensor() {
        return Main.isConnectedToSensor();
    }

    private boolean isConnectedToMouse() {
        return Main.isConnectedToMouse();
    }

    private void setSensorConnectButtonState() {
        String name = null;
        if (isConnectedToSensor()) {
            name = Main.getSerialMonitorThread().getDeviceName();
            this.buttonConnect.setText("Dis-Connect");
            buttonCalibrateHeading.setDisable(false);
            buttonCalibrateVertical.setDisable(false);
            this.status2.setText("Connected to Sensor: " + name);
            buttonMouseMotion.setDisable(false);
        } else {
            this.buttonConnect.setText("Connect");
            this.status2.setText("Select a Sensor port and press 'Connect'");
            buttonCalibrateHeading.setDisable(true);
            buttonCalibrateVertical.setDisable(true);
            buttonMouseMotion.setDisable(true);
        }
    }

    private void setMouseConnectButtonState() {
        buttonMouseMotion.setText(isConnectedToMouse() ? "Stop" : "Start");
    }

    private void calibrateVerticle() {
        if (readings.hasLastReading()) {
            long[] verticalData = ConfigData.getLongs(ConfigData.CALIB_VERTICAL_DATA, 3);
            verticalData[0] = (long) readings.getLastReading().getY();
            ConfigData.set(ConfigData.CALIB_VERTICAL_DATA, String.format("%d,%d,%d", verticalData[0], verticalData[1], verticalData[2]));
        }
    }

    private void calibrateHeading() {
        if (readings.hasLastReading()) {
            long[] headingData = ConfigData.getLongs(ConfigData.CALIB_HEADING_DATA, 3);
            headingData[0] = (long) readings.getLastReading().getHeading();
            ConfigData.set(ConfigData.CALIB_HEADING_DATA, String.format("%d,%d,%d", headingData[0], headingData[1], headingData[2]));
        }
    }

    private static void alertOk(String ti, String txt, String ht) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert:" + ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(Main.getMainStage().getX() + 50);
        alert.setY(Main.getMainStage().getY() + 50);
        alert.showAndWait();
    }

}
