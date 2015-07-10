/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

import com.phante.sarabandasaloon.ButtonStatus;
import com.phante.sarabandasaloon.SarabandaButton;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;

/**
 *
 * @author deltedes
 */
public class SarabandaController {

    // Numero dei pulsanti del sarabanda
    private final static int BUTTON_NUMBER = 4;

    // Stati del server udp
    public final static int SERVER_STARTED = 0;
    public final static int SERVER_UNKNOWN = 1;
    public final static int SERVER_STOPPED = 2;

    // Header standard del pacchetto Sarabanda
    public final static String MESSAGE_HEADER = "SRBND-";

    // Comandi sarabanda validi
    public final static String RESET_COMMAND = "RESET";
    public final static String FULLRESET_COMMAND = "FULLRESET";
    public final static String ERROR_COMMAND = "ERROR";
    public final static String DEMO_COMMAND = "DEMO";
    public final static String HWRESET_COMMAND = "X";
    public final static String BUTTON_COMMAND = "B";
    
    public final String buttonCommandRegex;

    // Porta di ascolto
    private final static int UDPPORT = 8888;

    // Memorizzare l'indirizzo di broadcast
    private InetAddress broadcastAddress;

    // Server UDP per la comunicazione con il master
    private UDPServerService udpservice;
    // Memorizza lo stato del server
    private final ReadOnlyIntegerWrapper serverStatus = new ReadOnlyIntegerWrapper();

    // Stato dei pulsanti
    public final ObservableList<SarabandaButton> buttons = FXCollections.observableArrayList();

    /**
     * Inizializza lo stato del controller andando a creare il servizio che si
     * occupa della lettura dei pacchetti di rete e i pulsanti.
     */
    private SarabandaController() {
        // Inizializza il Service UDP
        initUDPService(UDPPORT);

        // Crea i pulsanti del sarabanda
        for (int i = 0; i < BUTTON_NUMBER; i++) {
            buttons.add(new SarabandaButton());
        }
        
        // Espressione regolare per identificare un pacchetto pulsanti valido
        StringBuffer regex = new StringBuffer()
                .append("^")
                .append(MESSAGE_HEADER)
                .append(BUTTON_COMMAND)
                .append("[");
        for (ButtonStatus status: ButtonStatus.values()) {
            regex.append(status);
        }
        regex.append("]{")
                .append(buttons.size())
                .append("}");
        
        buttonCommandRegex = regex.toString();

    }

    public static SarabandaController getInstance() {
        return SarabandaControllerHolder.INSTANCE;
    }

    private static class SarabandaControllerHolder {

        private static final SarabandaController INSTANCE = new SarabandaController();
    }

    /**
     * Inizializza il service UDP per la gestione della comunicazione di rete
     */
    private void initUDPService(int port) {
        try {
            // Imposta l'indirizzo di broascast
            //broadcastAddress = InetAddress.getByName("255.255.255.255");
            broadcastAddress = InetAddress.getLoopbackAddress(); // TEST per non spammare sulla rete

            InetAddress localAddress = InetAddress.getLocalHost();

            // Creao il servizio
            udpservice = new UDPServerService(port);

            // Listener per identificare l'arrivio di un nuovo pacchetto
            udpservice.packetProperty().addListener(((observableValue, oldValue, newValue) -> {
                // Ignoro i messaggi che arrivano da me stesso
                //if (localAddress.getHostAddress().equals(udpservice.senderProperty().getValue())) 
                {
                // Effettua il parsing del messaggio il messaggio, uso il runLater 
                    // per disaccoppiare i thread e consentire la modifica della UI
                    // dal thread principale
                    Platform.runLater(() -> {
                        parseMessage(newValue);
                    });
                }
            }));
        } catch (UnknownHostException ex) {
            Logger.getLogger(SarabandaController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Idetifico lo stato del server
        udpservice.setOnRunning(value -> {
            serverStatus.setValue(SERVER_STARTED);
        });
        udpservice.setOnScheduled(value -> {
            serverStatus.setValue(SERVER_UNKNOWN);
        });
        udpservice.setOnReady(value -> {
            serverStatus.setValue(SERVER_STOPPED);
        });
        udpservice.setOnCancelled(value -> {
            serverStatus.setValue(SERVER_STOPPED);
        });
        udpservice.setOnFailed(value -> {
            serverStatus.setValue(SERVER_STOPPED);
        });
    }

    /**
     * Effettua il parsing dei messaggi. Per definizione il software è uno slave
     * e quindi va a gestire solo ed esclusivamente i messaggi di tipo B in
     * quanto quelli normalmente inviati dal Master. I messaggi ERROR, FULLRESET
     * e RESET sono messaggi inviati dagli slave per comandare lo stato del
     * master.
     *
     * @param message
     */
    private void parseMessage(String message) {
        // Verifico il comando
        if (message.matches(buttonCommandRegex)) {
            String button = message.substring(message.length() - 4);
            for (int i = 0; buttons.size() > i; i++) {
                ButtonStatus status = ButtonStatus.parse(button.substring(i, i + 1));
                buttons.get(i).setStatus(status);
            }
        }
    }

    /**
     * Avvia il servizio UDP
     */
    public void startServer() {
        // Ripulisce lo stato del task
        if ((udpservice.getState() == Worker.State.CANCELLED) || (udpservice.getState() == Worker.State.FAILED)) {
            udpservice.reset();
        }

        // Se il server è pronto effettua lo start del service
        if (udpservice.getState() == Worker.State.READY) {
            udpservice.start();
        }
    }

    /**
     * Disattiva il servizio UDP invocando la cancellazione del servizio e
     * spedento un pacchetto per bypassare il fatto che la lettura del socket è
     * bloccante
     */
    public void stopServer() {
        if (udpservice.isRunning()) {
            // Invia al servizio il comando di spegnersi
            udpservice.cancel();

            // Invia un pacchetto UDP generico per andare a far uscire il server dallo stato di listen
            // necessario in quanto la lettura del socket è bloccante
            sendSarabandaMessage("");
        }
    }

    public void sendSarabandaReset() {
        sendSarabandaMessage(SarabandaController.RESET_COMMAND);
    }

    public void sendSarabandaFullReset() {
        sendSarabandaMessage(SarabandaController.FULLRESET_COMMAND);
    }

    public void sendSarabandaError() {
        sendSarabandaMessage(SarabandaController.ERROR_COMMAND);
    }

    public void sendSarabandaDemo() {
        sendSarabandaMessage(SarabandaController.DEMO_COMMAND);
    }

    public void sendSarabandaMasterPhysicalReset() {
        sendSarabandaMessage(SarabandaController.HWRESET_COMMAND);
    }

    /**
     * Invia un messaggio Sarabanda
     *
     * @param message
     */
    public void sendSarabandaMessage(String message) {
        sendPacket(new StringBuilder()
                .append(SarabandaController.MESSAGE_HEADER)
                .append(message)
                .toString(),
                UDPPORT,
                broadcastAddress);
    }

    /**
     * Invia un messaggio all'indirizzo specificato
     *
     * @param message
     * @param port
     * @param destination
     */
    public void sendPacket(String message, int port, InetAddress destination) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            DatagramPacket sendPacket = new DatagramPacket(
                    message.getBytes(),
                    message.getBytes().length,
                    destination,
                    port
            );
            socket.send(sendPacket);
        } catch (SocketException ex) {
            Logger.getLogger(SarabandaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SarabandaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public ReadOnlyIntegerProperty serverStatusProperty() {
        return serverStatus.getReadOnlyProperty();
    }

}
