/*
 * TestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.services.status;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContextManager;

import com.rhythm.pb.RequestProtos.ErrorPB;

import com.rhythm.louie.util.CalcList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.server.TaskScheduler;
import com.rhythm.louie.DAO;
import com.rhythm.louie.request.ProtoProcessor;
import com.rhythm.louie.server.ThreadInspector;
import com.rhythm.louie.stream.StreamingConsumer;

import com.rhythm.pb.RequestProtos.RequestPB;

/**
 *
 * @author cjohnson
 */
@DAO
public class StatusDAO implements StatusService {
    private static final int THREAD_POOL_SIZE = 20;
    public StatusDAO() {
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
        List<String> args = new ArrayList<>(hosts);
        String host = args.remove(0);
        StatusClient client = StatusClientFactory.getClient(
                LouieConnectionFactory.getConnection(host));
        return client.loopTest(args);
    }

    @Override
    public List<ErrorPB> streamTest(final Integer numResults, final  Integer resultSize, final Integer sleep) throws Exception {
        List<String> values = new ArrayList<>(numResults);
        for (int i = 0; i<numResults;i++) {
            values.add("");
        }
        
        CalcList<String,ErrorPB> list = new CalcList<>(new Function<String, ErrorPB>() {
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
        final List<String> args = new ArrayList<>(hosts);
        final String host = args.remove(0);
        
        final StreamingConsumer<ErrorPB> consumer = new StreamingConsumer<>(100);
        
        TaskScheduler.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    StatusServiceClient client = StatusClientFactory.getClient(
                            LouieConnectionFactory.getConnection(host));
                    client.streamLoopTest(numResults, resultSize, sleep, args, consumer);
                } catch (Exception ex) {
                    LoggerFactory.getLogger(StatusDAO.class).error("Error calling streamLoopTest on Remote Client",ex);
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
    public Map<String, String> mapTest() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> setTest() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    @Override
    public String deprecatedTest(String blah) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String updatingTest(String blah) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Long> findDeadlockedThreads() throws Exception {
        long[] deadlocked = ThreadInspector.INSTANCE.findDeadlockedThreads();
        List<Long> deadList = new ArrayList<>();
        if (deadlocked != null) {
            for (long l : deadlocked) {
                deadList.add(l);
            }
        }
        return deadList;
    }

    @Override
    public String dumpStack(Long threadId, Integer maxDepth) throws Exception {
        return ThreadInspector.INSTANCE.dumpStack(threadId, maxDepth);
    }

    @Override
    public List<Long> findMonitorDeadlockedThreads() throws Exception {
        long[] deadlocked = ThreadInspector.INSTANCE.findMonitorDeadlockedThreads();
        List<Long> deadList = new ArrayList<>();
        if (deadlocked != null) {
            for (long l : deadlocked) {
                deadList.add(l);
            }
        }
        return deadList;
    }

    @Override
    public List<RequestPB> getActiveRequests() throws Exception {
        return ProtoProcessor.getActiveRequests();
    }
    
}
