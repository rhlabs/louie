/*
 * TestServiceTest.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import java.util.List;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.connection.LouieServiceClient;
import com.rhythm.louie.connection.Response;
import com.rhythm.louie.server.LouieClientTest;
import com.rhythm.louie.stream.Consumer;

import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.StringPB;
import com.rhythm.pb.PBParam;
import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.RoutePathPB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.louie.stream.StreamingConsumer;

/**
 *
 * @author cjohnson
 */
public class TestServiceTest {
    
    private static TestServiceClient client;
    public TestServiceTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
//        client = TestClientFactory.getClient(
//                LouieConnectionFactory.getConnection("vans256",
//                Identity.createJUnitIdentity()));
        client = TestClientFactory.getClient(
                LouieConnectionFactory.getLocalConnection(
                        Identity.createJUnitIdentity()));
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
                        client = TestClientFactory.getClient(
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
        
        String result = client.loopTest(Arrays.asList("louiebeta.van.rhythm.com","louiebeta.rhythm.com"));
        assertNotNull(result);
        
        System.out.println(result);
    }
    
     /**
     * Test of getService method, of class LouieClient.
     */
    @Test(expected = Exception.class)
    public void loopTest_FAIL() throws Exception {
        System.out.println("loopTest");
        
        String result = client.loopTest(Arrays.asList("louiebeta.van.rhythm.com","louiebeta.rhythm.com", "lid1577"));
        assertNotNull(result);
        
        System.out.println(result);
    }
    
    /**
     * Test of getService method, of class LouieClient.
     */
    @Test
    public void routeTest() throws Exception {
        System.out.println("loopTest");
        
        RouteTestFacade facade = new RouteTestFacade(
                LouieConnectionFactory.getLocalConnection(Identity.createJUnitIdentity()));
        
        StringListPB hosts = StringListPB.newBuilder()
                .addValues("louiebeta.van.rhythm.com")
                .addValues("louiebeta.rhythm.com")
                .build();
        
        Response<StringPB> response = facade.loopTest(hosts);
        assertNotNull(response);
        
        System.out.println(response.getResponse());
        
        assertTrue(response.getResponse().getRouteCount()==1);
        
        RoutePathPB route = response.getResponse().getRouteList().get(0);
        
        assertEquals(route.getRoute().getHostIp(), "10.4.23.77");
        assertTrue(route.getPathCount()==1);
        route = route.getPath(0);
        assertEquals(route.getRoute().getHostIp(), "10.48.5.200");
        assertTrue(route.getPathCount()==1);
        route = route.getPath(0);
        assertEquals(route.getRoute().getHostIp(), "10.4.37.48");
        assertTrue(route.getPathCount()==0);
        
        System.out.println(response.getSingleResult());
        
    }
    
    public class RouteTestFacade extends LouieServiceClient {

        public RouteTestFacade(LouieConnection connection) {
            super("test", connection);
        }

        public Response<StringPB> loopTest(StringListPB hosts) throws Exception {
            PBParam param = PBParam.createParam(hosts);
            return doRequest("loopTest", param, StringPB.getDefaultInstance());
        }
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
        
        List<String> hosts = Arrays.asList("louiebeta.rhythm.com","louiebeta.van.rhythm.com");
        
        client.streamLoopTest(10, 100, 500, hosts, new Consumer<ErrorPB>() {
            @Override
            public void consume(ErrorPB object) {
                System.out.println("Got Object : "+(System.nanoTime()/1000000));
            }
        });
        System.out.println("YES:" + (System.nanoTime() - start) / 1000000);
    }
    
    @Test
    public void streamTest_StreamConsumer() throws Exception {
        System.out.println("streamTest");

        long start = System.nanoTime();
        
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<ErrorPB>(4);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.streamTest(20, 100, 100, consumer);
                } catch (Exception ex) {
                    Logger.getLogger(TestServiceTest.class.getName()).log(Level.SEVERE, null, ex);
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
