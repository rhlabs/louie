/*
 * JmsAdapter.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

import java.util.Map;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

/**
 *
 * @author eyasukoc
 */
public interface JmsAdapter {
    public static String HOST_KEY = "host";
    public static String PORT_KEY = "port";
    public static String FAILOVER_KEY = "failover";
    
    public void configure(Map<String,String> configs);
    
    public QueueConnectionFactory getQueueConnectionFactory();
    
    public TopicConnectionFactory getTopicConnectionFactory();
}
