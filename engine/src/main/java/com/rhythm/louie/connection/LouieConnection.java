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
package com.rhythm.louie.connection;

import com.google.protobuf.Message;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionKey;

import java.net.URLConnection;

/**
 *
 * @author cjohnson
 */
public interface LouieConnection {

    IdentityPB getIdentity();

    SessionKey getSessionKey() throws Exception;

    <T extends Message> Response request(Request<T> req) throws Exception;
    
    void setMaxTimeout(int seconds);
    
    int getMaxTimeout();
    
    void setRetryEnable(boolean enable);
    
    boolean getRetryEnable();
    
    void enableAuthPort(boolean enable);
    
    void setGateway(String gateway);
    
    String getGateway();
    
    void setPort(int port);
    
    URLConnection getJsonForwardingConnection() throws Exception;

    URLConnection getForwardingConnection() throws Exception;
    
}
