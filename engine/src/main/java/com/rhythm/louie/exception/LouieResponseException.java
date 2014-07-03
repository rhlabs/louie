/*
 * LouieResponseException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.exception;

/**
 *
 * @author eyasukoc
 */
public class LouieResponseException extends LouieException{
    public LouieResponseException (String message) {
        super(message);
    }
    
    public LouieResponseException (Exception ex) {
        super(ex);
    }
}
