/*
 * MessageContext.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

/**
 *
 * @author cjohnson
 */
public class MessageContext {
  final private static MessageContext SINGLETON = new MessageContext();
  
  ThreadLocal<MessageInfo> message;
  
  private MessageContext() {
      message = new ThreadLocal<MessageInfo>();
  }
  
  public static MessageInfo getMessage() {
      return SINGLETON.message.get();
  }
    
  public static void setMessage(MessageInfo msg) {
      SINGLETON.message.set(msg);
  }
  
  public static void clearMessage() {
      SINGLETON.message.remove();
  }
}
