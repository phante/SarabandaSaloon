/*
 * Copyright 2015 deltedes.
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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author deltedes
 */
@XmlRootElement(name = "tracklist")
public class TrackListWrapper {

    private File file;
    private StringProperty name = new SimpleStringProperty();

    /**
     * Costruttore di default
     */
    private TrackListWrapper() {
        file = null;
        name.setValue(null);
    }

    /**
     * Costruttore specifico
     *
     * @param trackList
     */
    public TrackListWrapper(TrackList trackList) {
        this.file = trackList.getFile();
        this.name = trackList.nameProperty();
    }

    /**
     * Carica le informazioni dal file
     *
     * @param file
     * @return
     */
    public static TrackListWrapper fromFile(File file) {        
        TrackListWrapper trackListWrapper = new TrackListWrapper();
        try {
            JAXBContext context = JAXBContext.newInstance(TrackListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            trackListWrapper = (TrackListWrapper) um.unmarshal(file);
            trackListWrapper.setFile(file);
        } catch (JAXBException ex) {
            Logger.getLogger(TrackListWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Logger.getLogger(TrackListWrapper.class.getName()).log(Level.INFO, "Caricata la tracklist {0} dal file {1}", new Object[]{trackListWrapper.getName(), trackListWrapper.getFile().getPath()});

        return trackListWrapper;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String newName) {
        name.setValue(newName);
    }

    public String getName() {
        return name.getValue();
    }

    private void setFile(File newFile) {
        file = newFile;
    }

    public File getFile() {
        return file;
    }

    public TrackList getTrackList() {
        return TrackList.fromFile(file);
    }
}
