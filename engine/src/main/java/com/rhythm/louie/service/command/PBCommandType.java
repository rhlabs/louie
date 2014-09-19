/*
 * PBCommandType.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.command;

import java.io.Serializable;

/**
 * @author cjohnson
 * Created: Oct 24, 2011 4:52:56 PM
 */
public class PBCommandType implements Serializable, Comparable<PBCommandType> {
    private static final long serialVersionUID = 1L;
    
    private final String command;
    private final PBParamType params;
    
    private PBCommandType(String command,PBParamType params) {
        this.command=command;
        this.params=params;
    }

    static public PBCommandType valueOf(String command,PBParamType params) {
        return new PBCommandType(command,params);
    }
    
    @Override
    public String toString() {
        return command+"("+params+")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PBCommandType other = (PBCommandType) obj;
        if ((this.command == null) ? (other.command != null) : !this.command.equals(other.command)) {
            return false;
        }
        if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.command != null ? this.command.hashCode() : 0);
        hash = 59 * hash + (this.params != null ? this.params.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(PBCommandType other) {
        int cmp = this.command.compareTo(other.command);
        if (cmp == 0) {
            cmp = this.params.compareTo(other.params);
        }
        return cmp;
    }
}
