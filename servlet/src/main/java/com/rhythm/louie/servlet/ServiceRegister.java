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
import javax.servlet.annotation.WebListener;

import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * @author cjohnson
 */
@WebListener()
public class ServiceRegister implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
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
    
}