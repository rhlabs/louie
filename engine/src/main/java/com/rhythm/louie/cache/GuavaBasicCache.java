/*
 * GuavaBasicCache.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilderSpec;

import org.slf4j.LoggerFactory;

/**
 * The basic Google Guava Cache wrapper
 * @author eyasukoc
 */
public class GuavaBasicCache<K, V> implements GuavaCache<K, V> {
    
    private final String cacheName;
    private final com.google.common.cache.Cache<K,V> cache;
    
    private GuavaBasicCache(String name, CacheBuilder<Object, Object> bldr) {
        this.cache = bldr.build();
        this.cacheName = name;
    }
    
    @SuppressWarnings("unchecked")
    public static <K,V> GuavaBasicCache<K,V> nonCaching(String name) {
        return new GuavaBasicCache(name,CacheBuilder.from(CacheBuilderSpec.disableCaching()));
    }
    
    @SuppressWarnings("unchecked")
    public static <K,V> GuavaBasicCache<K,V> permanent(String name) {
        return new GuavaBasicCache(name,CacheBuilder.newBuilder());
    }
    
    @SuppressWarnings("unchecked")
    public static <K,V> GuavaBasicCache<K,V> fromSpec(String name, CacheBuilderSpec spec) {
        return new GuavaBasicCache(name,CacheBuilder.from(spec));
    }
     
    @SuppressWarnings("unchecked")
    public static <K,V> GuavaBasicCache<K,V> fromSpec(String name, String spec) {
        return new GuavaBasicCache(name,CacheBuilder.from(spec));
    }
    
    @Override
    public com.google.common.cache.Cache<K,V> getGuava() {
        return this.cache;
    }
    
    @Override
    public void put(K key, V value) throws Exception {
        cache.put(key, value);
    }

    /**
     * Returns the value if present, null otherwise
     * @param key
     * @return 
     */
    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }
    
    @Override
    public V get(K key, Callable<? extends V> valueLoader) {
        try {
            return cache.get(key,valueLoader);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(GuavaBasicCache.class.getName())
                    .error("Failed to execute callable for guava cache 'get' miss.");
            return null;
        }
    }
    
    @Override
    public void remove(K key) throws Exception {
        cache.invalidate(key);
    }

    @Override
    public void clear() throws Exception {
        cache.invalidateAll();
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public void putAll(Map<K, V> map) throws Exception {
        cache.putAll(map);
    }

    @Override
    public int getSize() {
        return (int) cache.size();
    }
    
    /**
     * Performs any pending maintenance operations needed by the cache.
     */
    @Override
    public void cleanUp() {
        cache.cleanUp();
    }
    
    @Override
    public ConcurrentMap<K,V> asMap() {
        return cache.asMap();
    }
    
    @Override
    public ImmutableMap<K,V> getAllPresent(Iterable<?> keys) {
        return cache.getAllPresent(keys);
    }
    
    @Override
    public CacheStats getStats() {
        return cache.stats();
    }

    @Override
    public void refresh(K key) {
        throw new UnsupportedOperationException("Only supported by GuavaLoadingCache.");
    }

    /**
     * Unlike LoadingCache, this executes a getAllPresent
     * @param keys
     * @return
     * @throws ExecutionException 
     */
    @Override
    public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
        return cache.getAllPresent(keys);
    }
    
}
