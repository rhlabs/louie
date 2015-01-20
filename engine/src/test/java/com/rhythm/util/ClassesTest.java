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
package com.rhythm.util;

import com.rhythm.louie.Classes;

import java.util.List;

import org.junit.Test;

import com.rhythm.louie.services.auth.AuthService;
import com.rhythm.louie.Service;
import com.rhythm.louie.services.info.InfoService;
import com.rhythm.louie.services.status.StatusService;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class ClassesTest {
    
    public ClassesTest() {
    }

    @Test
    public void testGetTypesAnnotatedWith() throws Exception {
        System.out.println("getTypesAnnotatedWith");
        
        List<Class<?>> cl = Classes.getTypesAnnotatedWith("com.rhythm.louie.server", Service.class);
        
        assertEquals(cl.size(), 1);
        assertEquals(cl.get(0).getName(), InfoService.class.getName());
        
        for (Class<?> c : cl) {
            System.out.println(c.getName());
        }
    }
    
    @Test
    public void testGetRecursiveTypesAnnotatedWith() throws Exception {
        System.out.println("getRecursiveTypesAnnotatedWith");
        
        List<Class<?>> cl = Classes.getRecursiveTypesAnnotatedWith("com.rhythm", Service.class);
        
        assertEquals(cl.size(), 3);
        assertTrue(cl.contains(InfoService.class));
        assertTrue(cl.contains(AuthService.class));
        assertTrue(cl.contains(StatusService.class));
        
        for (Class<?> c : cl) {
            System.out.println(c.getName());
        }
    }

    @Test
    public void speedTest() throws Exception {
        System.out.println("Google");
        for (int i=0;i<10;i++) {
            long start = System.nanoTime();
            Classes.getRecursiveTypesAnnotatedWith("com.rhythm", Service.class);
            long time = System.nanoTime()-start;
            System.out.println(time);
        }
        
        
        System.out.println("Google Package");
        for (int i=0;i<10;i++) {
            long start = System.nanoTime();
            Classes.getTypesAnnotatedWith("com.rhythm.louie.server", Service.class);
            long time = System.nanoTime()-start;
            System.out.println(time);
        }
    }
}
