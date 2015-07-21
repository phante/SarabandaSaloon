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
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TextField;
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
    private TableColumn<Song, String> songIdColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songTitleColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songArtistColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songAlbumColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songFileNameColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> songPlayedColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> songOKColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> songKOColumn = new TableColumn<>();

    @FXML
    private TableView<Song> finalTable = new TableView<>();
    @FXML
    private TableColumn<Song, String> finalIdColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalTitleColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalArtistColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalAlbumColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalFileNameColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalPlayedColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalOKColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalKOColumn = new TableColumn<>();

    @FXML
    private TextField searchTextField = new TextField();
    private ChangeListener<String> searchTextFieldChangeListener = (observableValue, oldValue, newValue) -> {
    };

    @FXML
    private MenuItem songMoveUpMenuItem = new MenuItem();
    @FXML
    private MenuItem songMoveDownMenuItem = new MenuItem();
    @FXML
    private MenuItem finalMoveUpMenuItem = new MenuItem();
    @FXML
    private MenuItem finalMoveDownMenuItem = new MenuItem();

    @FXML
    private Button duplicateTrackListButton = new Button();
    @FXML
    private Button deleteTrackListButton = new Button();

    @FXML
    private Button chooseSong = new Button();
    @FXML
    private Button saveTrackListButton = new Button();

    // Lista delle tracklist
    private ObservableList<TrackListWrapper> trackListArray = FXCollections.observableArrayList();

    // Tracklist attiva
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

        searchTextField.setDisable(true);

        // Imposta i dati per la colonna dei nomi delle tracklist
        trackListNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Imposta le tabella a selezione singola
        trackListTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Imposta il listener per identificare il click sulla tabella
        trackListTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldValue, newValue) -> {
                    Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Carico la tracklist: {0}", newValue.getName());
                    
                    boolean proceed = true;
                    
                    if (!saveTrackListButton.isDisabled()) {
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Cambio di tracklist");
                        alert.setHeaderText("Se sicuro di voler cambiare tracklist?");
                        alert.setContentText("La tracklist attuale è stata modificata e non hai salvato lo stato. Continuando annullerai tutte le modifiche.");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() != ButtonType.OK) proceed = false;
                    }
                    
                    if (proceed) {
                        chooseSong.setDisable(false);

                        duplicateTrackListButton.setDisable(false);
                        deleteTrackListButton.setDisable(false);

                        searchTextField.setDisable(false);

                        loadTrackListData(newValue);
                    }
                }
        );

        // Carica le tracklist esistenti da file system
        loadTraklistFromConfiguration();

        // Aggiunge la sorgente dati alla tabella
        trackListTable.setItems(trackListArray);

        // Imposta le singole colonne
        songIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        songTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        songAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        songArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        songFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        songPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        songPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        songOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        songKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Imposta le tabella delle canzoni a selezione multipla
        songTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        songTable.getSelectionModel().getSelectedItems().addListener((Change<? extends Song> change) -> {
            if (change.getList().size() <= 1) {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione singola");
                songMoveUpMenuItem.setDisable(false);
                songMoveDownMenuItem.setDisable(false);
            } else {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione multipla");
                songMoveUpMenuItem.setDisable(true);
                songMoveDownMenuItem.setDisable(true);
            }
        });

        finalIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        finalTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        finalAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        finalArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        finalFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        finalPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        finalPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        finalOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        finalKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Imposta le tabella delle canzoni a selezione multipla
        finalTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        finalTable.getSelectionModel().getSelectedItems().addListener((Change<? extends Song> change) -> {
            if (change.getList().size() <= 1) {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione singola");
                finalMoveUpMenuItem.setDisable(false);
                finalMoveDownMenuItem.setDisable(false);
            } else {
                Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Selezione multipla");
                finalMoveUpMenuItem.setDisable(true);
                finalMoveDownMenuItem.setDisable(true);
            }
        });

    }

    /**
     * Carica la tracklist sull'interfaccia
     *
     * @param trackList
     */
    private void loadTrackListData(TrackListWrapper tlw) {
        trackListTable.setDisable(true);
        songTable.setDisable(true);
        finalTable.setDisable(true);
        searchTextField.setDisable(true);

        // Sposto il caricamento su un thread a parte per non bloccare l'interfaccia
        Platform.runLater(() -> {
            saveTrackListButton.setDisable(true);

            // Carica la tracklist completa
            activeTrackList = TrackList.getTrackList(tlw.getFile());

            // Crea le versioni filtrate e ordinate della tabella
            FilteredList<Song> filteredData = new FilteredList<>(activeTrackList.songListProperty(), p -> true);

            // Rimuove il listener precedente
            searchTextField.textProperty().removeListener(searchTextFieldChangeListener);

            // Crea e assegna un nuovo listener
            searchTextFieldChangeListener = (observable, oldValue, newValue) -> {
                filteredData.setPredicate(song -> {
                    Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Filtro la tabella della canzoni con {0}", newValue);
                    // If filter text is empty, display all persons.
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
            };
            searchTextField.textProperty().addListener(searchTextFieldChangeListener);

            SortedList<Song> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(songTable.comparatorProperty());

            songTable.setItems(sortedData);
            finalTable.setItems(activeTrackList.finalSongsListProperty());

            trackListTable.setDisable(false);
            songTable.setDisable(false);
            finalTable.setDisable(false);
            searchTextField.setDisable(false);

            Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "La tracklist {0} ha {1} elementi", new Object[]{activeTrackList.getName(), activeTrackList.songListProperty().size()});
        });
    }

    /**
     *
     */
    public void loadTraklistFromConfiguration() {
        Platform.runLater(() -> {

            trackListArray.clear();
            File configPath = new File(PreferencesUtility.get(PreferencesUtility.BASE_PATH) + PreferencesUtility.TRACKLIST_DIRECTORY);

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

        File configPath = new File(PreferencesUtility.get(PreferencesUtility.BASE_PATH) + PreferencesUtility.TRACKLIST_DIRECTORY);

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

        TrackListWrapper selectedWrapper = trackListTable.getSelectionModel().selectedItemProperty().getValue();
        if (selectedWrapper == null) {
            return;
        }

        // Cicla finchè il nome non è univoco
        do {
            exists = false;

            TextInputDialog dialog = new TextInputDialog(
                    new StringBuffer()
                    .append("Copia di ")
                    .append(selectedWrapper.getName()).toString()
            );
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
            TrackList newTrackList = TrackList.getTrackList(selectedWrapper.getFile());
            // Imposto il nome
            newTrackList.setName(result.get());

            File newFile = new File(getTrackListFileName(newTrackList));
            newTrackList.saveTrackList(newFile);

            TrackListWrapper newTrackListWrapper = new TrackListWrapper(newTrackList, newFile);
            trackListArray.add(newTrackListWrapper);
            trackListTable.getSelectionModel().select(newTrackListWrapper);
        }
    }

    @FXML
    public void handleDeleteTracklist() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Conferma cancellazione");
        alert.setHeaderText("Se sicuro di voler cancellare a tracklist?");
        alert.setContentText("La cancellazione della tracklist è irreveresibile.");

        TrackListWrapper selectedWrapper = trackListTable.getSelectionModel().selectedItemProperty().getValue();
        if (selectedWrapper == null) {
            return;
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            selectedWrapper.getFile().delete();
            trackListTable.getItems().remove(selectedWrapper);
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
        Logger.getLogger(TrackListController.class.getName()).log(Level.INFO, "Salva la tracklist {0} su file {1}", new Object[]{activeTrackList.getName(), trackListTable.getSelectionModel().selectedItemProperty().getValue().getFile().getPath()});
        activeTrackList.saveTrackList(trackListTable.getSelectionModel().selectedItemProperty().getValue().getFile());

        saveTrackListButton.setDisable(true);
    }

    @FXML
    public void handleMoveUp() {
        TableViewSelectionModel<Song> sm = null;

        if (songTable.isFocused()) {
            sm = songTable.getSelectionModel();
        } else if (finalTable.isFocused()) {
            sm = finalTable.getSelectionModel();
        }

        if (sm != null) {
            Song selectedSong = sm.selectedItemProperty().getValue();
            activeTrackList.moveUp(selectedSong);
            sm.clearSelection();
            sm.select(selectedSong);

            saveTrackListButton.setDisable(false);
        }
    }

    @FXML
    public void handleMoveDown() {
        TableViewSelectionModel<Song> sm = null;

        if (songTable.isFocused()) {
            sm = songTable.getSelectionModel();
        } else if (finalTable.isFocused()) {
            sm = finalTable.getSelectionModel();
        }

        if (sm != null) {
            Song selectedSong = sm.selectedItemProperty().getValue();
            activeTrackList.moveDown(selectedSong);
            sm.clearSelection();
            sm.select(selectedSong);

            saveTrackListButton.setDisable(false);
        }
    }

    @FXML
    public void handleSongDeletion() {
        TableViewSelectionModel<Song> sm = null;
       
        if (songTable.isFocused()) {
            sm = songTable.getSelectionModel();
        } else if (finalTable.isFocused()) {
            sm = finalTable.getSelectionModel();
        }

        if (sm != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Conferma cancellazione");
            alert.setHeaderText("Se sicuro di voler cancellare i brani selezionati?");
            alert.setContentText("La cancellazione del brano dalla tracklist è irreveresibile ma il sistema non cancella il file audio originale che rimarrà disponibile sul disco.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                activeTrackList.removeAll(sm.getSelectedItems());

                saveTrackListButton.setDisable(false);
            }
        }
    }
    
    @FXML
    public void handleSwitchList() {
        TableViewSelectionModel<Song> sm = null;
        
        if (songTable.isFocused()) {
            sm = songTable.getSelectionModel();
        } else if (finalTable.isFocused()) {
            sm = finalTable.getSelectionModel();
        }
        
        activeTrackList.switchContext(sm.getSelectedItems());
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
