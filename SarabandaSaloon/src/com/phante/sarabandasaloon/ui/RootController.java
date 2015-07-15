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
package com.phante.sarabandasaloon.ui;

import com.phante.sarabandasaloon.SarabandaSaloon;
import com.phante.sarabandasaloon.entity.Game;
import com.phante.sarabandasaloon.entity.TrackList;
import com.phante.sarabandasaloon.network.SarabandaSlaveController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * FXML Controller class
 *
 * @author deltedes
 */
public class RootController implements Initializable {

    @FXML
    private TabPane tabPane;

    private GameController gameController;
    private NetworkController networkController;

    private Stage primaryStage;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Pane gamePage = gameLoader.load();
            gameController = (GameController) gameLoader.getController();

            Tab gameTab = new Tab();
            gameTab.setContent(gamePage);
            gameTab.setText("Game");

            TrackList tl = new TrackList("Nuova tracklist");
            String source = getDefaultDirectoryFilePath().getAbsolutePath();
            tl.loadMediaListFromDirectory(source);
            saveTrackListDataToFile(tl);

            //tl.loadMediaListFromDirectory("/Users/elvisdeltedesco/Music/iTunes/iTunes Music/Music/AC_DC/Black Ice");
            gameController.setGame(new Game(tl));

            /*
             FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("Network.fxml"));
             Pane documentPage = contentLoader.load();
             networkController = (NetworkController) contentLoader.getController();

             Tab documentTab = new Tab();
             documentTab.setContent(documentPage);
             documentTab.setText("Document");
             */
            tabPane.getTabs().setAll(gameTab);

            // Avvia il server
            SarabandaSlaveController.getInstance().startServer();

        } catch (IOException ex) {
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Richiede il path per il caricamento delle traccie audio
     */
    @FXML
    public void getSongPath() throws IOException {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File directory = dirChooser.showDialog(primaryStage);

        setDefaultDirectoryFilePath(directory);

        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("Game.fxml"));
        Pane gamePage = gameLoader.load();
        gameController = (GameController) gameLoader.getController();
        Tab gameTab = new Tab();
        gameTab.setContent(gamePage);
        gameTab.setText("Game");

        TrackList tl = new TrackList("caricata fresca fresca");
        String source = getDefaultDirectoryFilePath().getAbsolutePath();
        tl.loadMediaListFromDirectory(source);
        saveTrackListDataToFile(tl);

        //tl.loadMediaListFromDirectory("/Users/elvisdeltedesco/Music/iTunes/iTunes Music/Music/AC_DC/Black Ice");
        gameController.setGame(new Game(tl));
        
        tabPane.getTabs().add(gameTab);

        /*
         TrackList tl = new TrackList();
         tl.loadMediaListFromDirectory("D:\\mp3");
         gameController.loadGameSong(directory.getPath());
         */
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public void saveTrackListDataToFile(TrackList tl) {
        Logger.getLogger(RootController.class.getName()).log(Level.INFO, "Provo a salvare il file su disco");
        File file = new File("D://trackList.xml");

        try {
            JAXBContext context = JAXBContext.newInstance(TrackList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshalling and saving XML to the file.
            m.marshal(tl, file);
        } catch (Exception e) { // catches ANY exception
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public File getDefaultDirectoryFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(SarabandaSaloon.class);
        String filePath = prefs.get("filePath", null);

        Logger.getLogger(RootController.class.getName()).log(Level.INFO, filePath);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    public void setDefaultDirectoryFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(SarabandaSaloon.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("AddressApp");
        }
    }

    @FXML
    public void handleErrorMenu() {
        gameController.errorGame();
    }

    @FXML
    public void handleCorrectMenu() {
        gameController.goodGame();
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }
}
