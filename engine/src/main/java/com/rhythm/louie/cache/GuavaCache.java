/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
