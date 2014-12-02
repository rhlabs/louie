/*
 * TaskSchedulerProperties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.server;

/**
 *
 * @author eyasukoc
 */
public class TaskSchedulerProperties {
    
    private static int threadPoolSize = 8;
    
    protected static void setThreadPoolSize(int size) {
        threadPoolSize = size;
    }
    
    public static int getThreadPoolSize() {
        return threadPoolSize;
    }
    
}
