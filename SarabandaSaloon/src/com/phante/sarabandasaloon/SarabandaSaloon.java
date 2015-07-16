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
package com.phante.sarabandasaloon;

import com.phante.sarabandasaloon.network.SarabandaSlaveController;
import com.phante.sarabandasaloon.ui.RootController;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

import javafx.stage.Stage;

/**
 *
 * @author deltedes
 */
public class SarabandaSaloon extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        // Inizializza la finestra principale
        FXMLLoader rootloader = new FXMLLoader(getClass().getResource("ui/Root.fxml"));
        Pane rootLayout = rootloader.load();
        //((RootController) rootloader.getController()).setStage(primaryStage);

        // Inizializza il controller del Sarabanda
        SarabandaSlaveController sarabanda = SarabandaSlaveController.getInstance();

        Scene scene = new Scene(rootLayout);

        primaryStage.setTitle("Sarabanda Saloon");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
