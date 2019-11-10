/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import robot.Rect;
import robot.PocRobot;
import java.awt.AWTException;
import java.awt.Point;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 *
 * @author huw
 */
public class FXMLDocumentController implements Initializable {

    private final PocRobot pocRobot = new PocRobot();
 
    @FXML
    private Label label;

    public void action(String message) {
        if (label != null) {
            label.setText(message);
        }
    }
    
    @FXML
    private void handleButtonClickMe1(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*
                Move the mouse to button 2
                 */
                Point p = pocRobot.getMousePos();
                label.setText("Clicked 1. Mouse at: " + p.x + ", " + p.y);
                pocRobot.moveMouseRel(0, 30);
            }
        });
    }

    @FXML
    private void handleButtonClickMe2(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*
                Move the mouse to button 1
                 */
                Point p = pocRobot.getMousePos();
                label.setText("Clicked 2. Mouse at: " + p.x + ", " + p.y);
                pocRobot.moveMouseRel(0, -30);
            }
        });
    }

    @FXML
    private void handleButtonFinish(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*
                Move the mouse in a straight line to the window close X top right
                Then press the left mouse button
                 */
                Point p = pocRobot.getMousePos();
                Rect r = Main.getRectangle();
                pocRobot.moveLine(1, p.x, p.y, r.getX() + r.getW() - 15, r.getY() - 15);
                pocRobot.delay(10);
                pocRobot.leftClick();
            }
        });
    }

    @FXML
    private void handleButtonRoundTrip(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*
                Move the mouse from the button to the four corners and back
                 */
                Point buttonPos = pocRobot.getMousePos();
                Rect rect = Main.getRectangle();
                pocRobot.moveLine(2, buttonPos.x, buttonPos.y, rect.getX(), rect.getY());
                pocRobot.moveLine(2, rect.getX(), rect.getY(), rect.getMaxX(), rect.getMaxY());
                pocRobot.moveLine(2, rect.getMaxX(), rect.getMaxY(), rect.getMaxX(), rect.getY());
                pocRobot.moveLine(2, rect.getMaxX(), rect.getY(), rect.getX(), rect.getMaxY());
                pocRobot.moveLine(2, rect.getX(), rect.getMaxY(), rect.getX(), rect.getY());
                pocRobot.moveLine(2, rect.getX(), rect.getY(), buttonPos.x, buttonPos.y);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            pocRobot.init();
            // TODO
        } catch (AWTException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
