/*
 * TestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContextManager;

import com.rhythm.pb.RequestProtos.ErrorPB;

import com.rhythm.util.CalcList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import com.rhythm.louie.TaskScheduler;
import com.rhythm.louie.process.DAO;
import com.rhythm.louie.stream.StreamingConsumer;

/**
 *
 * @author cjohnson
 */
@DAO
public class TestDAO implements TestClient {
    private static final int THREAD_POOL_SIZE = 20;
    public TestDAO() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("qhostTEST-threadscheduler-%d").build();
    }
 
    @Override
    public String echoTest(String value, Integer sleep) throws Exception {
        String key = RequestContextManager.getRequest().getSessionKey();
        
        Thread.sleep(sleep);
        
        if (!RequestContextManager.getRequest().getSessionKey().equals(key)) {
            throw new Exception("Context does not work!!!");
        }
        return value;
    }

    @Override
    public String loopTest(List<String> hosts) throws Exception {
        if (hosts.isEmpty()) {
            return "Done";
        }
        List<String> args = new ArrayList<String>(hosts);
        String host = args.remove(0);
        TestClient client = TestClientFactory.getClient(
                LouieConnectionFactory.getConnection(host));
        return client.loopTest(args);
    }

    @Override
    public List<ErrorPB> streamTest(final Integer numResults, final  Integer resultSize, final Integer sleep) throws Exception {
        List<String> values = new ArrayList<String>(numResults);
        for (int i = 0; i<numResults;i++) {
            values.add("");
        }
        
        CalcList<String,ErrorPB> list = new CalcList<String,ErrorPB>(new Function<String, ErrorPB>() {
            @Override
            public ErrorPB apply(String input) {
                if (sleep>0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (Exception e) {}
                }
                
                return ErrorPB.newBuilder().setDescription(Strings.repeat("a", resultSize)).build();
            }
        },values);
        
        return list;
    }
    
    @Override
    public List<ErrorPB> noStreamTest(Integer numResults, Integer resultSize, Integer sleep) throws Exception {
        return streamTest(numResults,resultSize,sleep);
    }

    @Override
    public List<ErrorPB> streamLoopTest(final Integer numResults, final Integer resultSize, 
            final Integer sleep, final List<String> hosts) throws Exception {
        if (hosts.isEmpty()) {
            return streamTest(numResults,resultSize,sleep);
        }
        final List<String> args = new ArrayList<String>(hosts);
        final String host = args.remove(0);
        
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<ErrorPB>(100);
        
        TaskScheduler.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TestServiceClient client = TestClientFactory.getClient(
                            LouieConnectionFactory.getConnection(host));
                    client.streamLoopTest(numResults, resultSize, sleep, args, consumer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return consumer.getStreamList();
    }
    

//    @Override
//    public List<TestServicePB> streamTest(Integer resultSize, Integer sleepTime) throws Exception {
//        FutureList<TestServicePB> results = new FutureList<TestServicePB>();
//        
//        SleepThread s = new SleepThread(resultSize, sleepTime);
//        
//        for(int i = 0; i < resultSize; i++) {
//            SettableFuture<TestServicePB> sf = SettableFuture.create();
//            results.addFuture(sf);
//            s.addFuture(sf);
//        }
//        scheduler.submit(s);
//        return results;
//    }
    
//    private class SleepThread implements Callable<TestServicePB>{
//
//        private int size;
//        private int sleep;
//        private List<SettableFuture<TestServicePB>> res = new ArrayList<SettableFuture<TestServicePB>>();
//        
//        SleepThread(int size, int sleep) {
//            this.size = size;
//            this.sleep = sleep;
//        }
//        
//        public void addFuture(SettableFuture<TestServicePB> sf) {
//            res.add(sf);
//        }
//
//        @Override
//        public TestServicePB call() throws Exception {
//            for (int i = 0; i<size; i++) {
//                Thread.sleep(sleep);
//                res.get(i).set(TestServicePB.newBuilder().setValue("String response # " + i + " @ "+ System.nanoTime() / 1000000).build());
//            }   
//            return null;
//        }
//    }

    @Override
    public String messageTest(String message) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
