/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.view;

import com.phante.sarabandasaloon.network.SarabandaController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

/**
 *
 * @author deltedes
 */
public class NetworkController implements Initializable {

    @FXML
    private CheckBox button1 = new CheckBox();
    @FXML
    private CheckBox button2 = new CheckBox();
    @FXML
    private CheckBox button3 = new CheckBox();
    @FXML
    private CheckBox button4 = new CheckBox();
    @FXML
    private CheckBox serverStatus = new CheckBox();
    
    private ChangeListener<Number> serverStatusChangeListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ReadOnlyIntegerProperty status = SarabandaController.getInstance().serverStatusProperty();

        serverStatusChangeListener = (ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
            switch (newValue.intValue()) {
                case SarabandaController.SERVER_STARTED: 
                    serverStatus.setIndeterminate(false);
                    serverStatus.setSelected(true);
                    break;
                case SarabandaController.SERVER_UNKNOWN: 
                    serverStatus.setIndeterminate(true);
                    break;
                case SarabandaController.SERVER_STOPPED: 
                    serverStatus.setIndeterminate(false);
                    serverStatus.setSelected(false);
                    break;
                default:
                    serverStatus.setIndeterminate(true);
            }
        };
        status.addListener(serverStatusChangeListener);
        
        SarabandaController.getInstance().startServer();
    }

    @FXML
    private void handleSendButtonStatus() {
        System.out.println("handleSendButtonStatus");
    }

    @FXML
    private void handleFullReset() {
        SarabandaController.getInstance().sendSarabandaFullReset();
    }

    @FXML
    private void handleReset() {
        SarabandaController.getInstance().sendSarabandaReset();
    }

    @FXML
    private void handleError() {
        SarabandaController.getInstance().sendSarabandaError();
    }

    @FXML
    private void handleServerStatus() {
        SarabandaController sarabanda = SarabandaController.getInstance();
        if (serverStatus.isSelected()) {
            sarabanda.stopServer();
        } else {
            sarabanda.startServer();
        }
    }

    @FXML
    private void handleMasterReset() {
        SarabandaController.getInstance().sendSarabandaMasterPhysicalReset();
    }

}
