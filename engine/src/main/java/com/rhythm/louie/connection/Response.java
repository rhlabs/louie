/*
 * Response.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.Message;
import com.rhythm.pb.RequestProtos.ResponsePB;
import java.util.List;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:36:16 PM
 */
 public interface Response<T extends Message> {
    public ResponsePB getResponse();
    public List<T> getResults();
    public T getSingleResult();
}
