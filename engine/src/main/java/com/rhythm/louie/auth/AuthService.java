/*
 * AuthService.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import java.util.List;
import com.rhythm.louie.process.CommandDescriptor;
import com.rhythm.louie.process.ServiceFacade;

import com.rhythm.pb.DataTypeProtos.BoolPB;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
@ServiceFacade(factory=false)
public interface AuthService {
    
    @CommandDescriptor(description = "Creates a Session Key", args={"identity"})
    public SessionKey createSession(IdentityPB identity) throws Exception;
    
    @CommandDescriptor(description = "Gets a Session by key", args = {"sessionKey"})
    SessionBPB getSession(SessionKey sessionKey) throws Exception;
    
    @CommandDescriptor(description = "Finds sessions starting with the given key.", args = {"sessionKey"})
    List<SessionBPB> findSessions(SessionKey sessionKey) throws Exception;

    @CommandDescriptor(description = "Gets an Indentity by key", args = {"sessionKey"})
    IdentityPB getSessionIdentity(SessionKey sessionKey) throws Exception;

    @CommandDescriptor(description = "Check to see if a session key is valid", args = {"sessionKey"})
    BoolPB isValidSession(SessionKey sessionKey) throws Exception;

}
