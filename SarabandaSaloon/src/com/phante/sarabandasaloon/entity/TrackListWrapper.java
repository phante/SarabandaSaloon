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

import com.phante.sarabandasaloon.ui.RootController;
import com.phante.sarabandasaloon.ui.TrackListController;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
public class TrackListWrapper {

    private File file;
    private StringProperty name = new SimpleStringProperty();
    
    public TrackListWrapper() {
        file = null;
        name.setValue(null);
    }

    public TrackList getTrackList() {
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
    
    public void saveTrackList(TrackList trackList) {
        try {
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshalling and saving XML to the file.
            m.marshal(trackList, file);
        } catch (Exception e) { // catches ANY exception
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void setName(String newName) {
        name.setValue(newName);
    }
    
    public String getName() {
        return name.getValue();
    }

    public void setFile(File newFile) {
        file = newFile;
    }

    public File getFile() {
        return file;
    }

    public StringProperty nameProperty() {
        return name;
    }
}