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
package com.phante.sarabandasaloon.entity;

import com.phante.sarabandasaloon.ui.RootController;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author deltedes
 */
@XmlRootElement(name = "tracklist")
@XmlType(propOrder = { "name", "locked", "games", "songs", "finalSongs" })
public class TrackList {

    // File della tracklist
    private File file;
    // Nome della tracklist
    private StringProperty name = new SimpleStringProperty();
    // Lista delle canzoni
    private final ObservableList<Song> songList = FXCollections.observableArrayList();
    // Lista della canzoni per la manche finale
    private final ObservableList<Song> finalSongList = FXCollections.observableArrayList();
    // Lista dei giochi associati
    private final ObservableList<Game> gameList = FXCollections.observableArrayList();
    //
    private final ReadOnlyBooleanWrapper unmodified = new ReadOnlyBooleanWrapper();
    //
    private final ReadOnlyBooleanWrapper locked = new ReadOnlyBooleanWrapper();

    /**
     * Costruttore di default privato
     */
    private TrackList() {
        file = null;
        name.setValue("Nuova tracklist");
        unmodified.setValue(Boolean.TRUE);
        locked.setValue(Boolean.FALSE);
    }

    /**
     * Costruttore pubblico
     *
     * @param trackListName
     * @param toDisk
     */
    public TrackList(String trackListName, File toDisk) {
        file = toDisk;
        name.setValue(trackListName);
        unmodified.setValue(Boolean.TRUE);
        locked.setValue(Boolean.FALSE);
    }
    
    /**
     * Calcola il nome del file della tracklist
     *
     * @param name
     * @return
     */
    public static String standardFileName(String name) {
        StringBuilder strBuf = new StringBuilder();
        String trackListName = name.toLowerCase().replace(" ", "_");

        File configPath = new File(PreferencesUtility.get(PreferencesUtility.BASE_PATH));

        strBuf.append(configPath.getPath())
                .append("//")
                .append(trackListName)
                .append(".xml");
        return strBuf.toString();
    }

    /**
     * Carica la tracklist da file
     *
     * @param file
     * @return
     */
    public static TrackList fromFile(File file) {
        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Caricata la tracklist dal file {0}", file.getPath());
        TrackList trackList = new TrackList();
        try {
            // Estraggo la tracklist dal file
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Unmarshaller um = context.createUnmarshaller();
            trackList = (TrackList) um.unmarshal(file);
            trackList.file = file;
        } catch (JAXBException ex) {
            Logger.getLogger(TrackList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Caricata la tracklist {0} dal file {1}", new Object[]{trackList.getName(), trackList.getFile().getPath()});
        return trackList;
    }
    
    /**
     * Crea una copia della tracklist esistente partendo dal file della tracklist
     * 
     * @return 
     */
    public TrackList copy(String newName) {
        TrackList newTrackList = TrackList.fromFile(this.file);
        
        newTrackList.setName(newName);
        newTrackList.file = new File(TrackList.standardFileName(newName));
        newTrackList.save();
        
        return newTrackList;
    }

    /**
     * Salva la tracklist su file
     *
     * @param file
     */
    public void save() {
        try {
            //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Salva la tracklist sul file {0}", file.getPath());
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshalling and saving XML to the file.
            m.marshal(this, file);
            
            // Imposta la tracklist come salvata
            unmodified.setValue(Boolean.TRUE);
        } catch (Exception e) { // catches ANY exception
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Cambia il contesto della canzoni dalla manche alla finale e viceversa
     *
     * @param songs
     */
    public void switchContext(ObservableList<Song> songs) {
        if (evaluateLock()) return;        
        
        ObservableList<Song> sourceList;
        ObservableList<Song> destList;

        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio la lista di {0} canzoni", songs.size());

        if (songList.containsAll(songs)) {
            //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio dalla manche alla finale");
            sourceList = songList;
            destList = finalSongList;
        } else {
            //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio dalla finale alla manche");
            sourceList = finalSongList;
            destList = songList;
        }

        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Le liste hanno rispettivamente {0} e {1} canzoni", new Object[]{songList.size(), finalSongList.size()});

        ObservableList<Song> newList = FXCollections.observableArrayList();
        songs.stream().forEach((song) -> {
            newList.add(song);
        });

        sourceList.removeAll(newList);
        destList.addAll(newList);

        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Le liste dopo lo scambio hanno rispettivamente {0} e {1} canzoni", new Object[]{songList.size(), finalSongList.size()});

        // Aggiorna l'id
        updateSongIndex();
    }

    /**
     * Sposta la canzone "in su" nella lista
     *
     * @param song
     */
    public void moveUp(Song song) {
        if (evaluateLock()) return;
        
        ObservableList<Song> sourceList = null;
        if (songList.contains(song)) {
            sourceList = songList;
        } else if (finalSongList.contains(song)) {
            sourceList = finalSongList;
        }

        if (sourceList != null) {
            int newIndex = sourceList.indexOf(song) == 0 ? 0 : sourceList.indexOf(song) - 1;
            sourceList.remove(song);
            sourceList.add(newIndex, song);

            // Aggiorna l'id
            updateSongIndex();
        }
    }

    /**
     * Sposta la canzone "in giù" nella lista
     *
     * @param song
     */
    public void moveDown(Song song) {
        if (evaluateLock()) return;
        
        ObservableList<Song> sourceList = null;
        if (songList.contains(song)) {
            sourceList = songList;
        } else if (finalSongList.contains(song)) {
            sourceList = finalSongList;
        }

        if (sourceList != null) {
            int newIndex = sourceList.indexOf(song) == (sourceList.size() - 1) ? (sourceList.size() - 1) : sourceList.indexOf(song) + 1;
            sourceList.remove(song);
            sourceList.add(newIndex, song);

            // Aggiorna l'id
            updateSongIndex();
        }
    }

    /**
     * Rimuove le canzoni dalla lista
     *
     * @param removeList
     */
    public void removeAll(Collection<Song> removeList) {
        if (evaluateLock()) return;
        
        if (songList.containsAll(removeList)) {
            songList.removeAll(removeList);
        } else if (finalSongList.containsAll(removeList)) {
            finalSongList.removeAll(removeList);
        }

        // Aggiorna l'id
        updateSongIndex();
    }

    /**
     * Aggiorna gli id delle canzoni in funzione della lista e dell'ordine
     *
     */
    private void updateSongIndex() {
        if (evaluateLock()) return;
        
        DecimalFormat format = new DecimalFormat("000");
        // Aggiorna l'indice della manche in formato numerico
        int index = 1;
        for (Song song : songList) {
            String id = format.format(index++);
            song.setId(id);
        }

        // Aggiorna l'indice della finale in formato testuale
        index = 0;
        for (Song song : finalSongList) {
            String id = new StringBuilder().append((char) ('A' + (char) index++)).toString();
            song.setId(id);
        }
        
        unmodified.setValue(Boolean.FALSE);
    }

    /**
     * Carica la lista dei file audio con estensione mp3 o m4a dal path indicato
     *
     * @param path
     */
    public void addMediaListFromDirectory(File path) {
        if (evaluateLock()) return;
        
        // Ripulisce il contenuto delle liste
        DecimalFormat format = new DecimalFormat("000");

        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Carico le canzoni da {0}", path.getPath());

        // Legge i file e crea le canzoni associate
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path.toPath(), "*.{mp3, m4a}")) {
            for (Path entry : stream) {
                //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Carico {0}", entry.getFileName());

                // identifica il comportamento dal nome del file
                String fileName = entry.getFileName().toString();

                if (fileName.matches("\\d{3}-(.*)")) {
                    songList.add(new Song(fileName.split("-")[0], entry.toUri().toURL().toString()));
                } else if (fileName.matches("[a-zA-Z]-(.*)")) {
                    finalSongList.add(new Song(fileName.split("-")[0], entry.toUri().toURL().toString()));
                } else {
                    // TODO Migliorare
                    String id = format.format(songList.size() + 1);
                    songList.add(new Song(id, entry.toUri().toURL().toString()));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TrackList.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "La tracklist adesso ha {0} canzoni per la manche e {1} per la finale", new Object[]{songList.size(), finalSongList.size()});

        // Aggiorna l'id
        updateSongIndex();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(String newName) {
        name.setValue(newName);
    }
    
    /**
     * Ritorna il riferimento al file
     *
     * @return 
     */
    public File getFile() {
        return file;
    }

    public ObservableList<Song> songsProperty() {
        return songList;
    }

    public List<Song> getSongs() {
        return songList.subList(0, songList.size());
    }

    public void setSongs(List<Song> songList) {        
        songList.clear();
        songList.addAll(songList);
    }

    public ObservableList<Song> finalSongsProperty() {
        return finalSongList;
    }

    public List<Song> getFinalSongs() {
        return finalSongList.subList(0, finalSongList.size());
    }

    public void setFinalSongs(List<Song> songList) {
        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Imposto la lista delle canzoni della finale con {0} canzoni", songList.size());
        finalSongList.clear();
        finalSongList.addAll(songList);
    }
    
    public ObservableList<Game> gamesProperty() {
        return gameList;
    }
    
    public List<Game> getGames() {
        return gameList.subList(0, gameList.size());
    }
    
    private boolean evaluateLock() {
        if (gameList.isEmpty()) {
            locked.setValue(Boolean.FALSE);
        } else {
            locked.setValue(Boolean.TRUE);
        }
        
        //Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "La tracklist ha stato del lock a {0}", locked.getValue());
        
        return locked.getValue();
    }

    public void setGames(List<Game> list) {
        gameList.clear();
        gameList.addAll(list);
        unmodified.setValue(Boolean.FALSE);
        
        evaluateLock();
    }
    
    public void add(Game game) {
        gameList.add(game);
        unmodified.setValue(Boolean.FALSE);
        
        evaluateLock();
    }
    
    public void remove(Game game) {
        gameList.remove(game);
        unmodified.setValue(Boolean.FALSE);
        
        evaluateLock();
    }
    
    public ReadOnlyBooleanProperty modifiedProperty() {
        return unmodified.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty lockedProperty() {
        return locked.getReadOnlyProperty();
    }
    
    public void setLocked(boolean value) {
        evaluateLock();
    }
    
    public boolean getLocked() {
        evaluateLock();
        
        return locked.getValue();
    }

}
