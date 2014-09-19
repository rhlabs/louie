/*
 * PBParams.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.pb;

import com.google.protobuf.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author cjohnson
 * Created: Aug 18, 2011 1:59:19 PM
 */
public class PBParam implements Serializable {
    public static final PBParam EMPTY = new PBParam(0);
    
    private final ArrayList<Message> arguments;
    
    private PBParam(int size) {
        arguments = new ArrayList<Message>(size);
    }
    private PBParam(List<Message> messages) {
        arguments = new ArrayList<Message>(messages);
    }
    private PBParam(Message message) {
        arguments = new ArrayList<Message>(1);
        arguments.add(message);
    }
    
    public static PBParam createParam(List<Message> messages) {
        return new PBParam(messages);
    }
    
    public static PBParam createParam(Message... messages) {
        return new PBParam(Arrays.asList(messages));
    }
    
    public static PBParam singleParam(Message message) {
        return new PBParam(message);
    }
    
    public int count() {
        return arguments.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PBParam other = (PBParam) obj;
        if (this.arguments != other.arguments && (this.arguments == null || !this.arguments.equals(other.arguments))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.arguments != null ? this.arguments.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return Joiner.on(",").join(arguments);
    }
    
    public List<Message> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
}
