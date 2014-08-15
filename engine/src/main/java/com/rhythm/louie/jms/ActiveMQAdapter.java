/*
 * ActiveMQConnector.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

import java.util.Map;
import javax.jms.QueueConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Connector class for ActiveMQ
 * @author eyasukoc
 */
public class ActiveMQAdapter implements JmsAdapter{

    private ActiveMQConnectionFactory amqcf;
    private String host,port;
    private boolean failover;
    
    public ActiveMQAdapter() {}
   
    @Override
    public QueueConnectionFactory getQueueConnectionFactory() {
        if (amqcf == null) {    
            StringBuilder url = new StringBuilder();
            if (failover) {
                url.append("failover:(tcp://");
                url.append(host).append(":").append(port).append("?trace=false)");
            } else {
                url.append("tcp://");
                url.append(host).append(":").append(port);
            }
            amqcf = new ActiveMQConnectionFactory(url.toString());
        }
        return amqcf;
    }

    @Override
    public void configure(Map<String, String> configs) {
        this.host = configs.get(HOST_KEY);
        this.port = configs.get(PORT_KEY);
        this.failover = Boolean.valueOf(configs.get(FAILOVER_KEY));
    }
}
