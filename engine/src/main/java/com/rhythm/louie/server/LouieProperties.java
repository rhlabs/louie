/*
 * Properties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.slf4j.LoggerFactory;

/**
 * A central driver for populating ServiceProperties and Server objects, as well as additional
 * CustomProperty objects from multiple xml configuration files.
 * @author eyasukoc
 */
public class LouieProperties {
  
    private static final Map<String, CustomProperty> customProperties = new HashMap<>();
    
    ////////////////////
    //// Known Keys ////
    ////////////////////
    
    private static final String NAME = "name";
    private static final String ALT_PATH = "config_path";
    private static final String DEFAULT = "defaults";
    private static final String RESERVED = "reserved";
    
    //services
    private static final String SERVICE = "service";
    private static final String SERVICE_PARENT = "services";
    private static final String ENABLE = "enable";
    private static final String CENTRAL_HOST = "central_host";
    private static final String CENTRAL = "centralized";
    private static final String READ_ONLY = "read_only";
    private static final String CACHING = "caching";
    private static final String PROVIDER_CL = "provider_class";
    private static final String DAO_CL = "dao_class";
    private static final String CACHE_CL = "cache_class";
    private static final String ROUTER_CL = "router_class";
    
    //servers
    private static final String SERVER = "server";
    private static final String SERVER_PARENT = "servers";
    private static final String HOST = "host";
    private static final String DISPLAY = "display";
    private static final String TIMEZONE = "timezone";
    private static final String LOCATION = "location";
    private static final String GATEWAY = "gateway";
    private static final String IP = "ip";
    private static final String CENTRAL_AUTH = "central_auth";
    private static final String ROUTER = "router";
    private static final String PORT = "port";
    
    public static void processProperties(URL configs, String contextGateway) throws Exception {
        loadInternals(); //this code organization is weird but it's from iterations of design
        
        if (contextGateway != null) {
            //Overrides a default set by internal properties
            Server.setDefaultGateway(contextGateway);
        }
        
        Document properties = loadDocument(configs);
        if (properties == null) return;
        
        Element louie = properties.getRootElement();
        
        //Check for alternate loading point 
        Element altLoadPath = louie.getChild(ALT_PATH);
        if (altLoadPath != null) {
            //overwrite document and root element with values from alternate config 
            properties = loadDocument(new File(altLoadPath.getText()).toURI().toURL());
            if (properties == null) return;
            
            louie = properties.getRootElement(); 
        }
        
        //Process servers
        Element servers = louie.getChild(SERVER_PARENT);
        processServers(servers);
        
        //Process services
        Element services = louie.getChild(SERVICE_PARENT);
        processServices(services, false);
        
        //Process custom configs
        List<Element> louieChildren = louie.getChildren();
        for (Element louieChild : louieChildren) {
            String configName = louieChild.getName();
            if (SERVICE_PARENT.equals(configName) || SERVER_PARENT.equals(configName)) continue;
            
            CustomProperty custom = new CustomProperty(configName);
            for (Element child : louieChild.getChildren()) {
                custom.setProperty(child.getName(), child.getText());
            }
            customProperties.put(configName, custom);
        }
    }
    
    private static Document loadDocument(URL configs){
        Document properties;
        SAXBuilder docBuilder = new SAXBuilder();
        try {
            properties = docBuilder.build(configs);
        } catch (IOException | JDOMException | NullPointerException ex) {
            LoggerFactory.getLogger(LouieProperties.class)
                    .error("Failed to load properties file! Defaults will be used.\n{}",ex.toString());
            Server.processServers(Collections.EMPTY_LIST);
            return null;
        }
        return properties;
    }
    
    public static void loadInternals() throws JDOMException, IOException {
        Document internals;
        SAXBuilder docBuilder = new SAXBuilder();

        URL xmlURL = LouieProperties.class.getResource("/config/louie-internal.xml");
        internals = docBuilder.build(xmlURL);

        Element louie = internals.getRootElement();

        //Load internal defaults into Server
        Element serverDef = louie.getChild("server_defaults");

        Server.setDefaultHost(serverDef.getChildText(HOST));
        Server.setDefaultGateway(serverDef.getChildText(GATEWAY));
        Server.setDefaultDisplay(serverDef.getChildText(DISPLAY));
        Server.setDefaultIP(serverDef.getChildText(IP));
        Server.setDefaultTimezone(serverDef.getChildText(TIMEZONE));
        Server.setDefaultLocation(serverDef.getChildText(LOCATION));
        Server.setDefaultRouter(Boolean.valueOf(serverDef.getChildText(ROUTER)));
        Server.setDefaultPort(Integer.valueOf(serverDef.getChildText(PORT)));

        //Load internal defaults into ServiceProperties
        Element serviceDef = louie.getChild("service_defaults");

        ServiceProperties.setDefaultCaching(Boolean.valueOf(serviceDef.getChildText(CACHING)));
        ServiceProperties.setDefaultCentralized(Boolean.valueOf(serviceDef.getChildText(CENTRAL)));
        ServiceProperties.setDefaultEnable(Boolean.valueOf(serviceDef.getChildText(ENABLE)));
        ServiceProperties.setDefaultCentralHost(serviceDef.getChildText(CENTRAL_HOST));
        ServiceProperties.setDefaultReadOnly(Boolean.valueOf(serviceDef.getChildText(READ_ONLY)));

        //Load internal services into ServiceProperties
        Element coreServices = louie.getChild("core_services");
        processServices(coreServices, true);

    }
    
    private static void processServers(Element servers){
        List<Server> serverList = new ArrayList<>();
        
        if (servers == null) {
            Server.processServers(Collections.EMPTY_LIST);
            return;
        }
        
        for (Element server : servers.getChildren()) {
            if (!SERVER.equals(server.getName().toLowerCase())) continue;
            
            String name = null;
            for (Attribute attr : server.getAttributes()) {
                if (NAME.equals(attr.getName().toLowerCase())) name = attr.getValue();
            }
            if (name == null) {
                LoggerFactory.getLogger(LouieProperties.class)
                        .error("A server was missing it's 'name' attribute and will be skipped!");
                continue;
            }
            Server prop = new Server(name);
            
            for (Element serverProp : server.getChildren()) {
                String propName = serverProp.getName().toLowerCase();
                String propValue = serverProp.getText();
                if (null != propName) switch (propName) {
                    case HOST: prop.setHost(propValue);
                        break;
                    case DISPLAY: prop.setDisplay(propValue);
                        break;
                    case LOCATION: prop.setLocation(propValue);
                        break;
                    case GATEWAY: prop.setGateway(propValue);
                        break;
                    case IP: prop.setIp(propValue);
                        break;
                    case ROUTER: prop.setRouter(Boolean.valueOf(propValue));
                        break;
                    case CENTRAL_AUTH: prop.setCentralAuth(Boolean.valueOf(propValue));
                        break;
                    case PORT: prop.setPort(Integer.valueOf(propValue));
                        break;
                    default: prop.addCustomProperty(propName, propValue);
                        break;
                }
            }
            serverList.add(prop);
        }
        Server.processServers(serverList);
    }
    
    private static void processServiceDefaults(Element defaults) {
        for (Element defaultProp : defaults.getChildren()) {
            String propName = defaultProp.getName().toLowerCase();
            String propValue = defaultProp.getText();
            if (null != propName) switch (propName) {
                case CACHING: ServiceProperties.setDefaultCaching(Boolean.valueOf(propValue));
                    break;
                case ENABLE: ServiceProperties.setDefaultEnable(Boolean.valueOf(propValue));
                    break;
                case CENTRAL_HOST: ServiceProperties.setDefaultCentralHost(propValue);
                    break;
                case CENTRAL: ServiceProperties.setDefaultCentralized(Boolean.valueOf(propValue));
                    break;
                case READ_ONLY: ServiceProperties.setDefaultReadOnly(Boolean.valueOf(propValue));
                    break;
                default: 
                    LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected default service config key {}:{}",propName,propValue);
                    break;
            }
        }
    }
    
    private static void processServices(Element services, boolean internal) {
        List<ServiceProperties> servicesList = new ArrayList<>();
        
        if (services == null) return;
        
        for (Element service : services.getChildren()) {
            if (!SERVICE.equals(service.getName().toLowerCase())) continue;
            
            String serviceName = null;
            Boolean enable = false;
            for (Attribute attr : service.getAttributes()) {
                String propName = attr.getName().toLowerCase();
                String propValue = attr.getValue();
                if (null != propName) switch (propName) {
                    case NAME: serviceName = propValue;
                        break;
                    case ENABLE: enable = Boolean.valueOf(propValue);
                        break;
                    default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected service attribute {}:{}",propName,propValue);
                        break;
                }
            }
            
            if (serviceName == null) {
                LoggerFactory.getLogger(LouieProperties.class)
                        .error("A service was missing it's 'name' attribute and will be skipped");
                continue;
            }
            
            ServiceProperties prop = new ServiceProperties(serviceName);
            if (enable != null) {
                prop.setEnable(enable);
            }
            
            for (Element serviceProp : service.getChildren()) {
                String propName = serviceProp.getName().toLowerCase();
                String propValue = serviceProp.getText();
                if (null != propName) switch (propName) {
                    case DEFAULT: processServiceDefaults(serviceProp);
                        break;
                    case CACHING: prop.setCaching(Boolean.valueOf(propValue));
                        break;
                    case CENTRAL_HOST: prop.setCentralHost(propValue);
                        break;
                    case CENTRAL: prop.setCentralized(Boolean.valueOf(propValue));
                        break;
                    case READ_ONLY: prop.setReadOnly(Boolean.valueOf(propValue));
                        break;
                    case DAO_CL: prop.setDaoClass(propValue);
                        break;
                    case CACHE_CL: prop.setCacheClass(propValue);
                        break;
                    case ROUTER_CL: prop.setRouterClass(propValue);
                        break;
                    case PROVIDER_CL: prop.setProviderClass(propValue);
                        break;
                    case RESERVED: 
                        if (internal) prop.setReserved(Boolean.valueOf(propValue));
                        break;
                    default: prop.addCustomProp(propName, propValue);
                }
            }
            
            servicesList.add(prop);
        }
        
        ServiceProperties.processServices(servicesList);
    }
    
    public static CustomProperty getCustomProperty(String key) {
        return customProperties.get(key);
    }
    
}
