/*
 * SupplierCacheTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.cache;


import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class SupplierCacheTest {
    
    public SupplierCacheTest() {
    }

    private class RecordingSupplier implements Supplier<String> {

        public int count = 0;
                
        @Override
        public String get() {
            count++;
            return "VALUE";
        }
    }
        
    
    @Test
    public void testNonCaching() {
        System.out.println("SupplierCache.nonCaching");
        
        RecordingSupplier supplier = new RecordingSupplier();
        SupplierCache<String> cache = SupplierCache.nonCaching("Blah",supplier);
        
        assertEquals(0, supplier.count);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        assertNotNull(cache.get());
        assertEquals(2, supplier.count);
    }

    @Test
    public void testPermanent() throws Exception {
        System.out.println("SupplierCache.permanent");
        
        RecordingSupplier supplier = new RecordingSupplier();
        SupplierCache<String> cache = SupplierCache.permanent("Blah",supplier);
        
        assertEquals(0, supplier.count);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        
        cache.clear();
        assertNotNull(cache.get());
        assertEquals(2, supplier.count);
        
    }

    @Test
    public void testExpiring() throws Exception {
        System.out.println("SupplierCache.nonCaching");
        
        RecordingSupplier supplier = new RecordingSupplier();
        SupplierCache<String> cache = SupplierCache.expiring("Blah",supplier,1,TimeUnit.SECONDS);
        
        assertEquals(0, supplier.count);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        
        Thread.sleep(500);
        assertNotNull(cache.get());
        assertEquals(1, supplier.count);
        
        Thread.sleep(1000);
        assertNotNull(cache.get());
        assertEquals(2, supplier.count);
        
    }

}
