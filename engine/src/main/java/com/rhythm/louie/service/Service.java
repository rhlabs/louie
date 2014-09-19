/*
 * NewInterface.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import java.util.Collection;

import com.rhythm.louie.jms.MessageHandler;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.service.command.PBCommand;

/**
 *
 * @author chris
 */
public interface Service {
    static final String PK_CHECK_ERROR = "Invalid request Parameters!";

    Result executeCommand(RequestContext req) throws Exception;

    Collection<PBCommand<?, ?>> getCommands();

    String getServiceName();
    
    Class<?> getServiceInterface();
    
    void initialize() throws Exception;
    
    void shutdown() throws Exception;
    
    MessageHandler getMessageHandler();
    
}
