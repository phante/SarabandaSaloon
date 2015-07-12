/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.ui;

import com.phante.sarabandasaloon.entity.PushButtonStatus;
import com.phante.sarabandasaloon.entity.Game;
import com.phante.sarabandasaloon.entity.PushButton;
import com.phante.sarabandasaloon.entity.Song;
import com.phante.sarabandasaloon.network.SarabandaController;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;

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
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class GameController implements Initializable {

    // Gestisce l'interfaccia in modo forzato costringendo l'utilizzatore a seguire un percorso obbligato
    private static final boolean forceGamePlay = true;

    @FXML
    private Label currentTitle = new Label();
    @FXML
    private Label currentAlbum = new Label();
    @FXML
    private Label currentArtist = new Label();
    @FXML
    private Label currentTotalDuration = new Label();
    @FXML
    private Label messageLabel = new Label();
    @FXML
    private Label listenerLabel = new Label();

    // Etichette per il contatempo
    @FXML
    private Label timeKeeperLabel = new Label();

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
    @FXML
    private Button errorButton = new Button();
    @FXML
    private Button correctButton = new Button();

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
        // Inizializza il controlled con un Game di default
        currentGame = new Game();
        
        // Imposta lo stato iniziale dell'interfaccia
        resetGameInterface();
        // Imposta lo stato iniziale dei pulsanti del player, non gestiti dalla resetGameInterface()
        enablePlayerInterface(false);
        
        // Disabilita le tabelle di scelta della canzone
        //songTable.setDisable(false);
        //finalSongTable.setDisable(false);
 
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

        // Inizializza gli elementi della status bar
        messageLabel.textProperty().bind(SarabandaController.getInstance().messageProperty());
        SarabandaController.getInstance().serverStatusProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case SarabandaController.SERVER_STARTED:
                    listenerLabel.setText("Listener acceso");
                    break;
                case SarabandaController.SERVER_STOPPED:
                    listenerLabel.setText("Listener spento");
                    break;
                default:
                    listenerLabel.setText("");
            }
        });

    }

    /**
     * Inizializza l'aspetto grafico per lo stato dei pulsanti
     */
    private void buttonStatusPaneInit() {
        // TODO Scollgerae la dimensione statica e collegare il ridimensionamneto dei singoli pushbuttun al parent
        double maxSize = 100;

        // Inizializza i pulsanti
        SarabandaController.getInstance().getPushButton().stream().forEach((button) -> {
            // Crea il simbolo
            PushButtonSimbol simbol = new PushButtonSimbol();

            // Aggiunge il simbolo al pannello
            buttonPane.getChildren().add(simbol);

            // Imposta le dimensioni
            simbol.setMaxSize(maxSize, maxSize);
            simbol.setMinSize(maxSize, maxSize);
            simbol.setPrefSize(maxSize, maxSize);

            // Aggiunge il listener sullo stato dei pulsanti
            button.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                // Verifico se il gioco è in corso per capise se gestire lo stato del gioco
                // TODO fare una inizializzazione separata con un listener dedicato
                /*
                if (currentGame.runningProperty().getValue() && ) {
                    pushButtonPressed();
                }
                */
                
                // Al cambio dello stato del pulsante cambio il simbolo come feedback visivo di cosa succede sul palco
                simbol.setValue(PushButtonStatus.parse(newValue));
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
            // Elimina il listener sui progressi
            currentSong.getPlayer().currentTimeProperty().removeListener(progressChangeListener);
        }

        // Controllo formale di sicurezza
        if (song == null) {
            resetGameInterface();
            enablePlayerInterface(false);
        } else {
            resetGameInterface();
            enablePlayerInterface(true);
            
            // Blocca i pulsanti del sarabanda per eliminare noiose pressioni inutili
            SarabandaController.getInstance().sendSarabandaFullReset();
            SarabandaController.getInstance().disableAllPushButton();

            // Assegna la canzone corrente
            currentSong = song;

            // Imposta la traccia all'inizio
            currentSong.rewind();

            // Imposta l'interfaccia
            // Imposto il contatempo a 0
            updateTimeKeeper(Duration.ZERO);

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

                updateTimeKeeper(currentTime);
            };
            currentSong.getPlayer().currentTimeProperty().addListener(progressChangeListener);

            // Cambia lo stato del pulsante per consentire la pausa della canzone
            currentSong.getPlayer().setOnPlaying(() -> {
                playButton.setText("Pause");
            });

            // Cambia lo stato del pulsante e allinea il contatempo al tempo reale
            currentSong.getPlayer().setOnPaused(() -> {
                updateTimeKeeper(currentSong.getPlayer().getCurrentTime());
                playButton.setText("Play");
            });

            // Gestione del marker del timeoput
            currentSong.getPlayer().setOnMarker((final MediaMarkerEvent event) -> {
                timeoutGame();
            });
        }
    }

    /**
     * Resetta lo stato dell'interfaccia in modo che sia pronta per l'esecuzione
     * di un nuovo gioco
     */
    public void resetGameInterface() {
        /*
         Riabilita l'interfaccia per far partire il brano. Se il gameplay è forzato l'interfaccia rimane disabilitata
         fino alla scelta del prossimo brano
         */
        if (!forceGamePlay) {
            enablePlayerInterface(true);
        }

        // Disabilita i pulsanti per la gestione delle risposte
        correctButton.setDisable(true);
        errorButton.setDisable(true);

        // Abilita le tabelle di scelta della canzone
        songTable.setDisable(false);
        finalSongTable.setDisable(false);
    }

    /**
     * Gestisce l'inizio del gioco
     */
    public void startGame() {
        currentGame.start();
        
        // Disabilita la scelta della canzone
        songTable.setDisable(true);
        finalSongTable.setDisable(true);

        // Disabilita i pulsanti per la gestione delle risposte
        correctButton.setDisable(true);
        errorButton.setDisable(true);

        // Abilita i push button
        SarabandaController.getInstance().enableAllPushButton();

        // Avvia la canzone
        currentSong.play();
    }

    /**
     * Gestisce la pressione di un pushButton
     *
     */
    public void pushButtonPressed() {
        // Ferma la canzone
        currentSong.pause();

        // Disabilita i pulsanti per il play o la pausa
        if (forceGamePlay) enablePlayerInterface(false);

        // Abilita i pulsanti per la gestione delle risposte
        correctButton.setDisable(true);
        correctButton.setDisable(true);
    }

    /**
     * Un giocatore ha indovinato la canzone
     */
    @FXML
    public void goodGame() {
        // Imposta i push button non premuti con in errore
        SarabandaController.getInstance().errorUnpressedPushButton();

        // TODO Esegue suono di vittoria
        
        currentGame.stop();
        resetGameInterface();
    }

    /**
     * Gestisce lo stato del gioco con l'errore dei giocatori
     */
    @FXML
    public void errorGame() {
        // Invia un comando di errore al master del sarabanda
        SarabandaController.getInstance().sendSarabandaError();
        // Disabilita tutti i pulsanti non premuti per evitare pressioni spurie
        SarabandaController.getInstance().disableAllPushButton();

        // TODO Esegue suono di errore
        // Riabilita l'interfaccia per eseguire il brano, in questo caso è necessario dare la possibile di continuare
        enablePlayerInterface(true);

        // TODO C'è il rischio che il giro SLAVE->MASTER->SLAVE sia troppo lento, valutare di rallentare
        // Verifica se tutti i pulsanti sono in errore
        boolean allInError = true;
        for (PushButton button : SarabandaController.getInstance().getPushButton()) {
            allInError = allInError && (button.getStatus() == PushButtonStatus.ERROR);
        }

        // Gioco concluso in errore, si comporta come il timeout
        if (allInError) {
            // Forsa lo stato dei pulsanti in errore con il doppio scopo di dare un segno visuale e bloccare i pulsanti
            SarabandaController.getInstance().errorUnpressedPushButton();
            
            resetGameInterface();

        }
    }

    /**
     * Gestisce il comportamento del gioco in caso di timeout
     */
    public void timeoutGame() {
        // Mette in pausa la traccia audio
        currentSong.pause();

        // Forsa lo stato dei pulsanti in errore con il doppio scopo di dare un segno visuale e bloccare i pulsanti
        SarabandaController.getInstance().errorUnpressedPushButton();

        // TODO Esegue il suono dell'errore finale
        
        currentGame.stop();
        resetGameInterface();
    }

    /**
     * Si occupa di disabilitare gli elementi grafici che permettono
     * l'esecuzione della canzion
     *
     * @param status
     */
    public void enablePlayerInterface(boolean status) {
        playButton.setDisable(!status);
        rewindButton.setDisable(!status);
    }

    /**
     * Imposta il testo del contatempo
     *
     * @param currentTime
     */
    public void updateTimeKeeper(Duration currentTime) {
        DecimalFormat format = new DecimalFormat("00");

        String sign = "";

        // Imposta il colore rosso per i valori negativi
        if (currentTime.lessThan(Duration.ZERO)) {
            sign = "-";
            timeKeeperLabel.setTextFill(Color.RED);
        } else {
            timeKeeperLabel.setTextFill(Color.BLACK);
        }

        Double absoluteTime = Math.abs(currentTime.toMillis());

        String minute = format.format((int) Math.floor(absoluteTime / 1000) / 60);
        String second = format.format((int) Math.floor(absoluteTime / 1000) % 60);
        String millis = format.format((int) Math.floor(absoluteTime % 1000) / 10);

        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Aggiorno il contatempo con i valori {0}{1}:{2}.{3}", new Object[]{sign, minute, second, millis});

        String timeStr = new StringBuilder()
                .append(sign)
                .append(minute)
                .append(":")
                .append(second)
                .append(".")
                .append(millis)
                .toString();
        timeKeeperLabel.setText(timeStr);
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
     *
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
                SarabandaController.getInstance().enableAllPushButton();
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
    public void handleMoveToFinalSongList() {
        currentGame.moveSongToFinal(currentSong);
    }

    @FXML
    public void handleMoveToSongList() {
        currentGame.moveSongToGame(currentSong);
    }

}
