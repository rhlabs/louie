/*
 * NewInterface.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.List;

import com.rhythm.pb.info.InfoProtos.ServicePB;
import com.rhythm.pb.info.InfoProtos.ServerPB;

import com.rhythm.louie.process.Service;

/**
 *
 * @author cjohnson
 */
@Service(factory=false)
public interface InfoService {
    public static final String SERVICE_NAME = "info";
    
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
    
    /**
     * Return a list of all unique locations in this server installation
     * 
     * @return
     * @throws Exception 
     */
    List<String> getServerLocations() throws Exception;
    
    /**
     * Return all Servers in this installation
     * 
     * @return
     * @throws Exception 
     */
    List<ServerPB> getServers() throws Exception;
}
