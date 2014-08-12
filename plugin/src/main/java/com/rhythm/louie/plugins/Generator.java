/*
 * Generator.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.plugins;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.reflections.Reflections;

/**
 *
 * @author sfong
 */
public class Generator {
    private static final String PERL_TEMPLATE = "templates/perl/ServiceClient.vm";
    private static final String PYTHON_TEMPLATE = "templates/python/ServiceClient.vm";
    private static final String PERL_OUTPUT_PATH = "target/generated-sources/perl/RH/Louie/";
    private static final String PYTHON_OUTPUT_PATH = "target/generated-sources/python/rh/louie/";
    private static final String PERL_CLIENT_MODULE = "Client.pm";
    private static final String PYTHON_CLIENT_MODULE = "client.py";
    
    public static void main(String[] args) {
        String host = "localhost";
        String gateway = "louie";
        String prefix = null;
        
        if (args.length>0 && args[0] != null) {
            host = args[0];
        }
        if (args.length>1 && args[1] != null) {
            gateway = args[1];
        }
        
        if (args.length>2 && args[2] != null) {
            prefix = args[2];
        }
            
        Reflections reflections = new Reflections(prefix);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(com.rhythm.louie.process.ServiceFacade.class);
        
        // Perl
        for (Class<?> service : services) {
            if (!service.isInterface()) {
                continue;
            }

            try {
                List<MethodInfo> methods = new ArrayList<MethodInfo>();
                for (Method method : service.getMethods()) {
                    methods.add(new PerlMethodInfo(method));
                }
                Collections.sort(methods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, methods);
                generatePerl(info);
            } catch (Exception e) {
                Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        
        // Python
        for (Class<?> service : services) {
            if (!service.isInterface()) {
                continue;
            }

            try {
                List<MethodInfo> methods = new ArrayList<MethodInfo>();
                for (Method method : service.getMethods()) {
                    methods.add(new PythonMethodInfo(method));
                }
                Collections.sort(methods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, methods);
                generatePython(info);
            } catch (Exception e) {
                Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private static void printServiceInfo(ServiceInfo info) {
        System.out.print(info.getInputFile());
        if (!info.getServiceFacade().factory()) {
            System.out.print(" (NO FACTORY)");
        }
        System.out.println();
    }
    
    public static void generatePerl(ServiceInfo info) throws Exception {
        printServiceInfo(info);
        String output = PERL_OUTPUT_PATH + info.getBaseName() + "/" + PERL_CLIENT_MODULE;
        processTemplate(info, PERL_TEMPLATE, output);
    }

    public static void generatePython(ServiceInfo info) throws Exception {
        printServiceInfo(info);
        String output = PYTHON_OUTPUT_PATH + info.getServiceName() + "/" + PYTHON_CLIENT_MODULE;
        processTemplate(info, PYTHON_TEMPLATE, output);
    }
    
    public static void processTemplate(ServiceInfo info, String template, String output) throws Exception {
        Properties props = new Properties();
        URL url = Generator.class.getClassLoader().getResource("config/velocity.properties");
        props.load(url.openStream());

        VelocityEngine ve = new VelocityEngine(props);
        ve.init();

        VelocityContext vc = new VelocityContext();
        vc.put("info", info);
        vc.put("baseName", info.getBaseName());
        vc.put("serviceName", info.getServiceName());

        Template vt = ve.getTemplate(template);
        File f = new File(output);
        f.getParentFile().mkdirs();
        Writer writer = new PrintWriter(f);
        vt.merge(vc, writer);
        writer.close();
    }
}
