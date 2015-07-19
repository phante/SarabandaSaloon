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

/**
 *
 * @author elvisdeltedesco
 */
public class PreferencesUtility {
    public static final String BASE_PATH = "SarabandaPath";
    public static final String TRACKLIST_DIRECTORY = "//tracklist//";
    
    public static String get(String properties) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SarabandaSaloon.class);
        return prefs.get(properties, null);
    }
    
    public static void set(String properties, String value) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SarabandaSaloon.class);
        prefs.put(properties, value);
    }
    
    public static void delete(String properties) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SarabandaSaloon.class);
        prefs.remove(properties);
    }
}
