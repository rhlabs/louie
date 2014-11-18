/*
 * UnauthorizedSessionException.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.auth;

import com.rhythm.louie.exception.LouieException;

import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class UnauthorizedSessionException extends LouieException {
    public UnauthorizedSessionException(SessionKey key) {
        super("Invalid Session Key: "+(key==null?null:key.getKey()));
    }
}
