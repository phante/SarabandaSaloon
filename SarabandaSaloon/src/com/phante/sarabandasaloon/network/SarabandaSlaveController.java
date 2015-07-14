/*
 * Copyright 2015 Elvis Del Tedesco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phante.sarabandasaloon.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author deltedes
 */
public class SarabandaSlaveController extends SarabandaController {

    /**
     * Inizializza lo stato del controller andando a creare il servizio che si
     * occupa della lettura dei pacchetti di rete e i pulsanti.
     */
    private SarabandaSlaveController() {
        super();

        // Imposta la modalità di funzionamento per lo slave a nuovo
        udpSendPort = UDP_MASTER_PORT;
        udpListenPort = UDP_SLAVE_PORT;
        classicModeProperty.setValue(Boolean.FALSE);

        Logger.getLogger(SarabandaSlaveController.class.getName()).log(Level.INFO, "Impostazione dello slave in ascolto su porta {0} con invio su porta {1}", new Object[]{udpListenPort, udpSendPort});
        Logger.getLogger(SarabandaSlaveController.class.getName()).log(Level.INFO, "Impostazione delllo slave con invio messaggi verso ip {0}", broadcastAddress.getHostAddress());

    }

    public static SarabandaSlaveController getInstance() {
        return SarabandaSlaveControllerHolder.INSTANCE;
    }

    private static class SarabandaSlaveControllerHolder {
        private static final SarabandaSlaveController INSTANCE = new SarabandaSlaveController();
    }

    /**
     *
     * @param classicMode
     */
    @Override
    public void setClassicMode(boolean classicMode) {
        classicModeProperty.setValue(classicMode);
        if (classicMode) {
            udpSendPort = UDP_MASTER_PORT;
            udpListenPort = UDP_SLAVE_CLASSIC_PORT;
        } else {
            udpSendPort = UDP_MASTER_PORT;
            udpListenPort = UDP_SLAVE_PORT;
        }

        Logger.getLogger(SarabandaSlaveController.class.getName()).log(Level.INFO, "Cambio della modalità con ricezione alla porta {0}", udpSendPort);

        // Rigenera il server con la nuova porta di ascolto
        stopServer();
        initUDPService();
        startServer();
    }

    /**
     *
     * @param localhostOnly
     */
    @Override
    public void setLocalhostOnly(boolean localhostOnly) {
        onlyLocalhostModeProperty.setValue(localhostOnly);

        // Imposta l'indirizzo di broascast
        if (localhostOnly) {
            broadcastAddress = InetAddress.getLoopbackAddress();
        } else {
            try {
                broadcastAddress = InetAddress.getByName("255.255.255.255");
            } catch (UnknownHostException ex) {
                Logger.getLogger(SarabandaController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Logger.getLogger(SarabandaController.class.getName()).log(Level.INFO, "Cambio della modalità con invio su indirizzo {0}", broadcastAddress.getHostAddress());
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
        Logger.getLogger(SarabandaSlaveController.class.getName()).log(Level.INFO, "Effettuo il parsing del messaggio {0}", message);
        // Verifico il comando
        parseButtonMessage(message);
    }
}
