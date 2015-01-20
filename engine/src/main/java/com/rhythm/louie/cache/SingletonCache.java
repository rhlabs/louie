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

import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;

/**
 *  Purpose of this cache is to act as a means to put lists and other non-map
 *  style caches into the cache framework.
 */
public class SingletonCache<V> implements Cache<Object, V> {
    private static final Integer KEY = 1;
    
    private final String cacheName;
    private final com.google.common.cache.Cache<Object,V> cache;

    private SingletonCache(String cacheName, CacheBuilder<Object, V> bldr) {
        this.cacheName = cacheName;
        this.cache = bldr.build();
    }
    
    @SuppressWarnings("unchecked")
    public static <V> SingletonCache<V> nonCaching(String name) {
        return new SingletonCache(name,CacheBuilder.from(CacheBuilderSpec.disableCaching()));
    }
    
    @SuppressWarnings("unchecked")
    public static <V> SingletonCache<V> permanent(String name) {
        return new SingletonCache(name,CacheBuilder.newBuilder());
    }
    
    @SuppressWarnings("unchecked")
    public static <V> SingletonCache<V> fromSpec(String name, CacheBuilderSpec spec) {
        return new SingletonCache(name,CacheBuilder.from(spec));
    }
    
    @SuppressWarnings("unchecked")
    public static <V> SingletonCache<V> fromSpec(String name, String spec) {
        return new SingletonCache(name,CacheBuilder.from(spec));
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
        return cache.getIfPresent(KEY);
    }
    
    public void set(V value) {
        cache.put(KEY, value);
    }

    @Override
    public void remove(final Object key) throws Exception {
        throw new UnsupportedOperationException();
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
    public int getSize() {
        return (int) cache.size();
    }
}
