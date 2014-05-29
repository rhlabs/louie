/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.cache;

import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
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

    public com.google.common.cache.Cache getGuava();
    
    public V get(K key, Callable<? extends V> valueLoader);
    
    public void refresh(K key);
    
    public void cleanUp();
    
    public ConcurrentMap<K,V> asMap();
    
    public ImmutableMap<K,V> getAllPresent(Iterable<?> keys);
    
    public CacheStats getStats();
    
    ImmutableMap<K,V> getAll(Iterable<? extends K> keys) throws ExecutionException;
}
