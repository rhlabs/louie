/*
 * UnauthenticatedException.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.services.auth;

import com.rhythm.louie.exception.LouieException;

/**
 *
 * @author eyasukoc
 */
public class UnauthenticatedException extends LouieException{

    public UnauthenticatedException(String message) {
        super(message);
    }
    
}
