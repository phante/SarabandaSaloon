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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    
    /**
     * Carica la lista dei file audio con estensione mp3 o m4a dal path indicato
     *
     * @param path
     */
    public void loadMediaListFromDirectory(String path) {        
        // Ripulisce il contenuto delle liste
        songList.clear();
        finalSongList.clear();
        
        DecimalFormat format = new DecimalFormat("000");

        // Legge i file e crea le canzoni associate
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), "*.{mp3, m4a}")) {
            for (Path entry : stream) {
                // identifica il comportamento dal nome del file
                String fileName = entry.getFileName().toString();
                
                if (fileName.matches("\\d{3}-(.*)")) {                    
                    songList.add(new Song(fileName.split("-")[0], entry.toUri().toURL().toString()));
                } else if (fileName.matches("[a-zA-Z]-(.*)")) {
                    finalSongList.add(new Song(fileName.split("-")[0], entry.toUri().toURL().toString()));
                } else {
                    // TODO Migliorare
                    String id = format.format(songList.size()+1);
                    songList.add(new Song(id, entry.toUri().toURL().toString()));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public StringProperty nameProperty () {
        return name;
    }
    
    public String getName () {
        return name.getValue();
    }
    
    public void setName (String newName) {
        name.setValue(newName);
    }
    
    public ObservableList<Song> songListProperty () {
        return songList;
    }
    
    //@XmlElement(name = "song")
    public List<Song> getSongs () {
        return songList.subList(0, songList.size());
    }
    
    public void setSongs (List<Song> songList) {
        songList.clear();
        songList.addAll(songList);
    }
    
    public ObservableList<Song> finalSongsListProperty () {
        return finalSongList;
    }
    
    //@XmlElement(name = "finalSong")
    public List<Song> getFinalSongs () {
        return finalSongList.subList(0, finalSongList.size());
    }
    
    public void setFinalSongs (List<Song> songList) {
        finalSongList.clear();
        finalSongList.addAll(songList);
    }
    
}
