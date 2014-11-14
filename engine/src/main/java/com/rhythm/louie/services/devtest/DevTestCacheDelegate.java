/*
 * JmsTestCacheDelegate.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.services.devtest;

import com.rhythm.louie.Delegate;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.jms.MessageUpdate;
import com.rhythm.louie.jms.AnnotatedMessageHandler;
import com.rhythm.louie.jms.MessageAction;
import com.rhythm.louie.jms.MessageHandler;
import com.rhythm.louie.jms.MessageOperation;

import com.rhythm.pb.DataTypeProtos.StringPB;

import com.rhythm.louie.service.CacheLayer;

import com.rhythm.louie.CacheDelegate;
import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.email.EmailTask;

/**
 *
 * @author eyasukoc
 */
@CacheDelegate
public class DevTestCacheDelegate implements CacheLayer, DevTestService, Delegate<DevTestService> {
    private final String SERVICE_NAME = "test";
    private DevTestService delegate;
    private final MessageHandler msgHandler;
    
    public DevTestCacheDelegate() {
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
    public void setDelegate(DevTestService delegate) {
        this.delegate = delegate;
    }

    @Override
    public DevTestService getDelegate() {
        return delegate;
    }

    @Override
    public String messageTest(String message) throws Exception {
        MessageUpdate.getInstance().sendUpdate(SERVICE_NAME, MessageAction.UPDATE, StringPB.newBuilder().setValue(message).build());
        return "Test Service CacheDelegate received: " + message;
    }

    @Override
    public Boolean sendEmail(String sender, String receiver, String subject, String body) throws Exception {
        EmailTask emailTask = new EmailTask(sender, receiver, subject, body);
        EmailService.getInstance().sendMail(emailTask);
        return true;
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
