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

import com.rhythm.louie.service.layer.AnnotatedServiceLayer;
import com.rhythm.louie.service.layer.CustomServiceLayer;
import com.rhythm.louie.service.layer.RemoteServiceLayer;

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
    private static final String REMOTE_HOST = "remote_host";
    private static final String READ_ONLY = "read_only";
    private static final String CACHING = "caching";
    private static final String PROVIDER_CL = "provider_class";
    
    private static final String LAYERS = "layers";
    private static final String LAYER = "layer";
    private static final String LAYER_DAO = "dao";
    private static final String LAYER_CACHE = "cache";
    private static final String LAYER_ROUTER = "router";
    private static final String LAYER_REMOTE = "remote";
    
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
    private static final String SECURE = "secure";
    
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
        boolean resetRoot = false;
        for (Element elem : louie.getChildren()) {
            if (ALT_PATH.equalsIgnoreCase(elem.getName())) {
                //overwrite document with values from alternate config 
                properties = loadDocument(new File(elem.getText()).toURI().toURL());
                if (properties == null) return;
                resetRoot = true;
            }
        }
        if (resetRoot) {
            //reset root to new properties obj root
            louie = properties.getRootElement(); 
        }
        
        boolean serversConfigured = false;
        for (Element elem : louie.getChildren()) {
            String elemName = elem.getName().toLowerCase();
            
            if (null != elemName) switch(elemName) {
                case ALT_PATH: LoggerFactory.getLogger(LouieProperties.class)
                        .warn("Extra config_path alternate loading point specified. "
                                + "Only one file-switch can be performed.\n"
                                + "  Please verify what is specified in the embedded xml file.\n"
                                + "  Found: {}",elem.getText());
                    break;
                case SERVER_PARENT: processServers(elem);
                    serversConfigured = true;
                    break;
                case SERVICE_PARENT: processServices(elem,false);
                    break;
                default: String configName = elemName;
                    CustomProperty custom = new CustomProperty(configName);
                    for (Element child : elem.getChildren()) {
                        custom.setProperty(child.getName(), child.getText());
                    }
                    customProperties.put(configName, custom);
                    break;
            }
        }
        if (!serversConfigured) processServers(null); //ugly bootstrapping workflow
    }
    
    private static Document loadDocument(URL configs){
        Document properties;
        SAXBuilder docBuilder = new SAXBuilder();
        try {
            properties = docBuilder.build(configs);
        } catch (IOException | JDOMException | NullPointerException ex) {
            LoggerFactory.getLogger(LouieProperties.class)
                    .error("Failed to load properties file! Defaults will be used.\n{}",ex.toString());
            List<Server> empty = Collections.emptyList();
            Server.processServers(empty);
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
        ServiceProperties.setDefaultEnable(Boolean.valueOf(serviceDef.getChildText(ENABLE)));
        ServiceProperties.setDefaultReadOnly(Boolean.valueOf(serviceDef.getChildText(READ_ONLY)));

        //Load internal services into ServiceProperties
        Element coreServices = louie.getChild("core_services");
        processServices(coreServices, true);

    }
    
    private static void processServers(Element servers){
        List<Server> serverList = new ArrayList<>();
        
        if (servers == null) {
            List<Server> empty = Collections.emptyList();
            Server.processServers(empty);
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
                    case SECURE: prop.setSecure(Boolean.valueOf(propValue));
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
                case REMOTE_HOST: ServiceProperties.setDefaultRemoteHost(propValue);
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
        if (services == null) return;
        
        for (Element elem : services.getChildren()) {
            if (DEFAULT.equalsIgnoreCase(elem.getName())) {
                processServiceDefaults(elem);
                break;
            }
        }
        
        List<ServiceProperties> servicesList = new ArrayList<>();
        for (Element service : services.getChildren()) {
            String elementName = service.getName();
            if (!SERVICE.equalsIgnoreCase(elementName)) {
                if (!DEFAULT.equalsIgnoreCase(elementName)) {
                    LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unknown {} element: {}", SERVICE_PARENT, elementName);
                }
                continue;
            }
            
            String serviceName = null;
            Boolean enable = null;
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
                    case CACHING: prop.setCaching(Boolean.valueOf(propValue));
                        break;
                    case READ_ONLY: prop.setReadOnly(Boolean.valueOf(propValue));
                        break;
                    case PROVIDER_CL: prop.setProviderClass(propValue);
                        break;
                    case RESERVED: 
                        if (internal) prop.setReserved(Boolean.valueOf(propValue));
                        break;
                    case LAYERS:
                        processServiceLayers(serviceProp, prop);
                        break;
                    default: prop.addCustomProp(propName, propValue);
                }
            }
            
            servicesList.add(prop);
        }
        
        ServiceProperties.processServices(servicesList);
    }
    
    private static void processServiceLayers(Element layers, ServiceProperties props) {
        for (Element layer : layers.getChildren()) {
            String layerName = layer.getName().toLowerCase();
            switch (layerName) {
                case LAYER:
                    props.addLayer(new CustomServiceLayer(layer.getAttributeValue("class")));
                    break;
                case LAYER_DAO:
                    props.addLayer(AnnotatedServiceLayer.DAO);
                    break;
                case LAYER_CACHE:
                    props.addLayer(AnnotatedServiceLayer.CACHE);
                    break;
                case LAYER_ROUTER:
                    props.addLayer(AnnotatedServiceLayer.ROUTER);
                    break;
                case LAYER_REMOTE:
                    String host = layer.getAttributeValue("host");
                    if (host==null || host.trim().isEmpty()) {
                        host = ServiceProperties.getDefaultRemoteHost();
                    }
                    props.addLayer(new RemoteServiceLayer(host));
                    break;
                default:
                    LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unkown layer:{}",layerName);
            }
        }
    }
    
    public static CustomProperty getCustomProperty(String key) {
        return customProperties.get(key);
    }
    
}
