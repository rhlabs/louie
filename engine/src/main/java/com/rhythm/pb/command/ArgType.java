/*
 * ArgType.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.pb.command;

import com.google.protobuf.Descriptors.Descriptor;

import com.rhythm.pb.PBType;
import java.io.Serializable;

/**
 * @author cjohnson
 * Created: Sep 9, 2011 2:07:20 PM
 */
public class ArgType implements Serializable, Comparable<ArgType> {
    private static final long serialVersionUID = 1L;
    
    private final String type;
    private final String name;
    
    public ArgType(String type,String name) {
        this.type = type;
        this.name = name;
    }

    public ArgType(String type) {
        this(type,"");
    }
    
    public ArgType(Descriptor type,String name) {
        this(type.getFullName(),name);
    }
    
    public ArgType(Descriptor type) {
        this(type.getFullName());
    }
    
    public ArgType(PBType type) {
        this(type.getName());
    }

    public ArgType(PBType type,String name) {
        this(type.getName(),name);
    }
    
    
    public String getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArgType other = (ArgType) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return type;
    }

    @Override
    public int compareTo(ArgType other) {
        return this.type.compareTo(other.type);
    }
}
