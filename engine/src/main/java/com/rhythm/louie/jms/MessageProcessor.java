/*
 * MessageProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import com.rhythm.pb.data.Data;

/**
 *
 * @author cjohnson
 */
public interface MessageProcessor {
    
    public String getType();
    
    public void execute(MessageAction action, Data data) throws Exception;

}
