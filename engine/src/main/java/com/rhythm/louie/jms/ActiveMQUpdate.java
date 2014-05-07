/*
 * ActiveMQUpdate.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.Message;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 
 * @author sfong
 */
public class ActiveMQUpdate {
    private ActiveMQConnectionFactory louieTCF;
    private ExecutorService queue;
    
    private ActiveMQUpdate() {
        louieTCF = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?trace=false)");
    }
    
    public static ActiveMQUpdate getInstance() {
        return ActiveMQUpdateHolder.INSTANCE;
    }

    private static class ActiveMQUpdateHolder {
        private static final ActiveMQUpdate INSTANCE = new ActiveMQUpdate();
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

    public void sendUpdate(String service, MessageAction action, Collection<Message> pbList) throws Exception {
        if (queue==null) {
            throw new Exception("Executor Not initialized!");
        }
        if (pbList==null) {
            return;
        }
        MessageTask task = new MessageTask(service, louieTCF, action, pbList);
        queue.submit(task);
    }
    
    public void sendUpdate(String service, MessageAction action, Message pb) throws Exception {
        if (pb==null) {
            return;
        }
        sendUpdate(service, action, Collections.singletonList(pb));
    }
}