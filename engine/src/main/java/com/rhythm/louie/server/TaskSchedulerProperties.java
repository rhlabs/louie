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
package com.rhythm.louie.server;

import org.jdom2.Element;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
public class TaskSchedulerProperties {
    
    private static final String POOL_SIZE = "thread_pool_size";
    private static final String JNDI = "jndi";

    
    private static int threadPoolSize = 8;
    private static String jndiKey = null;
    
    public static String getJndiKey() {
        return jndiKey;
    }
    
    protected static void setThreadPoolSize(int size) {
        threadPoolSize = size;
    }
    
    public static int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    public static void processProperties(Element scheduler) {
        for (Element child : scheduler.getChildren()) {
            String elemName = child.getName().toLowerCase();
            String elemValue = child.getTextTrim();
            switch (elemName) {
                case POOL_SIZE: threadPoolSize = Integer.parseInt(elemValue);
                    break;
                case JNDI: jndiKey = elemValue;
                    break;
                default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected scheduler property  {}:{}",elemName,elemValue);
                    break;
            }
        }
    }
    
}
