/*
 * CustomServiceLayer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.layer;

/**
 *
 * @author cjohnson
 */
public class CustomServiceLayer implements ServiceLayer {
    private final String className;
    
    public CustomServiceLayer(String className) {
        this.className = className;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadLayer(Class<T> service) throws Exception {
        return (T) Class.forName(className).newInstance();
    }
    
}
