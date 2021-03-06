/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.services.status;

import java.util.*;

import com.rhythm.louie.*;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.RequestPB;

/**
 *
 * @author cjohnson
 */

@Service
public interface StatusService {
    
    /**
     * ThreadMXBean
     * @return Returns a list of thread IDs
     * @throws Exception 
     */
    List<Long> findDeadlockedThreads() throws Exception;
    
    /**
     * ThreadMXBean
     * @return Returns a list of thread IDs
     * @throws Exception 
     */
    List<Long> findMonitorDeadlockedThreads() throws Exception;
    
    /**
     * ThreadMXBean exploration
     * @param threadId the threadId
     * @param maxDepth the number of StackTraceElements to lookup
     * @return a String representation of a stacktrace
     * @throws Exception 
     */
    @Admin
    String dumpStack(Long threadId, Integer maxDepth) throws Exception;
    
    /**
     * Live Request Introspection
     * @return
     * @throws Exception 
     */
    @Admin
    List<RequestPB> getActiveRequests() throws Exception;
    
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
 
    @Internal
    public Map<String,String> mapTest() throws Exception;

    @Internal
    public Set<String> setTest() throws Exception;
    
    /**
     * Does nothing just testing display
     * 
     * @param blah
     * @return
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    String deprecatedTest(String blah) throws Exception;
    
    /**
     * Does nothing just testing display
     * 
     * @param blah
     * @return
     * @throws Exception 
     */
    @Updating
    String updatingTest(String blah) throws Exception;
    
}

