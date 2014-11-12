/*
 * SupplierCache.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * A Guava Supplier based cache.  Uses Suppliers.memoize to persist the original 
 * supplier value.  
 * 
 * DO NOT create a memoized supplier outside of this class 
 * and use it to create a cache.  This will result in undesirable caching behavior
 * as certain functionality will be indeterminate.
 * 
 * @param <V> the type of the value
 */
public class SupplierCache <V> implements Cache<Object, V> {
    
    private final String name;
    private final Supplier<V> supplier;
    private final long duration;
    private final TimeUnit timeUnit;
    
    private Supplier<V> cache;
    
    @SuppressWarnings("unchecked")
    public static <V> SupplierCache<V> nonCaching(String name, Supplier<V> supplier) {
        return new SupplierCache(name,supplier,0, TimeUnit.SECONDS);
    }
    
    @SuppressWarnings("unchecked")
    public static <V> SupplierCache<V> permanent(String name, Supplier<V> supplier) {
        return new SupplierCache(name,supplier,-1, TimeUnit.SECONDS);
    }
    
    /**
     * Creates an expiring,caching Supplier, 
     * if duration = 0 caching will effectively be disabled
     * if duration < 0 the cache will never expire
     * 
     * @param <V>
     * @param name
     * @param supplier
     * @param duration
     * @param timeUnit
     * @return 
     */
    @SuppressWarnings("unchecked")
    public static <V> SupplierCache<V> expiring(String name, Supplier<V> supplier,
            long duration, TimeUnit timeUnit) {
        return new SupplierCache(name,supplier,duration,timeUnit);
    }
    
    private SupplierCache(final String name, Supplier<V> supplier,
            long duration, TimeUnit timeUnit) {
        this.name = name;
        this.supplier = supplier;
        this.duration = duration;
        this.timeUnit = timeUnit;
        cache = createCache();
    }

    private Supplier<V> createCache() {
        if (duration==0) {
            return supplier;
        } else if (duration<0) {
            return Suppliers.memoize(supplier);
        } else {
            return Suppliers.memoizeWithExpiration(supplier,duration,timeUnit);
        }
    }
    
    public Supplier<V> getBaseSupplier() {
        return supplier;
    }
    
    public V get() {
        return cache.get();
    }
    
    /**
     * Calls the underlying supplier, re-storing it in the cache
     * 
     * @return the new value
     */
    public V reload() {
        Supplier<V> temp = createCache();
        V value = temp.get();
        cache = temp;
        return value;
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
        // Only way to clear a memoized supplier is to throw away the whole thing
        // So we do this here to just recreate the memoized supplier, if needed
        cache = createCache();
    }

    @Override
    public String getCacheName() {
        return name;
    }

    @Override
    public void putAll(Map<Object, V> map) throws Exception {
        throw new UnsupportedOperationException("Singleton cache doesn't support this");
    }

    @Override
    public int getSize() {
        return 1;
    }
}
