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
package com.phante.sarabandasaloon.ui;

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
