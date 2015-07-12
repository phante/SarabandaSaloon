/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.entity;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author deltedes
 */
public class PushButton {
    private final StringProperty value = new SimpleStringProperty();
    
    public PushButton() {
        //Logger.getLogger(PushButton.class.getName()).log(Level.INFO, "Imposto lo stato del pulsante a {0}", PushButtonStatus.ENABLED.toString());
        value.setValue(PushButtonStatus.ENABLED.toString());
    }
    
    public StringProperty valueProperty () {
        return value;
    }
    
    public PushButtonStatus getStatus() {
        //Logger.getLogger(PushButton.class.getName()).log(Level.INFO, "Ritorno lo stato del pulsante che è {0}", value.getValue());
        return PushButtonStatus.parse(value.getValue());
    }
    
    public void setStatus(PushButtonStatus newvalue) {
        value.setValue(newvalue.toString());
    }
}
