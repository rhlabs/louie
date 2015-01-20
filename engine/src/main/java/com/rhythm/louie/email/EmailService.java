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
