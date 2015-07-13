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
package com.phante.sarabandasaloon.entity;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author deltedes
 */
public class Game {
   
    private final StringProperty name = new SimpleStringProperty();
    // Indica se la manche è in corso
    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper();
    // Lista delle canzoni
    private final ObservableList<Song> songs = FXCollections.observableArrayList();
    // Lista della canzoni per la manche finale
    private final ObservableList<Song> finalSongs = FXCollections.observableArrayList();

       
    /**
     * La lista delle canzoni
     * 
     * @return 
     */
    public ObservableList<Song> getSongs () {
        return songs;
    }
    
    /**
     * La lista delle canzoni per il finale
     * @return 
     */
    public ObservableList<Song> getFinalSongs() {
        return finalSongs;
    }
    
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }
    
    public boolean isRunning () {
        return running.getValue();
    }
    
    public void start() {
        running.setValue(true);
    }
    
    public void stop() {
        running.setValue(false);
    }
    
    /**
     * Ritorna l'id della canzone
     * 
     * @param search
     * @return 
     */
    public StringProperty getSongIDProperty(Song search) {
        StringProperty id = new SimpleStringProperty();
        id.setValue("undefined");
        
        // Cerca la canzone sulla lista normale
        int songId = songs.indexOf(search);
        if (songId != -1) {
            DecimalFormat format = new DecimalFormat("000");
            id.setValue(format.format(songId + 1));
        } else {
            songId = finalSongs.indexOf(search);
            if (songId != -1) {
                String c = new StringBuilder().append((char) ('A' + (char)songId)).toString();
                id.setValue(c);
            }
        }
        return id;
    }
    
    public String getSongID(Song search) {
        return getSongIDProperty(search).getValue();
    }
    
    /**
     * Carica la lista dei file audio con estensione mp3 o m4a dal path indicato
     *
     * @param path
     */
    public void loadMediaListFromDirectory(String path) {        
        // Ripulisce il contenuto delle liste
        songs.clear();
        finalSongs.clear();

        // Legge i file e crea le canzoni associate
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), "*.{mp3, m4a}")) {
            for (Path entry : stream) {
                // identifica il comportamento dal nome del file
                String fileName = entry.getFileName().toString();
                if (fileName.matches("[\\d]+-(.*)")) {
                    finalSongs.add(new Song(entry.toUri().toURL().toString()));
                } else {
                    songs.add(new Song(entry.toUri().toURL().toString()));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sposta la canzone indicata dalla lista normale alla lista delle finali
     * 
     * @param currentSong 
     */
    public void moveSongToFinal(Song currentSong) {
        Logger.getLogger(Game.class.getName()).log(Level.INFO, "Sposto {0} sulla finale.", currentSong.getFileName());
        songs.remove(currentSong);
        finalSongs.add(currentSong);
    }

    /**
     * Sposta la canzone indicata dalla lista della finale alla lista normale
     * @param currentSong 
     */
    public void moveSongToGame(Song currentSong) {
        Logger.getLogger(Game.class.getName()).log(Level.INFO, "Sposto {0} sulla manche.", currentSong.getFileName());
        finalSongs.remove(currentSong);
        songs.add(currentSong);
    }


}
