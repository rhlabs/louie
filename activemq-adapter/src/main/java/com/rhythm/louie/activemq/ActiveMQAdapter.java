/*
 * ActiveMQConnector.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.activemq;

import com.rhythm.louie.jms.JmsAdapter;

import java.util.Map;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Connector class for ActiveMQ
 * @author eyasukoc
 */
public class ActiveMQAdapter implements JmsAdapter {

    private ActiveMQConnectionFactory connectionFactory;
    private String host,port;
    private boolean failover;
    
    public ActiveMQAdapter() {}
   
    @Override
    public QueueConnectionFactory getQueueConnectionFactory() {
        return getConnectionFactory();
    }
    
    @Override
    public TopicConnectionFactory getTopicConnectionFactory() {
        return getConnectionFactory();
    }
    
    private synchronized ActiveMQConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            String url = "tcp://"+host+":"+port;
            if (failover) {
                url = "failover:("+url+"?trace=false)";
            }
            connectionFactory = new ActiveMQConnectionFactory(url);
        }
        return connectionFactory;
    }

    @Override
    public synchronized void configure(Map<String, String> configs) {
        this.host = configs.get(HOST_KEY);
        this.port = configs.get(PORT_KEY);
        this.failover = Boolean.valueOf(configs.get(FAILOVER_KEY));
        
        connectionFactory = null;
    }
}

   
