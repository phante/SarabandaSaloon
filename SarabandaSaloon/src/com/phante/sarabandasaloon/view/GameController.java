/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.view;

import com.phante.sarabandasaloon.Song;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class GameController implements Initializable {

    @FXML
    private Label currentTitle = new Label();
    @FXML
    private Label currentAlbum = new Label();
    @FXML
    private Label currentArtist = new Label();
    @FXML
    private Label currentTotalDuration = new Label();

    @FXML
    private Label timer = new Label();
    @FXML
    private TextField timerValue = new TextField();

    @FXML
    private ProgressBar progress = new ProgressBar();
    @FXML
    private ProgressBar progressTimer = new ProgressBar();

    @FXML
    private ToggleButton timerSwitch = new ToggleButton();

    @FXML
    private TableView<Song> songTable = new TableView<>();
    @FXML
    private TableColumn<Song, String> songIDColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songTitleColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songArtistColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> songAlbumColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> songPlayedColumn = new TableColumn<>();

    @FXML
    private Button playButton = new Button();
    @FXML
    private Button rewindButton = new Button();

    private Song currentSong = null;
    private final ObservableList<Song> players = FXCollections.observableArrayList();

    private ChangeListener<Duration> progressChangeListener;

    private StringProperty statusMessage = new SimpleStringProperty();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusMessage.set("Inizializzazione del gioco in corso");

        // Imposta la tabella delle canzoni a selezione singola
        songTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Forza la disabilitazione dei pulsanti di play/rewind
        playButton.setDisable(true);
        rewindButton.setDisable(true);

        // Imposta il listener per identificare il click sulla tabella
        songTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldSong, newSong) -> {
                    songSelection(newSong);
                }
        );

        // Imposta la tabella
        songIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
        songTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        songAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        songArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        songPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        songPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        statusMessage.set("Inizializzazione delle tracce audio completata");
    }

    /**
     * Imposta lo stato dell'interfaccia con la canzone selezionata
     *
     * @param song
     */
    private void songSelection(Song song) {
        if (currentSong != null) {
            // Ferma la riproduzione della traccia
            if (currentSong.status() == Status.PLAYING) {
                currentSong.stop();
                playButton.setText("Play");
            }
            // Elimina il listener sui progressi
            currentSong.getPlayer().currentTimeProperty().removeListener(progressChangeListener);
        }

        // 
        if (song == null) {
            // Sisabilita i pulsanti
            playButton.setDisable(true);
            rewindButton.setDisable(true);
        } else {
            // Abilita i pulsanti e imposta i dati
            playButton.setDisable(false);
            rewindButton.setDisable(false);

            // Assegna la canzone corrente
            currentSong = song;

            // Imposta la traccia all'inizio
            currentSong.rewind();

            // Imposta l'interfaccia
            timer.setText("00:00.00"); // resetta l'etichetta del timer
            progress.setProgress(0); // resetta la progressbar complessiva
            progressTimer.setProgress(0); // resetta la progressbar del timer

            // Gestisce le impostazioni del timer
            handleTimer();

            // Imposta le etichette con i metadati
            currentTitle.setText(currentSong.getTitle());
            currentAlbum.setText(currentSong.getArtist());
            currentArtist.setText(currentSong.getAlbum());
            currentTotalDuration.setText(currentSong.getTotalDuration());

            // Imposta il listener per le progressbar e il segnatempo
            progressChangeListener
                    = (ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) -> {
                        Duration currentTime = currentSong.getPlayer().getCurrentTime();

                        // Abilita o disabilita le gestione della progress bar del timer
                        if (timerSwitch.selectedProperty().getValue()) {
                            progressTimer.setDisable(false);
                            Double maxTime = getTimerValue().toMillis() < currentSong.getDuration().toMillis() ? getTimerValue().toMillis() : currentSong.getDuration().toMillis();
                            progressTimer.setProgress(1.0 * currentTime.toMillis() / maxTime);
                        }

                        progress.setProgress(1.0 * currentTime.toMillis() / currentSong.getDuration().toMillis());

                        DecimalFormat format = new DecimalFormat("00");
                        String minute = format.format((int) Math.floor((currentTime.toMillis() / 1000) / 60));
                        String second = format.format((int) Math.floor((currentTime.toMillis() / 1000) % 60));
                        String millis = format.format((int) Math.floor((currentTime.toMillis() % 1000) / 10));

                        String timeStr = new StringBuilder()
                        .append(minute)
                        .append(":")
                        .append(second)
                        .append(".")
                        .append(millis)
                        .toString();
                        timer.setText(timeStr);
                    };
            currentSong.getPlayer().currentTimeProperty().addListener(progressChangeListener);

            currentSong.getPlayer().setOnStopped(() -> {
                System.out.println("setOnStopped: " + currentSong.getPlayer().getCurrentTime());
                playButton.setText("Play");
            });

            currentSong.getPlayer().setOnEndOfMedia(() -> {
                System.out.println("setOnEndOfMedia: " + currentSong.getPlayer().getCurrentTime());
                playButton.setText("Play");
            });

            currentSong.getPlayer().setOnPlaying(() -> {
                System.out.println("setOnPlaying: " + currentSong.getPlayer().getCurrentTime());
                playButton.setText("Pause");
            });

            currentSong.getPlayer().setOnPaused(() -> {
                System.out.println("setOnPaused: " + currentSong.getPlayer().getCurrentTime());
                playButton.setText("Play: " + currentSong.getPlayer().getCurrentTime());
            });
        }
    }

    /**
     * gestisce il pulsante play
     */
    @FXML
    private void handlePlaySong() {
        if (currentSong != null) {
            if ("Pause".equals(playButton.getText())) {
                currentSong.pause();
            } else {
                currentSong.play();
            }
        }
    }

    /**
     *
     */
    @FXML
    private void handleTimer() {
        if (timerSwitch.selectedProperty().getValue()) {
            // Disabilita la modifica del timer e abilita la progressbar
            timerValue.setDisable(true);
            progressTimer.setDisable(false);

            if (currentSong != null) {
                currentSong.getPlayer().setStopTime(getTimerValue());
            }
        } else {
            // Abilita la modifica del timer e disaabilita la progressbar
            timerValue.setDisable(false);
            progressTimer.setProgress(0);
            progressTimer.setDisable(true);

            if (currentSong != null) {
                Duration currentTime = currentSong.getPlayer().getCurrentTime();
                currentSong.getPlayer().seek(currentTime);
                currentSong.getPlayer().setStopTime(currentSong.getDuration());
            }
        }
    }

    /**
     * Estrae il valore del timeout
     *
     * @return
     */
    private Duration getTimerValue() {
        return Duration.seconds(Double.parseDouble(timerValue.textProperty().getValue()));
    }

    /**
     * Gestisce il pulsante rewind
     */
    @FXML
    private void handleRewindSong() {
        if (currentSong != null) {
            currentSong.stop();
            currentSong.rewind();
        }
    }

    /**
     * Carica la lista dei file audio con estensione mp3 o m4a dal path indicato
     *
     * @param path
     */
    public void loadMediaListFromDirectory(String path) {
        // Ripulisce il contenuto della lista
        players.clear();

        // Legge i file e crea i player
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), "*.{mp3, m4a}")) {
            int k = 1;
            for (Path entry : stream) {
                players.add(new Song(k++, entry.toUri().toURL().toString()));

            }
        } catch (IOException ex) {
            Logger.getLogger(GameController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        // Carica la lista sulla tabella
        songTable.setItems(players);
    }

    /**
     *
     * @return
     */
    public StringProperty statusProperty() {
        return this.statusMessage;
    }

}
