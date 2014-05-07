/*
 * MessageInfo.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jms;

import com.rhythm.pb.jms.JmsProtos.ContentPB;
import com.rhythm.pb.jms.JmsProtos.MessageBPB;

/**
 *
 * @author cjohnson
 */
public class MessageInfo {
    private final MessageBPB bundle;
    private final MessageAction action;
    private int contextIndex = -1;
    
    public MessageInfo(MessageBPB bundle) {
        this.bundle = bundle;
        action = MessageAction.typeFromAction(bundle.getAction());
    }
    
    public MessageBPB getBundle() {
        return bundle;
    }
    
    public MessageAction getAction() {
        return action;
    }
    
    public int getContentCount() {
        return bundle.getContentCount();
    }
    
    public int getContentIndex() {
        return contextIndex;
    }
    
    public boolean hasMoreContent() {
        return bundle.getContentCount()>(contextIndex+1);
    }
    
    public ContentPB getNextContent() throws Exception {
        return bundle.getContent(++contextIndex);
    }

    public ContentPB getCurrentContent() throws Exception {
        return bundle.getContent(contextIndex);
    }
}
