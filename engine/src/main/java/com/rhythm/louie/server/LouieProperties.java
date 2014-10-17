/*
 * Properties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.server;

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
        
        if (configs == null) {
            LoggerFactory.getLogger(LouieProperties.class)
                    .warn("Failed to load any Properties file. URL was null, and no JVM arg was set");
            Server.processServers(Collections.EMPTY_LIST);
            return;
        }
        
        Document properties;
        SAXBuilder docBuilder = new SAXBuilder();
        try {
            properties = docBuilder.build(configs);
        } catch (Exception ex) {
            LoggerFactory.getLogger(LouieProperties.class)
                    .error("Failed to load properties file! Defaults will be used.\n{}",ex.toString());
            Server.processServers(Collections.EMPTY_LIST);
            return;
        }
        
        Element louie = properties.getRootElement();
        
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
        for (Element server : servers.getChildren()) {
            if (!SERVER.equals(server.getName())) continue;
            
            Server prop = new Server(server.getAttributeValue(NAME));
            
            List<Element> serverProperties = server.getChildren();
            
            Element hostElem = server.getChild(HOST);
            if (hostElem != null) {
                prop.setHost(hostElem.getText());
                serverProperties.remove(hostElem);
            }
            Element dispElem = server.getChild(DISPLAY);
            if (dispElem != null) {
                prop.setDisplay(dispElem.getText());
                serverProperties.remove(dispElem);
            }
            Element locElem = server.getChild(LOCATION);
            if (locElem != null) {
                prop.setLocation(locElem.getText());
                serverProperties.remove(locElem);
            }
            Element gateElem = server.getChild(GATEWAY);
            if (gateElem != null) {
                prop.setGateway(gateElem.getText());
                serverProperties.remove(gateElem);
            }
            Element ipElem = server.getChild(IP);
            if (ipElem != null) {
                prop.setIp(ipElem.getText());
                serverProperties.remove(ipElem);
            }
            Element routerElem = server.getChild(ROUTER);
            if (routerElem != null) {
                prop.setRouter(Boolean.valueOf(routerElem.getText()));
                serverProperties.remove(routerElem);
            }
            Element authElem = server.getChild(CENTRAL_AUTH);
            if (authElem != null) {
                prop.setCentralAuth(Boolean.valueOf(authElem.getText()));
                serverProperties.remove(authElem);
            }
            Element portElem = server.getChild(PORT);
            if (portElem != null) {
                prop.setPort(Integer.valueOf(portElem.getText()));
                serverProperties.remove(portElem);
            }
            for (Element remains : serverProperties) {
                prop.addCustomProperty(remains.getName(), remains.getText());
            }
            serverList.add(prop);
        }
        Server.processServers(serverList);
    }
    
    private static void processServiceDefaults(Element defaults) {
        String defCache = defaults.getChildText(CACHING);
        if (defCache != null) ServiceProperties.setDefaultCaching(Boolean.valueOf(defCache));
        String defEnable = defaults.getChildText(ENABLE);
        if (defEnable != null) ServiceProperties.setDefaultEnable(Boolean.valueOf(defEnable));
        String defMain = defaults.getChildText(CENTRAL_HOST);
        if (defMain != null) ServiceProperties.setDefaultCentralHost(defMain);
        String defCentr = defaults.getChildText(CENTRAL);
        if (defCentr != null) ServiceProperties.setDefaultCentralized(Boolean.valueOf(defCentr));
        String defReadOnly = defaults.getChildText(READ_ONLY);
        if (defReadOnly != null) ServiceProperties.setDefaultReadOnly(Boolean.valueOf(defReadOnly));
    }
    
    private static void processServices(Element services, boolean internal) {
        List<ServiceProperties> servicesList = new ArrayList<>();
        
        Element defaults = services.getChild(DEFAULT);
        if (defaults != null) processServiceDefaults(defaults);
        
        for (Element service : services.getChildren()) {
            if (DEFAULT.equals(service.getName())) continue;
            if (!SERVICE.equals(service.getName())) continue;
            
            String serviceName = service.getAttributeValue(NAME);
            List<Element> serviceProperties = service.getChildren();
            ServiceProperties prop = new ServiceProperties(serviceName);
            
            String enable = service.getAttributeValue(ENABLE);
            if (enable != null) {
                prop.setEnable(Boolean.valueOf(enable));
            }
            
            Element cacheElem = service.getChild(CACHING);
            if (cacheElem != null) {
                prop.setCaching(Boolean.valueOf(cacheElem.getText()));
                serviceProperties.remove(cacheElem);
            }
            Element centralElem = service.getChild(CENTRAL_HOST);
            if (centralElem != null) {
                prop.setCentralLocation(centralElem.getText());
                serviceProperties.remove(centralElem);
            }
            Element centralizedElem = service.getChild(CENTRAL);
            if (centralizedElem != null) {
                prop.setCentralized(Boolean.valueOf(centralizedElem.getText()));
                serviceProperties.remove(centralizedElem);
            }
            Element readonlyElem = service.getChild(READ_ONLY);
            if (readonlyElem != null) {
                prop.setReadOnly(Boolean.valueOf(readonlyElem.getText()));
                serviceProperties.remove(readonlyElem);
            }
            Element daoClElem = service.getChild(DAO_CL);
            if (daoClElem != null) {
                prop.setDaoClass(daoClElem.getText());
                serviceProperties.remove(daoClElem);
            }
            Element cacheClElem = service.getChild(CACHE_CL);
            if (cacheClElem != null) {
                prop.setCacheClass(cacheClElem.getText());
                serviceProperties.remove(cacheClElem);
            }
            Element routerClElem = service.getChild(ROUTER_CL);
            if (routerClElem != null) {
                prop.setRouterClass(routerClElem.getText());
                serviceProperties.remove(routerClElem);
            }
            Element providerElem = service.getChild(PROVIDER_CL);
            if (providerElem != null) {
                prop.setProviderClass(providerElem.getText());
                serviceProperties.remove(providerElem);
            }
            
            if (internal) {
                Element reservedElem = service.getChild(RESERVED);
                if (reservedElem != null) {
                    prop.setReserved(Boolean.valueOf(reservedElem.getText()));
                    serviceProperties.remove(providerElem);
                }
            }
            
            for (Element remains : serviceProperties) {
                prop.addCustomProp(remains.getName(), remains.getText());
            }
            
            servicesList.add(prop);
        }
        
        ServiceProperties.processServices(servicesList);
    }
    
    public static CustomProperty getCustomProperty(String key) {
        return customProperties.get(key);
    }
    
}
