/*
 * CustomProperty.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.server;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eyasukoc
 */
public class CustomProperty {
    
    private final String name;
    private final Map<String,String> properties;    
    
    public CustomProperty(String name) {
        this.name = name;
        properties = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setProperty(String key, String value){
        properties.put(key, value);
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public String getProperty(String key, String def) {
        String value = properties.get(key);
        if (value == null) return def;
        return value;
    }
}
