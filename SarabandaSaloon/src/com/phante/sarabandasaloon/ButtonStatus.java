/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon;

/**
 *
 * @author deltedes
 */
public enum ButtonStatus {
    ENABLED("-"),
    PRESSED("O"),
    ERROR("X"),
    DISABLED("#");
    
    private final String name;       

    private ButtonStatus(String s) {
        name = s;
    }
    
    @Override
    public String toString(){
       return name;
    }
    
    public static ButtonStatus parse(String value) {
        for (ButtonStatus status: ButtonStatus.values()) {
            if (value.equals(status.toString())) return status;
        }
        return ENABLED;
    }
}
