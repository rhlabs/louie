/*
 * TestService.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import com.rhythm.louie.process.CommandDescriptor;
import com.rhythm.louie.process.ServiceFacade;

import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.StringPB;
import com.rhythm.pb.DataTypeProtos.UIntPB;

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
}

