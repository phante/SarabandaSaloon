/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author deltedes
 */
public class MessageController {
    
    private static final Logger log = Logger.getLogger(MessageController.class.getName());
    
    // Memorizza l'ultimo messaggio
    private String lastMessage;
    private String lastSender;
    
    public void setMessage(String message, String sender) {
        lastMessage = message;
        lastSender = sender; 
        
        log.log(Level.INFO, "Packet received from {0} with \"{1}\"", new Object[] {sender, message} );
    }
}
