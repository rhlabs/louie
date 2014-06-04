/*
 * GuavaLoadingCacheTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.cache;

import com.google.common.cache.CacheLoader;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class GuavaLoadingCacheTest {
    
    public GuavaLoadingCacheTest() {
    }

    public class TestLoader extends CacheLoader<String,Integer>  {
        public int count = 0;
        @Override
        public Integer load(String key) throws Exception {
            count++;
            return key.length();
        }
    }
    
    @Test
    public void testNonCaching() {
        System.out.println("SupplierCache.nonCaching");
        
        String KEY = "abc";
        
        TestLoader loader = new TestLoader();
        GuavaLoadingCache<String,Integer> cache = GuavaLoadingCache.nonCaching("Blah",loader);
        
        assertEquals(0, loader.count);
        assertNotNull(cache.get(KEY));
        assertEquals(1, loader.count);
        assertNotNull(cache.get(KEY));
        assertEquals(2, loader.count);
    }

    @Test
    public void testPermanent() throws Exception {
        System.out.println("SupplierCache.nonCaching");
        
        String KEY = "abc";
        
        TestLoader loader = new TestLoader();
        GuavaLoadingCache<String,Integer> cache = GuavaLoadingCache.permanent("Blah",loader);
        
        assertEquals(0, loader.count);
        assertNotNull(cache.get(KEY));
        assertEquals(1, loader.count);
        assertNotNull(cache.get(KEY));
        assertEquals(1, loader.count);
        
        cache.clear();
        assertNotNull(cache.get(KEY));
        assertEquals(2, loader.count);
        assertNotNull(cache.get(KEY));
        assertEquals(2, loader.count);
    }

}
