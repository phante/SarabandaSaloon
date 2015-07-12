    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.ui;


import com.phante.sarabandasaloon.network.SarabandaController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
            SarabandaController.getInstance().startServer();
            
            //gameController.loadGameSong("D:\\mp3");
            gameController.loadGameSong("/Users/elvisdeltedesco/Music/iTunes/iTunes Music/Music/AC_DC/Black Ice");

        } catch (IOException ex) {
            Logger.getLogger(RootController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Richiede il path per il caricamento delle traccie audio
     */
    @FXML
    public void getSongPath() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File directory = dirChooser.showDialog(primaryStage);
        gameController.loadGameSong(directory.getPath());
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
