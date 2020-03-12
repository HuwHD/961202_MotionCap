/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import robot.RobotMouseThread;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author huw
 */
public class FXMLDocumentController implements Initializable {

    private RobotMouseThread robotMouseThread;
 
    @FXML
    private Label label;

    public void action(String message) {
        if (label != null) {
            label.setText(message);
        }
    }
    
    @FXML
    private void handleButtonClickMe1(ActionEvent event) {
    }

    @FXML
    private void handleButtonClickMe2(ActionEvent event) {
    }

    @FXML
    private void handleButtonFinish(ActionEvent event) {
    }

    @FXML
    private void handleButtonRoundTrip(ActionEvent event) {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

}
