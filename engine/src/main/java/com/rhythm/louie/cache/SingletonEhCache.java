/*
 * SingletonEhCache.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 *
 * @author cjohnson
 */
public class SingletonEhCache<V> extends SingletonCache<V> {
    private static final Integer KEY = 1;
    
    private final net.sf.ehcache.CacheManager cacheManager;

    public SingletonEhCache(final String cacheName, final net.sf.ehcache.CacheManager cacheManager) {
        super(cacheName);
        this.cacheManager = cacheManager;
        
        cacheManager.addCacheIfAbsent(cacheName);
    }
    
    public Ehcache getCache() {
        return cacheManager.getEhcache(getCacheName());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public V get() {
        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        try {
            Element element = getCache().get(KEY);
            if (element != null) {
                return (V) element.getObjectValue();
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(prev);
        }
    }
    
    @Override
    public void set(V value) {
        getCache().put(new Element(KEY, value));
    }
    
    @Override
    public void clear() throws Exception {
        getCache().removeAll();
    }

    @Override
    public int getSize() {
        return getCache().getSize();
    }
}
