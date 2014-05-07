/*
 * MessageHandler.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.Collection;

import com.rhythm.pb.data.Data;

/**
 *
 * @author cjohnson
 */
public interface MessageHandler {
    public void executeMessageHandler(MessageAction action, String type, Data data) throws Exception;
    
    public Collection<MessageProcessor> getMessageProcessors();
    
}
