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
