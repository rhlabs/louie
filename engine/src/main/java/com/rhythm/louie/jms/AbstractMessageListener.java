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

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.*;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public abstract class AbstractMessageListener implements MessageListener, ExceptionListener {
    private final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageListener.class);
            
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    private boolean connected = false;
    private boolean connecting = false;
    private final Object CONNECT_LOCK = new Object();
    
    private Thread connectThread;
    private final ScheduledExecutorService checkService;
    
    public AbstractMessageListener() {
        checkService = Executors.newSingleThreadScheduledExecutor();
    }

    public abstract Destination getDestination(Session session) throws Exception;

    protected void connect() {
        connect("");
    }
    
    protected void connect(Collection<MessageType> messageTypes) {
        connect(Joiner.on(" OR ").join(messageTypes));
    }
    
    protected void connect(final String messageSelector) {
        synchronized (CONNECT_LOCK) {
            if (connected || connecting) {
                return;
            }
            connecting = true;
        }
        
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JmsAdapter jms = MessageManager.getAdapter();
                    if (jms == null) {
                        LoggerFactory.getLogger(this.getClass()).error("Unable to connect to Message Server: No adapter Configured!");
                        return;
                    }
                    
                    QueueConnectionFactory tcf = jms.getQueueConnectionFactory();

                    connection = tcf.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Destination dest = getDestination(session);
                    if (messageSelector != null && !messageSelector.equals("")) {
                        String fixedMessageSelector = messageSelector + " OR type = 'rh.pb.jms.MessageBPB'";
                        consumer = session.createConsumer(dest, fixedMessageSelector, false);
                    } else {
                        consumer = session.createConsumer(dest);
                    }
                    consumer.setMessageListener(AbstractMessageListener.this);
                    connection.setExceptionListener(AbstractMessageListener.this);
                    connection.start();
                    synchronized (CONNECT_LOCK) {
                        connecting = false;
                        connected = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error listening to messages", e);
                }
            }
        });
        connectThread.start();
        
        checkService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (CONNECT_LOCK) {
                    if (!connected) {
                        LOGGER.error("UNABLE TO CONNECT to message server");
                    } else {
                        checkService.shutdown();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        if (connectThread!=null && connectThread.isAlive()) {
            connectThread.interrupt();
        }
        if (checkService!=null) {
            checkService.shutdownNow();
        }
        
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                LOGGER.error("Error closing consumer", e);
            }
            consumer = null;
        }

        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                LOGGER.error("Error closing session", e);
            }
            session = null;
        }

        if (connection != null) {
            try {
                connection.stop();
                connection.setExceptionListener(null);
                connection.close();
            } catch (Exception e) {
                LOGGER.error("Error closing connection", e);
            }
            connection = null;
        }
    }

    @Override
    public void onException(JMSException jmse) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
