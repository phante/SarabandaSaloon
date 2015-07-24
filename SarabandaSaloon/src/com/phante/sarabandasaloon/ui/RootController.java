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

import com.phante.sarabandasaloon.entity.Game;
import com.phante.sarabandasaloon.entity.PreferencesUtility;
import com.phante.sarabandasaloon.network.SarabandaSlaveController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class RootController implements Initializable {

    private Stage primaryStage;
    
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
            checkDefaultDirectoryPath();
        
            // Carico il controller delle tracklist
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TrackList.fxml"));
            Pane trackListPane = loader.load();
            TrackListController trackListController = (TrackListController) loader.getController();
            trackListController.setParent(this);
            
            // Aggiunge la gestione delle tracklist alla schermata principale
            Tab trackListTab = new Tab();
            trackListTab.setContent(trackListPane);
            trackListTab.setText("Gestione TrackList");
            tabPane.getTabs().add(trackListTab);
            
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
    
    public void addNewGameTab(Game game) {
        try {
            //Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Creo un tab per il gioco {0}", game.getName() );
            
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Pane gamePage = gameLoader.load();
            GameController gameController = (GameController) gameLoader.getController();
            gameController.setGame(game);
            
            Tab gameTab = new Tab();
            gameTab.setContent(gamePage);
            gameTab.setText(game.getName());
            
            tabPane.getTabs().add(gameTab);
            
            // Evidenzia il tab
            tabPane.getSelectionModel().select(gameTab);
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
        String filePath = PreferencesUtility.get(PreferencesUtility.BASE_PATH);
        File configPath;
        
        //Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Il path di default della configurazione è {0}", filePath);
        
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
            //Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Salvo {0} nelle impostazioni di default", configPath.getPath());
            PreferencesUtility.set(PreferencesUtility.BASE_PATH, configPath.getPath());
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
    
    @FXML
    private void handleConfig () throws IOException {
        // Carico il controller delle tracklist
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Configuration.fxml"));
        DialogPane configPane = (DialogPane)loader.load();
        
        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifica le impostazioni");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);        

        Scene scene = new Scene(configPane);
        dialogStage.setScene(scene);
        
        // Set the person into the controller.
        ConfigurationController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        //controller.setPerson(person);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
    }
    
    /**
     * 
     * @param stage 
     */
    public void setStage(Stage stage) {
        primaryStage = stage;
    }
}
