/*
 * ServiceProperties.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class ServiceProperties {
    
    private static final String DEFAULT_NAME = "default";
    private static final String PROP_ENABLE = "enable";
    private static final String PROP_MAIN = "main";
    private static final String PROP_CENTRAL = "centralized";
    private static final String PROP_DAO = "dao";
    private static final String PROP_READ_ONLY = "readonly";
    private static final String PROP_CACHE = "caching";
    
    private static final Map<String,ServiceProperties> SERVICES = 
            new ConcurrentHashMap<String,ServiceProperties>();

    private static ServiceProperties DEFAULT = new ServiceProperties(DEFAULT_NAME,false,"",false,"",false,true);
    
    private String name;
    private boolean enable;
    private String main;
    private boolean centralized;
    private String dao;
    private boolean readOnly;
    private boolean caching;
    private Map<String,String> properties;
    
    public static ServiceProperties getServiceProperties(String name) {
        ServiceProperties service = SERVICES.get(name);
        if (service==null) {
            service = new ServiceProperties(name);
            SERVICES.put(name, service);
        }
        return service;
    }
    
    private ServiceProperties(String name) {
        this(name,DEFAULT.enable && !name.equals("test"),DEFAULT.main,
                DEFAULT.centralized,DEFAULT.dao,DEFAULT.readOnly,DEFAULT.caching);
    }
    
    private ServiceProperties(String name, boolean enable, String main, 
            boolean centralized, String dao, boolean readOnly, boolean caching) {
        this.name = name;
        this.enable = enable;
        this.main = main;
        this.centralized = centralized;
        this.dao = dao;
        this.readOnly = readOnly;
        this.caching = caching;
        properties = new ConcurrentHashMap<String, String>();
    }
     
    public String getName() {
        return name;
    }
    
    public String getMain() {
        return main;
    }
    
    public boolean isEnabled() {
        return enable;
    }
    
    public boolean isCentralized() {
        return centralized;
    }
    
    public String getDAO() {
        return dao;
    }
    
    public boolean isReadOnly(){
        return readOnly;
    }
    
    public boolean isCachingOn(){
        return caching;
    }
    
    public String getCustomProperty(String attribute,String def) {
        String value = properties.get(attribute);
        if (value == null) {
            return def;
        }
        return value;
    }
    
    public static void processServiceProperties(Properties props) {
        synchronized(SERVICES) {
            DEFAULT.enable = props.getProperty(DEFAULT_NAME+"."+PROP_ENABLE,"false").equals("true");
            DEFAULT.main = props.getProperty(DEFAULT_NAME+"."+PROP_MAIN,"");
            DEFAULT.centralized = props.getProperty(DEFAULT_NAME+"."+PROP_CENTRAL,"false").equals("true");
            DEFAULT.dao = props.getProperty(DEFAULT_NAME+"."+PROP_DAO,"rh");
            DEFAULT.readOnly = props.getProperty(DEFAULT_NAME+"."+PROP_READ_ONLY,"false").equals("true");
            DEFAULT.caching = props.getProperty(DEFAULT_NAME+"."+PROP_CACHE,"true").equals("true");
            
            for (String key : props.stringPropertyNames()) {
                String[] keyParts = key.split("\\.",2);
                if (keyParts.length!=2) {
                    LoggerFactory.getLogger(ServiceProperties.class)
                        .warn("Skipping key as it does not match service.attribute: {}", key);
                    continue;
                }
                
                String value = props.getProperty(key);
                String serviceName = keyParts[0];
                String attribute = keyParts[1];
            
                if (ServiceManager.isServiceReserved(serviceName) &&
                        !ServiceManager.isTestService(serviceName)) {
                    LoggerFactory.getLogger(ServiceProperties.class)
                        .warn("Ignoring property for reserved service: {}={}", key, value);
                } else if (!serviceName.equals(DEFAULT_NAME)) {
                    ServiceProperties service = getServiceProperties(serviceName);

                    if (attribute.equals(PROP_ENABLE)) {
                        service.enable = value.equals("true");
                    } else if (attribute.equals(PROP_MAIN)) {
                        service.main = value;
                    } else if (attribute.equals(PROP_CENTRAL)){
                        service.centralized = value.equals("true");
                    } else if (attribute.equals(PROP_DAO)) {
                        service.dao = value;
                    } else if (attribute.equals(PROP_READ_ONLY)) {
                        service.readOnly = value.equals("true");
                    } else if (attribute.equals(PROP_CACHE)) {
                        service.caching = value.equals("true");
                    } else {
                        service.properties.put(attribute, value);
                    }
                }
            }
        }
    }
}
