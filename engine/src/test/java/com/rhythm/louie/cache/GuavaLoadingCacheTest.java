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
