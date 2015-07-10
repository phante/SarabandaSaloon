/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author deltedes
 */
public class Game {

    private static final int BUTTON_NUMBER = 4;
    
    private final StringProperty name = new SimpleStringProperty();
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
        songs.remove(currentSong);
        finalSongs.add(currentSong);
    }

    /**
     * Sposta la canzone indicata dalla lista della finale alla lista normale
     * @param currentSong 
     */
    public void moveSongToGame(Song currentSong) {
        finalSongs.remove(currentSong);
        songs.add(currentSong);
    }


}
