/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author deltedes
 */
public class MessageController {
    
    private static final Logger log = Logger.getLogger(MessageController.class.getName());
    
    // Memorizza l'ultimo messaggio
    private byte[] lastMessage;
    private String lastSender;
    
    // Memorizza le informazioni della rete
    private String masterIP;
    private HashMap<String, String> slaveArray = new HashMap<String, String>();
    
    // Identifica se lo slave è registrato dal master
    private boolean isRegistered = false;
    
    // Properry per l'interfacciamento con la gui
    public StringProperty outMessage = new SimpleStringProperty();
    
    /*
    
    */
    public void setMessage(byte[] message, String sender) {
        lastMessage = message;
        lastSender = sender; 
        
        log.log(Level.INFO, "Packet received from {0} with \"{1}\"", new Object[] {sender, new String(lastMessage)} );
        
        Platform.runLater(() -> {
        outMessage.set(lastSender + " ha inviato un messaggio");
        });

        // CONTROLLO RICEZIONE PACCHETTO
        // Estraggo dal pacchetto:
        // - magicNumber 	1 byte 0x53
        // - packetType         1 byte
        // - message    	resto del pacchetto
        
        int magicNumber = lastMessage[0];
       
        //Controllo minimale per identificare i pacchetti del Sarabanda
        if (magicNumber == 0x53) {
            int packetType = lastMessage[1];

            log.log(Level.INFO, "Packet magic is 0x{0} from host {1} with command type 0x{2}", new Object[] {
                String.format("%x", magicNumber),
                lastSender,
                String.format("%x", packetType)
                }
            );

            //Identifica il tipo di comando ricevuto
            // Tipo comando
            // 0x01 MASTER START - inviato dal master in fase di avvio, richiede la registrazione degli SLAVE
            // 0x02 SLAVE REGISTRATION - inviato dagli slave per avvisare della loro connessione
            // 0x03 SLAVE REGISTRATION OK
            // 0x04 SLAVE DEREGISTRATION
            // 0x05 BUTTON
            
            switch (packetType) {
                case 0x01: // MASTER START
                    // Salva l'IP del master
                    masterIP = lastSender;
                    isRegistered = false;
                    Platform.runLater(() -> {
                        outMessage.set(lastSender + " ha segnalato che è il master");
                    });
                    // TODO Invia un pacchetto di registrazione dello SLAVE
                    break;
                case 0x02: // SLAVE REGISTRATION
                    // Uno slave ha richiesto la registrazione
                    slaveArray.put(lastSender, "registred");
                    Platform.runLater(() -> {
                    outMessage.set(lastSender + " è stato registrato come slave");
                    });
                    break;
                case 0x03: // SLAVE REGISTRATION OK
                    // Verifica che il master abbia accettato la registrazione 
                    if (sender.compareTo(masterIP) == 0) {
                        Platform.runLater(() -> {
                            outMessage.set("Lo slave è stato registrato dal master");
                        });
                        isRegistered = true;
                    }
                    break;
                case 0x04: // SLAVE DEREGISTRATION
                    // Uno slave si è spento
                    slaveArray.remove(lastSender);
                    Platform.runLater(() -> {
                        outMessage.set(lastSender + " è stato rimosso dalla rete");
                    });
                case 0x05: // BUTTON
                    // Pacchetto pulsanti
                    if (sender.compareTo(masterIP) == 0) {
                        // Ho ricevuto uno stato pulsanti dal master
                        // Cambio lo stato dei pulsanti
                    } else {
                        // Ho intercettato la richiesta di un setup pulsanti da parte di unao slave
                        // Mi aspetto che
                    }
                default: break;
            }   
        }
        log.log(Level.INFO, "Fine del setMessage");
    }
}
