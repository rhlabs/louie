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
package com.rhythm.louie.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.*;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.XMLOutputter;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.email.MailProperties;
import com.rhythm.louie.jms.MessagingProperties;
import com.rhythm.louie.service.layer.*;

/**
 * A central driver for populating ServiceProperties and Server objects, as well as additional
 * CustomProperty objects from multiple xml configuration files.
 * @author eyasukoc
 */
public class LouieProperties {
  
    private static final Map<String, CustomProperty> customProperties = new HashMap<>();
    
    private static String document;
    
    ////////////////////
    //// Known Keys ////
    ////////////////////
    
    private static final String NAME = "name";
    private static final String ALT_PATH = "config_path";
    private static final String DEFAULT = "defaults";
    private static final String RESERVED = "reserved";
    
    private static final String CUSTOM = "custom";
    
    //services
    private static final String SERVICE = "service";
    private static final String SERVICE_PARENT = "services";
    private static final String ENABLE = "enable";
    @Deprecated
    private static final String REMOTE_HOST = "remote_host";
    private static final String REMOTE_SERVER = "remote_server";
    private static final String READ_ONLY = "read_only";
    private static final String CACHING = "caching";
    private static final String PROVIDER_CL = "provider_class";
    private static final String RESPECTED_GROUPS = "respected_groups";
    
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
    private static final String EXTERNAL_IP = "external_ip";
    private static final String CENTRAL_AUTH = "central_auth";
    private static final String PORT = "port";
    private static final String SECURE = "secure";
    
    //messaging
    private static final String MESSAGING = "messaging";
    
    // email
    private static final String MAIL = "mail";
    
    //scheduler
    private static final String SCHEDULER = "scheduler";
    private static final String POOL_SIZE = "thread_pool_size";
    
    //alerts
    private static final String ALERTS = "alerts";
    
    //administration
    private static final String GROUPS = "groups";
     
    /**
     * An example of what a custom property block might look like:
     * <custom>
     *   <prop_one>
     *     <key_one>value</key_one>
     *     <key_two>value</key_two>
     *   </prop_one>
     *   <prop_two>
     *     <key_three>value</key_three>
     *   </prop_two>
     * </custom>
     * @param key the top level element name inside the custom block
     * @return a CustomProperty, if one was found
     */
    public static CustomProperty getCustomProperty(String key) {
        return customProperties.get(key);
    }
    
    public static void processProperties(URL configs, String contextGateway) throws Exception {
        loadInternals(); //this code organization is weird but it's from iterations of design
        
        if (contextGateway != null) {
            //Overrides a default set by internal properties
            Server.setDefaultGateway(contextGateway);
        }
        
        Document properties = loadDocument(configs);
        if (properties == null) {
            return;
        }
        
        Element louie = properties.getRootElement();
        
        //Check for alternate loading point 
        boolean resetRoot = false;
        for (Element elem : louie.getChildren()) {
            if (ALT_PATH.equalsIgnoreCase(elem.getName())) {
                String altPath = elem.getTextTrim();
                LoggerFactory.getLogger(LouieProperties.class)
                        .info("Loading Louie configs from alternate file: {}", altPath);
                //overwrite document with values from alternate config 
                properties = loadDocument(new File(altPath).toURI().toURL());
                if (properties == null) return;
                resetRoot = true;
            }
        }
        if (resetRoot) {
            //reset root to new properties obj root
            louie = properties.getRootElement(); 
        }
        
        Element groups = louie.getChild(GROUPS);
        if (groups != null) {
            AccessManager.loadGroups(groups);
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
                case MESSAGING: 
                    MessagingProperties.processMessaging(elem);
                    break;
                case MAIL:
                    MailProperties.processProperties(elem);
                    break;
                case SCHEDULER:
                    TaskSchedulerProperties.processProperties(elem);
                    break;
                case ALERTS:
                    AlertProperties.processProperties(elem);
                    break;
                case CUSTOM:
                    processCustomProperties(elem);
                    break;
                case GROUPS:
                    break;
                default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected top level property  {}",elemName);
                    break;
            }
        }
        if (!serversConfigured) processServers(null); //ugly bootstrapping workflow
    }

    private static Document loadDocument(URL configs) {
        return loadDocument(configs,true);
    }
    
    private static final Pattern missingElem = Pattern.compile(".*Cannot find the declaration of element 'louie'.*");
    
    private static Document loadDocument(URL configs, boolean validate){
        Document properties;
        SAXBuilder docBuilder;
        if (validate) {
            docBuilder = new SAXBuilder(XMLReaders.XSDVALIDATING);
        } else {
            docBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        }
        try {
            properties = docBuilder.build(configs);
        } catch (NullPointerException ex) {
            LoggerFactory.getLogger(LouieProperties.class)
                    .error("Failed to load properties file. Defaults will be used.\n{}",ex.toString());
            System.out.println(ex.getMessage());
            List<Server> empty = Collections.emptyList();
            Server.processServers(empty);
            return null;
        } catch (IOException | JDOMException ex) {
            Matcher match = missingElem.matcher(ex.getMessage());
            if (match.matches()) {
                LoggerFactory.getLogger(LouieProperties.class)
                        .info("No schema located: no validation performed.");
                return loadDocument(configs, false);
            } else {
                String error = "Properties file error! All services shutdown";
                ServiceManager.recordError(error, ex);
                LoggerFactory.getLogger(LouieProperties.class)
                        .error("{}\n{}",error,ex.toString());
                List<Server> empty = Collections.emptyList();
                ServiceProperties.globalDisable(); //brute disable
                Server.processServers(empty);
                return null;
            }
        }
        document = new XMLOutputter().outputString(properties);
        return properties;
    }
    
    private static void loadInternals() throws JDOMException, IOException {
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
        Server.setDefaultPort(Integer.valueOf(serverDef.getChildText(PORT)));
        Server.setDefaultSecure(Boolean.valueOf(serverDef.getChildText(SECURE)));
            
        //Load internal defaults into ServiceProperties
        Element serviceDef = louie.getChild("service_defaults");

        ServiceProperties.setDefaultCaching(Boolean.valueOf(serviceDef.getChildText(CACHING)));
        ServiceProperties.setDefaultEnable(Boolean.valueOf(serviceDef.getChildText(ENABLE)));
        ServiceProperties.setDefaultReadOnly(Boolean.valueOf(serviceDef.getChildText(READ_ONLY)));

        //Load internal services into ServiceProperties
        Element coreServices = louie.getChild("core_services");
        processServices(coreServices, true);
        
        Element schedDef = louie.getChild("scheduler_defaults");
        TaskSchedulerProperties.setThreadPoolSize(Integer.parseInt(schedDef.getChildText(POOL_SIZE)));
        
        Element accessDef = louie.getChild("group_defaults");
        AccessManager.loadGroups(accessDef);

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
                String propValue = serverProp.getTextTrim();
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
                    case EXTERNAL_IP: prop.setExternalIp(propValue);
                        break;
                    case CENTRAL_AUTH: prop.setCentralAuth(Boolean.valueOf(propValue));
                        break;
                    case PORT: prop.setPort(Integer.valueOf(propValue));
                        break;
                    case SECURE: prop.setSecure(Boolean.valueOf(propValue));
                        break;
                    case TIMEZONE: prop.setTimezone(propValue);
                        break;
                    case CUSTOM: 
                        for (Element child : serverProp.getChildren()) {
                            prop.addCustomProperty(child.getName(), child.getTextTrim());
                        }
                        break;
                    default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected server property  {}:{}",propName,propValue);
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
            String propValue = defaultProp.getTextTrim();
            if (null != propName) switch (propName) {
                case CACHING: ServiceProperties.setDefaultCaching(Boolean.valueOf(propValue));
                    break;
                case ENABLE: ServiceProperties.setDefaultEnable(Boolean.valueOf(propValue));
                    break;
                case REMOTE_HOST: ServiceProperties.setDefaultRemoteHost(propValue);
                    break;
                case REMOTE_SERVER: ServiceProperties.setDefaultRemoteServer(propValue);
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
                String propValue = serviceProp.getTextTrim();
                if (null != propName) switch (propName) {
                    case CACHING: prop.setCaching(Boolean.valueOf(propValue));
                        break;
                    case READ_ONLY: prop.setReadOnly(Boolean.valueOf(propValue));
                        break;
                    case PROVIDER_CL: prop.setProviderClass(propValue);
                        break;
                    case RESPECTED_GROUPS: AccessManager.loadServiceAccess(serviceName, serviceProp);
                        break;
                    case RESERVED: 
                        if (internal) prop.setReserved(Boolean.valueOf(propValue));
                        break;
                    case LAYERS:
                        processServiceLayers(serviceProp, prop);
                        break;
                    case CUSTOM: 
                        for (Element child : serviceProp.getChildren()) {
                            prop.addCustomProp(child.getName(), child.getTextTrim());
                        }
                        break;
                    default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected server property  {}:{}",propName,propValue);
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
                    String server = layer.getAttributeValue(SERVER);
                    String host = layer.getAttributeValue(HOST);
                    String gateway = layer.getAttributeValue(GATEWAY);
                    String port = layer.getAttributeValue(PORT);
                    if (server != null) {
                        props.addLayer(new RemoteServiceLayer(server));
                    } else if (host != null && gateway != null && port != null) {
                        props.addLayer(new RemoteServiceLayer(host,gateway,Integer.parseInt(port)));
                    } else {
                        String defaultServer = ServiceProperties.getDefaultRemoteServer();
                        if (defaultServer == null) {
                            defaultServer = ServiceProperties.getDefaultRemoteHost();
                            if (defaultServer == null) {
                                LoggerFactory.getLogger(LouieProperties.class)
                                    .error("Failed to configure remote layer for service {}. Check configs.",props.getName());
                            } 
                        }
                        props.addLayer(new RemoteServiceLayer(defaultServer));
                    }
                    break;
                default:
                    LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unkown layer:{}",layerName);
            }
        }
    }
    
    private static void processCustomProperties(Element customElem) {
        for (Element customProp : customElem.getChildren()) {
            String propName = customProp.getName();
            CustomProperty custom = new CustomProperty(propName);
            for (Element child : customProp.getChildren()) {
                custom.setProperty(child.getName(), child.getTextTrim());
            }
            customProperties.put(propName, custom);
        }
    }
    
    public static String getDocument() {
        return document;
    }
    
}
