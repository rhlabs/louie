/*
 * AuthUtils.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class AuthUtils {
    
    public static SessionStat accessSession(SessionKey key) throws UnauthorizedSessionException,Exception {
         SessionStat session = AuthDMO.getInstance().getSessionStat(key);
         if (session == null || session.getIdentity()==null) {
             throw new UnauthorizedSessionException(key);
         }
         session.update();
         return session;
    }
    
}