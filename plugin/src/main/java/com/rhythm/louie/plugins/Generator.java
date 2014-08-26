/*  
 * Generator.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.plugins;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.Constants;
import com.rhythm.louie.process.Disabled;
import com.rhythm.louie.process.Private;
import com.rhythm.louie.process.ServiceCall;
import com.rhythm.louie.process.ServiceHandler;

import com.rhythm.util.Classes;
import java.net.URLClassLoader;

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
        String gateway = Constants.DEFAULT_GATEWAY;
        List<String> prefix = new ArrayList<String>();
        
        if (args.length>0 && args[0] != null) {
            host = args[0];
        }
        if (args.length>1 && args[1] != null) {
            gateway = args[1];
        }
        
        if (args.length>2 && args[2] != null) {
            prefix.add(args[2]);
        }
        exec(host,gateway,prefix,null,null);

    }
    
    public static void exec(String host, String gateway, List<String> prefix, List<String> whitelist, List<String> blacklist) {
//        if (blacklist != null) {
//            for (String bl : blacklist) {
//                System.out.println("Bl contains: " + bl);
//            }
//        } else {
//            System.out.println("Blacklist null");
//        }
//        
//        if (whitelist != null) {
//            for (String bl : whitelist) {
//                System.out.println("wl contains: " + bl);
//            }
//        } else {
//            System.out.println("Whitelist null");
//        }
        
        List<Class<?>> services;
        
        try {
            services = Classes.getAnnotatedSpecialized(prefix, ServiceHandler.class, whitelist, blacklist);
        } catch (IOException e) {
            LoggerFactory.getLogger(Generator.class).error("Error Finding Service Handlers", e);
            return;
        }
        for (Class<?> service : services) {
            List<MethodInfo> perlMethods = new ArrayList<MethodInfo>();
            List<MethodInfo> pythonMethods = new ArrayList<MethodInfo>();
            try {
                for (Method method : service.getMethods()) {
                    if (!Modifier.isStatic(method.getModifiers())
                                && Modifier.isPublic(method.getModifiers())
                                && method.isAnnotationPresent(ServiceCall.class)
                                && !method.isAnnotationPresent(Private.class)
                                && !method.isAnnotationPresent(Disabled.class)) {
                        perlMethods.add(new PerlMethodInfo(method));
                        pythonMethods.add(new PythonMethodInfo(method));
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(Generator.class).error("Error Processing Service Methods", e);
            }
            
            // Perl
            try {
                Collections.sort(perlMethods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, perlMethods);
                generatePerl(info);
            } catch (Exception e) {
                LoggerFactory.getLogger(Generator.class).error("Error Generating Perl Clients", e);
            }
            
            // Python
            try {
                Collections.sort(pythonMethods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, pythonMethods);
                generatePython(info);
            } catch (Exception e) {
                LoggerFactory.getLogger(Generator.class).error("Error Generating Python Clients", e);
            }
        }
    }
    
    private static void printServiceInfo(ServiceInfo info) {
        System.out.println(info.getInputFile()+ " "+info.getServiceName());
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
