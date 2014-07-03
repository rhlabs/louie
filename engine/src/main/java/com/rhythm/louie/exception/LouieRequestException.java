/*
 * LouieRequestException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.exception;

/**
 *
 * @author eyasukoc
 */
public class LouieRequestException extends LouieException{
    public LouieRequestException (String message) {
        super(message);
    }
    
    public LouieRequestException (Exception ex) {
        super(ex);
    }
}
