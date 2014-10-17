/*
 * ServiceProperties.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.ServiceManager;
import com.rhythm.louie.service.ServiceUtils;

/**
 *
 * @author cjohnson
 */
public class ServiceProperties {
    
    private static final Map<String,ServiceProperties> SERVICES = new ConcurrentHashMap<>();
    
    private final String name;
    private boolean enable;
    private String centralHost;
    private boolean centralized;
    private boolean readOnly;
    private boolean caching;
    private boolean reserved = false;
    
    private String daoClass = null;
    private String cacheClass = null;
    private String routerClass = null;
    private String providerClass = null;
    
    /* Basic defaults */
    private static boolean defaultEnable;
    private static String defaultCentralHost;
    private static boolean defaultCentralized;
    private static boolean defaultReadOnly;
    private static boolean defaultCaching;
    
    
    private final Map<String,String> properties;
    
    /**
     * Returns properties for a service by name, it is recommended to use the 
     * method that takes a class to avoid discrepancies storing the name directly
     * 
     * @param name the name of the service
     * @return 
     */
    public static ServiceProperties getServiceProperties(String name) {
        ServiceProperties service = SERVICES.get(name);
        if (service==null) {
            service = new ServiceProperties(name);
            SERVICES.put(name, service);
        }
        return service;
    }
    
    /**
     * Returns the service properties for a class by determining the Service that it implements
     * 
     * @param cl a class in the service hierarchy
     * @return 
     */
    public static ServiceProperties getServiceProperties(Class cl) {
        return getServiceProperties(ServiceUtils.getServiceName(cl));
    }
    
    public static List<ServiceProperties> getAllServiceProperties() {
        return Collections.unmodifiableList(new ArrayList<>(SERVICES.values())); //is that seriously the best way to do that???
    }
    
    protected ServiceProperties(String name) { 
        this.name = name;
        this.enable = defaultEnable;
        this.centralHost = defaultCentralHost;
        this.centralized = defaultCentralized;
        this.readOnly = defaultReadOnly;
        this.caching = defaultCaching;
        properties = new ConcurrentHashMap<>();
    }
     

    public String getName() {
        return name;
    }
    
    public String getCentralHost() {
        return centralHost;
    }
    
    public boolean isEnabled() {
        return enable;
    }
    
    public boolean isCentralized() {
        return centralized;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public boolean isCachingOn() {
        return caching;
    }
    
    public boolean isReserved() {
        return reserved;
    }
    
    public String getDAOClass() {
        return daoClass;
    }
    
    public String getCacheClass() {
        return cacheClass;
    }
    
    public String getRouterClass() {
        return routerClass;
    }
    
    public String getProviderClass() {
        return providerClass;
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
    
    protected static void addService(String name, ServiceProperties service) {
        SERVICES.put(name,service);
    }
    
    /////////////////////////////////////////////////////
    // Protected Setters to be used by LouieProperties //
    /////////////////////////////////////////////////////
        
    protected void addCustomProp(String attribute, String value) {
        properties.put(attribute, value);
    }
    
    protected void setEnable(boolean enable) {
        this.enable = enable;
    }

    protected void setCentralLocation(String main) {
        this.centralHost = main;
    }

    protected void setCentralized(boolean centralized) {
        this.centralized = centralized;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    protected void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    protected void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    protected void setDaoClass(String daoClass) {
        this.daoClass = daoClass;
    }

    protected void setCacheClass(String cacheClass) {
        this.cacheClass = cacheClass;
    }

    protected void setRouterClass(String routerClass) {
        this.routerClass = routerClass;
    }
    
    protected void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    protected static void setDefaultEnable(boolean defaultEnable) {
        ServiceProperties.defaultEnable = defaultEnable;
    }

    protected static void setDefaultCentralHost(String defaultHost) {
        ServiceProperties.defaultCentralHost = defaultHost;
    }

    protected static void setDefaultCentralized(boolean defaultCentralized) {
        ServiceProperties.defaultCentralized = defaultCentralized;
    }

    protected static void setDefaultReadOnly(boolean defaultReadOnly) {
        ServiceProperties.defaultReadOnly = defaultReadOnly;
    }

    protected static void setDefaultCaching(boolean defaultCaching) {
        ServiceProperties.defaultCaching = defaultCaching;
    }
    
    protected static void processServices(List<ServiceProperties> services) {
        for (ServiceProperties prop : services) {
            ServiceProperties existing = SERVICES.get(prop.getName());
            if (existing != null && existing.isReserved()) continue;
            SERVICES.put(prop.getName(), prop); //allows for overwriting non-reserved services
        }
    }
//    @Deprecated
//    public static void processServiceProperties(Properties props) {
//        synchronized(SERVICES) {
//            // Load up defaults first
//            Map<String,String> nonDefaultProps = new HashMap<>();
//            for (String key : props.stringPropertyNames()) {
//                String[] keyParts = key.split("\\.",2);
//                if (keyParts.length!=2) {
//                    LoggerFactory.getLogger(ServiceProperties.class)
//                        .warn("Skipping key as it does not match service.attribute: {}", key);
//                    continue;
//                }
//                
//                String serviceName = keyParts[0];
//                String attribute = keyParts[1];
//                String value = props.getProperty(key);
//                
//                if (ServiceManager.isServiceReserved(serviceName)) {
//                    LoggerFactory.getLogger(ServiceProperties.class)
//                        .warn("Ignoring property for reserved service: {}={}", key, value);
//                } else if (serviceName.equals(DEFAULT_NAME)) {
//                    setProperty(serviceName, attribute, value);
//                } else {
//                    nonDefaultProps.put(key, value);
//                }
//            }
//            
//            for (Map.Entry<String,String> entry : nonDefaultProps.entrySet()) {
//                String[] keyParts = entry.getKey().split("\\.",2);
//                String serviceName = keyParts[0];
//                String attribute = keyParts[1];
//                
//                setProperty(serviceName, attribute, entry.getValue());
//            }
//        }
//    }
    
//    private static void setProperty(String serviceName, String attribute, String value) {
//        ServiceProperties service;
//        if (serviceName.equals(DEFAULT_NAME)) {
//            service = DEFAULT;
//        } else {
//            service = getServiceProperties(serviceName);
//        }
//            
//        switch (attribute) {
//            case PROP_ENABLE:
//                service.enable = Boolean.parseBoolean(value);
//                break;
//            case PROP_MAIN:
//                service.main = value;
//                break;
//            case PROP_CENTRAL:
//                service.centralized = Boolean.parseBoolean(value);
//                break;
//            case PROP_READ_ONLY:
//                service.readOnly = Boolean.parseBoolean(value);
//                break;
//            case PROP_CACHING:
//                service.caching = Boolean.parseBoolean(value);
//                break;
//            case PROP_JMS:
//                service.jmsAdapter = value;
//                break;
//            case PROP_DAO:
//                if (!serviceName.equals(DEFAULT_NAME)) {
//                    service.dao = value;
//                }   break;
//            case PROP_ROUTER:
//                if (!serviceName.equals(DEFAULT_NAME)) {
//                    service.router = value;
//                }   break;
//            case PROP_CACHE:
//                if (!serviceName.equals(DEFAULT_NAME)) {
//                    service.cache = value;
//            }   break;
//            default:
//                service.properties.put(attribute, value);
//                break;
//        }
//    }
}
