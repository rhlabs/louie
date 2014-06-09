/*
 * Cache.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.cache;

import java.util.Map;

/**
 * @author cjohnson
 * Created: Feb 10, 2011 2:03:55 PM
 */
public interface Cache<K, V> {

    void put(K key, V value) throws Exception;

    V get(K key);

    void remove(K key) throws Exception;

    void clear() throws Exception;

    String getCacheName();
    
    void putAll(Map<K,V> map) throws Exception;
    
    int getSize();
}
