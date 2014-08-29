/*
 * TestService.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhythm.louie.process.Internal;
import com.rhythm.louie.process.Service;
import com.rhythm.louie.process.Streaming;

import com.rhythm.pb.RequestProtos.ErrorPB;

/**
 *
 * @author cjohnson
 */

@Service
public interface TestService {
    /**
     * Echoes the value back after sleeping
     * 
     * @param value
     * @param sleep
     * @return
     * @throws Exception 
     */
    String echoTest(String value, Integer sleep) throws Exception;
    
    /**
     * Calls this method on the first host in the list, passing the rest of the hosts
     * 
     * @param hosts
     * @return
     * @throws Exception 
     */
    String loopTest(List<String> hosts) throws Exception;
    
    /**
     * Echoes back the values listed, sleeping in between.
     * Client should be able to start processing immediately
     *
     * @param numResults
     * @param resultSize
     * @param sleep
     * @return
     * @throws Exception 
     */
    @Streaming
    public List<ErrorPB> streamTest(Integer numResults, Integer resultSize, Integer sleep) throws Exception;
    
    /**
     * Echoes back the values listed, sleeping in between.
     * Client should be able to start processing immediately
     * 
     * @param numResults
     * @param resultSize
     * @param sleep
     * @return
     * @throws Exception 
     */
    public List<ErrorPB> noStreamTest(Integer numResults, Integer resultSize, Integer sleep) throws Exception;
    
    /**
     * Echoes back the values listed, sleeping in between. 
     * Client should be able to start processing immediately
     * 
     * @param numResults
     * @param resultSize
     * @param sleep
     * @param hosts
     * @return
     * @throws Exception 
     */
    @Streaming
    public List<ErrorPB> streamLoopTest(Integer numResults, Integer resultSize, Integer sleep, List<String> hosts) throws Exception;
 
    /**
     * Generates a message using the configured JMS adapter,
     * and the same delegate should receive that message
     * 
     * @param message
     * @return
     * @throws Exception 
     */
    public String messageTest(String message) throws Exception;
    
    
    @Internal
    public Map<String,String> mapTest() throws Exception;

    @Internal
    public Set<String> setTest() throws Exception;
    
}

