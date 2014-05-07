/*
 * MessageAction.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

/**
 *
 * @author sfong
 */
public enum MessageAction {
    UPDATE("update"),
    DELETE("delete"),
    INSERT("insert");
    
    private final String action;
    
    MessageAction(String action) {
        this.action = action;
    }
    
    public String getAction() {
        return action;
    }
    
    public static MessageAction typeFromAction(String action) {
        for (MessageAction type : values()){
            if (type.action.equals(action)) {
                return type;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return action;
    }
}
