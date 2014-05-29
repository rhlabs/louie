/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.logging.Level;
import org.slf4j.LoggerFactory;

/**
 * A hacky piece of shit
 * @author eyasukoc
 */
public class GuavaSingletonLoadingCache <V> extends SingletonCache<V>{
    
    private final String cacheName;
    private final LoadingCache<String,V> cache;
    private final String KEY = "KEY";
    
    public GuavaSingletonLoadingCache(final String name, CacheBuilder bldr, CacheLoader<String,V> loader) {
        super(name);
        this.cache = bldr.build(loader);
        this.cacheName = name;
    }

    public com.google.common.cache.Cache getGuava() {
        return this.cache;
    }
    
    @Override
    public V get() {
        try {
            return cache.get(KEY);
        } catch (ExecutionException ex) {
            LoggerFactory.getLogger(GuavaBasicCache.class.getName())
                        .error("Failed to execute loader for guava cache 'get' miss.");
            return null;
        }
    }
    
    @Override
    public void set(V value) {
        cache.put(KEY, value);
    }
    
    @Override
    public void put(Object key, V value) throws Exception {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }

    @Override
    public V get(Object key) {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }

    public V get(Object key, Callable<? extends V> valueLoader) {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }
    
    @Override
    public void remove(Object key) throws Exception {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
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
    public void putAll(Map<Object, V> map) throws Exception {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }

    @Override
    public int getSize() {
        return (int) cache.size();
    }
    
    /**
     * Performs any pending maintenance operations needed by the cache.
     */
    public void cleanUp() {
        cache.cleanUp();
    }
    
    public ConcurrentMap<Object,V> asMap() {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }
    
    public ImmutableMap<Object,V> getAllPresent(Iterable<?> keys) {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }
    
    public CacheStats getStats() {
        return cache.stats();
    }

    public void refresh(Object key) {
        cache.refresh(this.KEY);
    }
}
