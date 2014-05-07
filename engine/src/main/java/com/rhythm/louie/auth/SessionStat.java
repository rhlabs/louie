/*
 * SessionStat.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import com.rhythm.pb.RequestProtos.IdentityPB;
import java.io.Serializable;
import org.joda.time.DateTime;

import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;
import com.rhythm.pb.RequestProtos.SessionStatsPB;

/**
 *
 * @author cjohnson
 */
public class SessionStat implements Serializable {
    private final IdentityPB identity;
    private final SessionKey key;
    
    private final DateTime create = new DateTime();
    private DateTime modified = new DateTime();
    private int count;
    
    public SessionStat(SessionKey key,IdentityPB identity) {
        this.key = key;
        this.identity = identity;
        count=0;
    }

    public void update() {
        count++;
        modified = new DateTime();
    }
    
    public DateTime getCreateTime() {
        return create;
    }

    public DateTime getLastRequestTime() {
        return modified;
    }
    
    public IdentityPB getIdentity() {
        return identity;
    }
    
    public SessionKey getKey() {
        return key;
    }
    
    public SessionBPB toPB() {
        return SessionBPB.newBuilder()
                .setKey(key)
                .setIdentity(identity)
                .setStats(SessionStatsPB.newBuilder()
                    .setRequestCount(count)
                    .setCreateTime(create.getMillis())
                    .setLastRequestTime(modified.getMillis()))
                .build();
    }
}
