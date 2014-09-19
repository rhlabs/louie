/*
 * TestCacheDelegate.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import com.rhythm.louie.Delegate;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.jms.MessageUpdate;
import com.rhythm.louie.jms.AnnotatedMessageHandler;
import com.rhythm.louie.jms.MessageAction;
import com.rhythm.louie.jms.MessageHandler;
import com.rhythm.louie.jms.MessageOperation;

import com.rhythm.pb.DataTypeProtos.StringPB;
import com.rhythm.pb.RequestProtos.ErrorPB;

import com.rhythm.louie.service.CacheLayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eyasukoc
 */
public class TestCacheDelegate implements CacheLayer, TestService, Delegate<TestService> {
    private final String SERVICE_NAME = "test";
    private TestService delegate;
    private final MessageHandler msgHandler;
    
    public TestCacheDelegate() {
        msgHandler= new TestServiceMessageHandler();
    }
    
    @Override
    public MessageHandler getMessageHandler() {
        return msgHandler;
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public void initialize() throws Exception {}

    @Override
    public void shutdown() throws Exception {}

    @Override
    public void setDelegate(TestService delegate) {
        this.delegate = delegate;
    }

    @Override
    public TestService getDelegate() {
        return delegate;
    }

    @Override
    public String echoTest(String value, Integer sleep) throws Exception {
        return delegate.echoTest(value, sleep);
    }

    @Override
    public String loopTest(List<String> hosts) throws Exception {
        return delegate.loopTest(hosts);
    }

    @Override
    public List<ErrorPB> streamTest(Integer numResults, Integer resultSize, Integer sleep) throws Exception {
        return delegate.streamTest(numResults, resultSize, sleep);
    }

    @Override
    public List<ErrorPB> noStreamTest(Integer numResults, Integer resultSize, Integer sleep) throws Exception {
        return delegate.noStreamTest(numResults, resultSize, sleep);
    }

    @Override
    public List<ErrorPB> streamLoopTest(Integer numResults, Integer resultSize, Integer sleep, List<String> hosts) throws Exception {
        return delegate.streamLoopTest(numResults, resultSize, sleep, hosts);
    }

    @Override
    public String messageTest(String message) throws Exception {
        MessageUpdate.getInstance().sendUpdate(SERVICE_NAME, MessageAction.UPDATE, StringPB.newBuilder().setValue(message).build());
        return "Test Service CacheDelegate received: " + message;
    }

    @Override
    public Map<String, String> mapTest() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> setTest() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public class TestServiceMessageHandler extends AnnotatedMessageHandler {
        
        @MessageOperation
        public void updateTest(MessageAction action, StringPB received) throws Exception{
            if (action == MessageAction.UPDATE) {
                System.out.println("Test Service MessageHandler received UPDATE: " + received);
            } else if (action == MessageAction.DELETE) {
                System.out.println("Test Service MessageHandler received DELETE: " + received);
            } else if (action == MessageAction.INSERT) {
                System.out.println("Test Service MessageHandler received INSERT: " + received);
            } else {
                System.out.println("Test Service MessageHandler received UNKNOWN: " + received);
            }
        }
    }
}
