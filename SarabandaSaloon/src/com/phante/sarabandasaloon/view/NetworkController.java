/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.view;

import com.phante.sarabandasaloon.SarabandaSaloon;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author deltedes
 */
public class NetworkController implements Initializable {
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
    
    private StringProperty statusMessage = new SimpleStringProperty();
    
    private static final Logger log = Logger.getLogger(NetworkController.class.getName());
    
     // Reference to the main application
    private SarabandaSaloon mainApp;
    private boolean switchOne = true;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String labelText = "Waiting for an action ...";
        System.out.println(labelText);
        MainLabel.setText(labelText);
        
        log.log(Level.INFO, "initialize");
    }    

    @FXML
    private void ButtonOneAction(ActionEvent event) {
        if (switchOne) {
            mainApp.startUDPServer();
            ButtonOne.setText("Stop Server");
        } else {
            mainApp.stopUDPServer();
            ButtonOne.setText("Start Server");
        } 
        
        switchOne = !switchOne;
    }

    @FXML
    private void ButtonTwoAction(ActionEvent event) {
        Logger.getLogger(NetworkController.class.getName()).log(Level.INFO, "Stop the server");
        ChangeMainLabel("UDP Server stop");
        
        mainApp.stopUDPServer();
    }

    @FXML
    private void ButtonThreeAction(ActionEvent event) {
        Logger.getLogger(NetworkController.class.getName()).log(Level.INFO, "exit");
        ChangeMainLabel("Button Three, You clicked me!");
    }

    @FXML
    private void ButtonFourAction(ActionEvent event) {
        Logger.getLogger(NetworkController.class.getName()).log(Level.INFO, "exit");
        Platform.exit();
    }
    
    public void ChangeMainLabel(String labelText) {
        System.out.println(labelText);
        MainLabel.setText(labelText);
    }

    void setMainApp(SarabandaSaloon mainApplication) {
        this.mainApp = mainApplication;
    }
    
    StringProperty getServerLabelProperty() {
        return MainLabel.textProperty();
    }
    
    /**
     * 
     * @return 
     */
    public StringProperty statusProperty() {
        return this.statusMessage;
    }
}
