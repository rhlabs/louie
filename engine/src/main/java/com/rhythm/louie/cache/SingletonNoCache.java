/*
 * SingletonCache.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

/**
 *  Purpose of this cache is to act as a means to put lists and other non-map
 *  style caches into the cache framework.
 *  THIS DOES NOT CACHE
 */
public class SingletonNoCache<V> extends SingletonCache<V> {
    public SingletonNoCache(final String cacheName) {
        super(cacheName);
    }

    @Override
    public V get() {
        return null;
    }
    
    @Override
    public void set(V value) {}

    @Override
    public void clear() throws Exception {}

    @Override
    public int getSize() {
        return 0;
    }
}
