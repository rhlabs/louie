/*
 * Response.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import java.util.List;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.RoutePathPB;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:36:16 PM
 */
 public class Response {
    private final ResponsePB response;
    
    public Response(ResponsePB response) {
        this.response = response;
    }
    
    public List<RoutePathPB> getRouteList() {
        return response.getRouteList();
    }
    
    public ErrorPB getError() {
        return response.getError();
    }
    
    public int getResultCount() {
        return response.getCount();
    }
     
}
