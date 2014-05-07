/*
 * SingletonCache.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import java.util.Map;

/**
 *  Purpose of this cache is to act as a means to put lists and other non-map
 *  style caches into the cache framework.
 */
public class SingletonCache<V> implements Cache<Object, V> {
    private final String cacheName;
    private V value;

    public SingletonCache(final String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void put(final Object key, final V value) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void putAll(Map<Object,V> map) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(final Object key) {
        throw new UnsupportedOperationException();
    }
    
    public V get() {
        return value;
    }
    
    public void set(V value) {
        this.value=value;
    }

    @Override
    public void remove(final Object key) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() throws Exception {
        value = null;
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }
    
    @Override
    public int getSize() {
        return value==null?0:1;
    }
}
