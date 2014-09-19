/*
 * ProtoProcess.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.request;

import com.rhythm.louie.request.data.Result;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author eyasukoc
 */
public interface ProtoProcess {
    
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws Exception;

}
