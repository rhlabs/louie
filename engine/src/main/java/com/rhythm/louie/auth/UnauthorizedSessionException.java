/*
 * UnauthorizedSessionException.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class UnauthorizedSessionException extends Exception {
    public UnauthorizedSessionException(SessionKey key) {
        super("Invalid Session Key: "+key);
    }
}
