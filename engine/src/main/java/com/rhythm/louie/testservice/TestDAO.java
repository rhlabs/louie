/*
 * TestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContext;

import com.rhythm.pb.RequestProtos.ErrorPB;

import com.rhythm.util.CalcList;

/**
 *
 * @author cjohnson
 */
public class TestDAO implements TestClient {
    public TestDAO() {
    }
 
    @Override
    public String echoTest(String value, Integer sleep) throws Exception {
        String key = RequestContext.getRequest().getSessionKey();
        
        Thread.sleep(sleep);
        
        if (!RequestContext.getRequest().getSessionKey().equals(key)) {
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
    public List<ErrorPB> streamLoopTest(Integer numResults, Integer resultSize, Integer sleep, List<String> hosts) throws Exception {
        if (hosts.isEmpty()) {
            return streamTest(numResults,resultSize,sleep);
        }
        List<String> args = new ArrayList<String>(hosts);
        String host = args.remove(0);
        
        
        // TODO this does not yet stream
        
        TestClient client = TestClientFactory.getClient(
                LouieConnectionFactory.getConnection(host));
        return client.streamLoopTest(numResults,resultSize,sleep,args);
    }
}
