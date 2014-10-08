/*
 * AuthService.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.auth;

import java.util.List;

import com.rhythm.louie.NoFactory;
import com.rhythm.louie.Service;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
@NoFactory
@Service
public interface AuthService {
    public static final String SERVICE_NAME = "auth";
    
    /**
     * Creates a Session Key
     * 
     * @param identity
     * @return
     * @throws Exception 
     */
    public SessionKey createSession(IdentityPB identity) throws Exception;
    
    /**
     * Gets a Session by key
     * 
     * @param sessionKey
     * @return
     * @throws Exception 
     */
    SessionBPB getSession(SessionKey sessionKey) throws Exception;
    
    /**
     * Finds sessions starting with the given key
     * 
     * @param sessionKey
     * @return
     * @throws Exception 
     */
    List<SessionBPB> findSessions(SessionKey sessionKey) throws Exception;

    /**
     * Gets an Identity by key
     * 
     * @param sessionKey
     * @return
     * @throws Exception 
     */
    IdentityPB getSessionIdentity(SessionKey sessionKey) throws Exception;

    /**
     * Check to see if a session key is valid
     * 
     * @param sessionKey
     * @return
     * @throws Exception 
     */
    Boolean isValidSession(SessionKey sessionKey) throws Exception;

}
