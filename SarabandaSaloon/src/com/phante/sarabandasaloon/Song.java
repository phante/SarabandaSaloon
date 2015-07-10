/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;


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

    private final MediaPlayer player;

    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final StringProperty album = new SimpleStringProperty();
    private final StringProperty totalDuration = new SimpleStringProperty();
    private Duration duration;

    private final BooleanProperty played = new SimpleBooleanProperty();
    private final BooleanProperty ok = new SimpleBooleanProperty();
    private final BooleanProperty ko = new SimpleBooleanProperty();

    public Song(String source) {
        fileName.setValue(source);
        played.setValue(false);
        ok.setValue(false);
        ko.setValue(false);

        player = new MediaPlayer(new Media(source));

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
        });

    }

    public StringProperty fileNameProperty() {
        return fileName;
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

    public MediaPlayer getPlayer() {
        return player;
    }

    public String getTitle() {
        return title.getValueSafe();
    }

    public String getArtist() {
        return artist.getValueSafe();
    }

    public String getAlbum() {
        return album.getValueSafe();
    }

    public String getTotalDuration() {
        return totalDuration.getValueSafe();
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
    
    public BooleanProperty playedProperty() {
        return played;
    }
    
    public BooleanProperty koProperty() {
        return ko;
    }

    public BooleanProperty okProperty() {
        return ok;
    }

}
