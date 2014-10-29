/*
 * RequestFactory.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie;


import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.jms.*;
import com.rhythm.louie.server.*;
import com.rhythm.louie.service.Service;
import com.rhythm.louie.service.ServiceFactory;
import com.rhythm.louie.topology.Route;

/**
 * @author cjohnson
 * Created: Mar 1, 2011 4:16:04 PM
 */
public class ServiceManager {
    private static final Map<String, Service> servicesByName =
        Collections.synchronizedMap(new TreeMap<String, Service>());
    private static boolean init = false;
    
    private static final Map<String, ServiceFactory> serviceFactories = new HashMap<>();
    private static final Map<String, String> failedServiceProviders = new HashMap<>();
    
    private ServiceManager() {};
    
    public static synchronized void initialize() throws MessageAdapterException {
        initialize(null);
    }
    
    public static synchronized void initialize(ServletContext context) throws MessageAdapterException {
        if (init) return;
        
        Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
        
        StringBuilder versionInfo = new StringBuilder();
        
        for (BuildProperties build : BuildProperties.getServiceBuildProperties()) {
            if("LoUIE Processor".equals(build.getName())) continue; //hardcoded processor skip
            versionInfo.append("  ").append(build.getName()).append(": ")
                    .append(build.getVersion()).append(" ")
                    .append(build.getBuildString()).append("\n");
        }
        LOGGER.info("\n********************************************************\n"
                + "LoUIE ServiceManager Initialization - \n{}"
                + "\n********************************************************\n"
                + "{} : {}",
                versionInfo,
                LocalConstants.HOST,LocalConstants.HOSTDOMAIN);
        init = true;
         
        Identity.registerLouieIdentity();
        
        
        
        try {
            loadProperties(context);
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            return;
        }
        
        if (Server.LOCAL==Server.UNKNOWN) {
            return;
        }
        
        // Services
        loadServiceProviders();
        
        // Router
        if (Server.LOCAL.isARouter()) {
            LOGGER.info("This Server started as a router.");
            configureRoutes(context);
        }

        try {
            EmailService.getInstance().initialize();
        } catch (Exception ex) {
            LOGGER.error("Error initializing email service", ex);
        }
        
        // JMS
        try {
            MessageUpdate.getInstance().initialize();
        } catch (Exception ex) {
            LOGGER.error("Error initializing JMS service", ex);
        }
        
        // Load Services
        StringBuilder disabled = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("\nServices:\n\n");
        
        for (ServiceFactory factory : serviceFactories.values()) {
            String serviceName = factory.getServiceName().toLowerCase();
            ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
            if (props.isEnabled()) {
                sb.append(String.format("%-15s",serviceName));
                try {
                    long start = System.nanoTime();
                    initializeService(factory);
                    long time = (System.nanoTime()-start) / 1000000;
                    sb.append(String.format("%6d ms - ",time));
                    
                    int depth = 0;
                    Object level = servicesByName.get(serviceName);
                    while(level instanceof Delegate) {
                        level = ((Delegate)level).getDelegate();
                        if (depth>0) {
                            sb.append("->");
                        }
                        sb.append(level.getClass().getSimpleName());
                        depth++;
                    }
                    
                    if (props.isCentralized()) {
                        sb.append(" || centralized @ ").append(props.getCentralHost());
                    }
                    if (props.isReadOnly()) {
                        sb.append(" || Read-Only");
                    }
                    sb.append("\n");
                } catch (MessageAdapterException ex) {
                    throw ex;
                } catch (Exception ex) {
                    failedServiceProviders.put(factory.getClass().getSimpleName(), ex.toString());
                    sb.append(" - ERROR: ")
                            .append(ex.toString())
                            .append("\n");
                }
            } else {
                disabled.append(factory.getServiceName()).append("\n");
            }
        }
        
        LOGGER.info(sb.toString());
    }
    
    private static final String CONF_DIR = "/WEB-INF/conf/"; //old
    private static final String PROP_DIR = "/WEB-INF/classes/"; //new
    private static final String ROUTE_PROPERTIES = "routing";
    private static final String LOUIE_PROPERTIES = "louie.xml";
    
    private static void loadServiceProviders() {
        
        //First, load from ServiceProperties if something was set
        for (ServiceProperties prop : ServiceProperties.getAllServiceProperties()) {
            String serviceProvider = prop.getProviderClass();
            if (serviceProvider != null) {
                try {
                    ServiceFactory servFactory = (ServiceFactory) Class.forName(serviceProvider).newInstance();
                    serviceFactories.put(servFactory.getServiceName(), servFactory);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    failedServiceProviders.put(serviceProvider, ex.toString());
                    LoggerFactory.getLogger(ServiceManager.class)
                            .error("Failed to load ServiceProvider {} from properties: {}",serviceProvider,ex.toString());
                    
                }
            }
        }
        
        //Now, populate additional services using the generated louie-serviceprovider file in each jar
        Enumeration<URL> serviceClasses;
        try {
            serviceClasses = ServiceManager.class.getClassLoader().getResources(ServiceProcessor.SERVICE_PROVIDER_FILE);
        } catch (IOException ex) {
            LoggerFactory.getLogger(ServiceManager.class)
                    .error("Failed to fetch ServiceProvider prop files: {}",ex.toString());
            return;
        }
        
        while (serviceClasses.hasMoreElements()) {
            URL serviceClass = serviceClasses.nextElement();
            
            try (BufferedReader reader = new BufferedReader( new InputStreamReader(serviceClass.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        ServiceFactory servFactory = (ServiceFactory) Class.forName(line).newInstance();
                        String serviceName = servFactory.getServiceName();
                        if (serviceFactories.containsKey(serviceName)) {
                            if (ServiceProperties.getServiceProperties(serviceName).getProviderClass() == null) {
                                failedServiceProviders.put(line, "Multiple providers found for the same service");
                                LoggerFactory.getLogger(ServiceManager.class)
                                        .warn("An additional ServiceProvider: {} was BLOCKED from being loaded for service {}", line, serviceName);
                            }
                        } else {
                            serviceFactories.put(serviceName, servFactory);
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex) {
                        failedServiceProviders.put(line, ex.toString());
                        LoggerFactory.getLogger(ServiceManager.class)
                                .error("Failed to load a class from ServiceProvider prop file: {}",ex.toString());
                    }
                }
            } catch (IOException ex) {
                LoggerFactory.getLogger(ServiceManager.class).error("Failed to parse a ServiceProvider prop file: {} ",ex.toString());
            }
        }    
        
        // Remove any services that are marked as failed in case of ambiguous loading
        for (String failed : failedServiceProviders.keySet()) {
            serviceFactories.remove(failed);
        }
    }
    
    private static void configureRoutes(ServletContext context) {
        Properties routeProps = loadProperties(context, CONF_DIR, ROUTE_PROPERTIES);
        Route.initialize(routeProps);
    }
    
    /**
     * Attempts to locate a full file and path via a property set in web.xml
     * If this fails, it will attempt to locate a URL as a servlet resource
     * If that also fails, you will startup with a default set of properties for each service 
     * but Servers will remain unconfigured (only localhost known)
     * @param context The ServletContext for this deployment
     * @throws Exception 
     */
    private static void loadProperties(ServletContext context) throws Exception{
        URL louieXml = null;
        try {
            louieXml = context.getResource(PROP_DIR + LOUIE_PROPERTIES);
        } catch (MalformedURLException ex) {
            LoggerFactory.getLogger(ServiceManager.class)
                    .error("Failed to get URL for Properties file: {}",ex.toString());
        }
        String contextGateway = context.getContextPath().replaceFirst("/", "");
        LouieProperties.processProperties(louieXml, contextGateway);
    }
    
    private static Properties loadProperties(ServletContext context,String dir,String propFile) {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = context.getResourceAsStream(dir + propFile+"."+LocalConstants.HOST);
            if (in == null){
                in = context.getResourceAsStream(dir + propFile);
            }
            props.load(in);
        } catch (IOException ex) {
            LoggerFactory.getLogger(ServiceManager.class).warn(ex.getMessage());
            return props;
        } catch (NullPointerException npe) {
            System.out.println("NPE encountered for file: " 
                    + dir + propFile + "." + LocalConstants.HOST);
        } finally {
            if (in!=null)  {
                try {
                    in.close();
                } catch (IOException ex) {
                    LoggerFactory.getLogger(ServiceManager.class).warn(ex.getMessage());
                }
            }
        }
        
        return props;
    }
    
    private static void initializeService(ServiceFactory factory) throws Exception, MessageAdapterException {
        Service service = factory.getService();
        service.initialize();
        MessageHandler mh = service.getMessageHandler();
        if (mh!=null) {
            try {
                MessageManager.getInstance().listenToQueue("Server.current." + service.getServiceName() + "Update");
                MessageManager.getInstance().addMessageHandler(mh);
            } catch (MessageAdapterException e) {
                LoggerFactory.getLogger(ServiceManager.class).
                        error("Unable to listen to messages for service: "+service.getServiceName(),e);
            }
        }
        
        servicesByName.put(service.getServiceName(), service);
    }

    public static synchronized void shutdown() {
        Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
        
        LOGGER.info("LoUIE Shutdown Initiated");
        
        MessageManager.getInstance().shutdown();
        
        for (Service service : getServices()) {
            try {
                service.shutdown();
            } catch (Exception ex) {
                LOGGER.error("Error shutting down service",ex);
            }
        }
        
        MessageUpdate.getInstance().shutdown();
        TaskScheduler.getInstance().shutdown();
        EmailService.getInstance().shutdown();
        CacheManager.shutdown();
        
        servicesByName.clear();
        
        LOGGER.info("All LoUIE Services Shutdown");
    }

    public static Service getService(String serviceName) throws Exception {
        Service service;
        if (serviceName.equals("louie")) {
            LoggerFactory.getLogger(ServiceManager.class).warn("Routing legacy service: louie->info");
            service = servicesByName.get("info");
        } else {
            service = servicesByName.get(serviceName);
        }
        
        if (service == null) {
            throw new Exception("No such service: "+serviceName);
        }
        return service;
    }

    public static boolean hasService(String serviceName) {
        return servicesByName.containsKey(serviceName);
    }
    
    public static Collection<Service> getServices() {
        return Collections.unmodifiableCollection(servicesByName.values());
    }
    
    public static Collection<String> getServiceNames() {
        return Collections.unmodifiableCollection(servicesByName.keySet());
    }

    public static Map<String, String> getFailedServiceProviders() {
        return Collections.unmodifiableMap(failedServiceProviders);
    }
    
}
