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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class GuavaBasicCacheTest {
    
    public GuavaBasicCacheTest() {
    }

    @Test
    public void testNonCaching() throws Exception {
        System.out.println("GuavaBasicCache.nonCaching");
        
        String key = "TEST";
        GuavaBasicCache<String,Integer> cache = GuavaBasicCache.nonCaching("Blah");
        
        assertEquals(0, cache.getSize());
        cache.put(key, 1);
        assertEquals(0, cache.getSize());
        
        Integer value = cache.get(key);

        assertNull(value);
    }

    @Test
    public void testPermanent() throws Exception {
        System.out.println("GuavaBasicCache.permanent");
        
        String key = "TEST";
        GuavaBasicCache<String,Integer> cache = GuavaBasicCache.permanent("Blah");
        
        assertEquals(0, cache.getSize());
        cache.put(key, 1);
        assertEquals(1, cache.getSize());
        
        Integer value = cache.get(key);

        assertNotNull(value);
    }

    @Test
    public void testFromSpec() throws Exception{
         System.out.println("GuavaBasicCache.fromSpec");
        
        String key = "TEST";
        
        String spec = "expireAfterWrite=1s";
        GuavaBasicCache<String,Integer> cache = GuavaBasicCache.fromSpec("Blah",spec);
        
        assertEquals(0, cache.getSize());
        cache.put(key, 1);
        assertEquals(1, cache.getSize());
        Integer value = cache.get(key);
        assertNotNull(value);
        
        Thread.sleep(500);
        assertEquals(1, cache.getSize());
        value = cache.get(key);
        assertNotNull(value);
        
        Thread.sleep(800);
        value = cache.get(key);
        assertNull(value);
        assertEquals(0, cache.getSize());
        
    }

}
