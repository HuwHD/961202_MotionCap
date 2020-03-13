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
