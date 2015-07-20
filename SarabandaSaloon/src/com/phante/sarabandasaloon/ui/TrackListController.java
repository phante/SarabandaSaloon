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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

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
    private TableView<Song> songTable = new TableView<>();
    @FXML
    private TableColumn<Song, String> idColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> titleColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> artistColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> albumColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> fileNameColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> playedColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> OKColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> KOColumn = new TableColumn<>();

    @FXML
    private MenuItem moveUpMenuItem = new MenuItem();
    @FXML
    private MenuItem moveDownMenuItem = new MenuItem();

    @FXML
    private Button duplicateTrackListButton = new Button();
    @FXML
    private Button deleteTrackListButton = new Button();

    @FXML
    private Button chooseSong = new Button();
    @FXML
    private Button saveTrackListButton = new Button();

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
        saveTrackListButton.setDisable(true);

        duplicateTrackListButton.setDisable(true);
        deleteTrackListButton.setDisable(true);

        // Imposta i dati per la colonna dei nomi delle tracklist
        trackListNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Imposta le tabella a selezione singola
        trackListTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Imposta il listener per identificare il click sulla tabella
        trackListTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldValue, newValue) -> {
                    Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Carico la tracklist: {0}", newValue.getName());

                    activeTrackListWrapper = newValue;
                    chooseSong.setDisable(false);

                    duplicateTrackListButton.setDisable(false);
                    deleteTrackListButton.setDisable(false);

                    loadTrackListData(newValue);
                }
        );

        // Carica le tracklist esistenti da file system
        loadTraklistFromConfiguration();

        // Aggiunge la sorgente dati alla tabella
        trackListTable.setItems(trackListArray);

        // Imposta le singole colonne
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        playedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        playedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        OKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        OKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        KOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        KOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Imposta le tabella delle canzoni a selezione singola
        songTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        songTable.getSelectionModel().getSelectedItems().addListener((Change<? extends Song> change) -> {
            if (change.getList().size() <= 1) {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione singola");
                moveUpMenuItem.setDisable(false);
                moveDownMenuItem.setDisable(false);
            } else {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione multipla");
                moveUpMenuItem.setDisable(true);
                moveDownMenuItem.setDisable(true);
            }
        });

        //trackListTable.getSelectionModel().selectedItemProperty().addListener(
        //        (observedValue, oldValue, newValue) -> {

        /*((observable, oldValue, newValue) -> {
         filteredData.setPredicate(song -> {
         // Se il filtro è vuto mostra tutte le canzoni
         if (newValue == null || newValue.isEmpty()) {
         return true;
         }
        
         String lowerCaseFilter = newValue.toLowerCase();
         boolean found = false;
         // Verifica che l'id contenga il testo
         found = found || song.getId().toLowerCase().contains(lowerCaseFilter);
         // Verifica che il titolo contenga il testo
         found = found || song.titleProperty().getValue().toLowerCase().contains(lowerCaseFilter);
         return found;
         });
         })*/
    }

    /**
     * Carica la tracklist sull'interfaccia
     *
     * @param trackList
     */
    private void loadTrackListData(TrackListWrapper tlw) {
        trackListTable.setDisable(true);
        songTable.setDisable(true);

        // Sposto il caricamento su un thread a parte per non bloccare l'interfaccia
        Platform.runLater(() -> {
            saveTrackListButton.setDisable(true);

            // Carica la tracklist completa
            activeTrackList = TrackList.getTrackList(tlw.getFile());

            // Crea le versioni filtrate e ordinate della tabella
            FilteredList<Song> filteredData = new FilteredList<>(activeTrackList.songListProperty(), p -> true);
            SortedList<Song> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(songTable.comparatorProperty());

            songTable.setItems(activeTrackList.songListProperty());

            // Imposta il listener per identificare il click sulla tabella
            /*songTable.getSelectionModel().selectedItemProperty().addListener(
             (observedValue, oldSong, newSong) -> {
             activeSong = newSong;
             }
             );*/
            trackListTable.setDisable(false);
            songTable.setDisable(false);
            Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "La tracklist {0} ha {1} elementi", new Object[]{activeTrackList.getName(), activeTrackList.songListProperty().size()});
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
     * Calcola il nome del file della tracklist
     *
     * @param trackList
     * @return
     */
    private String getTrackListFileName(TrackList trackList) {
        StringBuilder strBuf = new StringBuilder();
        String trackListName = trackList.getName().toLowerCase().replace(" ", "_");

        strBuf.append(configPath.getPath())
                .append("//")
                .append(trackListName)
                .append(".xml");
        return strBuf.toString();
    }

    /**
     * Crea una nuova tracklist da zero
     *
     */
    @FXML
    public void handleNewTrackList() {
        Optional<String> result = Optional.empty();
        boolean exists;

        // Cicla finchè il nome non è univoco
        do {
            exists = false;
            TextInputDialog dialog = new TextInputDialog("Nuova tracklist");
            dialog.setTitle("Crea nuova tracklist");
            dialog.setHeaderText("Definisci il nome della nuova tracklist");
            dialog.setContentText("Nome: ");

            result = dialog.showAndWait();

            if (result.isPresent()) {
                // Verifica se il nome esiste già
                for (TrackListWrapper tlw : trackListArray) {
                    if (result.get().equals(tlw.getName())) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Nome duplicato");
                        alert.setHeaderText("Il nome della tracklist è duplicato.");
                        alert.setContentText(null);

                        alert.showAndWait();
                        exists = true;
                    }
                }
            }
        } while (exists);

        // 
        if (result.isPresent()) {
            TrackList trackList = new TrackList(result.get());
            File newFile = new File(getTrackListFileName(trackList));
            trackList.saveTrackList(newFile);

            trackListArray.add(new TrackListWrapper(trackList, newFile));
        }

        saveTrackListButton.setDisable(false);
    }

    @FXML
    public void handleDuplicateTrackList() {
        Optional<String> result = Optional.empty();
        boolean exists;

        // Cicla finchè il nome non è univoco
        do {
            exists = false;
            TextInputDialog dialog = new TextInputDialog("Copia di " + activeTrackListWrapper.getName());
            dialog.setTitle("Duplica la tracklist");
            dialog.setHeaderText("Definisci il nome della nuova tracklist");
            dialog.setContentText("Nome: ");

            result = dialog.showAndWait();

            if (result.isPresent()) {
                // Verifica se il nome esiste già
                for (TrackListWrapper tlw : trackListArray) {
                    if (result.get().equals(tlw.getName())) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Nome duplicato");
                        alert.setHeaderText("Il nome della tracklist è duplicato.");
                        alert.setContentText(null);

                        alert.showAndWait();
                        exists = true;
                    }
                }
            }
        } while (exists);

        if (result.isPresent()) {
            // Carico da file la nuova tracklist
            TrackList newTrackList = TrackList.getTrackList(activeTrackListWrapper.getFile());
            // Imposto il nome
            newTrackList.setName(result.get());

            File newFile = new File(getTrackListFileName(newTrackList));
            newTrackList.saveTrackList(newFile);

            TrackListWrapper newTrackListWrapper = new TrackListWrapper(newTrackList, newFile);
            trackListArray.add(newTrackListWrapper);
            activeTrackListWrapper = newTrackListWrapper;
            trackListTable.getSelectionModel().select(activeTrackListWrapper);
        }
    }

    @FXML
    public void handleDeleteTracklist() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Conferma cancellazione");
        alert.setHeaderText("Se sicuro di voler cancellare a tracklist?");
        alert.setContentText("La cancellazione della tracklist è irreveresibile.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            activeTrackListWrapper.getFile().delete();
            trackListTable.getItems().remove(activeTrackListWrapper);
            trackListTable.getSelectionModel().selectFirst();
        }

    }

    public void addSongFile() {
        if (activeTrackList != null) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Seleziona la directory con le canzoni da caricare");
            File path = dirChooser.showDialog(null);
            activeTrackList.addMediaListFromDirectory(path);

            songTable.setItems(activeTrackList.songListProperty());

            saveTrackListButton.setDisable(false);
        }
    }

    @FXML
    public void handleSaveTrackList() {
        System.out.println(activeTrackList);
        System.out.println(activeTrackListWrapper);
        Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Salva la tracklist {0} su file {1}", new Object[]{activeTrackList.getName(), activeTrackListWrapper.getFile().getPath()});
        activeTrackList.saveTrackList(activeTrackListWrapper.getFile());

        saveTrackListButton.setDisable(true);
    }

    @FXML
    public void handleMoveUp() {
        TableViewSelectionModel<Song> sm = songTable.getSelectionModel();
        
        Song selectedSong = sm.selectedItemProperty().getValue();
        activeTrackList.moveUp(selectedSong);
        sm.clearSelection();
        sm.select(selectedSong);

        saveTrackListButton.setDisable(false);
    }

    @FXML
    public void handleMoveDown() {
        TableViewSelectionModel<Song> sm = songTable.getSelectionModel();
        
        Song selectedSong = sm.selectedItemProperty().getValue();
        activeTrackList.moveDown(selectedSong);
        sm.clearSelection();
        sm.select(selectedSong);

        saveTrackListButton.setDisable(false);
    }

    @FXML
    public void handleSongDeletion() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Conferma cancellazione");
        alert.setHeaderText("Se sicuro di voler cancellare i brani selezionati?");
        alert.setContentText("La cancellazione del brano dalla tracklist è irreveresibile ma il sistema non cancella il file audio originale che rimarrà disponibile sul disco.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            activeTrackList.removeAll(songTable.getSelectionModel().getSelectedItems());
            
            saveTrackListButton.setDisable(false);
        }
    }

    @FXML
    public void handlePrintTrackList() {
        Printer printer = Printer.getDefaultPrinter();

        PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        double scaleX = pageLayout.getPrintableWidth() / songTable.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / songTable.getBoundsInParent().getHeight();
        songTable.getTransforms().add(new Scale(scaleX, scaleY));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(songTable);
            if (success) {
                job.endJob();
            }
        }
    }

    @FXML
    public void handleExportTocsv() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Salvo la tracklist in formato cvs sul file {0}", file.getPath());
            StringBuilder csvContents = new StringBuilder();

            // Estrae la lista degli header dalla tabella
            songTable.getColumns().stream().forEach((column) -> {
                csvContents.append(column.textProperty().getValue());
                csvContents.append(",");
            });
            csvContents.append("\n");

            // Creo il file
            for (Song song : songTable.getItems()) {
                songTable.getColumns().stream().forEach((column) -> {
                    switch (column.textProperty().getValue().toLowerCase()) {
                        case "id":
                            csvContents.append(song.idProperty().getValue());
                            break;
                        case "titolo":
                            csvContents.append(song.titleProperty().getValue());
                            break;
                        case "artista":
                            csvContents.append(song.artistProperty().getValue());
                            break;
                        case "album":
                            csvContents.append(song.albumProperty().getValue());
                            break;
                        case "file":
                            csvContents.append(song.fileNameProperty().getValue());
                            break;
                        default:
                            csvContents.append("");
                    }
                    csvContents.append(",");
                });
                csvContents.append("\n");
            }

            System.out.println(csvContents.toString());

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(csvContents.toString());
                writer.close();
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Tracklist salvata in formato cvs sul file {0}", file.getPath());
            } catch (IOException ex) {
                Logger.getLogger(TrackListController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
