/* 
 * Copyright 2015 Rhythm & Hues Studios.
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
