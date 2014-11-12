/*
 * ActiveMQUpdate.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.LoggerFactory;

/**
 * 
 * @author sfong
 */
public class MessageUpdate {
    private ExecutorService queue;
    
    private MessageUpdate() {}
    
    public static MessageUpdate getInstance() {
        return MessageUpdateHolder.INSTANCE;
    }

    private static class MessageUpdateHolder {
        private static final MessageUpdate INSTANCE = new MessageUpdate();
    }
    
    public synchronized void initialize() throws Exception {
        if (queue==null) {
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("louie-jms-%d").build();
            queue = Executors.newSingleThreadExecutor(threadFactory);
        } else if (queue.isShutdown()) {
            throw new Exception("Cannot initialize!  Already shutdown!");
        }
    }
    
    public synchronized void shutdown() {
        if (queue!=null) {
            queue.shutdown();
        }
    }

    // TODO all these permutations are terrible. Should convert this to a builder pattern
    public void sendUpdate(String service, MessageAction action, Collection<Message> pbList) throws Exception {
        this.sendUpdate(service, action, pbList, null);
    }

    public void sendUpdate(String service, MessageAction action, Collection<Message> pbList, Map<String,String> headers) throws Exception {
        if (queue==null) {
            throw new Exception("Executor Not initialized!");
        }
        if (pbList==null) {
            return;
        }
        JmsAdapter jms = MessageManager.getAdapter();
        if (jms!=null) {
            MessageTask task = new MessageTask(service, jms, action, pbList, headers);
            queue.submit(task);
        } else {
            LoggerFactory.getLogger(this.getClass()).error("Unable to send "+service+" message: No adapter Configured!");
        }
    }
    
    public void sendUpdate(String service, MessageAction action, Message pb, Map<String,String> headers) throws Exception {
        if (pb==null) {
            return;
        }
        sendUpdate(service, action, Collections.singletonList(pb), headers);
    }
    
    public void sendUpdate(String service, MessageAction action, Message pb) throws Exception {
        if (pb==null) {
            return;
        }
        sendUpdate(service, action, Collections.singletonList(pb), null);
    }
}