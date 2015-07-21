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
import com.phante.sarabandasaloon.ui.TrackListController;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author deltedes
 */
@XmlRootElement(name = "tracklist")
public class TrackList {

    // Nome della tracklist
    private StringProperty name = new SimpleStringProperty();
    // Lista delle canzoni
    private final ObservableList<Song> songList = FXCollections.observableArrayList();
    // Lista della canzoni per la manche finale
    private final ObservableList<Song> finalSongList = FXCollections.observableArrayList();

    private TrackList() {
        name.setValue("Nuova tracklist");
    }

    public TrackList(String trackListName) {
        name.setValue(trackListName);
    }

    public void switchContext(ObservableList<Song> songs) {
        ObservableList<Song> sourceList;
        ObservableList<Song> destList;
        
        Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio la lista di {0} canzoni", songs.size());
        
        if (songList.containsAll(songs)) {
            Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio dalla manche alla finale");
            sourceList = songList;
            destList = finalSongList;
        } else {
            Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Scambio dalla finale alla manche");
            sourceList = finalSongList;
            destList = songList;
        }
        
        Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Le liste hanno rispettivamente {0} e {1} canzoni", new Object[]{songList.size(), finalSongList.size()});
        
        ObservableList<Song> newList = FXCollections.observableArrayList();
        songs.stream().forEach((song) -> {
            newList.add(song);
        });
        
        sourceList.removeAll(newList);
        destList.addAll(newList);
                
        Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Le liste dopo lo scambio hanno rispettivamente {0} e {1} canzoni", new Object[]{songList.size(), finalSongList.size()});

        // Aggiorna l'id
        updateSongIndex();
    }

    public void moveUp(Song song) {
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

    public void moveDown(Song song) {
        ObservableList<Song> sourceList = null;
        if (songList.contains(song)) {
            sourceList = songList;
        } else if (finalSongList.contains(song)) {
            sourceList = finalSongList;
        }
        
        if (sourceList != null) {
            int newIndex = sourceList.indexOf(song) == (sourceList.size()-1)  ? (sourceList.size()-1) : sourceList.indexOf(song)+1;
            sourceList.remove(song);
            sourceList.add(newIndex, song);
            
            // Aggiorna l'id
            updateSongIndex();
        }
    }
    
    public void removeAll(Collection<Song> removeList) {
        if (songList.containsAll(removeList)) {
            songList.removeAll(removeList);
        } else if (finalSongList.containsAll(removeList)) {
            finalSongList.removeAll(removeList);
        }
        
        // Aggiorna l'id
        updateSongIndex();
    }

    private void updateSongIndex() {
        DecimalFormat format = new DecimalFormat("000");
        int index = 1;
        for (Song song : songList) {
            String id = format.format(index++);
            song.setId(id);
        }

        index = 1;
        for (Song song : finalSongList) {
            String id = format.format(index++);
            song.setId(id);
        }
    }

    /**
     * Carica la lista dei file audio con estensione mp3 o m4a dal path indicato
     *
     * @param path
     */
    public void addMediaListFromDirectory(File path) {
        // Ripulisce il contenuto delle liste
        //songList.clear();
        //finalSongList.clear();

        DecimalFormat format = new DecimalFormat("000");

        Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Carico le canzoni da {0}", path.getPath());

        // Legge i file e crea le canzoni associate
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path.toPath(), "*.{mp3, m4a}")) {
            for (Path entry : stream) {
                Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Carico {0}", entry.getFileName());

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

        Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "La tracklist adesso ha {0} canzoni per la manche e {1} per la finale", new Object[]{songList.size(), finalSongList.size()});
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

    public ObservableList<Song> songListProperty() {
        return songList;
    }

    public List<Song> getSongs() {
        return songList.subList(0, songList.size());
    }

    public void setSongs(List<Song> songList) {
        songList.clear();
        songList.addAll(songList);
    }

    public ObservableList<Song> finalSongsListProperty() {
        return finalSongList;
    }

    public List<Song> getFinalSongs() {
        return finalSongList.subList(0, finalSongList.size());
    }

    public void setFinalSongs(List<Song> songList) {
        finalSongList.clear();
        finalSongList.addAll(songList);
    }

    public void saveTrackList(File file) {
        try {
            Logger.getLogger(TrackList.class.getName()).log(Level.INFO, "Salva la tracklist sul file {0}", file.getPath());
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshalling and saving XML to the file.
            m.marshal(this, file);
        } catch (Exception e) { // catches ANY exception
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static TrackList getTrackList(File file) {
        try {
            // Estraggo la tracklist
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Unmarshaller um = context.createUnmarshaller();
            return (TrackList) um.unmarshal(file);
        } catch (JAXBException ex) {
            Logger.getLogger(TrackListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
