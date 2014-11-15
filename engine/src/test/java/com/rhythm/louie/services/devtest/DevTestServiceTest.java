/*
 * JmsTestServiceTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.devtest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class DevTestServiceTest {
    
    
    private static DevTestServiceClient client;
    public DevTestServiceTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        LouieConnection conn = LouieConnectionFactory.getConnection("localhost",
                        Identity.createJUnitIdentity());
        
        client = DevTestClientFactory.getClient(conn);
    }
    
    
    @Test
    public void messageFeedbackTest() throws Exception {
        String captured = client.messageTest("Test Message");
        System.out.println("Captured: " + captured);
//        captured = client.messageTest("test2");
//        System.out.println("Captured: " + captured);
    }
    
    @Test
    public void sendEmail() throws Exception {
        client.sendEmail("cjohnson@rhythm.com", "cjohnson@rhythm.com", "Testing email services", "Hello!");
    }
    
}
