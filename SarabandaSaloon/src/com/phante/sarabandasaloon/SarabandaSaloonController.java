/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import com.phante.sarabandasaloon.network.MessageController;
import com.phante.sarabandasaloon.network.UDPServerService;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
    
    private static final Logger log = Logger.getLogger(SarabandaSaloonController.class.getName());
    
    private MessageController messageController;
    private UDPServerService udpservice;
    
    private final static int udpPort = 8888;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String labelText = "Waiting for an action ...";
        System.out.println(labelText);
        MainLabel.setText(labelText);
        
        log.log(Level.INFO, "initialize");
        
        // Inizializza il Message Controller che andrà a fare il parsing dei messaggi UDP
        messageController = new MessageController();
        
        // Inizializza il server UDP
        udpservice = new UDPServerService(messageController, udpPort);
    }    

    @FXML
    private void ButtonOneAction(ActionEvent event) {
        Logger.getLogger(SarabandaSaloonController.class.getName()).log(Level.INFO, "Start the server");
        ChangeMainLabel("UDP Server start");
        
        log.log(Level.INFO, "Server is running? {0}", udpservice.isRunning());

        // Specifies the current state of this Worker. The initial value is State.READY. 
        // A Task may be restarted, in which case it will progress from one of these end states (SUCCEEDED, CANCELLED, or FAILED) back to READY and then immediately to SCHEDULED and RUNNING. 
        // These state transitions may occur immediately one after the other, but will always occur in the prescribed order.
        
        if (!udpservice.isRunning()) {
            udpservice.reset();
            udpservice.start();
        }
    }

    @FXML
    private void ButtonTwoAction(ActionEvent event) {
        Logger.getLogger(SarabandaSaloonController.class.getName()).log(Level.INFO, "Stop the server");
        if (udpservice.cancel()) {
            ChangeMainLabel("UDP Server stopped");
        } else {
            ChangeMainLabel("UDP Server still running");
        }
    }

    @FXML
    private void ButtonThreeAction(ActionEvent event) {
        Logger.getLogger(SarabandaSaloonController.class.getName()).log(Level.INFO, "exit");
        ChangeMainLabel("Button Three, You clicked me!");
    }

    @FXML
    private void ButtonFourAction(ActionEvent event) {
        Logger.getLogger(SarabandaSaloonController.class.getName()).log(Level.INFO, "exit");
        Platform.exit();
    }
    
    public void ChangeMainLabel(String labelText) {
        System.out.println(labelText);
        MainLabel.setText(labelText);
    }
}
