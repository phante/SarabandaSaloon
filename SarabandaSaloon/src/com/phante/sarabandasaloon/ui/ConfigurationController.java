/*
 * Copyright 2015 elvisdeltedesco.
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

import com.phante.sarabandasaloon.entity.PreferencesUtility;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author elvisdeltedesco
 */
public class ConfigurationController implements Initializable {

    @FXML
    private TextField basePath = new TextField();

    @FXML
    private TextField timeoutValue = new TextField();

    @FXML
    private TextField correctTrack = new TextField();
    @FXML
    private TextField errorTrack = new TextField();
    @FXML
    private TextField timeoutTrack = new TextField();

    @FXML
    private CheckBox classicNetwork = new CheckBox();

    private Stage dialogStage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        basePath.setText(PreferencesUtility.get(PreferencesUtility.BASE_PATH));
        timeoutValue.setText(PreferencesUtility.get(PreferencesUtility.TIMEOUT_VALUE));
        correctTrack.setText(PreferencesUtility.get(PreferencesUtility.CORRECT_TRACK));
        errorTrack.setText(PreferencesUtility.get(PreferencesUtility.ERROR_TRACK));
        timeoutTrack.setText(PreferencesUtility.get(PreferencesUtility.TIMEOUT_TRACK));
        classicNetwork.selectedProperty().setValue(Boolean.parseBoolean(PreferencesUtility.CLASSIC_NETWORK));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void handleChangeBasePath() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(basePath.getText()));
        dirChooser.setTitle("Seleziona la directory di configurazione del Sarabanda");
        File newPath = dirChooser.showDialog(null);
    }
    
    private void chooseAudioFile(TextField field) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(basePath.getText()));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Audio Files", "*.mp4", "*.m4a")
        );
        fileChooser.setTitle("Seleziona la traccia audio");
        File file = fileChooser.showOpenDialog(null);
        
        field.setText(file.getPath());
    }
    
    private void testAudioFile(String path) {
        MediaPlayer player = new MediaPlayer(new Media(path));
        
        player.play();
    }

    @FXML
    public void handleChangeCorrectTrack() {
        chooseAudioFile(correctTrack);
    }

    @FXML
    public void handleTestCorrectTrack() {
        testAudioFile(correctTrack.getText());
    }

    @FXML
    public void handleChangeErrorTrack() {
        chooseAudioFile(errorTrack);
    }

    @FXML
    public void handleTestErrorTrack() {
        testAudioFile(errorTrack.getText());
    }

    @FXML
    public void handleChangeTimeoutTrack() {
        chooseAudioFile(timeoutTrack);
    }

    @FXML
    public void handleTestTimeoutTrack() {
        testAudioFile(timeoutTrack.getText());
    }

    @FXML
    private void handleOkButton() {
        dialogStage.close();
    }

    @FXML
    private void handleCancelButton() {
        dialogStage.close();
    }
}
