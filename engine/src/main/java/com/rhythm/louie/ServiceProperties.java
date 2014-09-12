/*
 * ServiceProperties.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import java.util.HashMap;
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
    private static final String PROP_READ_ONLY = "readonly";
    private static final String PROP_CACHING = "caching";
    
    private static final String PROP_DAO = "dao";
    private static final String PROP_CACHE = "cache";
    private static final String PROP_ROUTER = "router";
    private static final String PROP_JMS = "jmsadapter";
    
    private static final Map<String,ServiceProperties> SERVICES = 
            new ConcurrentHashMap<String,ServiceProperties>();

    private static final ServiceProperties DEFAULT = new ServiceProperties(DEFAULT_NAME,false,"",false,false,true);
    
    private final String name;
    private boolean enable;
    private String main;
    private boolean centralized;
    private boolean readOnly;
    private boolean caching;
    
    private String dao = null;
    private String cache = null;
    private String router = null;
    private String jmsAdapter = null;
    
    private final Map<String,String> properties;
    
    public static ServiceProperties getServiceProperties(String name) {
        ServiceProperties service = SERVICES.get(name);
        if (service==null) {
            service = new ServiceProperties(name);
            SERVICES.put(name, service);
        }
        return service;
    }
    
    public static ServiceProperties getDefaultServiceProperties() {
        return DEFAULT;
    }
    
    protected static void initReservedProperties(String name) {
        ServiceProperties props = getServiceProperties(name);
        props.enable = true;
        props.main="";
        props.centralized=false;
        props.readOnly=false;
        props.caching=true;
    }
        
    private ServiceProperties(String name, boolean enable, String main, 
            boolean centralized, boolean readOnly, boolean caching) {
        this.name = name;
        this.enable = enable;
        this.main = main;
        this.centralized = centralized;
        this.readOnly = readOnly;
        this.caching = caching;
        properties = new ConcurrentHashMap<String, String>();
    }
    
    private ServiceProperties(String name) { 
        this.name = name;
        this.enable = DEFAULT.enable;
        this.main = DEFAULT.main;
        this.centralized = DEFAULT.centralized;
        this.readOnly = DEFAULT.readOnly;
        this.caching = DEFAULT.caching;
        this.jmsAdapter = DEFAULT.jmsAdapter;
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
    
    public boolean isReadOnly(){
        return readOnly;
    }
    
    public boolean isCachingOn(){
        return caching;
    }
    
    public String getDAO() {
        return dao;
    }
    
    public String getCache() {
        return cache;
    }
    
    public String getRouter() {
        return router;
    }
    
    public String getMessageAdapter() {
        return jmsAdapter;
    }
    
    public String getCustomProperty(String attribute,String def) {
        String value = properties.get(attribute);
        if (value == null) {
            return def;
        }
        return value;
    }
    
    public int getCustomIntegerProperty(String attribute, int def) {
        String value = properties.get(attribute);
        if (value == null || value.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LoggerFactory.getLogger(ServiceProperties.class)
                    .error("Error parsing integer {}: \"{}\"", attribute, value);
            return def;
        }
    }
    
    public static void processServiceProperties(Properties props) {
        synchronized(SERVICES) {
            // Load up defaults first
            Map<String,String> nonDefaultProps = new HashMap<String,String>();
            for (String key : props.stringPropertyNames()) {
                String[] keyParts = key.split("\\.",2);
                if (keyParts.length!=2) {
                    LoggerFactory.getLogger(ServiceProperties.class)
                        .warn("Skipping key as it does not match service.attribute: {}", key);
                    continue;
                }
                
                String serviceName = keyParts[0];
                String attribute = keyParts[1];
                String value = props.getProperty(key);
                
                if (ServiceManager.isServiceReserved(serviceName)) {
                    LoggerFactory.getLogger(ServiceProperties.class)
                        .warn("Ignoring property for reserved service: {}={}", key, value);
                } else if (serviceName.equals(DEFAULT_NAME)) {
                    setProperty(serviceName, attribute, value);
                } else {
                    nonDefaultProps.put(key, value);
                }
            }
            
            for (Map.Entry<String,String> entry : nonDefaultProps.entrySet()) {
                String[] keyParts = entry.getKey().split("\\.",2);
                String serviceName = keyParts[0];
                String attribute = keyParts[1];
                
                setProperty(serviceName, attribute, entry.getValue());
            }
        }
    }
    
    private static void setProperty(String serviceName, String attribute, String value) {
        ServiceProperties service;
        if (serviceName.equals(DEFAULT_NAME)) {
            service = DEFAULT;
        } else {
            service = getServiceProperties(serviceName);
        }
            
        if (attribute.equals(PROP_ENABLE)) {
            service.enable = value.equals("true");
        } else if (attribute.equals(PROP_MAIN)) {
            service.main = value;
        } else if (attribute.equals(PROP_CENTRAL)) {
            service.centralized = value.equals("true");
        } else if (attribute.equals(PROP_READ_ONLY)) {
            service.readOnly = value.equals("true");
        } else if (attribute.equals(PROP_CACHING)) {
            service.caching = value.equals("true");
        } else if (attribute.equals(PROP_JMS)) {
            service.jmsAdapter = value;
        } else if (attribute.equals(PROP_DAO)) {
            if (!serviceName.equals(DEFAULT_NAME)) {
                service.dao = value;
            }
        } else if (attribute.equals(PROP_ROUTER)) {
            if (!serviceName.equals(DEFAULT_NAME)) {
                service.router = value;
            }
        } else if (attribute.equals(PROP_CACHE)) {
            if (!serviceName.equals(DEFAULT_NAME)) {
                service.cache = value;
            }
        } else {
            service.properties.put(attribute, value);
        }
    }
}
