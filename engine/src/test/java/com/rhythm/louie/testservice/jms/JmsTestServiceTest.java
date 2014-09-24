/*
 * JmsTestServiceTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.testservice.jms;

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
public class JmsTestServiceTest {
    
    
    private static JmsTestServiceClient client;
    public JmsTestServiceTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        LouieConnection conn = LouieConnectionFactory.getLocalConnection(
                        Identity.createJUnitIdentity());
        
        client = JmsTestClientFactory.getClient(conn);
    }
    
    
    @Test
    public void messageFeedbackTest() throws Exception {
        String captured = client.messageTest("test1");
        System.out.println("Captured: " + captured);
//        captured = client.messageTest("test2");
//        System.out.println("Captured: " + captured);
    }
    
}
