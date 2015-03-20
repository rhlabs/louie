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
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author Rhythm & Hues Studios
 */
public class LoadingSupplierTest {
    
    public LoadingSupplierTest() {
    }

    @Test
    public void testGet() {
        int breakInterval = 3;
        Supplier<Integer> supplier = new FaultTestSupplier(breakInterval);
        
        for (int i=1;i<=10;i++) {
            Integer value = supplier.get();
            System.out.println(i+": "+ value);
            
            int testValue = i;
            if (i%breakInterval==0) {
                testValue--;
            }
            assertEquals(value,Integer.valueOf(testValue));
        }
        
    }

    public class FaultTestSupplier extends LoadingSupplier<Integer> {
        private final int breakInterval;
        private int i = 0;
        public FaultTestSupplier(int breakInterval) {
            this.breakInterval = breakInterval;
        }
        
        @Override
        public Integer load() throws Exception {
            i++;
            if (i%breakInterval==0) {
                throw new Exception("Break every "+breakInterval+" request");
            }
            return i;
        }
    }
    
    
    @Test
    public void testGet2() {
        int breakInterval = 3;
        Supplier<Integer> supplier = new FaultTestSupplier2<>(new FaultySupplier(breakInterval));
        
        for (int i=1;i<=10;i++) {
            Integer value = supplier.get();
            System.out.println(i+": "+ value);
            
            int testValue = i;
            if (i%breakInterval==0) {
                testValue--;
            }
            assertEquals(value,Integer.valueOf(testValue));
        }
        
    }

    public class FaultTestSupplier2<T> implements Supplier<T> {
        private final Supplier<T> supplier;
        public FaultTestSupplier2(Supplier<T> supplier) {
            this.supplier = supplier;
        }
        
        private T previous = null;

        @Override
        public final T get() {
            try {
                previous = supplier.get();
            } catch (Exception e) {
                LoggerFactory.getLogger(LoadingSupplier.class).error("Error looking up supplier value!", e);
            }
            return previous;
        }
        
    }
    
    public class FaultySupplier implements Supplier<Integer> {
        private final int breakInterval;
        private int i = 0;
        public FaultySupplier(int breakInterval) {
            this.breakInterval = breakInterval;
        }
        
        @Override
        public Integer get() {
            i++;
            if (i%breakInterval==0) {
                throw new RuntimeException("Break every "+breakInterval+" request");
            }
            return i;
        }
    }
    
     private class RecordingSupplier extends LoadingSupplier<Integer> {

        public int count = 0;
                
        @Override
        public Integer load() {
            count++;
            System.out.println("load:" +count);
            return count;
        }
    }
    
    @Test
    public void testFlushing() throws Exception {
        System.out.println("SupplierCache.permanent");
        
        CacheManager cacheManager = CacheManager.createIfNeeded("TestCacheManager");
        
        SupplierCache<Integer> cache = cacheManager.supplierCache("Groups", 
                new RecordingSupplier(), 1, TimeUnit.HOURS);
        
        assertEquals(1, cache.get().intValue());
        assertEquals(1, cache.get().intValue());
        
        cache.clear();
        assertEquals(2, cache.get().intValue());
        
    }

}
