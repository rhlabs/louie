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

import com.google.common.base.Function;
import com.google.common.base.Strings;

import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContextManager;

import com.rhythm.pb.RequestProtos.ErrorPB;

import com.rhythm.louie.util.CalcList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public StatusDAO() {}
 
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
                    } catch (Exception e) {
                        LoggerFactory.getLogger(StatusDAO.class)
                                .error("Error sleeping in streamTest", e);
                    }
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
