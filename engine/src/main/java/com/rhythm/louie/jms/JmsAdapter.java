/*
 * JmsAdapter.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

import java.util.Map;
import javax.jms.QueueConnectionFactory;

/**
 *
 * @author eyasukoc
 */
public interface JmsAdapter {
    public static String HOST_KEY = "jms.host";
    public static String PORT_KEY = "jms.port";
    public static String FAILOVER_KEY = "jms.failover";
    public void configure(Map<String,String> configs);
    public QueueConnectionFactory getQueueConnectionFactory();
}
