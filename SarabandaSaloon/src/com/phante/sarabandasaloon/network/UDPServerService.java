/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author deltedes
 */
public class UDPServerService extends Service<Void> {

    // Dimensione massima del buffer di ricezione
    private static final int BUFFERSIZE = 256;
    // Porta udp del server
    private final int serverUdpPort;
    
    // Property per il contenuto del messaggio
    public final StringProperty packet = new SimpleStringProperty();
    // Propertry per il contenuto del sender
    public final StringProperty sender = new SimpleStringProperty();
    
    private final String messageRegex;

    /**
     *
     * @param udpPort
     */
    public UDPServerService(int udpPort) {
        this.serverUdpPort = udpPort;
        messageRegex = new StringBuilder()
                .append("^")
                .append(SarabandaController.MESSAGE_HEADER)
                .append(".+")
                .toString();
    }
    
    /**
     *
     * @return
     */
    public StringProperty packetProperty() {
        return packet;
    }
    
    /**
     *
     * @return
     */
    public StringProperty senderProperty() {
        return sender;
    }

    /**
     *
     * @return
     */
    @Override
    protected Task<Void> createTask() {

        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Logger.getLogger(UDPServerService.class.getName()).log(Level.INFO, "Avvio il server UDP");
                    // Apre il socket
                    DatagramSocket socket = new DatagramSocket(serverUdpPort);
                    socket.setBroadcast(true);

                    // Loop principale che controlla lo stato del task e lo rende interrompibile
                    while (!isCancelled()) {

                        // Riceve un pacchetto
                        byte[] recvBuf = new byte[BUFFERSIZE];
                        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(recvPacket);

                        if (!isCancelled()) {
                            // Valuto se è un messaggio Sarabanda Valido
                            String message = new String(recvPacket.getData());
                            message = message.trim();
                            
                            Logger.getLogger(UDPServerService.class.getName()).log(Level.INFO, 
                                    "Ricevuto il messaggio {0} che {1} un messaggio Sarabanda valido secondo {2}", 
                                    new Object[]{message, (message.matches(messageRegex)? "è":"non è"), messageRegex} );
                                                       
                            if (message.matches(messageRegex)) {
                                sender.setValue(recvPacket.getAddress().getHostAddress());
                                packet.setValue(message);
                            }
                        } else {
                            // Chiude il socket
                            Logger.getLogger(UDPServerService.class.getName()).log(Level.ALL, "Spengo il server UDP");
                            socket.close();
                        }
                    }
                } catch (BindException ex) {
                    Logger.getLogger(UDPServerService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPServerService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UDPServerService.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
    }

}
