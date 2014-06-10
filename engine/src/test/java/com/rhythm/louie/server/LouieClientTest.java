/*
 * LouieClientTest.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.pb.louie.LouieProtos.ServicePB;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnectionFactory;
import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class LouieClientTest {
    
    private static LouieClient client;
    public LouieClientTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = LouieClientFactory.getClient(
                LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity()));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getAllServiceNames method, of class LouieClient.
     */
    @Test
    public void testGetAllServiceNames() throws Exception {
        System.out.println("getAllServiceNames");
        
        List<String> result = client.getAllServiceNames();
        assertNotNull(result);
        assertTrue(!result.isEmpty());

        for (String s : result) {
            System.out.println(s);
        }
    }

    /**
     * Test of getAllServices method, of class LouieClient.
     */
    @Test
    public void testGetAllServices() throws Exception {
        System.out.println("getAllServices");

        List<ServicePB> result = client.getAllServices();
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        
        for (ServicePB service :result) {
            System.out.println(service);
        }
    }

    /**
     * Test of getService method, of class LouieClient.
     */
    @Test
    public void testGetService() throws Exception {
        System.out.println("getService");
   
        ServicePB result = client.getService("scene");
        assertNotNull(result);
    
        System.out.println(result);
    }
    
    
    /**
     * Test of getService method, of class LouieClient.
     */
    @Test
    public void echoTest() throws Exception {
        System.out.println("echoTest");
        
        ArrayList<Thread> threads = new ArrayList<Thread>();
        
        for (int i=0;i<10;i++) {
            final int f = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client = LouieClientFactory.getClient(
                        LouieConnectionFactory.getLocalConnection(
                        Identity.createJUnitIdentity()));
                        
                        String result = client.echoTest(Integer.toString(f),5000);
                        assertNotNull(result);
                        
                        System.out.println(result);
                    } catch (Exception ex) {
                        Logger.getLogger(LouieClientTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            threads.add(t);
            t.start();
        }
        
        for (Thread t : threads) {
            while (t.isAlive()) {
                Thread.sleep(10);
            }
            System.out.println("Thread finished");
        }
        
        
    }
    
     /**
     * Test of getService method, of class LouieClient.
     */
    @Test
    public void loopTest() throws Exception {
        System.out.println("loopTest");
        
        String result = client.loopTest(Arrays.asList("vans256","louiebeta.rhythm.com","lid1577"));
        assertNotNull(result);
        
        System.out.println(result);
    }
    
    
}
