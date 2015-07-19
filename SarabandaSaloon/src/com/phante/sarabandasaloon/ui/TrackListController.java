/*
 * Copyright 2015 Elvis Del Tedesco.
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
import com.phante.sarabandasaloon.entity.Song;
import com.phante.sarabandasaloon.entity.TrackList;
import com.phante.sarabandasaloon.entity.TrackListWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author elvisdeltedesco
 */
public class TrackListController implements Initializable {

    @FXML
    private TableView<TrackListWrapper> trackListTable = new TableView();
    @FXML
    private TableColumn<TrackListWrapper, String> trackListNameColumn = new TableColumn();

    // Tabella con la lista delle canzoni
    @FXML
    private TableView<Song> songTable;
    
    @FXML
    private Button chooseSong = new Button();

    // Path della configurazione delle tracklist
    private File configPath = null;

    // Lista delle tracklist
    private ObservableList<TrackListWrapper> trackListArray = FXCollections.observableArrayList();
    // Tracklist attiva
    private TrackListWrapper activeTrackListWrapper;
    private TrackList activeTrackList;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        songTable = new SongTableView();
                
        // Imposta i dati per la colonna dei nomi delle tracklist
        trackListNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Imposta le tabella a selezione singola
        trackListTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Imposta il listener per identificare il click sulla tabella
        trackListTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldValue, newValue) -> {
                    chooseSong.setDisable(false);
                    loadTrackListData(newValue);
                }
        );

        // Carica le tracklist esistenti da file system
        loadTraklistFromConfiguration();

        // Aggiunge la sorgente dati alla tabella
        trackListTable.setItems(trackListArray);
    }

    /**
     * Carica la tracklist sull'interfaccia
     *
     * @param trackList
     */
    private void loadTrackListData(TrackListWrapper tlw) {
        // Sposto il caricamento su un thread a parte per non bloccare l'interfaccia
        Platform.runLater(() -> {
            // Carica la tracklist completa
            activeTrackList = TrackList.getTrackList(tlw.getFile());

            songTable.setItems(activeTrackList.songListProperty());
        });
    }

    /**
     *
     */
    public void loadTraklistFromConfiguration() {
        Platform.runLater(() -> {

            trackListArray.clear();
            configPath = new File(PreferencesUtility.get(PreferencesUtility.BASE_PATH) + PreferencesUtility.TRACKLIST_DIRECTORY);

            // Creo la directory se non esiste
            if (!configPath.exists()) {
                configPath.mkdir();
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(configPath.toPath(), "*.xml")) {
                for (Path entry : stream) {
                    TrackListWrapper trackListWrapper = TrackListWrapper.loadTrackListWrapperFromFile(entry.toFile());

                    Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Caricata la tracklist {0} dal file {1}", new Object[]{trackListWrapper.getName(), trackListWrapper.getFile().getPath()});

                    trackListArray.add(trackListWrapper);

                }
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
    }

    /**
     * Crea una nuova tracklist da zero
     *
     */
    @FXML
    public void createNewTrackList() {
        TextInputDialog dialog = new TextInputDialog("Nuova tracklist");
        dialog.setTitle("Crea nuova tracklist");
        dialog.setHeaderText("Definisci il nome della nuova tracklist");
        dialog.setContentText("Nome: ");

        // The Java 8 way to get the response value (with lambda expression).
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            TrackList trackList = new TrackList(result.get());
            File newFile = new File(configPath.getPath() + "//" + trackList.getName() + ".xml");
            trackList.saveTrackList(newFile);

            trackListArray.add(new TrackListWrapper(trackList, newFile));
        }
    }

    public void addSongFile() {
        if (activeTrackList != null) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Seleziona la directory con le canzoni da caricare");
            File path = dirChooser.showDialog(null);
            activeTrackList.addMediaListFromDirectory(path);
            
            songTable.setItems(activeTrackList.songListProperty());
        }

    }
}
