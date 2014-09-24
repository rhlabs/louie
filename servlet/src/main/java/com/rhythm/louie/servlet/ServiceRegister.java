/*
 * ServiceRegister.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.servlet;


import com.rhythm.louie.ServiceManager;
import com.rhythm.louie.service.ServiceFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * @author cjohnson
 */
public abstract class ServiceRegister implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            
            // Init services
            for (ServiceFactory factory : loadFactories()) {
                ServiceManager.addService(factory);
            }

            try {
                Class<?> c = Class.forName("com.rhythm.swagr.SwagrServiceFactory");
                @SuppressWarnings("unchecked")
                Method factoryMethod = c.getDeclaredMethod("getInstance");
                ServiceFactory swagrFactory = (ServiceFactory) factoryMethod.invoke(c);
                ServiceManager.addService(swagrFactory);
            } catch (Exception ex){
                //LoggerFactory.getLogger(ServiceRegister.class.getName()).error("SWAGr Service was not loaded");
            }
            
            ServiceManager.initialize(sce.getServletContext());
        } catch (Exception ex) {
            Logger.getLogger(ServiceRegister.class.getName()).log(Level.SEVERE, 
                    "ERROR Initializing Services", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServiceManager.shutdown();
    }
    
    abstract public Collection<ServiceFactory> loadFactories();
}