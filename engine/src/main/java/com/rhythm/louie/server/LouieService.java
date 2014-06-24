/*
 * NewInterface.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.List;

import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.StringPB;
import com.rhythm.pb.louie.LouieProtos.ServicePB;

import com.rhythm.louie.process.CommandDescriptor;
import com.rhythm.louie.process.Disabled;
import com.rhythm.louie.process.ServiceFacade;

import com.rhythm.pb.DataTypeProtos.UIntPB;

/**
 *
 * @author cjohnson
 */
@ServiceFacade(factory=false)
public interface LouieService {

    @CommandDescriptor(description = "Returns the names of all services")
    StringListPB getAllServiceNames() throws Exception;

    @CommandDescriptor(description = "Returns info for all services")
    List<ServicePB> getAllServices() throws Exception;

    @CommandDescriptor(description = "Returns info for a single service", args = {"name"})
    ServicePB getService(StringPB name) throws Exception;
    
}
