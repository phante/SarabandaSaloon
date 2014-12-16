/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

/**
 *
 * @author deltedes
 */
public class MessageController {
    // Memorizza l'ultimo messaggio
    private String lastMessage;
    private String lastSender;
    
    public void setMessage(String message, String sender) {
        lastMessage = message;
        lastSender = sender; 
    }
}
