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
