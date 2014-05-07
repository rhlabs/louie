/*
 * ServiceFactory.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.pb.command;

/**
 *
 * @author eyasukoc
 */
public interface ServiceFactory {
    
    public Service getService() throws Exception;
    
    public String getServiceName();
    
}
