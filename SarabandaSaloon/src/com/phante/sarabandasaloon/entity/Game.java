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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author deltedes
 */
public class Game {
    // Nome del gioco
    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
    // Indica se la manche è in corso
    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper();
    // La tracklist associata
    private TrackList trackList;
     
    private Game() {
        trackList = null;
    }
    
    public Game(String newName, TrackList newList) {
        name.setValue(newName);
        trackList = newList;
    }
    
    public void start() {
        running.setValue(true);
    }
    
    public void stop() {
        running.setValue(false);
    }
    
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }
    
    public void setRunning(boolean value) {
        running.setValue(value);
    }
    
    public boolean getRunning() {
        return running.getValue();
    }
    
    public boolean isRunning () {
        return running.getValue();
    }
    
    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }
    
    public void setName(String newName) {
        name.setValue(newName);
    }
    
    public String getName() {
        return name.getValue();
    }
    
    @XmlTransient
    public void setTrackList(TrackList newList) {
        trackList = newList;
    }
    
    public TrackList getTrackList() {
        return trackList;
    }
    
}
