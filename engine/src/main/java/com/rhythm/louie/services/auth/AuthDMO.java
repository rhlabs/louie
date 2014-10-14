/*
 * AuthDMO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.auth;

import com.google.common.cache.CacheBuilderSpec;

import com.rhythm.louie.server.Server;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.cache.GuavaCache;
import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.rhythm.louie.request.LogVars;

/**
 *
 * @author cjohnson
 */
public class AuthDMO implements AuthService {
    private final Logger LOGGER  = LoggerFactory.getLogger("louie.auth");
    
    private final CacheManager cacheManager;
    
    private final GuavaCache<String,SessionStat> SESSION_STATS;
    
    private Boolean centralAuthProvider = null;
    private AuthClient authClient = null;
    
    private AuthDMO() {
        cacheManager = CacheManager.createCacheManager("auth");
        CacheBuilderSpec spec = CacheBuilderSpec.parse("expireAfterAccess=4h,maximumSize=1000000"); //4 hours of no calls it expires. max capacity is 1M sessions
        SESSION_STATS = cacheManager.guavaCache("Auth_SessionStats", spec);
    }
    
    /**
     * Centralized Auth control. If a host was specified to be central auth in 
     * the server config file, we will check it against the LOCAL server. Else we 
     * function as though we are the central auth, to accommodate the basic/original behavior
     * @return 
     */
    private boolean isCentralAuth() {                                                           
        if (centralAuthProvider == null) {                 
            if (Server.getCentralAuth() != null) {
                centralAuthProvider = Server.LOCAL.equals(Server.getCentralAuth());
            } else { // there is no configured central auth, revert to basic behavior
                centralAuthProvider = true;
            }
        }
        return centralAuthProvider;                                                                                                                 
    } 
    
    public static AuthDMO getInstance() {
        return AuthDMOHolder.INSTANCE;
    }
    
    private static class AuthDMOHolder {
        private static final AuthDMO INSTANCE = new AuthDMO();
    }
    
    private AuthClient getAuthClient() {                                          
        if (authClient == null) {                                                 
            LouieConnection authConnection = LouieConnectionFactory.getConnectionForServer(Server.getCentralAuth(), 
                    Identity.createIdentity("LoUIE", "LoUIE-"+Server.LOCAL.getHostName()+"/"+Server.LOCAL.getGateway()));
            authClient = new AuthServiceClient(authConnection);                 
        }                                                                                       
        return authClient;                                                                      
    }
    
    private static String nextSessionId() {
        UUID uuid = UUID.randomUUID();
        String hex = Long.toHexString(uuid.getMostSignificantBits())+
                Long.toHexString(uuid.getLeastSignificantBits());
        return hex;
    }
    
    private SessionStat addSessionStat(SessionKey key,IdentityPB identity) throws Exception {
        SessionStat stat = new SessionStat(key,identity);
        SESSION_STATS.put(key.getKey(), stat);
        return stat;
    }
    
    public IdentityPB getIdentity(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());
        if (stat!=null) {
            return stat.getIdentity();
        }
        return null;
    }
    
    public SessionStat getSessionStat(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());                                                                                  
        if (stat == null && !isCentralAuth()) {
            SessionBPB statBPB = getAuthClient().getSession(sessionKey);                                                                            
            if (statBPB.hasKey()) { //check that this is not an empty instance                  
                stat = new SessionStat(statBPB.getKey(), statBPB.getIdentity());
                SESSION_STATS.put(sessionKey.getKey(), stat);                                                                   
                return stat;                                                                                                    
            }                                                                                                                                       
        }                   
        return stat;
    }
    
    @Override
    public SessionKey createSession(IdentityPB identity) throws Exception {
        if (!isCentralAuth()) {                                                                                                                     
            SessionKey sessionKey = getAuthClient().createSession(identity);                                                                        
            addSessionStat(sessionKey, identity);                                                                                                   
            return sessionKey;                                                                                                                      
        }                                                                                                                                           
        String key = nextSessionId();                                                                                                               
        while (SESSION_STATS.get(key)!=null) {                                                                                                      
            key = nextSessionId();                                                                                                                  
        }                                                                                                                                           
                                                                                                                                                    
        SessionKey skey = SessionKey.newBuilder().setKey(key).build();   
        MDC.put(LogVars.IP, identity.getIp());
        MDC.put(LogVars.USER, identity.getUser());
        MDC.put(LogVars.SESSION, key);
        MDC.put(LogVars.LANGUAGE, identity.getLanguage());
        MDC.put(LogVars.PROGRAM, identity.getProgram());
        MDC.put(LogVars.LOCATION , identity.getLocation());
        MDC.put(LogVars.MACHINE, identity.getMachine());
        MDC.put(LogVars.PROCESSID, identity.getProcessId());
        MDC.put(LogVars.PATH, identity.getPath());
        
        LOGGER.info("");
        addSessionStat(skey, identity);                                                                                                             
        return skey;                                                                                                                                
    }
    
    @Override
    public SessionBPB getSession(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());
        if (stat!=null) {
            return stat.toPB();
        }
        if (!isCentralAuth()) {                                                                                                                     
            SessionBPB statBPB = getAuthClient().getSession(sessionKey);              
            if (statBPB.hasKey()) {
                SESSION_STATS.put(sessionKey.getKey(), new SessionStat(statBPB.getKey(), statBPB.getIdentity()));                                                                   
                return statBPB;                                                                                                                     
            }
        } 
        return SessionBPB.getDefaultInstance();
    }
    
    @Override
    public List<SessionBPB> findSessions(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());
        if (stat!=null) {
            return Collections.singletonList(stat.toPB());
        } else {
            List<SessionBPB> found = new ArrayList<>();
            for (Object key : SESSION_STATS.asMap().keySet()) {
                
                if (key.toString().startsWith(sessionKey.getKey())) {
                    stat = SESSION_STATS.get(key.toString());
                    if (stat!=null) {
                        found.add(stat.toPB());
                    }
                }
            }
            if (found.isEmpty() && !isCentralAuth()) {
                return getAuthClient().findSessions(sessionKey);
            }
            return found;
        }
    }
    
    
    @Override
    public IdentityPB getSessionIdentity(SessionKey sessionKey) throws Exception {
        IdentityPB ident = getIdentity(sessionKey);
        if (ident == null && !isCentralAuth()) {
            ident = getAuthClient().getSessionIdentity(sessionKey);
            if (ident != null) {
                addSessionStat(sessionKey, ident);
            }
        }
        return ident;
    }
    
    @Override
    public Boolean isValidSession(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());
        if (stat!=null) {
            return Boolean.TRUE;
        }
        if (!isCentralAuth()) {
            SessionBPB remoteSession = getAuthClient().getSession(sessionKey);
            SessionStat remoteStat = new SessionStat(remoteSession.getKey(), remoteSession.getIdentity());
            if (remoteStat.getLastRequestTime()!=null) { // Need to somehow check that the returned object was valid
                SESSION_STATS.put(null, stat);
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
