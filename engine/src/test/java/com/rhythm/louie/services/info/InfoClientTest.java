/*
 * InfoClientTest.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.info;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnectionFactory;

import com.rhythm.louie.info.InfoProtos.*;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class InfoClientTest {
    
    private static InfoClient client;
    public InfoClientTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = InfoClientFactory.getClient(
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
     * Test of getAllServiceNames method, of class InfoClient.
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
     * Test of getAllServices method, of class InfoClient.
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
     * Test of getService method, of class InfoClient.
     */
    @Test
    public void testGetService() throws Exception {
        System.out.println("getService");
   
        ServicePB result = client.getService("louie");
        assertNotNull(result);
    
        System.out.println(result);
    }
    
     /**
     * Test of getService method, of class InfoClient.
     */
    @Test
    public void testGetServerLocations() throws Exception {
        System.out.println("getServerLocations");
   
        List<String> result = client.getServerLocations();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    
        for (String location : result) {
            System.out.println(location);
        }
    }
    
     /**
     * Test of getService method, of class InfoClient.
     */
    @Test
    public void testGetServers() throws Exception {
        System.out.println("getServers");
   
        List<ServerPB> result = client.getServers();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    
        for (ServerPB server : result) {
            System.out.println(server);
        }
    }
    
}
