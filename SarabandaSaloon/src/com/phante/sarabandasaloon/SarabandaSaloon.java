/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import com.phante.sarabandasaloon.network.SarabandaController;
import com.phante.sarabandasaloon.ui.RootController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import javafx.stage.Stage;

/**
 *
 * @author deltedes
 */
public class SarabandaSaloon extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inizializza la finestra principale
        FXMLLoader rootloader = new FXMLLoader(getClass().getResource("ui/Root.fxml"));
        Pane rootLayout = rootloader.load();
        ((RootController)rootloader.getController()).setStage(primaryStage);
        
        // Inizializza il controller del Sarabanda
        SarabandaController sarabanda = SarabandaController.getInstance();

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
