/*
 * AuthDMO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.cache.EhCache;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class AuthDMO implements AuthClient {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthDMO.class);
    private final CacheManager cacheManager;
    
    private final EhCache<String,SessionStat> SESSION_STATS;

    private AuthDMO() {
        cacheManager = CacheManager.createCacheManager("auth");
        
        SESSION_STATS = cacheManager.createEHCache("Auth_SessionStats");
    }
    
    public static AuthDMO getInstance() {
        return AuthDMOHolder.INSTANCE;
    }
    
    private static class AuthDMOHolder {
        private static final AuthDMO INSTANCE = new AuthDMO();
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
        return SESSION_STATS.get(sessionKey.getKey());
    }
    
    @Override
    public SessionKey createSession(IdentityPB identity) throws Exception {
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
        return null;
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
            return found;
        }
    }
    
    
    @Override
    public IdentityPB getSessionIdentity(SessionKey sessionKey) throws Exception {
        return getIdentity(sessionKey);
    }
    
    @Override
    public Boolean isValidSession(SessionKey sessionKey) throws Exception {
        SessionStat stat = SESSION_STATS.get(sessionKey.getKey());
        LOGGER.debug("isValidSession: {} : {}", sessionKey.getKey(), (stat!=null));
        if (stat!=null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
