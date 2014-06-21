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
    
    public void setMaxTimeout(int seconds);
    
    public int getMaxTimeout();
    
    public void setRetryEnable(boolean enable);
    
    public boolean getRetryEnable();
    
    public void setGateway(String gateway);
    
    public void setPort(int port);
    
    public URLConnection getJsonForwardingConnection() throws Exception;

    public URLConnection getForwardingConnection() throws Exception;
    
}
