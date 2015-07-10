/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phante.sarabandasaloon.network;

/**
 *
 * @author deltedes
 */
public class SarabandaStatus {
    
    private SarabandaStatus() {
    }
    
    public static SarabandaStatus getInstance() {
        return SarabandaStatusHolder.INSTANCE;
    }
    
    private static class SarabandaStatusHolder {

        private static final SarabandaStatus INSTANCE = new SarabandaStatus();
    }
}
