/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

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
    
    @FXML
    private Button loadSong = new Button();
    @FXML
    private Label gameMessage = new Label();
    @FXML
    private Label networkMessage = new Label();

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
            gameMessage.textProperty().bind(gameController.statusProperty());

            Tab gameTab = new Tab();
            gameTab.setContent(gamePage);
            gameTab.setText("Game");

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("Network.fxml"));
            AnchorPane documentPage = (AnchorPane) contentLoader.load();
            networkController = (NetworkController) contentLoader.getController();
            networkMessage.textProperty().bind(networkController.statusProperty());

            Tab documentTab = new Tab();
            documentTab.setContent(documentPage);
            documentTab.setText("Document");

            tabPane.getTabs().setAll(gameTab, documentTab);
            
            loadSong.setOnAction((ActionEvent actionEvent) -> {
                loadFile();
            });
        } catch (IOException ex) {
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadFile() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File directory = dirChooser.showDialog(primaryStage);
        gameController.loadMediaListFromDirectory(directory.getPath());
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }
}
