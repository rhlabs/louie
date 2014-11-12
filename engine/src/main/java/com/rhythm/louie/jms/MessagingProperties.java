/*
 * MessagingProperties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.server.CustomProperty;

/**
 *
 * @author cjohnson
 */
public class MessagingProperties {
    private static final Map<String, CustomProperty> customProperties = new HashMap<>();
    
    private static final String SYSTEM_PROP_KEY = "com.rhythm.louie.jmsadapter";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "61616";
    
    ////////////////////
    //// Known Keys ////
    ////////////////////
    
    private static final String JMSADAPTER = "jmsadapter";
    //Attributes
    private static final String CLASS = "class";
    
    private static final String SERVER = "server";
    private static final String CLIENT = "client";
    private static final String UPDATE = "update";
    //Attributes
    private static final String PREFIX = "prefix";
    private static final String TYPE = "type";
    
    private static final String PROPERTIES = "properties";
    
    // VALUES
    private static String adapterClass = System.getProperty(SYSTEM_PROP_KEY);;
    private static String host = DEFAULT_HOST;
    private static String port = DEFAULT_PORT;
    private static String failover = "true";
    
    //TODO load these from an internal louie config file
    private static String serverPrefix = "louie.server.";
    private static String serverType = "queue";
    private static String updatePrefix = "louie.update.";
    private static String updateType = "queue";
    private static String clientPrefix = "louie.";
    private static String clientType = "topic";

    public static void processMessaging(Element messaging){
        for (Element prop : messaging.getChildren()) {
            String propName = prop.getName().toLowerCase();
            if (null != propName) {
                switch (propName) {
                    case JMSADAPTER:
                        adapterClass = prop.getAttributeValue(CLASS, getAdapterClass());
                        host = prop.getAttributeValue(JmsAdapter.HOST_KEY, getHost());
                        port = prop.getAttributeValue(JmsAdapter.PORT_KEY, getPort());
                        failover = prop.getAttributeValue(JmsAdapter.FAILOVER_KEY, getFailover());
                        break;
                    case SERVER:
                        serverPrefix = prop.getAttributeValue(PREFIX, getServerPrefix());
                        serverType = prop.getAttributeValue(TYPE, getServerType());
                        break;
                    case CLIENT:
                        clientPrefix = prop.getAttributeValue(PREFIX, getClientPrefix());
                        clientType = prop.getAttributeValue(TYPE, getClientType());
                        break;
                    case UPDATE:
                        updatePrefix = prop.getAttributeValue(PREFIX, getUpdatePrefix());
                        updateType = prop.getAttributeValue(TYPE, getUpdateType());
                        break;
                    case PROPERTIES:
                        for (Element customProp : prop.getChildren()) {
                            String customName = customProp.getName().toLowerCase();
                            CustomProperty custom = new CustomProperty(customName);
                            for (Element child : customProp.getChildren()) {
                                custom.setProperty(child.getName(), child.getText().trim());
                            }
                            customProperties.put(customName, custom);
                        }
                        break;
                    default:
                         LoggerFactory.getLogger(MessagingProperties.class)
                            .warn("Unknown Message Property:{}",propName);
                        break;
                }
            }
        }
    }

    public static CustomProperty getCustomProperty(String key) {
        return customProperties.get(key);
    }
     
    /**
     * @return the adapterClass
     */
    public static String getAdapterClass() {
        return adapterClass;
    }

    /**
     * @return the host
     */
    public static String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public static String getPort() {
        return port;
    }

    /**
     * @return the failover
     */
    public static String getFailover() {
        return failover;
    }

    /**
     * @return the serverPrefix
     */
    public static String getServerPrefix() {
        return serverPrefix;
    }

    /**
     * @return the serverType
     */
    public static String getServerType() {
        return serverType;
    }

    /**
     * @return the clientPrefix
     */
    public static String getClientPrefix() {
        return clientPrefix;
    }

    /**
     * @return the clientType
     */
    public static String getClientType() {
        return clientType;
    }

    /**
     * @return the updatePrefix
     */
    public static String getUpdatePrefix() {
        return updatePrefix;
    }

    /**
     * @return the updateType
     */
    public static String getUpdateType() {
        return updateType;
    }
    
}
