/*
 * NoCache.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.cache;

import java.util.Map;

/**
 * This fulfills the Cache implementation without actually caching
 * THIS DOES NOT CACHE
 */
public class NoCache<K, V> implements Cache<K, V> {
    private final String cacheName;

    public NoCache(final String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void put(final K key, final V value) throws Exception {}

    @Override
    public void putAll(Map<K,V> map) throws Exception {}
    
    @Override
    public V get(final K key) {
        return null;
    }

    @Override
    public void remove(K key) throws Exception {}

    @Override
    public void clear() throws Exception {}

    @Override
    public String getCacheName() {
        return cacheName;
    }
    @Override
    public int getSize() {
        return 0;
    }
}
