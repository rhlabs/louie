/*
 * EhCache.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * @author cjohnson
 * Created: Jun 28, 2011 6:41:17 PM
 */
public class EhCache<K, V> implements Cache<K, V> {
    private final String cacheName;
    private final net.sf.ehcache.CacheManager cacheManager;

    public EhCache(final String cacheName, final net.sf.ehcache.CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
        
        cacheManager.addCacheIfAbsent(cacheName);
    }

    @Override
    public void put(final K key, final V value) {
        getCache().put(new Element(key, value));
    }
    
    @Override
    public void putAll(Map<K,V> map) throws Exception {
        List<Element> elements = new ArrayList<Element>(map.size());
        for (Map.Entry<K,V> entry : map.entrySet()) {
            elements.add(new Element(entry.getKey(), entry.getValue()));
        }
        getCache().putAll(elements);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final K key) {
        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        try {
            Element element = getCache().get(key);
            if (element != null) {
                return (V) element.getObjectValue();
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(prev);
        }
    }

    public Ehcache getCache() {
        return cacheManager.getEhcache(cacheName);
    }
    
    @Override
    public void remove(K key) throws Exception {
        getCache().remove(key);
    }

    @Override
    public void clear() throws Exception {
        getCache().removeAll();
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }
    
    @Override
    public int getSize() {
        return getCache().getSize();
    }
}
