/*
 * Client.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.Message;

import com.rhythm.pb.PBParam;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:35:55 PM
 */
public interface Client {
    public <T extends Message> Response<T> doRequest(String system,String cmd, T template) throws Exception;
    public <T extends Message> Response<T> doRequest(String system,String cmd,Message message,T template) throws Exception;
    public <T extends Message> Response<T> doRequest(String system,String cmd,PBParam param,T template) throws Exception;
    public <T extends Message> Response<T> doRequest(RequestParams<T> req) throws Exception;
}
