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
public class Message {
    // StringPropertry per il contenuto del messaggio
    public final StringProperty message = new SimpleStringProperty();
    // StringPropertry per il contenuto del messaggio
    public final StringProperty packet = new SimpleStringProperty();
}
