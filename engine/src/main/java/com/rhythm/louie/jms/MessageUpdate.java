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
    public void sendUpdate(String service, MessageAction action, Collection<? extends Message> pbList) throws Exception {
        this.sendUpdate(service, action, pbList, null);
    }

    public void sendUpdate(String service, MessageAction action, Collection<? extends Message> pbList, Map<String,String> headers) throws Exception {
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