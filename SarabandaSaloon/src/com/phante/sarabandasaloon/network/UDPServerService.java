/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author deltedes
 */
public class UDPServerService extends Service<Void> {
    
    private static final Logger log = Logger.getLogger(UDPServerService.class.getName());
    
    private static final int BufferSize = 20;

    private final int udpPort;
    private final MessageController messageController;

    private DatagramSocket socket;

    public UDPServerService(MessageController messageController, int udpPort) {
        this.messageController = messageController;
        this.udpPort = udpPort;
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
                    // Apre il socket
                    socket = new DatagramSocket(udpPort);
                    socket.setBroadcast(true);
                    log.log(Level.INFO, "Server inizialized on {0}:{1}", new Object[] {socket.getLocalAddress().getHostAddress(), socket.getLocalPort()} );

                    // Loop principale che controlla lo stato del task e lo rende interrompibile
                    while (!isCancelled()) {
                        log.log(Level.INFO, "Server is listening");

                        // Riceve un pacchetto
                        byte[] recvBuf = new byte[BufferSize];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);
                        
                        String hostAddress = packet.getAddress().getHostAddress();
                        byte[] message = packet.getData();
                        
                        log.log(Level.INFO, "Message {1} received from {0}", new Object[] {hostAddress, new String(message)} );
                        
                        // Elabora il messaggio
                        if (!isCancelled()) {
                            messageController.setMessage(message, hostAddress);
                        } else {
                            log.log(Level.INFO, "Server stop request, ignore the message");
                            // Chiude il socket
                            socket.close();
                        }
                    }
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
                log.log(Level.INFO, "Server is stopped");
                return null;
            }
        };
    }
}
