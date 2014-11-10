/*
 * LouieException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.exception;

/**
 * Base Louie Exception
 * @author eyasukoc
 */
public class LouieException extends Exception{
    public LouieException (String message) {
        super(message);
    }
    
    public LouieException (Exception ex) {
        super(ex);
    }
    
    public LouieException (String message, Exception ex) {
        super(message, ex);
    }
}
