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
    
    // Costanti di messaggio
    public static final byte MAGIC_NUMBER = 'S';
    public static final byte NULL = '.';
    public static final byte MASTER_START = 'M';
    public static final byte SLAVE_REGISTRATION = 'R';
    public static final byte SLAVE_REGISTRATION_OK = 'O';
    public static final byte SLAVE_DEREGISTRATION = 'D';
    public static final byte BUTTON = 'B';


    // Porta UDP del protocollo di comunicazione
    private final int destinationUDPPort;
        
    // Memorizza l'ultimo messaggio
    private byte[] lastMessage;
    private String lastSender;
    
    // Memorizza le informazioni della rete
    private String masterIP;
    private final HashMap<String, String> slaveArray = new HashMap<>();
    
    // Identifica se lo slave è registrato dal master
    private boolean isRegistered = false;
    
    // Property per l'interfacciamento con la gui
    public StringProperty outMessage = new SimpleStringProperty();
    
    /**
     * 
     * @param udpPort 
     */
    public MessageController(int udpPort) {
        destinationUDPPort = udpPort;
    }
    
    /**
     * Effettua il parsing del messaggio ed agisce di conseguenza
     * @param message
     * @param sender 
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
        if (magicNumber == MAGIC_NUMBER) {
            int packetType = lastMessage[1];

            log.log(Level.INFO, "Packet magic is 0x{0} from host {1} with command type 0x{2}", new Object[] {
                String.format("%x", magicNumber),
                lastSender,
                String.format("%x", packetType)
                }
            );

            //Identifica il tipo di comando ricevuto
            // Tipo comando
            // M - MASTER START - inviato dal master in fase di avvio, richiede la registrazione degli SLAVE
            // R - SLAVE REGISTRATION - inviato dagli slave per avvisare della loro connessione
            // O - SLAVE REGISTRATION OK
            // D - SLAVE DEREGISTRATION
            // B - BUTTON
            
            switch (packetType) {
                case MASTER_START: // MASTER START
                    // Salva l'IP del master
                    masterIP = lastSender;
                    isRegistered = false;
                    Platform.runLater(() -> {
                        outMessage.set(lastSender + " ha segnalato che è il master");
                    });
                    
                    // Inoltra in broadcast il messaggio di registrazione come slave
                    byte[] registrationMessage = {MAGIC_NUMBER, SLAVE_REGISTRATION};
                    UDPClient.sendPacket(registrationMessage, destinationUDPPort, "255.255.255.255");
                    
                    break;
                case SLAVE_REGISTRATION: // SLAVE REGISTRATION
                    // Uno slave ha richiesto la registrazione e lo memorizzo
                    slaveArray.put(lastSender, "registred");
                    Platform.runLater(() -> {
                        outMessage.set(lastSender + " è stato registrato come slave");
                    });
                    
                    break;
                case SLAVE_REGISTRATION_OK: // SLAVE REGISTRATION OK
                    // Verifica che il master abbia accettato la registrazione 
                    if (sender.compareTo(masterIP) == 0) {
                        Platform.runLater(() -> {
                            outMessage.set("Lo slave è stato registrato dal master");
                        });
                        isRegistered = true;
                    }
                    break;
                case SLAVE_DEREGISTRATION: // SLAVE DEREGISTRATION
                    // Uno slave si è spento, lo rimuovo dalla lista
                    slaveArray.remove(lastSender);
                    Platform.runLater(() -> {
                        outMessage.set(lastSender + " è stato rimosso dalla rete");
                    });
                case BUTTON: // BUTTON
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

    /**
     * Comunica alla rete la registrazione del server
     */
    public void goOnline() {
        // Inoltra in broadcast il messaggio di registrazione come slave
        byte[] registrationMessage = {MAGIC_NUMBER, SLAVE_REGISTRATION};
        UDPClient.sendPacket(registrationMessage, destinationUDPPort, "255.255.255.255");
    }

    /**
     * Comunica alla rete lo spegnimento dello slave
     */
    public void goOffline() {        
        // Invia in broadcast il messaggio di deregistrazione dello slave
        byte[] nullMessage = {MAGIC_NUMBER, SLAVE_DEREGISTRATION};
        UDPClient.sendPacket(nullMessage, destinationUDPPort, "255.255.255.255");
    }

}
