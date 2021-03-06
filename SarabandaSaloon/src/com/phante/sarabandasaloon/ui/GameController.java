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

import com.phante.sarabandasaloon.entity.PushButtonStatus;
import com.phante.sarabandasaloon.entity.Game;
import com.phante.sarabandasaloon.entity.Preferences;
import com.phante.sarabandasaloon.entity.PushButton;
import com.phante.sarabandasaloon.entity.Song;
import com.phante.sarabandasaloon.network.SarabandaSlaveController;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class GameController implements Initializable {

    // Etichette con le informazioni sul brano
    @FXML
    private Label currentTitle = new Label();
    @FXML
    private Label currentAlbum = new Label();
    @FXML
    private Label currentArtist = new Label();
    @FXML
    private Label currentTotalDuration = new Label();

    // Etichette per il contatempo
    @FXML
    private Label timeKeeperLabel = new Label();

    // Etichetta per lo status bar che mostra i messaggi in entrata
    @FXML
    private Label messageLabel = new Label();
    // Etichetta per lo status bar che mostra lo stato del listener
    @FXML
    private Label listenerLabel = new Label();

    // Campo di test per la durata del timer
    @FXML
    private TextField timerValue = new TextField();
    // Campo di ricerca della canzone
    @FXML
    private TextField songFilter = new TextField();
    // Impostazioni di attivazione e disattivazione del timer
    @FXML
    private ToggleButton timerSwitch = new ToggleButton();

    // Gestisce l'interfaccia in modo forzato costringendo l'utilizzatore a seguire un percorso obbligato
    @FXML
    private ToggleButton freeGameSwitch = new ToggleButton();

    // ProgressBar per il tempom totale di esecuzione
    @FXML
    private ProgressBar progress = new ProgressBar();
    // ProgressBar per il tempo del timer
    @FXML
    private ProgressBar progressTimer = new ProgressBar();

    // Listener per l'indicazione del progresso sulla canzone
    private ChangeListener<Duration> progressChangeListener;

    // Tabella con la lista delle canzoni della manche normale
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

    // Tabella con la lista delle canzoni della manche finale
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

    // Pulsanti per la gestione del file audio
    @FXML
    private Button playButton = new Button();
    @FXML
    private Button rewindButton = new Button();

    // Pulsanti per la gestione del sarabanda
    @FXML
    private Button errorButton = new Button();
    @FXML
    private Button correctButton = new Button();

    // Contenitore per lo stato dei pulsanti
    @FXML
    private HBox buttonPane = new HBox();
    //private List<ButtonSimbol> buttonSimbols = new ArrayList();

    // Gioco associato al controller
    private Game game;

    // Canzone corrente
    private Song currentSong;

    // Media per i suoni di gioco
    private MediaPlayer correctMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer timeoutMediaPlayer;

    /**
     * Inizializza il controller.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timerValue.setText(Integer.toString(Preferences.getInstance().getTimeout()));

        try {
            // Inizializza i mediaplayer per i suoni di gioco
            correctMediaPlayer = new MediaPlayer(new Media(new File(Preferences.getInstance().getCorrectTrack()).toURI().toURL().toString()));
            errorMediaPlayer = new MediaPlayer(new Media(new File(Preferences.getInstance().getErrorTrack()).toURI().toURL().toString()));
            timeoutMediaPlayer = new MediaPlayer(new Media(new File(Preferences.getInstance().getTimeoutTrack()).toURI().toURL().toString()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Disabilita i pulsanti per la gestione delle risposte
        enableGameValutationInterface(false);

        // Disabilita i pulsanti di scelta
        enablePlayerInterface(false);

        // Inizializza il pannello con lo stato dei pushButton
        pushButtonStatusPaneInit();

        // Imposta le singole colonne
        songIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        songTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        songAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        songArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        songPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        songPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        songOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        songKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        songKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Imposta le singole colonne
        finalSongIDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        finalSongTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        finalSongAlbumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        finalSongArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        finalSongPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        finalSongPlayedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalSongOKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        finalSongOKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        finalSongKOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        finalSongKOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());

        // Inizializza gli elementi della status bar
        messageLabel.textProperty().bind(SarabandaSlaveController.getInstance().messageProperty());
        SarabandaSlaveController.getInstance().serverStatusProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case SarabandaSlaveController.SERVER_STARTED:
                    listenerLabel.setText("Listener acceso");
                    break;
                case SarabandaSlaveController.SERVER_STOPPED:
                    listenerLabel.setText("Listener spento");
                    break;
                default:
                    listenerLabel.setText("");
            }
        });
    }

    /**
     * Carica sul controller il game da gestire, quindi mentre sull'inizialize è
     * stato preimpostato l'aspetto grafico di base, qui andiamo a caricare e
     * popolarlo con i dati del caso.
     *
     * @param newGame
     */
    public void setGame(Game newGame) {
        //Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Imposto come gioco {0}", newGame.getName() );
        game = newGame;

        // Imposta i dati della tabella delle canzoni della manche normale
        songTableInit(game.getTrackList().songsProperty(), songTable);
        // Imposta la tabella delle canzioni per la finale
        songTableInit(game.getTrackList().finalSongsProperty(), finalSongTable);
    }

    /**
     * Inizializza l'aspetto grafico per lo stato dei pulsanti
     *
     */
    private void pushButtonStatusPaneInit() {
        // TODO Scollegare la dimensione statica e collegare il ridimensionamneto dei singoli pushbutton al parent
        double maxSize = 100;

        // Inizializza i simboli per i singoli pulsanti
        SarabandaSlaveController.getInstance().getPushButton().stream().forEach((button) -> {
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
                //Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Un pulsante ha cambiato stato da {0} a {1}.", new Object[]{oldValue, newValue});

                // Al cambio dello stato del pulsante cambio il simbolo come feedback visivo di cosa succede sul palco
                simbol.setValue(PushButtonStatus.parse(newValue));

                // Nel caso il gioco sia in corso verifica se il pulsante è stato premuto
                if (game.runningProperty().getValue() && (PushButtonStatus.parse(newValue) == PushButtonStatus.PRESSED)) {
                    //Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Un pushButton è stato premuto");
                    pushButtonPressed();
                }
            });
        });
    }

    /**
     * Inizializza il comportamento della tabelle delle canzoni
     *
     */
    private void songTableInit(ObservableList<Song> songs, TableView<Song> table) {
        // Crea le versioni filtrate e ordinate della tabella
        FilteredList<Song> filteredData = new FilteredList<>(songs, p -> true);
        SortedList<Song> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        // Aggiunge i dati alla tabella
        table.setItems(sortedData);

        // Aggiunge il listener per la gestione dei filtri
        songFilter.textProperty().addListener((observable, oldValue, newValue) -> {
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
        });

        // Imposta le tabella delle canzoni a selezione singola
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Imposta il listener per identificare il click sulla tabella
        table.getSelectionModel().selectedItemProperty().addListener(
                (observedValue, oldSong, newSong) -> {
                    songSelection(oldSong, newSong);
                }
        );

        /*
         Commento le impostazioni del drag and drop come annotazione per sviluppi futuri
         */
        // Inizio del drag and drop
        /*        table.setOnDragDetected((MouseEvent event) -> {
         Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
        
         ClipboardContent content = new ClipboardContent();
         content.putString(currentGame.getSongID(currentSong));
         db.setContent(content);
        
         event.consume();
         });*/
        // Controlla le gestione del d&d dalla listra normale alla finale
        /*        table.setOnDragOver((DragEvent event) -> {
         if (event.getGestureSource() != table
         && event.getDragboard().hasString()) {
         event.acceptTransferModes(TransferMode.MOVE);
         }
         event.consume();
         });*/
        // Fine del drag and drop
        /*        table.setOnDragDropped((DragEvent event) -> {
         Dragboard db = event.getDragboard();
         boolean success = false;
         if (db.hasString() && (db.getString().equals(currentGame.getSongID(currentSong)))) {

         // Utilizzando un metodo di inizializzazione unico è necessario distinguere
         // la tabella sorgente dalla tabella di destinazione
        
         currentGame.moveSongToGame(currentSong);
         success = true;
         }
         event.setDropCompleted(success);
        
         event.consume();
         });*/
    }

    /**
     * Imposta lo stato dell'interfaccia con la canzone selezionata
     *
     * @param song
     */
    private void songSelection(Song oldSong, Song song) {
        // Elimina il listener sui progressi
        if (oldSong != null) {
            oldSong.getPlayer().currentTimeProperty().removeListener(progressChangeListener);
        }
        resetGameInterface();

        // Controllo formale di sicurezza
        if (song == null) {
            // Disabilito l'interfaccia del player
            enablePlayerInterface(false);
        } else {
            // Mi assicuro che la traccia parta dall'inizio
            song.rewind();

            // Abilito l'interfaccia del player
            enablePlayerInterface(true);

            // Resetto lo stato del segnatempo e delle progress bar
            updateTimeKeeper(Duration.ZERO);
            progress.setProgress(0); // resetta la progressbar complessiva
            progressTimer.setProgress(0); // resetta la progressbar del timer

            // Imposta le etichette con i metadati
            currentTitle.setText(song.titleProperty().getValue());
            currentAlbum.setText(song.artistProperty().getValue());
            currentArtist.setText(song.albumProperty().getValue());
            currentTotalDuration.setText(song.getDuration().toString());

            // Gestisce le impostazioni del timer
            timerManagement(song);

            // Imposta il listener per le progressbar e il segnatempo
            progressChangeListener = (observableValue, oldValue, newValue) -> {
                Duration currentTime = song.getPlayer().getCurrentTime();

                // Abilita o disabilita le gestione della progress bar del timer
                if (timerSwitch.selectedProperty().getValue()) {
                    progressTimer.setDisable(false);
                    Double maxTime = getTimerValue().toMillis() < song.getDuration().toMillis() ? getTimerValue().toMillis() : song.getDuration().toMillis();
                    progressTimer.setProgress(1.0 * currentTime.toMillis() / maxTime);
                }

                progress.setProgress(1.0 * currentTime.toMillis() / song.getDuration().toMillis());

                updateTimeKeeper(currentTime);
            };
            song.getPlayer().currentTimeProperty().addListener(progressChangeListener);

            // Cambia lo stato del pulsante per consentire la pausa della canzone
            song.getPlayer().setOnPlaying(() -> {
                playButton.setText("Pause");
            });

            // Cambia lo stato del pulsante e allinea il contatempo al tempo reale
            song.getPlayer().setOnPaused(() -> {
                updateTimeKeeper(song.getPlayer().getCurrentTime());
                if (freeGameSwitch.isPressed()) {
                    enableGameValutationInterface(true);
                }
                playButton.setText("Play");
            });

            // Gestione del marker del timeoput
            song.getPlayer().setOnMarker((final MediaMarkerEvent event) -> {
                timeoutGame();
            });

            // Associo la canzone alla canzone corrente
            currentSong = song;
        }
    }

    /**
     * Gestisce l'inserimento dei marker per la gestione del timer
     *
     * @param song
     */
    private void timerManagement(Song song) {
        if (song != null) {
            // Rimuove gli eventuali marker
            final ObservableMap<String, Duration> marker = song.getPlayer().getMedia().getMarkers();
            marker.clear();

            if (timerSwitch.selectedProperty().getValue()) {
                // Disabilita la modifica del timer e abilita la progressbar
                timerValue.setDisable(true);
                progressTimer.setDisable(false);

                Duration timerStep = getTimerValue();
                Duration newStep = Duration.ZERO.add(timerStep);
                int i = 1;
                while (newStep.lessThan(song.getDuration())) {
                    Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Aggiungo un marker per il timer a {0}/{1} ", new Object[]{newStep.toSeconds(), song.getDuration()});
                    marker.put(new StringBuffer()
                            .append("SarabandaStop ")
                            .append(i++)
                            .toString(),
                            newStep);
                    newStep = newStep.add(timerStep);
                }
            } else {
                // Abilita la modifica del timer e disabilita la progressbar
                timerValue.setDisable(false);
                progressTimer.setProgress(0);
                progressTimer.setDisable(true);
            }
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
        if (!freeGameSwitch.isPressed()) {
            enablePlayerInterface(true);
        }

        // Disabilita i pulsanti per la gestione delle risposte
        enableGameValutationInterface(false);

        // Abilita le tabelle di scelta della canzone
        songTable.setDisable(false);
        finalSongTable.setDisable(false);
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
     * Si occupoa di abilitare/disabilitare i pulsanti per la valutazione del
     * gioco
     *
     * @param status
     */
    public void enableGameValutationInterface(boolean status) {
        // Abilita i pulsanti per la gestione delle risposte
        errorButton.setDisable(!status);
        correctButton.setDisable(!status);
    }

    /**
     * Gestisce l'inizio del gioco
     */
    public void startGame() {
        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Inizio il gioco");
        game.start();

        // Disabilita la scelta della canzone
        songTable.setDisable(true);
        finalSongTable.setDisable(true);

        // Disabilita i pulsanti per la gestione delle risposte
        enableGameValutationInterface(false);

        // Effettua il reset completo dello stato dei pulsanti
        SarabandaSlaveController.getInstance().sendSarabandaFullReset();

        // Avvia la canzone
        currentSong.play();
    }

    /**
     * Gestisce la pressione di un pushButton
     *
     */
    public void pushButtonPressed() {
        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Un pulsante è stato premuto");

        // Ferma la canzone
        currentSong.pause();

        // Disabilita i pulsanti per il play o la pausa
        if (freeGameSwitch.isPressed()) {
            enablePlayerInterface(false);
        }

        // Abilita i pulsanti per la gestione delle risposte
        enableGameValutationInterface(true);
    }

    /**
     * Riprendo un gioco interrotto a seguito della pressione di un pushbutton o
     * della messa in pausa
     */
    public void continueGame() {
        // Disabilita i pulsanti per la gestione delle risposte
        enableGameValutationInterface(false);

        // Effettua il reset dello stato del sarabanda
        SarabandaSlaveController.getInstance().sendSarabandaReset();

        // Avvia la canzone
        currentSong.play();
    }

    /**
     * Un giocatore ha indovinato la canzone
     */
    @FXML
    public void goodGame() {
        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Qualcuno ha indovinato");

        // Imposta i push button non premuti con in errore
        //SarabandaSlaveController.getInstance().errorUnpressedPushButton();
        correctMediaPlayer.play();
        
        // Chiude la canzone corrente e resetta l'interfaccia
        game.stop();
        currentSong.okProperty().setValue(true);
        resetGameInterface();
    }

    /**
     * Gestisce lo stato del gioco con l'errore dei giocatori
     */
    @FXML
    public void errorGame() {
        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Qualcuno ha sbagliato");

        // Invia un comando di errore al master del sarabanda
        SarabandaSlaveController.getInstance().sendSarabandaError();

        errorMediaPlayer.play();
        
        // Disabilita i pulsanti per la valutazione
        enableGameValutationInterface(false);
        // Riabilita l'interfaccia per eseguire il brano, in questo caso è necessario dare la possibile di continuare
        enablePlayerInterface(true);

        // TODO C'è il rischio che il giro SLAVE->MASTER->SLAVE sia troppo lento, valutare di rallentare
        // Verifica se tutti i pulsanti sono in errore
        boolean allInError = true;
        for (PushButton button : SarabandaSlaveController.getInstance().getPushButton()) {
            allInError = allInError && (button.getStatus() == PushButtonStatus.ERROR);
        }

        // Gioco concluso in errore chiude il gioco e resetta l'inrefaccia
        if (allInError) {
            game.stop();
            resetGameInterface();
        }
    }

    /**
     * Gestisce il comportamento del gioco in caso di timeout
     */
    public void timeoutGame() {
        Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Siamo in timeout");

        // Mette in pausa la traccia audio
        currentSong.pause();

        // Forsa lo stato dei pulsanti in errore con il doppio scopo di dare un segno visuale e bloccare i pulsanti
        //SarabandaSlaveController.getInstance().errorUnpressedPushButton();
        
        timeoutMediaPlayer.play();
        
        // Chiude il gioco e resetta l'interfaccia
        game.stop();
        currentSong.koProperty().setValue(true);
        resetGameInterface();
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

        //Logger.getLogger(GameController.class.getName()).log(Level.INFO, "Aggiorno il contatempo con i valori {0}{1}:{2}.{3}", new Object[]{sign, minute, second, millis});
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
     * gestisce il pulsante play
     */
    @FXML
    private void handlePlaySong() {
        if (currentSong != null) {
            if ("Pause".equals(playButton.getText())) {
                currentSong.pause();
            } else {
                if (game.isRunning()) {
                    continueGame();
                } else {
                    startGame();
                }
            }
        }
    }

    /**
     *
     */
    @FXML
    private void handleTimer() {
        timerManagement(currentSong);
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
        SarabandaSlaveController.getInstance().sendSarabandaReset();
    }

    @FXML
    public void handleSarabandaFullReset() {
        SarabandaSlaveController.getInstance().sendSarabandaFullReset();
    }

    @FXML
    public void handleSarabandaError() {
        SarabandaSlaveController.getInstance().sendSarabandaError();
    }

    @FXML
    public void handleSarabandaDemo() {
        SarabandaSlaveController.getInstance().sendSarabandaDemo();
    }

    @FXML
    public void handleSarabandaHWReset() {
        SarabandaSlaveController.getInstance().sendSarabandaMasterPhysicalReset();
    }
}
