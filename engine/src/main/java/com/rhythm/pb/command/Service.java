/*
 * NewInterface.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.pb.command;

import java.util.Collection;

import com.rhythm.louie.jms.MessageHandler;

import com.rhythm.pb.data.Request;
import com.rhythm.pb.data.Result;

/**
 *
 * @author chris
 */
public interface Service {
    static final String PK_CHECK_ERROR = "Invalid request Parameters!";

    Result executeCommand(Request req) throws Exception;

    Collection<PBCommand<?, ?>> getCommands();

    String getServiceName();
    
    void initialize() throws Exception;
    
    void shutdown() throws Exception;
    
    MessageHandler getMessageHandler();
    
}
