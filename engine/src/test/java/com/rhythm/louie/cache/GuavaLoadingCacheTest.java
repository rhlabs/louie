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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

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

    
    @Test
    public void testFaultyGet() throws Exception {
        int breakInterval = 4;
        
        GuavaLoadingCache<String,Integer> cache = GuavaLoadingCache.fromSpec("Blah","expireAfterWrite=1s",
                 new FaultTestLoader(breakInterval));
        
        for (int i=1;i<=6;i++) {
            System.out.println("getALL");
            Map<String,Integer> values = cache.getAll(Lists.newArrayList("A", "B"));
            for (Map.Entry<String,Integer> entry : values.entrySet()) {
                System.out.println(i+": "+ entry.getKey()+"->"+entry.getValue());
            }
            
//            int testValue = i;
//            if (i%breakInterval==0) {
//                testValue--;
//            }
//            assertEquals(value,Integer.valueOf(testValue));
            try {
                Thread.sleep(1100);
            } catch (Exception e) {}
        }
        
    }

    public class FaultTestLoader extends CacheLoader<String, Integer> {

        @Override
        public Map<String, Integer> loadAll(Iterable<? extends String> keys) throws Exception {
            System.out.println("loadALL");
          Map<String,Integer> results = new HashMap<>();
          for (String key : keys) {
            results.put(key, getValue());
          }
          return results;
        }

//        @Override
//        public ListenableFuture<Integer> reload(String key, Integer oldValue) throws Exception {
//            return super.reload(key, oldValue); //To change body of generated methods, choose Tools | Templates.
//        }

        private final int breakInterval;
        private int i = 0;

        public FaultTestLoader(int breakInterval) {
            this.breakInterval = breakInterval;
        }

        @Override
        public Integer load(String key) throws Exception {
            System.out.println("LOAD: "+key);
            return getValue();
        }
        
        private Integer getValue() throws Exception {
            Thread.sleep(1000);
            i++;
            if (i % breakInterval == 0) {
                throw new Exception("Break every " + breakInterval + " request");
            }
            return i;
        }
    }
    
    
    public class TestCacheLoader extends CacheLoader<String, Integer> {

        @Override
        public Map<String, Integer> loadAll(Iterable<? extends String> keys) throws Exception {
            System.out.println("loadAll: " + Joiner.on(",").join(keys));

            Map<String, Integer> results = new HashMap<>();
            for (String key : keys) {
                results.put(key, 1);
            }
            return results;
        }

        @Override
        public Integer load(String key) throws Exception {
            System.out.println("load: " + key);
            return 1;
        }
    }

    @Test
    public void testRefreshAfterWrite() throws Exception {

        LoadingCache<String, Integer> cache
                = CacheBuilder.from("refreshAfterWrite=1s").build(new TestCacheLoader());

        for (int i = 1; i <= 4; i++) {
            System.out.println("getAll");
            Map<String, Integer> values = cache.getAll(Lists.newArrayList("A", "B"));
            Thread.sleep(1100);
        }
    }
}
