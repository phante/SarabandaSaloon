/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.view;

import com.phante.sarabandasaloon.ButtonStatus;
import com.phante.sarabandasaloon.Game;
import com.phante.sarabandasaloon.Song;
import com.phante.sarabandasaloon.network.SarabandaController;
import com.phante.sarabandasaloon.view.simbols.ButtonSimbol;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaMarkerEvent;
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
    private TextField songFilter = new TextField();

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
    private TableColumn<Song, Boolean> songOKColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> songKOColumn = new TableColumn<>();

    @FXML
    private TableView<Song> finalSongTable = new TableView<>();
    @FXML
    private TableColumn<Song, String> finalSongIDColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalSongTitleColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalSongArtistColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> finalSongAlbumColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalSongPlayedColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalSongOKColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Boolean> finalSongKOColumn = new TableColumn<>();

    @FXML
    private Button playButton = new Button();
    @FXML
    private Button rewindButton = new Button();

    private Song currentSong = null;
    private Game currentGame = null;

    private ChangeListener<Duration> progressChangeListener;

    @FXML
    private HBox buttonPane = new HBox();
    //private List<ButtonSimbol> buttonSimbols = new ArrayList();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Forza la disabilitazione dei pulsanti di play/rewind che vengono abilitati al caricamento della traccia
        playButton.setDisable(true);
        rewindButton.setDisable(true);

        // Inizializza il controlled con un Game di default
        currentGame = new Game();

        // Imposta la tabella delle canzioni delle manche
        FilteredList<Song> filteredData = new FilteredList<>(currentGame.getSongs(), p -> true);
        SortedList<Song> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(songTable.comparatorProperty());
        songTable.setItems(sortedData);

        // Imposta le tabella delle canzoni a selezione singola
        songTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Imposta il listener per identificare il click sulla tabella
        songTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldSong, newSong) -> {
                    songSelection(newSong);
                }
        );

        // Inizio del drag and drop
        songTable.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = songTable.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(currentGame.getSongID(currentSong));
            db.setContent(content);

            event.consume();
        });

        // Controlla le gestione del d&d dalla listra normale alla finale
        songTable.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != songTable
                    && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Fine del drag and drop
        songTable.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && (db.getString().equals(currentGame.getSongID(currentSong)))) {
                currentGame.moveSongToGame(currentSong);
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        songIDColumn.setCellValueFactory(cellData -> currentGame.getSongIDProperty(cellData.getValue()));
        songTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        songAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        songArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        songPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        songPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        songOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        songKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Imposta la tabella delle canzioni per la finale
        finalSongTable.setItems(currentGame.getFinalSongs());
        finalSongTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Imposta il listener per identificare il click sulla tabella
        finalSongTable.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldSong, newSong) -> {
                    songSelection(newSong);
                }
        );

        // Inizio del drag and drop
        finalSongTable.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = finalSongTable.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(currentGame.getSongID(currentSong));
            db.setContent(content);

            event.consume();
        });

        // Controlla le gestione del d&d dalla listra normale alla finale
        finalSongTable.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != finalSongTable
                    && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Fine del drag and drop
        finalSongTable.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && (db.getString().equals(currentGame.getSongID(currentSong)))) {
                currentGame.moveSongToFinal(currentSong);
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        finalSongIDColumn.setCellValueFactory(cellData -> currentGame.getSongIDProperty(cellData.getValue()));
        finalSongTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        finalSongAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        finalSongArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        finalSongPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        finalSongPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalSongOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        finalSongOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalSongKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        finalSongKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        songFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(song -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                return currentGame.getSongID(song).toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Inizializza l'indicatore dello stato dei pulsanti
        buttonStatusPaneInit();

    }

    /**
     * Inizializza l'aspetto grafico per lo stato dei pulsanti
     */
    private void buttonStatusPaneInit() {     
        double maxSize = 100;
        // Inizializza i pulsanti
        SarabandaController.getInstance().buttons.stream().forEach((button) -> {
            // Crea il simbolo
            ButtonSimbol simbol = new ButtonSimbol();

            // Aggiunge il simbolo al pannello
            buttonPane.getChildren().add(simbol);
            //buttonSimbols.add(simbol);

            // Imposta le dimensioni
            simbol.setMaxSize(maxSize, maxSize);
            simbol.setMinSize(maxSize, maxSize);
            simbol.setPrefSize(maxSize, maxSize);
            
            // Aggiunge il listener sullo stato dei pulsanti
            button.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                simbol.setValue(ButtonStatus.parse(newValue));
            });
        });
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
            // Disabilita i pulsanti
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
            progressChangeListener = (observableValue, oldValue, newValue) -> {
                        Duration currentTime = currentSong.getPlayer().getCurrentTime();

                        // Abilita o disabilita le gestione della progress bar del timer
                        if (timerSwitch.selectedProperty().getValue()) {
                            progressTimer.setDisable(false);
                            Double maxTime = getTimerValue().toMillis() < currentSong.getDuration().toMillis() ? getTimerValue().toMillis() : currentSong.getDuration().toMillis();
                            progressTimer.setProgress(1.0 * currentTime.toMillis() / maxTime);
                        }

                        progress.setProgress(1.0 * currentTime.toMillis() / currentSong.getDuration().toMillis());

                        timerTextUpdate(currentTime);
                    };
            currentSong.getPlayer().currentTimeProperty().addListener(progressChangeListener);

            // Cambia lo stato del pulsante per consentire la pausa della canzone
            currentSong.getPlayer().setOnPlaying(() -> {
                playButton.setText("Pause");
                songTable.setDisable(true);
                finalSongTable.setDisable(true);
            });

            // Cambia lo stato del pulsante e allinea il contatempo al tempo reale
            currentSong.getPlayer().setOnPaused(() -> {
                playButton.setText("Play");
                songTable.setDisable(false);
                finalSongTable.setDisable(false);
                timerTextUpdate(currentSong.getPlayer().getCurrentTime());
            });

            // Mette in pausa
            currentSong.getPlayer().setOnMarker((final MediaMarkerEvent event) -> {
                currentSong.pause();
            });
        }
    }

    /**
     * Imposta il testo del contatempo
     *
     * @param currentTime
     */
    public void timerTextUpdate(Duration currentTime) {
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
     * Ricarica le canzoni
     * @param path
     */
    public void loadGameSong(String path) {
        currentGame.loadMediaListFromDirectory(path);
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
                final ObservableMap<String, Duration> marker = currentSong.getPlayer().getMedia().getMarkers();
                // Ripulisce la lista dei marker (PER SICUREZZA)
                marker.clear();
                // TODO aggiungere un marker ogni getTimerValue() fino alla fine del brano
                marker.put("SarabandaStop", getTimerValue());
            }
        } else {
            // Abilita la modifica del timer e disaabilita la progressbar
            timerValue.setDisable(false);
            progressTimer.setProgress(0);
            progressTimer.setDisable(true);

            if (currentSong != null) {
                final ObservableMap<String, Duration> marker = currentSong.getPlayer().getMedia().getMarkers();
                marker.clear();
            }
        }
    }

    /**
     * Gestisce il pulsante rewind
     */
    @FXML
    private void handleRewindSong() {
        if (currentSong != null) {
            currentSong.rewind();
        }
    }

    @FXML
    public void handleSarabandaReset() {
        SarabandaController.getInstance().sendSarabandaReset();
    }

    @FXML
    public void handleSarabandaFullReset() {
        SarabandaController.getInstance().sendSarabandaFullReset();
    }

    @FXML
    public void handleSarabandaError() {
        SarabandaController.getInstance().sendSarabandaError();
        currentSong.koProperty().setValue(true);
        currentSong.okProperty().setValue(false);
    }

    @FXML
    public void handleSarabandaCorrect() {
        currentSong.koProperty().setValue(false);
        currentSong.okProperty().setValue(true);
    }

    @FXML
    public void handleMoveToFinalSongList() {
        currentGame.moveSongToFinal(currentSong);
    }

    @FXML
    public void handleMoveToSongList() {
        currentGame.moveSongToGame(currentSong);
    }

}
