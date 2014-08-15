/*
 * HornetQConnector.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

import java.util.Map;
import javax.jms.QueueConnectionFactory;

/**
 * Connector class for HornetQ
 * @author eyasukoc
 */
public class HornetQAdapter implements JmsAdapter{

    @Override
    public QueueConnectionFactory getQueueConnectionFactory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(Map<String, String> configs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
