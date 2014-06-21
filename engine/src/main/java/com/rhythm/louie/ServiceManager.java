/*
 * RequestFactory.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.auth.AuthServiceFactory;
import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.jms.ActiveMQUpdate;
import com.rhythm.louie.jms.MessageHandler;
import com.rhythm.louie.jms.MessageManager;
import com.rhythm.louie.server.LouieServiceFactory;
import com.rhythm.louie.topology.Route;
import com.rhythm.louie.testservice.TestServiceFactory;

import com.rhythm.pb.command.Service;
import com.rhythm.pb.command.ServiceFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    private static MessageManager mm;
    
    private static final Set<String> reservedServices;
    private static final List<ServiceFactory> serviceFactories;
    static {
        serviceFactories = new ArrayList<ServiceFactory>();
        serviceFactories.add(LouieServiceFactory.getInstance());
        serviceFactories.add(AuthServiceFactory.getInstance());
        serviceFactories.add(TestServiceFactory.getInstance());
        
        reservedServices = new HashSet<String>();
        for (ServiceFactory factory : serviceFactories) {
            reservedServices.add(factory.getServiceName());
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
    
    public static synchronized void initialize() {
        initialize(null);
    }
    
    public static synchronized void initialize(ServletContext context) {
        if (init) return;
        
        Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
        
        StringBuilder versionInfo = new StringBuilder();
        Map<String,String> gitVersions = ExternalProperties.getInstance().getGitVersionMap();
        Map<String,String> compileDates = ExternalProperties.getInstance().getCompileDateMap();
        for (String impl : gitVersions.keySet()) {
            if("LoUIE Processor".equals(impl)) continue; //hardcoded processor skip
            versionInfo.append("  ").append(impl).append("\n");
            versionInfo.append("    Git Version: ").append(gitVersions.get(impl)).append("\n");
            versionInfo.append("    Compile Date: ").append(compileDates.get(impl)).append("\n");
        }
        LOGGER.info("\n********************************************************\n"
                + "LoUIE ServiceManager Initialization - \n{}"
                + "\n********************************************************\n"
                + "{} : {}",
                versionInfo,
                Constants.HOST,Constants.HOSTDOMAIN);
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
        mm = MessageManager.initializeMessageManager("localhost");
        try {
            ActiveMQUpdate.getInstance().initialize();
        } catch (Exception ex) {
            LOGGER.error("Error initializing JMS service", ex);
        }
        
        // Load Services
        StringBuilder disabled = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("\nServices:\n\n");
        
        for (ServiceFactory factory : serviceFactories) {
            long start = System.nanoTime();
            String serviceName = factory.getServiceName().toLowerCase();
            try {
                ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
                boolean enabled = initializeService(factory);
                if (enabled) {
                    sb.append(serviceName);
                    if (props.isCentralized()) {
                        sb.append(" || centralized @ ").append(props.getMain());
                    }
                    if (props.isReadOnly()) {
                        sb.append(" || Read-Only");
                    }
                    sb.append("  (");
                    long time = (System.nanoTime()-start) / 1000000;
                    sb.append(time);
                    sb.append(" ms)\n");
                } else {
                    disabled.append(factory.getServiceName()).append("\n");
                }
            } catch (Exception ex) {
                sb.append(serviceName)
                        .append(" - ERROR: ")
                        .append(ex.toString())
                        .append("\n");
            }
        }
        
        if (disabled.length()>0) {
            sb.append("\nDISABLED:\n\n").append(disabled);
        }
        
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
            File file = new File(context.getRealPath(dir + propFile+"."+Constants.HOST));
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
    
    private static boolean initializeService(ServiceFactory factory) throws Exception {
        String serviceName = factory.getServiceName();
        ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
        
        if (!isServiceReserved(serviceName) && !props.isEnabled()) {
            return false;
        }
        
        Service service = factory.getService();
        service.initialize();
        MessageHandler mh = service.getMessageHandler();
        if (mh!=null) {
            mm.listenToQueue("Server.current." + service.getServiceName() + "Update");
            mm.addMessageHandler(mh);
        }
        
        servicesByName.put(service.getServiceName(), service);
        return true;
    }

    public static synchronized void shutdown() {
        Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
        
        LOGGER.info("RequestFactory - Shutdown");
        
        if (mm!=null) {
            mm.shutdown();
            mm=null;
        }
        
        for (Service service : getServices()) {
            try {
                service.shutdown();
            } catch (Exception ex) {
                LOGGER.error("Error shutting down service",ex);
            }
        }
        LOGGER.info("All services shutdown");
        
        ActiveMQUpdate.getInstance().shutdown();
        LOGGER.info("ActiveMQUpdate shutdown");

        TaskScheduler.getInstance().shutdown();
        LOGGER.info("TaskScheduler shutdown");
        
        EmailService.getInstance().shutdown();
        LOGGER.info("EmailService shutdown");
        
        CacheManager.shutdown();
        LOGGER.info("CacheManager shutdown");
        
        servicesByName.clear();
        LOGGER.info("All services cleared");
    }

    public static Service getService(String serviceName) throws Exception {
        Service service = servicesByName.get(serviceName);
        if (service == null) {
            throw new Exception("No such service: "+serviceName);
        }
        return service;
    }

    public static Collection<Service> getServices() {
        return Collections.unmodifiableCollection(servicesByName.values());
    }
    
    public static Collection<String> getServiceNames() {
        return Collections.unmodifiableCollection(servicesByName.keySet());
    }

}
