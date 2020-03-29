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
import mouse.MouseController;
import serial.Reading;
import serial.SerialMonitorThread;
import serial.SerialPortListener;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author huw
 */
public class FXMLDocumentController implements Initializable, SerialPortListener {

    private static final double TO_RADIANS = Math.PI / 180.0;
    private final Timer displayTimer = new Timer();
    private GraphicsContext canvasGraphics;
    private boolean connectedToSensor;
    private double canvasWidth;
    private double canvasHeight;
    private long timeLastDrawn =0;
    /*
    A list (queue) of the last N readings. This is so we can plot the reading on the canvas
     */
    private static Readings readings = new Readings(50);

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

    @FXML
    private void handleButtonMouseMotion(ActionEvent event) {
        switchMouseState();
    }

    @FXML
    private void handleButtonFinish(ActionEvent event) {
        Main.closeApplication(0);
    }

    @FXML
    private void handleButtonSwapLR(ActionEvent event) {
        boolean swap = Main.getSerialMonitorThread().swapLR();
        ConfigData.set(ConfigData.CALIB_SWAP_LR, String.valueOf(swap));
        buttonSwapLR.setText("<- Swap " + (swap?"ON ":"OFF") + " ->");
    }

    @FXML
    private void handleButtonCalibrateVertical(ActionEvent event) {
        long[] verticalData = ConfigData.getLongs(ConfigData.CALIB_VERTICAL_DATA, 3);
        verticalData[0] = (long) readings.getLastReading().getY();
        ConfigData.set(ConfigData.CALIB_VERTICAL_DATA, String.format("%d,%d,%d", verticalData[0], verticalData[1], verticalData[2]));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.startMouseController();
            }
        });
    }

    ;


    @FXML
    private void handleButtonConnect() {
        if (connectedToSensor) {
            Main.getSerialMonitorThread().close();
        } else {
            Main.connectToSensor(choiceBoxPortList.getSelectionModel().getSelectedItem().toString());
        }
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    @FXML
    private void handleCalibrateHeading() {
        long[] headingData = ConfigData.getLongs(ConfigData.CALIB_HEADING_DATA, 3);
        headingData[0] = (long) readings.getLastReading().getHeading();
        ConfigData.set(ConfigData.CALIB_HEADING_DATA, String.format("%d,%d,%d", headingData[0], headingData[1], headingData[2]));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.startMouseController();
            }
        });
    }

    @Override
    public void reading(Reading reading) {
        if ((System.currentTimeMillis() - timeLastDrawn) > 1000) {
            System.out.println("DELAY");
        }
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
    public void connected(String devicePort, int baud, String name) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initConnectButtonState(true, devicePort, name);
            }
        });
    }

    @Override
    public void disConnected(String devicePort, String name) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initConnectButtonState(false, devicePort, name);
            }
        });
    }

    @Override
    public boolean rawData(String s) {
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initConnections();
        initTheCanvas();
        buttonSwapLR.setText("<- Swap " + (ConfigData.getBoolean(ConfigData.CALIB_SWAP_LR)?"ON ":"OFF") + " ->");
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
            Only draw if the Connections Tab is selected (showing)
             */
            canvasGraphics = mainCanvas.getGraphicsContext2D();
            try {
                /*
               Given the size of the canvas. Calculate each X step and the origin line. Also the tick height.
                 */
                double xStep = canvasWidth / (readings.capacity() - 1);
                double tickHeight = canvasHeight / 80;
                double yOrg = canvasHeight / 2; // Center of the canvas
                double xOrg = canvasWidth / 2;  // Center of the canvas
                double radius = Math.min(canvasHeight, canvasWidth) / 4;
                double radius2 = Math.min(canvasHeight, canvasWidth) / 3;
                double scaleY = canvasHeight / 3000;
                double scale = canvasHeight / 1000;
                double xPos;
                double yPos;
                double yPosPrev;
                double lastPlotReading;
                /*
                Init the canvas with a background colour and fill it!
                 */
                canvasGraphics.setFill(Color.AQUA);
                canvasGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
                /*
                Set the line width to 1. Draw a bounding box and origin line in black
                 */
                canvasGraphics.setLineWidth(1);
                canvasGraphics.setFont(new Font(15));
                canvasGraphics.setStroke(Color.BLACK);
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
                }
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
                    drawClockHand(xOrg, yOrg, radius2, (long) lastReading.getHeading(), col, 2, displayHeading);
                    drawButton(50, 50, 80 * scale, Main.getMouseController().isLeftButtonPressed(), lastReading.isB2S(), lastReading.isB2R(), "B");
                    drawButton(canvasWidth - 50, 50, 80 * scale, Main.getMouseController().isRightButtonPressed(), lastReading.isB1S(), lastReading.isB1R(), "A");
                timeLastDrawn = System.currentTimeMillis();
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    };

    public void drawButton(double x, double y, double size, boolean mousePressed, boolean pressed1, boolean pressed2, String marker) {
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
    public void drawVerticalLimitLine(double yOrg, double scale, Color colour, double line, String marker) {
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
    public void drawClockHand(double xOrg, double yOrg, double radius, long degrees, Color colour, double lineWidth, String marker) {
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

    public void alert(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                status2.setText("ALERT: " + message);
                setMouseConnectButtonState();
                alertOk("Mouse Movement Aborted", "Mouse is out of position.\nDid you move the mouse?", "Ok ot continue");
            }
        });
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

    private void initConnections() {
        choiceBoxPortList.setItems(FXCollections.observableArrayList(SerialMonitorThread.getPortList()));
        choiceBoxPortList.getSelectionModel().select(ConfigData.getDefaultPort());
        if (choiceBoxPortList.getSelectionModel().getSelectedIndex() < 0) {
            choiceBoxPortList.getSelectionModel().select(0);
        }
        if (Main.getSerialMonitorThread() != null) {
            initConnectButtonState(Main.getSerialMonitorThread().isRunning(), Main.getSerialMonitorThread().getDevicePort(), Main.getSerialMonitorThread().getDeviceName());
        } else {
            initConnectButtonState(false, null, null);
        }
        buttonConnect.setText(connectedToSensor ? "Dis-Connect" : "Connect");
    }

    private void initConnectButtonState(boolean connectedToSensor, String devicePort, String name) {
        this.connectedToSensor = connectedToSensor;
        this.buttonConnect.setText(connectedToSensor ? "Dis-Connect" : "Connect");
        if (devicePort == null) {
            this.status2.setText("Select a port and press 'Connect'");
        } else {
            this.status2.setText((connectedToSensor ? "" : "Dis") + "Connected Port:" + devicePort + " Device:" + name);
        }
        buttonCalibrateHeading.setDisable(!connectedToSensor);
        buttonCalibrateVertical.setDisable(!connectedToSensor);
        setMouseConnectButtonState();
    }

    private void switchMouseState() {
        MouseController mc = Main.getMouseController();
        if ((mc != null) && (connectedToSensor)) {
            if (mc.isMovingTheMouse()) {
                mc.stopMovingTheMouse();
            } else {
                mc.startMovingTheMouse();
            }
        }
        setMouseConnectButtonState();
    }

    private void setMouseConnectButtonState() {
        MouseController mc = Main.getMouseController();
        String text = "Start";
        boolean disable = true;
        if (mc != null) {
            if (connectedToSensor) {
                disable = false;
                if (mc.isMovingTheMouse()) {
                    text = "Stop";
                }
            } else {
                mc.stopMovingTheMouse();
            }
        }
        buttonMouseMotion.setText(text);
        buttonMouseMotion.setDisable(disable);
    }

    public static void alertOk(String ti, String txt, String ht) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert:" + ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(Main.getMainStage().getX() + 50);
        alert.setY(Main.getMainStage().getY() + 50);
        alert.showAndWait();
    }
}
