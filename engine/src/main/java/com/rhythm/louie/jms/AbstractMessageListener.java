/*
 * AbstractMessageListener.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.Collection;

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
    private Thread checkThread;
    
    private final JmsAdapter jmsAdapter;
    
    public AbstractMessageListener() {
        jmsAdapter = MessageManager.getManager().getAdapter();
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
//                    jmsAdapter.configure(getBrokerUrl());
                    QueueConnectionFactory tcf = jmsAdapter.getQueueConnectionFactory();

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
        
        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(5000);
                        synchronized (CONNECT_LOCK) {
                            if (connected) {
                                break;
                            } else {
                                LOGGER.error("UNABLE TO CONNECT to message server");
                            }
                        }
                    } catch (InterruptedException ex) {
                        LOGGER.error("Check thread interrupted", ex);
                        break;
                    }
                }
            }
        });
        checkThread.start();
    }

    public void stop() {
        if (connectThread!=null && connectThread.isAlive()) {
            connectThread.interrupt();
        }
        if (checkThread!=null && checkThread.isAlive()) {
            checkThread.interrupt();
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
