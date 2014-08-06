/*
 * NewInterface.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.List;

import com.rhythm.pb.louie.LouieProtos.ServicePB;

import com.rhythm.louie.process.ServiceFacade;

/**
 *
 * @author cjohnson
 */
@ServiceFacade(factory=false)
public interface LouieService {
    public static final String SERVICE_NAME = "louie";
    
    /**
     * Returns the names of all services
     * 
     * @return
     * @throws Exception 
     */
    List<String> getAllServiceNames() throws Exception;

    /**
     * Returns info for all services
     * 
     * @return
     * @throws Exception 
     */
    List<ServicePB> getAllServices() throws Exception;

    /**
     * Returns info for a single service
     * 
     * @param name
     * @return
     * @throws Exception 
     */
    ServicePB getService(String name) throws Exception;
    
}
