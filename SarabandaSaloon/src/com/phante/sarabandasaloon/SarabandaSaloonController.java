/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import com.phante.sarabandasaloon.network.UDPClient;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author deltedes
 */
public class SarabandaSaloonController implements Initializable {
    @FXML
    private Button ButtonOne;
    @FXML
    private Button ButtonTwo;
    @FXML
    private Button ButtonThree;
    @FXML
    private Button ButtonFour;
    @FXML
    private Label MainLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String labelText = "Waiting for an action ...";
        System.out.println(labelText);
        MainLabel.setText(labelText);
    }    

    @FXML
    private void ButtonOneAction(ActionEvent event) {
        ChangeMainLabel("Button One, You clicked me!");
        UDPClient client = new UDPClient();
        client.sendPacket("");
    }

    @FXML
    private void ButtonTwoAction(ActionEvent event) {
        ChangeMainLabel("Button Two, You clicked me!");
    }

    @FXML
    private void ButtonThreeAction(ActionEvent event) {
        ChangeMainLabel("Button Three, You clicked me!");
    }

    @FXML
    private void ButtonFourAction(ActionEvent event) {
        ChangeMainLabel("Button Four, You clicked me!");
    }
    
    public void ChangeMainLabel(String labelText) {
        System.out.println(labelText);
        MainLabel.setText(labelText);
    }
}
