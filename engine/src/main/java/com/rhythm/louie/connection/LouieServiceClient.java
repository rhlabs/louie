/*
 * LouieServiceClient.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.rhythm.pb.PBParam;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:24:43 PM
 */
public class LouieServiceClient {
    
    private final String SYSTEM;
    private final LouieClient client;
    
    public LouieServiceClient(String system,LouieConnection connection) {
        this.SYSTEM = system;
        client = new LouieClient(connection);
    }
    
    public String getSystem() {
        return SYSTEM;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    protected <T extends Message> Response<T> doRequest(String command, GeneratedMessage message, T template) throws Exception {
        return client.doRequest(SYSTEM, command, message, template);
    }
    
    protected <T extends Message> Response<T> doRequest(String command, PBParam param, T template) throws Exception {
        return client.doRequest(SYSTEM, command, param, template);
    }

    protected <T extends Message> Response<T> doRequest(String command, T template) throws Exception {
        return client.doRequest(SYSTEM, command, template);
    }
}
