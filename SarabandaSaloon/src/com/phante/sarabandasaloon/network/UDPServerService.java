/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author deltedes
 */
public class UDPServerService extends Service<Void> {

    private final int udpPort;
    private final String inetAddress;
    private final MessageController messageController;

    private DatagramSocket socket;
    private static final Logger log = Logger.getLogger(UDPServerService.class.getName());

    public UDPServerService(MessageController messageController, int udpPort) {
        this.messageController = messageController;
        this.udpPort = udpPort;
        this.inetAddress = "0.0.0.0";
    }

    /**
     *
     * @param messageController
     * @param udpPort
     * @param inetAddress
     */
    /*public UDPServerService(MessageController messageController, int udpPort, String inetAddress) {
     this.messageController = messageController;
     this.udpPort = udpPort;
     this.inetAddress = inetAddress;
     }*/
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
                    log.log(Level.INFO, "Server inizialization");
                    log.log(Level.INFO, "Server inizialized on " + inetAddress + ":" + udpPort);

                    //Keep a socket open to listen to all the UDP trafic that is destined for this port
                    socket = new DatagramSocket(udpPort, InetAddress.getByName(inetAddress));
                    socket.setBroadcast(true);

                    // Rende interrompibile il task
                    while (!isCancelled()) {
                        log.log(Level.FINE, "Server is listening");

                        //Receive a packet
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        System.out.println(">>>Packet received from: " + packet.getAddress().getHostAddress());
                        System.out.println(">>>Packet received; data: " + new String(packet.getData()));

                        // Compute the message
                        messageController.setMessage(new String(packet.getData()), packet.getAddress().getHostAddress());
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
