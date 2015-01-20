/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.services.auth;

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
