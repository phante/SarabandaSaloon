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
package com.phante.sarabandasaloon.ui;

import com.phante.sarabandasaloon.SarabandaSaloon;
import com.phante.sarabandasaloon.entity.Game;
import com.phante.sarabandasaloon.entity.TrackList;
import com.phante.sarabandasaloon.network.SarabandaSlaveController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class RootController implements Initializable {

    private File configPath;
    //private Stage primaryStage;
    
    @FXML
    private TabPane tabPane;


    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Prima di inizializzare la finestra principale verifica se esiste un path con la configurazione
            configPath = checkDefaultDirectoryPath();
        
            // Carico il controller delle tracklist
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TrackList.fxml"));
            Pane trackListPane = loader.load();
            ((TrackListController) loader.getController()).setConfigPath(configPath);
            
            // Aggiunge la gestione delle tracklist alla schermata principale
            Tab trackListTab = new Tab();
            trackListTab.setContent(trackListPane);
            trackListTab.setText("Gestione TrackList");
            tabPane.getTabs().add(trackListTab);
            


            
            
            /*FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Pane gamePage = gameLoader.load();
            gameController = (GameController) gameLoader.getController();
            
            Tab gameTab = new Tab();
            gameTab.setContent(gamePage);
            gameTab.setText("Game");
            
            TrackList tl = new TrackList("Nuova tracklist");
            String source = getDefaultDirectoryFilePath().getAbsolutePath();
            tl.loadMediaListFromDirectory(source);
            saveTrackListDataToFile(tl);*/

            //tl.loadMediaListFromDirectory("/Users/elvisdeltedesco/Music/iTunes/iTunes Music/Music/AC_DC/Black Ice");
            //gameController.setGame(new Game(tl));

            /*
             FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("Network.fxml"));
             Pane documentPage = contentLoader.load();
             networkController = (NetworkController) contentLoader.getController();

             Tab documentTab = new Tab();
             documentTab.setContent(documentPage);
             documentTab.setText("Document");
             */
            //tabPane.getTabs().setAll(gameTab);

            // Avvia il server
            SarabandaSlaveController.getInstance().startServer();
        } catch (IOException ex) {
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        /**
     * Verifica se nella configurazione del sistema è già presente un path
     *
     * @return
     */
    private File checkDefaultDirectoryPath() {
        // Carica il path dalle impostazioni
        Preferences prefs = Preferences.userNodeForPackage(SarabandaSaloon.class);
        String filePath = prefs.get("SarabandaPath", null);
        
        Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Il path di default della configurazione è {0}", filePath);
        
        File path;
        if (filePath == null) {
            configPath = newDefaultDirectoryPathDialog();
        } else {
            configPath = new File(filePath);
        }

        while (!configPath.exists()) {
            // Alert per path non esistente
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Errore nel caricamenteo del path");
            alert.setContentText("Il path indicato non esiste.");
            alert.showAndWait();

            // Se il path indicato non esiste
            configPath = newDefaultDirectoryPathDialog();
        }
        
        if (!configPath.getPath().equals(filePath)) {
            Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Salvo {0} nelle impostazioni di default", configPath.getPath());
            prefs.put("SarabandaPath", configPath.getPath());
        }

        return configPath;
    }

    /**
     *
     * @return
     */
    private File newDefaultDirectoryPathDialog() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleziona la directory di configurazione del Sarabanda");
        return dirChooser.showDialog(null);
    }

    /* @FXML
    public void handleErrorMenu() {
    gameController.errorGame();
    }*/

    /*@FXML
    public void handleCorrectMenu() {
    gameController.goodGame();
    }*/
}
