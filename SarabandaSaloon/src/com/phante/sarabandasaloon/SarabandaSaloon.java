/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import com.phante.sarabandasaloon.network.MessageController;
import com.phante.sarabandasaloon.network.UDPClient;
import com.phante.sarabandasaloon.network.UDPServerService;
import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author deltedes
 */
public class SarabandaSaloon extends Application {
    
    // Server UDP per la comunicazione con il master
    private UDPServerService udpservice;
    private final static int udpPort = 8888;
    
    private MessageController messageController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inizializza la finestra principale
        FXMLLoader rootloader = new FXMLLoader(getClass().getResource("view/RootLayout.fxml"));
        BorderPane rootLayout = (BorderPane) rootloader.load();

        Scene scene = new Scene(rootLayout);

        primaryStage.setTitle("Sarabanda Saloon");
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Carica il pannello di gestione 
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FXMLDocument.fxml"));
        AnchorPane overviewPage = (AnchorPane) loader.load();
        rootLayout.setCenter(overviewPage);
            
        SarabandaSaloonController controller = loader.getController();
        controller.setMainApp(this);
        
        // Inizializza il Message Controller che andrà a fare il parsing dei messaggi UDP
        messageController = new MessageController();
        
        // Associa la label della gui con la proprietà del Message Controller
        controller.getServerLabelProperty().bind(messageController.outMessage);
        
        // Inizializza il server UDP
        udpservice = new UDPServerService(messageController, udpPort);

    }
    
    /**
     * Avvia il servizion UDP
     */
    public void startUDPServer() {
        if ((udpservice.getState() == State.CANCELLED) || (udpservice.getState() == State.FAILED)) {
            udpservice.reset();
        }
        
        if (udpservice.getState() == State.READY) {
            udpservice.start();
            byte[] registrationMessage = {0x53, 0x02};
            UDPClient.sendPacket(registrationMessage, 55056, "255.255.255.255");
        }
    }
    
    /**
     * Disattiva il servizio UDP
     */
    public void stopUDPServer() {
        while (udpservice.isRunning()) {
            // Invia al servizio il comando di spegnersi
            udpservice.cancel();

            // Invia un pacchetto UDP al loopback per andare a far uscire il server dallo stato di listen
            byte[] nullMessage = {0x53, 0x04};
            UDPClient.sendPacket(nullMessage, udpPort, "255.255.255.255");
        } 
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
