/*
 * RequestFactory.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie;


import java.util.*;

import com.rhythm.louie.auth.AuthServiceFactory;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.jms.MessageAdapterException;
import com.rhythm.louie.jms.MessageUpdate;
import com.rhythm.louie.jms.MessageHandler;
import com.rhythm.louie.jms.MessageManager;
import com.rhythm.louie.info.InfoServiceFactory;
import com.rhythm.louie.server.ExternalProperties;
import com.rhythm.louie.server.LocalConstants;
import com.rhythm.louie.server.Server;
import com.rhythm.louie.server.ServiceProperties;
import com.rhythm.louie.server.TaskScheduler;
import com.rhythm.louie.topology.Route;
import com.rhythm.louie.testservice.TestServiceFactory;

import com.rhythm.pb.command.Service;
import com.rhythm.pb.command.ServiceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cjohnson
 * Created: Mar 1, 2011 4:16:04 PM
 */
public class ServiceManager {
    private static final Map<String, Service> servicesByName =
        Collections.synchronizedMap(new TreeMap<String, Service>());
    private static boolean init = false;
    
    private static final Set<String> reservedServices;
    private static final List<ServiceFactory> serviceFactories;
    static {
        serviceFactories = new ArrayList<ServiceFactory>();
        serviceFactories.add(InfoServiceFactory.getInstance());
        serviceFactories.add(AuthServiceFactory.getInstance());
        serviceFactories.add(TestServiceFactory.getInstance());
        
        reservedServices = new HashSet<String>();
        for (ServiceFactory factory : serviceFactories) {
            reservedServices.add(factory.getServiceName());
            ServiceProperties.initReservedProperties(factory.getServiceName());
        }
    }
    
    public static void addService(ServiceFactory factory) throws Exception {
        if (reservedServices.contains(factory.getServiceName())) {
            throw new Exception("Cannot addService: "+factory.getServiceName()+"!"
                    + "  This name is restricted.");
        }
        serviceFactories.add(factory);
    }
    
    public static boolean isServiceReserved(String serviceName) {
        return reservedServices.contains(serviceName);
    }
    
    private ServiceManager() {};
    
    public static synchronized void initialize() throws MessageAdapterException {
        initialize(null);
    }
    
    public static synchronized void initialize(ServletContext context) throws MessageAdapterException {
        if (init) return;
        
        Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
        
        StringBuilder versionInfo = new StringBuilder();
        Map<String,String> gitVersions = ExternalProperties.getInstance().getGitVersionMap();
        Map<String,String> compileDates = ExternalProperties.getInstance().getCompileDateMap();
        for (String impl : gitVersions.keySet()) {
            if("LoUIE Processor".equals(impl)) continue; //hardcoded processor skip
            versionInfo.append("  ").append(impl).append(": ")
                    .append(gitVersions.get(impl))
                    .append(" (").append(compileDates.get(impl)).append(")\n");
        }
        LOGGER.info("\n********************************************************\n"
                + "LoUIE ServiceManager Initialization - \n{}"
                + "\n********************************************************\n"
                + "{} : {}",
                versionInfo,
                LocalConstants.HOST,LocalConstants.HOSTDOMAIN);
        init = true;
         
        Identity.registerLouieIdentity();
        
         // Servers
        if(context !=null) {
            configureServers(context);
        }
        
        if (Server.LOCAL==Server.UNKNOWN) {
            return;
        }
        
        if (Server.LOCAL.isARouter()) {
            LOGGER.info("This Server started as a router.");
            configureRoutes(context);
        }

        // Services
        if(context !=null) {
            configureServices(context);
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
        
        for (ServiceFactory factory : serviceFactories) {
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
                        sb.append(" || centralized @ ").append(props.getMain());
                    }
                    if (props.isReadOnly()) {
                        sb.append(" || Read-Only");
                    }
                    sb.append("\n");
                } catch (MessageAdapterException ex) {
                    throw ex;
                } catch (Exception ex) {
                    sb.append(" - ERROR: ")
                            .append(ex.toString())
                            .append("\n");
                }
            } else {
                disabled.append(factory.getServiceName()).append("\n");
            }
        }
        
//        if (disabled.length()>0) {
//            sb.append("\nDISABLED:\n\n").append(disabled);
//        }
        
        LOGGER.info(sb.toString());
    }
    
    private static final String CONF_DIR = "/WEB-INF/conf/";
    private static final String SERVICE_PROPERTIES = "services";
    private static final String SERVER_PROPERTIES = "servers";
    private static final String ROUTE_PROPERTIES = "routing";
    
    private static void configureServers(ServletContext context) {
        Properties serverProps = loadProperties(context,CONF_DIR,SERVER_PROPERTIES);
        Server.setDefaultGateway(context.getContextPath().replaceFirst("/", ""));
        Server.processServerProperties(serverProps);
    }
    
    private static void configureServices(ServletContext context) {
        Properties serviceProps = loadProperties(context,CONF_DIR,SERVICE_PROPERTIES);
        ServiceProperties.processServiceProperties(serviceProps);
    }
    
    private static void configureRoutes(ServletContext context) {
        Properties routeProps = loadProperties(context, CONF_DIR, ROUTE_PROPERTIES);
        Route.initialize(routeProps);
    }
    
    private static Properties loadProperties(ServletContext context,String dir,String propFile) {
        Properties props = new Properties();
        InputStream in = null;
        try {
            File file = new File(context.getRealPath(dir + propFile+"."+LocalConstants.HOST));
            if (!file.exists()) {
                file = new File(context.getRealPath(dir + propFile));
            }
            in = new FileInputStream(file);
            props.load(in);
        } catch (IOException ex) {
            LoggerFactory.getLogger(ServiceManager.class).warn(ex.getMessage());
            return props;
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
        Service service = servicesByName.get(serviceName);
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

}
