/*
 * ServiceProperties.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.service.ServiceUtils;
import com.rhythm.louie.service.layer.ServiceLayer;

/**
 *
 * @author cjohnson
 */
public class ServiceProperties {
    
    private static final Map<String,ServiceProperties> SERVICES = new ConcurrentHashMap<>();
    
    /* Basic defaults */
    private static boolean defaultEnable;
    private static String defaultRemoteHost;
    private static boolean defaultReadOnly;
    private static boolean defaultCaching;
    
    private static boolean globalEnable = true;
    
    /* Instance properties */
    private final String name;
    private boolean enable;
    private boolean readOnly;
    private boolean caching;
    private boolean reserved = false;
    private String providerClass = null;
    
    private final List<ServiceLayer> layers;
    private final Map<String,String> properties;
    
    private String constructedLayers = null;
    
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
        this.readOnly = defaultReadOnly;
        this.caching = defaultCaching;
        properties = new ConcurrentHashMap<>();
        layers = new ArrayList<>();
    }
     
    public String getName() {
        return name;
    }
    
    public boolean isEnabled() {
        if (reserved) return enable;
        return (enable && globalEnable);
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
    
    public String getProviderClass() {
        return providerClass;
    }
    
    synchronized public List<ServiceLayer> getServiceLayers() {
        return Collections.unmodifiableList(layers);
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
    
    public void setLayersString(String layers) {
        constructedLayers = layers;
    }
    
    public String getConstructedLayers() {
        return constructedLayers;
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

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    protected void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    protected void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    protected void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    synchronized protected void addLayer(ServiceLayer layer) {
        layers.add(layer);
    }
    
    // Static Defaults
    
    protected static void setDefaultEnable(boolean defaultEnable) {
        ServiceProperties.defaultEnable = defaultEnable;
    }

    protected static void setDefaultRemoteHost(String defaultHost) {
        ServiceProperties.defaultRemoteHost = defaultHost;
    }

    public static String getDefaultRemoteHost() {
        return defaultRemoteHost;
    }
    
    protected static void setDefaultReadOnly(boolean defaultReadOnly) {
        ServiceProperties.defaultReadOnly = defaultReadOnly;
    }

    protected static void setDefaultCaching(boolean defaultCaching) {
        ServiceProperties.defaultCaching = defaultCaching;
    }
    
    protected static void globalDisable() {
        globalEnable = false;
    }
    
    // Process
    
    protected static void processServices(List<ServiceProperties> services) {
        for (ServiceProperties prop : services) {
            ServiceProperties existing = SERVICES.get(prop.getName());
            if (existing != null && existing.isReserved()) continue;
            SERVICES.put(prop.getName(), prop); //allows for overwriting non-reserved services
        }
    }

    public String printHtmlService() {
        StringBuilder out = new StringBuilder();
        
        out.append("<b>enabled:</b>   ").append(isEnabled()).append("<br/>\n");
        out.append("<b>provider:</b>  ").append(getProviderClass()).append("<br/>\n");
        out.append("<b>read_only:</b> ").append(isReadOnly()).append("<br/>\n");
        out.append("<b>caching:</b>   ").append(isCachingOn()).append("<br/>\n");
        out.append("<b>reserved:</b>  ").append(isReserved()).append("<br/>\n");
        if (getConstructedLayers() != null) {
            out.append("<b>layers:</b>    ").append(getConstructedLayers()).append("<br/>\n");
        }
        if (!properties.isEmpty()) {
            out.append("<b>custom:</b><br/>\n");
            for (String key : properties.keySet()) {
                out.append("<b style=\"padding-left:3em\">\t").append(key).append(":</b> ").append(properties.get(key)).append("<br/>\n");
            }
        }
        
        return out.toString();
    }
    
}
