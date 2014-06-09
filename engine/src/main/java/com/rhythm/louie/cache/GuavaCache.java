/*
 * GuavaCache.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * I hate that this exists.
 * It is done specifically so that the manager can handle it, and also so that 
 * we can present an EMPTY cache if caching is disabled
 * @author eyasukoc
 */
public interface GuavaCache <K, V> extends Cache <K, V>{

    com.google.common.cache.Cache getGuava();
    
    V get(K key, Callable<? extends V> valueLoader);
    
    V getIfPresent(K key);
    
    void refresh(K key);
    
    void cleanUp();
    
    ConcurrentMap<K,V> asMap();
    
    ImmutableMap<K,V> getAllPresent(Iterable<?> keys);
    
    CacheStats getStats();
    
    ImmutableMap<K,V> getAll(Iterable<? extends K> keys) throws ExecutionException;
}
