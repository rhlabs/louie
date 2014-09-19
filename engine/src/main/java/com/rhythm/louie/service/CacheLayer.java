/*
 * Layer.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.jms.MessageHandler;

/**
 *
 * @author cjohnson
 */
public interface CacheLayer {
    
    MessageHandler getMessageHandler();
    
    CacheManager getCacheManager();
    
    void initialize() throws Exception;
    
    void shutdown() throws Exception;

}
