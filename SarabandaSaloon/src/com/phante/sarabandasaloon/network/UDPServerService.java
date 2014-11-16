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

    private int port = 5005;
    
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
                    // Apre il socket UDP sulla porta di default
                    DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
                    socket.setBroadcast(true);

                    while (true) {
                        System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");

                        // Riceve un pacchetto
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        // Pacchetto ricevuto
                        System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                        System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

                        // Estrare il pacchetto e la stringa relativa per l'uotput (a chissà cosa servirà :-) )
                        byte[] message = packet.getData();
                        
                        // Elabora il pacchetto
                        
                        
                        /*if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                            byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                            //Send a response
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                            socket.send(sendPacket);

                            System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                        }*/
                    }
                } catch (IOException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
    }
}
