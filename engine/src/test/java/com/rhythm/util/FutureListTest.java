/*
 * FutureListTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class FutureListTest {
    
    public FutureListTest() {
        
       
    }
    
    private class TestCall implements Callable<String> {
        String s;
        public TestCall(String s) {
            this.s=s;
        }
        
        @Override
        public String call() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FutureListTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            char[] chars =  s.toCharArray();
            char[] rchars = new char[chars.length];
            for (int i=0;i<chars.length;i++) {
                rchars[chars.length-1-i]=chars[i];
            }
            String r = new String(rchars);
            System.out.println("Producing : "+r);
            return r;
        }
    };

    @Test
    public void testIter() {
        List<Future<String>> results = new ArrayList<Future<String>>();
        
        ExecutorService es = Executors.newFixedThreadPool(2);
        results.add(es.submit(new TestCall("cat")));
        results.add(es.submit(new TestCall("dog")));
        results.add(es.submit(new TestCall("blah")));
        results.add(es.submit(new TestCall("hello")));
        results.add(es.submit(new TestCall("goodbye")));
        results.add(es.submit(new TestCall("woohoo")));
        
        List<String> fList = new FutureList<String>(results);
        for (String s : fList) {
            System.out.println("Item : "+s);
        }
    }

    @Test
    public void testIsEmpty() {
    }

    @Test
    public void testContains() {
    }

    @Test
    public void testGet() {
    }

}
