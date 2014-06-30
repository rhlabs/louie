/*
 * NewInterface.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.Message;
import com.rhythm.pb.PBParam;
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

    <T extends Message> Response<T> request(String system, String cmd, PBParam params, T template) throws Exception;
    
    <T extends Message> void request(Request<T> req) throws Exception;
    
    void setMaxTimeout(int seconds);
    
    int getMaxTimeout();
    
    void setRetryEnable(boolean enable);
    
    boolean getRetryEnable();
    
    void setGateway(String gateway);
    
    void setPort(int port);
    
    URLConnection getJsonForwardingConnection() throws Exception;

    URLConnection getForwardingConnection() throws Exception;
    
}
