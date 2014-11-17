/*
 * GuavaLoadingCache.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilderSpec;

import org.slf4j.LoggerFactory;

/**
 * The Google Guava LoadingCache wrapper
 * @author eyasukoc
 * @param <K> key type
 * @param <V> value type
 */
public class GuavaLoadingCache<K, V> implements GuavaCache<K, V> {

    private final String cacheName;
    private final LoadingCache<K, V> cache;

    private GuavaLoadingCache(String name, CacheBuilder<K, V> bldr, CacheLoader<K, V> loader) {
        this.cacheName = name;
        this.cache = bldr.build(loader);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaLoadingCache<K,V> nonCaching(String name, CacheLoader<K, V> loader) {
        return new GuavaLoadingCache(name,CacheBuilder.from(CacheBuilderSpec.disableCaching()),loader);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaLoadingCache<K,V> permanent(String name, CacheLoader<K, V> loader) {
        return new GuavaLoadingCache(name,CacheBuilder.newBuilder(),loader);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaLoadingCache<K,V> fromSpec(String name, CacheBuilderSpec spec, CacheLoader<K, V> loader) {
        return new GuavaLoadingCache(name,CacheBuilder.from(spec),loader);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> GuavaLoadingCache<K,V> fromSpec(String name, String spec, CacheLoader<K, V> loader) {
        return new GuavaLoadingCache(name,CacheBuilder.from(spec),loader);
    }
    
    @Override
    public LoadingCache<K, V> getGuava() {
        return this.cache;
    }
    
    @Override
    public void put(K key, V value) throws Exception {
        cache.put(key, value);
    }

    /**
     * Returns the value if present, otherwise loads the value
     * 
     * @param key
     * @return 
     */
    @Override
    public V get(K key) {
        try {
            return cache.get(key);
        } catch (ExecutionException ex) {
            LoggerFactory.getLogger(GuavaBasicCache.class.getName())
                        .error("Failed to execute loader for guava cache 'get' miss.");
            return null;
        }
    }

    /**
     * Returns the value associated with key in this cache, or null if there is no cached value for key.

     * @param key
     * @return 
     */
    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }
    
    /**
     * Loads a new value for key key, possibly asynchronously. While the new 
     * value is loading the previous value (if any) will continue to be returned 
     * by get(key) unless it is evicted. If the new value is loaded successfully 
     * it will replace the previous value in the cache; if an exception is 
     * thrown while refreshing the previous value will remain, and the exception 
     * will be logged (using Logger) and swallowed. 
     * @param key 
     */
    @Override
    public void refresh(K key) {
        cache.refresh(key);
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
    public V get(K key, Callable<? extends V> valueLoader) {
        throw new UnsupportedOperationException("Not supported by LoadingCache"); 
    }

    @Override
    public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
        return cache.getAll(keys);
    }
}
