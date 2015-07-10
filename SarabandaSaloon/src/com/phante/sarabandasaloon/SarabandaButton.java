/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author deltedes
 */
public class SarabandaButton {
    private final StringProperty value = new SimpleStringProperty();
    
    public void Button() {
        value.setValue(ButtonStatus.ENABLED.toString());
    }
    
    public StringProperty valueProperty () {
        return value;
    }
    
    public ButtonStatus getStatus() {
        return ButtonStatus.valueOf(value.getValue());
    }
    
    public void setStatus(ButtonStatus newvalue) {
        value.setValue(newvalue.toString());
    }
}
