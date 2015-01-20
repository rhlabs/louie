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
