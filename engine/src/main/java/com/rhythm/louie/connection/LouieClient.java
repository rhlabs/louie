/*
 * LouieClient.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.Message;

import com.rhythm.pb.PBParam;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:40:45 PM
 */
public class LouieClient implements Client {
    private final LouieConnection louieConn;
    
    public LouieClient(LouieConnection connection) {
        this.louieConn = connection;
    }
    
    @Override
    public <T extends Message> Response<T> doRequest(String system,String cmd, T template) throws Exception {
        return doRequest(system,cmd,PBParam.EMPTY,template);
    }

    @Override
    public <T extends Message> Response<T> doRequest(String system,String cmd,Message message,T template) throws Exception {
        PBParam param;
        if (message == null) {
            param = PBParam.EMPTY;
        } else {
            param = PBParam.singleParam(message);
        }
        return doRequest(system,cmd,param,template);
    }

    @Override
    public <T extends Message> Response<T> doRequest(String system,String cmd,PBParam param,T template) throws Exception {
        return louieConn.request(system,cmd,param,template);
    }
}
