/*
 * LouieRouteException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.exception;

/**
 *
 * @author eyasukoc
 */
public class LouieRouteException extends LouieException{

    public LouieRouteException(String message) {
        super(message);
    }
    
    public LouieRouteException(Exception ex) {
        super(ex);
    }
}
