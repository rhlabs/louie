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
package com.rhythm.louie.services.status;

import java.net.InetAddress;
import java.util.List;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.connection.Response;
import com.rhythm.louie.services.info.InfoClientTest;
import com.rhythm.louie.stream.Consumer;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.RoutePathPB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.louie.stream.Consumers;
import com.rhythm.louie.stream.SingleConsumer;
import com.rhythm.louie.stream.StreamingConsumer;

/**
 *
 * @author cjohnson
 */
public class StatusServiceTest {
    
    private static final List<String> HOSTS = 
            Arrays.asList("louietest");

    private static StatusServiceClient client;
    public StatusServiceTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        LouieConnection conn = LouieConnectionFactory.getLocalConnection(
                        Identity.createJUnitIdentity());
        
        client = StatusClientFactory.getClient(conn);
    }
    
    /**
     * Test of getService method, of class LouieClient.
     * @throws java.lang.Exception
     */
    @Test
    public void echoTest() throws Exception {
        System.out.println("echoTest");
        
        ArrayList<Thread> threads = new ArrayList<>();
        
        for (int i=0;i<10;i++) {
            final int f = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client = StatusClientFactory.getClient(
                        LouieConnectionFactory.getLocalConnection(
                        Identity.createJUnitIdentity()));
                        
                        String result = client.echoTest(Integer.toString(f),5000);
                        assertNotNull(result);
                        
                        System.out.println(result);
                    } catch (Exception ex) {
                        Logger.getLogger(InfoClientTest.class.getName()).log(Level.SEVERE, null, ex);
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
     * @throws java.lang.Exception
     */
    @Test
    public void loopTest() throws Exception {
        System.out.println("loopTest");
        
        String result = client.loopTest(HOSTS);
        assertNotNull(result);
        
        System.out.println(result);
    }
    
     /**
     * Test of getService method, of class LouieClient.
     * @throws java.lang.Exception
     */
    @Test(expected = Exception.class)
    public void loopTest_FAIL() throws Exception {
        System.out.println("loopTest");
        
        List<String> dupHosts =new ArrayList<>();
        dupHosts.addAll(HOSTS);
        dupHosts.addAll(HOSTS);
        
        String result = client.loopTest(dupHosts);
        assertNotNull(result);
        
        System.out.println(result);
    }
    
    /**
     * Test of getService method, of class LouieClient.
     * @throws java.lang.Exception
     */
    @Test
    public void routeTest() throws Exception {
        System.out.println("loopTest");
        
        SingleConsumer<com.rhythm.pb.DataTypeProtos.StringPB> consumer = Consumers.newSingleConsumer();
        Response response = client.loopTest(HOSTS,consumer);
        assertNotNull(response);
        
        assertTrue(response.getRouteList().size()==1);
        
        RoutePathPB route = response.getRouteList().get(0);
        
        String localIp = InetAddress.getLocalHost().getHostAddress();
        assertEquals(route.getRoute().getHostIp(), localIp);
        for (String host : HOSTS) {
            String ip = InetAddress.getByName(host).getHostAddress();
            
            System.out.println(host+" : "+ip);
            
            assertTrue(route.getPathCount()==1);
            route = route.getPath(0);
            
            assertEquals(route.getRoute().getHostIp(), ip);
        }
        assertTrue(route.getPathCount()==0);
    }
    
    @Test
    public void streamTest() throws Exception {
        System.out.println("streamTest");

        long start = System.nanoTime();
        
        client.streamTest(10, 100, 500, new Consumer<ErrorPB>() {
            @Override
            public void consume(ErrorPB object) {
                System.out.println("Got Object : "+(System.nanoTime()/1000000));
            }
        });
        System.out.println("YES:" + (System.nanoTime() - start) / 1000000);

    }
    
    @Test
    public void streamTestBulk() throws Exception {
        System.out.println("streamTestBulk");
        
        for (int i=0;i<5;i++) {
            long start = System.nanoTime();
            List<ErrorPB> result = client.streamTest(1000,10000,0);
            System.out.println("YES:"+(System.nanoTime()-start)/1000000);
            
            start = System.nanoTime();
            result = client.noStreamTest(1000,10000,0);
            System.out.println("NO:"+(System.nanoTime()-start)/1000000);
        }
    }
    
    @Test
    public void streamTestLoop() throws Exception {
        System.out.println("streamTest");

        long start = System.nanoTime();
        
        
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<>(4);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.streamLoopTest(10, 100, 1000, HOSTS, consumer);
                } catch (Exception ex) {
                    Logger.getLogger(StatusServiceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
        
        Thread.sleep(1000);
        for (ErrorPB error : consumer.getStreamList()) {
            System.out.println("Got Object : "+(System.nanoTime()/1000000));
        }
        System.out.println("YES:" + (System.nanoTime() - start) / 1000000);
    }
    
    @Test
    public void streamTestLoop_LOCAL() throws Exception {
        System.out.println("streamTest");

        long start = System.nanoTime();
        
        final List<String> EMPTY_HOSTS = Collections.emptyList();
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<>(100);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.streamLoopTest(20, 100, 100, EMPTY_HOSTS, consumer);
                } catch (Exception ex) {
                    Logger.getLogger(StatusServiceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
        
        Thread.sleep(1000);
        for (ErrorPB error : consumer.getStreamList()) {
            System.out.println("Got Object : "+(System.nanoTime()/1000000));
        }
        System.out.println("YES:" + (System.nanoTime() - start) / 1000000);
    }
    
    
    @Test
    public void streamTest_StreamConsumer() throws Exception {
        System.out.println("streamTest");

        long start = System.nanoTime();
        
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<>(4);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.streamTest(20, 100, 100, consumer);
                } catch (Exception ex) {
                    Logger.getLogger(StatusServiceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();

        Thread.sleep(1000);
        for (ErrorPB error : consumer.getStreamList()) {
            System.out.println("Got Object : "+(System.nanoTime()/1000000));
        }
        System.out.println("YES:" + (System.nanoTime() - start) / 1000000);

    }
    
}
