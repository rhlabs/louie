/*
 * EmailService.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.email;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author sfong
 */
public class EmailService {
    private ExecutorService queue;
    
    private EmailService() {}
    
    public static EmailService getInstance() {
        return EmailServiceHolder.INSTANCE;
    }

    private static class EmailServiceHolder {
        private static final EmailService INSTANCE = new EmailService();
    }
    
    public synchronized void initialize() throws Exception {
        if (queue==null) {
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("louie-email-%d").build();
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
    
    public void sendMail(EmailTask task) throws Exception {
        if (queue==null) {
            throw new Exception("Executor Not initialized!");
        }
        queue.submit(task);
    }
    
    public void sendMail(String from, String to, String subject, String body) throws Exception {
        EmailTask task = new EmailTask(from, to, subject, body);
        sendMail(task);
    }
}
