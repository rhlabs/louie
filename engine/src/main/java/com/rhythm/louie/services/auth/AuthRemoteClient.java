/*
 * AuthRemoteClient.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.auth;

import com.rhythm.louie.connection.LouieConnection;

/**
 *
 * @author cjohnson
 */
public class AuthRemoteClient extends AuthServiceClient {
    private final LouieConnection connection;
    public AuthRemoteClient(LouieConnection connection) {
        super(connection);
        this.connection=connection;
    }
    
    public LouieConnection getConnection() {
        return connection;
    }
}
