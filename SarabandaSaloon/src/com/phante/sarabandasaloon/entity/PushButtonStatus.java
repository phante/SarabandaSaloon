/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author deltedes
 */
public enum PushButtonStatus {
    ENABLED("-"),
    PRESSED("O"),
    ERROR("X"),
    DISABLED("#");
    
    private final String name;       

    private PushButtonStatus(String s) {
        name = s;
    }
    
    @Override
    public String toString(){
       return name;
    }
    
    public static PushButtonStatus parse(String value) {
        for (PushButtonStatus status: PushButtonStatus.values()) {
            //Logger.getLogger(PushButtonStatus.class.getName()).log(Level.INFO, "Confronto {0} con {1}", new Object[]{value, status.toString()});
            if (value.equals(status.toString())) return status;
        }
        return ENABLED;
    }
}
