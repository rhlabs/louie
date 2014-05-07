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

    public void put(K key, V value) throws Exception;

    public V get(K key);

    public void remove(K key) throws Exception;

    public void clear() throws Exception;

    public String getCacheName();
    
    public void putAll(Map<K,V> map) throws Exception;
    
    public int getSize();
}
