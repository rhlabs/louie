/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.topology;

import com.rhythm.louie.Server;
import com.rhythm.louie.request.ProtoRouter;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
public class Route {
    
    public static void initialize(Properties props) {
        //it may be desirable to change the param here to a real path, 
        //so that we we can create and manage a WatchService to detect file changes
        
        processRoutingConfigs(props);
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoRouter.class.getName());
    
    private static final Map<String, Server> serviceMap = new ConcurrentHashMap<String, Server>();
    
    public static void processRoutingConfigs(Properties props) {
        boolean defaultFound = false;
        for (String key : props.stringPropertyNames()) {
            if (key.equals("default")) {
                defaultFound = true;
            }
                String value = props.getProperty(key);
                String[] valueParts = value.split("\\/",2);
            try {
                serviceMap.put(key, Server.getResolvedServer(valueParts[0],valueParts[1]));
            } catch (UnknownHostException ex) {
                LOGGER.error(ex.toString());
            }
        } 
        if (!defaultFound) {
            serviceMap.put("default", Server.LOCAL);
        }
    }
    
    public static Server get(String service) {
        return serviceMap.get(service);
    }
    
    public static void shutdown() {
        //shutdown a watchservice
    }
    
}
