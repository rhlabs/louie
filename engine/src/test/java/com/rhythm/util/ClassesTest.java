/*
 * ClassesTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.util;

import com.rhythm.louie.Classes;

import java.util.List;

import org.junit.Test;

import com.rhythm.louie.auth.AuthService;
import com.rhythm.louie.Service;
import com.rhythm.louie.info.InfoService;
import com.rhythm.louie.testservice.TestService;

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
        assertTrue(cl.contains(TestService.class));
        
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
