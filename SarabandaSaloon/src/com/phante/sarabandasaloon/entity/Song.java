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


import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 *
 * @author deltedes
 */
public class Song {
    private StringProperty id = new SimpleStringProperty();
    private StringProperty fileName = new SimpleStringProperty();
    private StringProperty title = new SimpleStringProperty();
    private StringProperty artist = new SimpleStringProperty();
    private StringProperty album = new SimpleStringProperty();
    private StringProperty totalDuration = new SimpleStringProperty();

    private BooleanProperty played = new SimpleBooleanProperty();
    private BooleanProperty ok = new SimpleBooleanProperty();
    private BooleanProperty ko = new SimpleBooleanProperty();
    
    private MediaPlayer player;
    private Duration duration;
    
    private Song() {
        id.setValue("-");
        fileName.setValue("");
        played.setValue(false);
        ok.setValue(false);
        ko.setValue(false);
        player = null;
    }

    /**
     * Costruttore di default
     * 
     * @param newId
     * @param source 
     */
    public Song(String newId, String source) {
        id.setValue(newId);
        fileName.setValue(source);
        played.setValue(false);
        ok.setValue(false);
        ko.setValue(false);

       loadMedia();
    }
    
    private void loadMedia() {
        player = new MediaPlayer(new Media(fileName.getValue()));

        player.setOnReady(() -> {
            duration = player.totalDurationProperty().getValue();

            DecimalFormat format = new DecimalFormat("00");
            String minute = format.format((int) Math.floor((duration.toMillis() / 1000) / 60));
            String second = format.format((int) Math.floor((duration.toMillis() /1000) %60));
            String millis = format.format((int) Math.floor((duration.toMillis() % 1000) /10));

            totalDuration.setValue(new StringBuilder()
                    .append(minute)
                    .append(":")
                    .append(second)
                    .append(".")
                    .append(millis)
                    .toString()
            );
        });

        player.setOnError(() -> {
            Logger.getLogger(Song.class.getName()).log(Level.WARNING, "Media error occured: {0}", player.getError());
        });

        ObservableMap<String, Object> metadata = player.getMedia().getMetadata();
        metadata.addListener((MapChangeListener.Change<? extends String, ?> change) -> {
            title.setValue((String) metadata.get("title"));
            album.setValue((String) metadata.get("album"));
            artist.setValue((String) metadata.get("artist"));
            Logger.getLogger(Song.class.getName()).log(Level.FINE, "Metadati di {0}: titolo {1}, album {2}, artista {3}", new Object[]{fileName.getValue(), title.getValue(), artist.getValue(), album.getValue()});
        });
        
    }

    public StringProperty idProperty() {
        return id;
    }
    
    public String getId() {
        return id.getValue();
    }
    
    public void setId(String newId) {
        id.set(newId);
    }
 
    public StringProperty fileNameProperty() {
        return fileName;
    }
    
    public String getFileName() {
        return fileName.getValue();
    }
    
    public void setFileName(String file) {
        fileName.set(file);
        loadMedia();
    }
    
    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty albumProperty() {
        return album;
    }

    public StringProperty totalDurationProperty() {
        return totalDuration;
    }
    
    public BooleanProperty playedProperty() {
        return played;
    }
    
    public BooleanProperty koProperty() {
        return ko;
    }

    public BooleanProperty okProperty() {
        return ok;
    }

    public MediaPlayer getPlayer() {
        return player;
    }
    
    public Duration getDuration() {
        return duration;
    }

    public void rewind() {
        player.seek(Duration.ZERO);
    }

    public void stop() {
        player.stop();
    }
    
    public void play() {
        player.play();
        played.setValue(true);
    }
 
    public void pause() {
        player.pause();
    }
    
    public MediaPlayer.Status status() {
        return player.statusProperty().getValue();
    }
}
