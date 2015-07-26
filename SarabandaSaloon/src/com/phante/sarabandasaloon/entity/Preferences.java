/*
 * Copyright 2015 elvisdeltedesco.
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
package com.phante.sarabandasaloon.entity;

import com.phante.sarabandasaloon.SarabandaSaloon;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

/**
 *
 * @author elvisdeltedesco
 */
public class Preferences {
    private static final String BASE_PATH = "base_path";
    private static final String TIMEOUT_VALUE = "timeout";
    private static final String CORRECT_TRACK = "correct_track";
    private static final String ERROR_TRACK = "error_track";
    private static final String TIMEOUT_TRACK = "timeout_track";
    private static final String CLASSIC_NETWORK = "classic_network";
    
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SarabandaSaloon.class);
    
    private Preferences(){
    }
    
    public static Preferences getInstance() {
        return PreferencesHolder.INSTANCE;
    }
    
    private static class PreferencesHolder {
        private static final Preferences INSTANCE = new Preferences();
    }
    
    public void removeAll() {
        try {
            prefs.removeNode();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getBasePath() {
        return prefs.get(BASE_PATH, "");
    }
    
    public void setBasePath(String value) {
        prefs.put(BASE_PATH, value);
    }
    
    public String getCorrectTrack() {
        return prefs.get(CORRECT_TRACK, "");
    }
    
    public void setCorrectTrack(String value) {
        prefs.put(CORRECT_TRACK, value);
    }
    
    public String getErrorTrack() {
        return prefs.get(ERROR_TRACK, "");
    }
    
    public void setErrorTrack(String value) {
        prefs.put(ERROR_TRACK, value);
    }
    
    public String getTimeoutTrack() {
        return prefs.get(TIMEOUT_TRACK, "");
    }
    
    public void setTimeoutTrack(String value) {
        prefs.put(TIMEOUT_TRACK, value);
    }
    
    public int getTimeout() {
        return prefs.getInt(TIMEOUT_VALUE, 10);
    }
    
    public void setTimeout(int value) {
        prefs.putInt(TIMEOUT_VALUE, value);
    }
    
    public boolean getClassicNetwork() {
        return prefs.getBoolean(CLASSIC_NETWORK, false);
    }
    
    public void setClassicNetwork(boolean value) {
        prefs.putBoolean(CLASSIC_NETWORK, value);
    }
    
}
