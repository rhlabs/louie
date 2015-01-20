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

   
