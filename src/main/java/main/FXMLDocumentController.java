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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import serial.Reading;
import serial.SerialPortListener;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author huw
 */
public class FXMLDocumentController implements Initializable, SerialPortListener {

    private final Timer displayTimer = new Timer();
    private GraphicsContext canvasGraphics;
    private double canvasWidth;
    private double canvasHeight;
    /*
    A list (queue) of the last N readings. This is so we can plot the reading on the canvas
    */
    private static Readings readings = new Readings(50);

    @FXML
    private Canvas mainCanvas;
    /*
    Needed so we can calculate the height of the canvas when the mainBorderPane changes size
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
    private void handleButtonFinish(ActionEvent event) {
        Main.closeApplication(0);
    }

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
        status2.setText(e.getMessage());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTheCanvas();
        displayTimer.scheduleAtFixedRate(displayTimerTask, 1, 200);
    }

    /**
     * The canvas (graph plot) is contained inside connectionsAnchorPane.
     * connectionsAnchorPane has a height and a width property that can cbe
     * listened to. When it changes we can call a method
     */
    private void initTheCanvas() {
        double hi = statusAnchorPane.getPrefHeight() + buttonFlowPane.getPrefHeight();
        /*
        Add a listener to the Width property that sets the canvas to the same width
         */
        mainBorderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
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
        });
        /*
        Add a listener to the height property that sets the canvas to the same height
         */
        mainBorderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            /*
            Height has changed. Update the canvas height!
            Note canvas starts 50 pixels from the top of the connectionsAnchorPane
            */
            try {
                canvasHeight = mainBorderPane.getHeight() - hi;
                mainCanvas.setHeight(canvasHeight);
                canvasGraphics = mainCanvas.getGraphicsContext2D();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


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
                double yOrg = canvasHeight / 2;
                double xOrg = canvasWidth / 2;
                int dataOffset = (int) Math.round((canvasWidth / 4) * 3);
                double tickHeight = canvasHeight / 50;

                    /*
                    Init the canvas with a background colour and fill it!
                     */
                canvasGraphics.setFill(Color.AQUA);
                canvasGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
                    /*
                    Set the line width to 1. Draw a bounding box and origin line in black
                     */
                canvasGraphics.setLineWidth(1);
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
                    Draw the fences.
                     */
//                if (mouseController != null) {
//                    connectionCanvasGraphics.setStroke(Color.PINK);
//                    Fences lrf = mouseController.getLeftRightFences();
//                    double lrv = 0;
//                    for (int i = 0; i < lrf.count(); i++) {
//                        lrv = xOrg + lrf.get(i);
//                        connectionCanvasGraphics.strokeLine(lrv, 0, lrv, connectionCanvasHeight);
//                    }
//                    connectionCanvasGraphics.setStroke(Color.CORAL);
//                    Fences udf = mouseController.getUpDownFences();
//                    double udv = 0;
//                    for (int i = 0; i < udf.count(); i++) {
//                        udv = yOrg + udf.get(i);
//                        connectionCanvasGraphics.strokeLine(0, udv, connectionCanvasWidth, udv);
//                    }
//                }

                    /*
                    If there are ANY values in the readings
                     */
//                if (readingData.size() > 0) {
//                        /*
//                        For each line.
//                         */
//                    for (int i = 0; i < 2; i++) {
//                            /*
//                            Set the colour and scale for each line
//                             */
//                        connectionCanvasGraphics.setStroke(colours[i]);
//                        double scaleY = connectionCanvasHeight / scales[i];
//                            /*
//                            Plot the values. We need from and to values as we want to draw lines
//                            The last to value becomes the next from value!
//                            Set the initial values
//                             */
//                        double xTo = -xStep;
//                        double yTo = yOrg;
//                        double xfrom = xTo;
//                        double yfrom = yTo;
//                        for (double[] reading : readingData) {
//                                /*
//                                Derive the to values and plot.
//                                 */
//                            yTo = yOrg - reading[i] * scaleY;
//                            xTo += xStep;
//                            connectionCanvasGraphics.strokeLine(xfrom, yfrom, xTo, yTo);
//                                /*
//                                Copy the to values to the from values for the next plot.
//                                 */
//                            xfrom = xTo;
//                            yfrom = yTo;
//                        }
//                    }
//                    Reading lastReading = readings.getLastReading();
//                    if (lastReading != null) {
//                        currentLeftRightReading = lastReading.getHeading();
//                        connectionCanvasGraphics.setStroke(Color.BLACK);
//                        connectionCanvasGraphics.strokeText("Latency: " + lastReading.getFormattedLatency() + " ms", dataOffset, 20);
//                        connectionCanvasGraphics.strokeText(" X: " + lastReading.getFormattedX(), dataOffset, 40);
//                        connectionCanvasGraphics.strokeText(" Y: " + lastReading.getFormattedY(), dataOffset, 60);
//                        connectionCanvasGraphics.strokeText("NS: " + lastReading.getFormattedNS(), dataOffset, 80);
//                        connectionCanvasGraphics.strokeText("WE: " + lastReading.getFormattedWE(), dataOffset, 100);
//                        connectionCanvasGraphics.strokeText(" D: " + currentLeftRightReading, dataOffset, 120);
//                        double compass = xOrg + currentLeftRightReading;
//                        connectionCanvasGraphics.strokeLine(compass, 0, compass, connectionCanvasHeight);
//                    }
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    };

}
