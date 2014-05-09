/*
 * LouieResponse.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.protobuf.Message;

import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.data.Data;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:30:41 PM
 */
public class LouieResponse<T extends Message> implements Response<T>{
    private ResponsePB response;
    private final List<T> results;
    private final T template;
    
    public LouieResponse(ResponsePB response,T template) {
        this.response = response;
        this.template = template;
        results = new ArrayList<T>(response.getCount());
    }
    public LouieResponse(ResponsePB response,T template,InputStream input) throws Exception {
        this(response,template);
        
        for (int d = 0; d < response.getCount(); d++) {
            addResult(Data.readPBData(input));
        }
    }

    public ResponsePB getResponse() {
        return response;
    }

    protected final void addResult(Data d) throws Exception {
        if (d!=null) {
            if (template!=null) {
                results.add(d.parse(template));
            } else {
                throw new Exception("Error Parsing Data: No Template!");
            }
        }
    }
    
    @Override
    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }
    
    @Override
    public T getSingleResult() {
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
}