/*
 * SimpleCache.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author cjohnson
 */
public class SimpleCache<K, V> implements Cache<K, V> {
    private final String cacheName;
    Map<K, V> cacheMap;

    public SimpleCache(final String cacheName) {
        this.cacheName = cacheName;
        cacheMap = new ConcurrentHashMap<K,V>();
    }

    @Override
    public void put(final K key, final V value) throws Exception {
        getCache().put(key, value);
    }
    
    @Override
    public void putAll(Map<K,V> map) throws Exception {
        getCache().putAll(map);
    }

    @Override
    public V get(final K key) {
        return getCache().get(key);
    }

    @Override
    public void remove(K key) throws Exception {
        getCache().remove(key);
    }

    @Override
    public void clear() throws Exception {
        getCache().clear();
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    public Map<K, V> getCache() {
        return cacheMap;
    }
    
    @Override
    public int getSize() {
        return getCache().size();
    }
}
