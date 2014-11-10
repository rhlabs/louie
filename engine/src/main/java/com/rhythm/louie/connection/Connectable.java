/*
 * Connectable.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

/**
 *  Interface to allow manipulation of an underlying LouieConnection
 */
public interface Connectable {
    /**
     * Set the underlying connection
     * @param conn 
     */
    public void setConnection(LouieConnection conn);
    
    /**
     * Return the underlying connection
     * @return a connections
     */
    public LouieConnection getConnection();
}
