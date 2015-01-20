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
package com.rhythm.louie.services.info;

import java.util.List;

import com.rhythm.louie.Service;

import com.rhythm.louie.info.InfoProtos.*;

/**
 *
 * @author cjohnson
 */
@Service
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
