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
    
    // Porta di ascolto
    private final static int udpListenPort = 8888;
    // Porta di invio
    private final static int udpSenderPort = 8888;
    
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
        
        // Carica il pannello di gestione all'interno della finestra principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FXMLDocument.fxml"));
        AnchorPane overviewPage = (AnchorPane) loader.load();
        rootLayout.setCenter(overviewPage);
            
        SarabandaSaloonController controller = loader.getController();
        controller.setMainApp(this);
        
        // Inizializza il Message Controller che andrà a fare il parsing dei messaggi UDP
        messageController = new MessageController(udpSenderPort);
        
        // Associa la label della gui con la proprietà del Message Controller
        controller.getServerLabelProperty().bind(messageController.outMessage);
        
        // Inizializza il server UDP e ci associa il Message Controller
        udpservice = new UDPServerService(messageController, udpListenPort);
    }
    
    /**
     * Avvia il servizio UDP
     */
    public void startUDPServer() {
        int retryCounter = 10;
        
        // TODO Mesaggio di server in avvio
        
        // Va in loop finché il server non è attivo
        while ((udpservice.getState() != State.RUNNING) || (retryCounter-- == 0)) {
            // Verifica l'ultimo stato del service e lo resetta eventualmente
            if ((udpservice.getState() == State.CANCELLED) || (udpservice.getState() == State.FAILED)) {
                udpservice.reset();
            }

            // Se il server è pronto effettua lo start del service
            if (udpservice.getState() == State.READY) {
                udpservice.start();
            }
        } 
        
        // Gestione dell'output
        if (retryCounter == 0) {
            //TODO Messaggio di errore perché il server non è in stato running
        } else {
            //TODO Messaggio di ok del server attivo
        }
    }
    
    /**
     * Disattiva il servizio UDP invocando la cancellazione del servizio e spedento un pacchetto per 
     * bypassare il fatto che la lettura del socket è bloccante
     */
    public void stopUDPServer() {
        // TODO Mesaggio di server in spegnimento
        
        while (udpservice.isRunning()) {
            // Invia al servizio il comando di spegnersi
            udpservice.cancel();
            
            // Invia un pacchetto UDP generico per andare a far uscire il server dallo stato di listen
            // necessario in quanto la lettura del socket è bloccante
            byte[] nullMessage = {MessageController.MAGIC_NUMBER, MessageController.NULL};
            UDPClient.sendPacket(nullMessage, udpSenderPort, "255.255.255.255");
        } 
        
        // TODO Mesaggio di server spento
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
