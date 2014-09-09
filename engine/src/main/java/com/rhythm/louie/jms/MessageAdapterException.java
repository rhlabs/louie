/*
 * MessageAdapterException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

/**
 *
 * @author eyasukoc
 */
public class MessageAdapterException extends Exception{
    public MessageAdapterException(String message) {
        super(message);
    }
    
    public MessageAdapterException(Exception ex) {
        super(ex);
    }
}
