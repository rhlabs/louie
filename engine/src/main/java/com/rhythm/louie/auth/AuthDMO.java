/*
 * AuthDMO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import com.rhythm.louie.Server;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.cache.EhCache;
import com.rhythm.louie.connection.DefaultLouieConnection;
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

/**
 *
 * @author cjohnson
 */
public class AuthDMO implements AuthClient {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthDMO.class);
    private final CacheManager cacheManager;
    
    private final EhCache<String,SessionStat> SESSION_STATS;
    
    private Boolean centralAuthProvider = null;
    private AuthClient authClient = null;
    
    private AuthDMO() {
        cacheManager = CacheManager.createCacheManager("auth");
        SESSION_STATS = cacheManager.createEHCache("Auth_SessionStats");
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
            if (centralAuthProvider) {
                LOGGER.debug("This host determined to be a central auth");
            } else {
                LOGGER.debug("Central auth determined to be located elsewhere");
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
            LOGGER.trace("getSessionStat found a null key locally, gonna go look to central auth");
            SessionBPB statBPB = getAuthClient().getSession(sessionKey);                                                                            
            if (statBPB.hasKey()) { //check that this is not an empty instance                  
                LOGGER.trace("getSession from central auth found something!");
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
            LOGGER.warn("YOU HIT THE LOTTERY!!!!!");                                                                                         
            key = nextSessionId();                                                                                                                  
        }                                                                                                                                           
                                                                                                                                                    
        SessionKey skey = SessionKey.newBuilder().setKey(key).build();                                                                              
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
                LOGGER.trace("getSession found a non null stat from Central!");
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
            List<SessionBPB> found = new ArrayList<SessionBPB>();
            for (Object key : SESSION_STATS.getCache().getKeys()) {
                LOGGER.debug("KEY: {}", key);
                
                if (key.toString().startsWith(sessionKey.getKey())) {
                    LOGGER.debug("Starts with!");
                    stat = SESSION_STATS.get(key.toString());
                    if (stat!=null) {
                        LOGGER.debug("Found!");
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
        LOGGER.trace("isValidSession: {} : {}", sessionKey.getKey(), (stat!=null));
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
