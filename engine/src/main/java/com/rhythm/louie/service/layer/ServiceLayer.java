/*
 * ServiceLayer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.layer;

/**
 *
 * @author cjohnson
 */
public interface ServiceLayer {
    <T> T loadLayer(Class<T> service) throws Exception; 
}
