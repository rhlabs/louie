/*
 * TestService.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import java.util.List;

import com.rhythm.louie.process.CommandDescriptor;
import com.rhythm.louie.process.ServiceFacade;
import com.rhythm.louie.process.Streaming;

import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.StringPB;
import com.rhythm.pb.DataTypeProtos.UIntPB;
import com.rhythm.pb.RequestProtos.ErrorPB;

/**
 *
 * @author cjohnson
 */

@ServiceFacade
public interface TestService {
    @CommandDescriptor(description = "Echoes the value back after sleeping", args = {"value","sleep"})
    StringPB echoTest(StringPB value, UIntPB sleep) throws Exception;
    
    @CommandDescriptor(description = "calls this method on the first host in the list, passing the rest of the hosts", args = {"hosts"})
    StringPB loopTest(StringListPB hosts) throws Exception;
    
    @Streaming
    @CommandDescriptor(description = "Echoes back the values listed, sleeping in between.\n"
            + "Client should be able to start processing immediately", args = {"numResults","resultSize","sleep"})
    public List<ErrorPB> streamTest(UIntPB numResults, UIntPB resultSize, UIntPB sleep) throws Exception;
    
    @CommandDescriptor(description = "Echoes back the values listed, sleeping in between.\n"
            + "Client should be able to start processing immediately", args = {"numResults","resultSize","sleep"})
    public List<ErrorPB> noStreamTest(UIntPB numResults, UIntPB resultSize, UIntPB sleep) throws Exception;
    
    @Streaming
    @CommandDescriptor(description = "Echoes back the values listed, sleeping in between.\n"
            + "Client should be able to start processing immediately", args = {"numResults","resultSize","sleep", "hosts"})
    public List<ErrorPB> streamLoopTest(UIntPB numResults, UIntPB resultSize, UIntPB sleep, StringListPB hosts) throws Exception;
 
    @CommandDescriptor(description = "Generates a message using the configured JMS adapter, and the same delegate should receive that message", args = {"message"})
    public StringPB messageTest(StringPB message) throws Exception;
    
}

