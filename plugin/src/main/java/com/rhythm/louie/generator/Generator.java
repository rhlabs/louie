/*  
 * Generator.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;


import com.rhythm.louie.Constants;
import com.rhythm.louie.process.Disabled;
import com.rhythm.louie.process.Internal;
import com.rhythm.louie.process.ServiceCall;
import com.rhythm.louie.process.ServiceHandler;
import com.rhythm.util.Classes;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

@Deprecated
/**
 *
 * @author sfong
 */
public class Generator {
    private static final String PYTHON_TEMPLATE = "templates/python/ServiceClient.vm";
    private static String PYTHON_OUTPUT_PATH;// = "target/generated-sources/python/";   //configurable
    private static final String PYTHON_CLIENT_MODULE = "client.py";
    
    private static final String AUTH_SERVICE = "auth";
    private static final String LOUIE_SERVICE = "louie";
    private static final String TEST_SERVICE = "test";
    
    public static void exec(String host, String gateway, List<String> prefix, List<String> whitelist, List<String> blacklist, String pypackage, String pythondir) {
        
        List<Class<?>> services;
        
        //sanitize python output dir
        if (pythondir.startsWith("/")) pythondir = pythondir.substring(1);
        if (!pythondir.endsWith("/")) pythondir = pythondir + "/";
        PYTHON_OUTPUT_PATH = pythondir;
        
        // sanitize pypath
        pypackage = pypackage.replaceAll("\\.", "\\/");
        if (pypackage.startsWith("/")) pypackage = pypackage.substring(1);
        if (!pypackage.endsWith("/")) pypackage = pypackage + "/";
        
        try {
            services = Classes.getAnnotatedSpecialized(prefix, ServiceHandler.class, whitelist, blacklist);
        } catch (IOException e) {
            LoggerFactory.getLogger(Generator.class).error("Error Finding Service Handlers", e);
            return;
        }
        for (Class<?> service : services) {
            List<MethodInfo> pythonMethods = new ArrayList<MethodInfo>();
            try {
                for (Method method : service.getMethods()) {
                    if (!Modifier.isStatic(method.getModifiers())
                                && Modifier.isPublic(method.getModifiers())
                                && method.isAnnotationPresent(ServiceCall.class)
                                && !method.isAnnotationPresent(Internal.class)
                                && !method.isAnnotationPresent(Disabled.class)) {
                        pythonMethods.add(new PythonMethodInfo(method));
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(Generator.class).error("Error Processing Service Methods", e);
            }
            
            // Python
            try {
                Collections.sort(pythonMethods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, pythonMethods);
                generatePython(info, pypackage);
            } catch (Exception e) {
                LoggerFactory.getLogger(Generator.class).error("Error Generating Python Clients", e);
            }
        }
    }
    
    private static void printServiceInfo(ServiceInfo info) {
        System.out.println(info.getInputFile()+ " "+info.getServiceName());
    }
    
    public static void createPythonBundle(String projectroot, List<String> siblingModules) {
        //use projectroot to locate other branch of stuffs
        for (String sibling : siblingModules) {
            System.out.println("looking at: " + projectroot +"/"+ sibling +"/"+ PYTHON_OUTPUT_PATH);
        }
//        copyFiles 
    }
    
    private static void copyFiles(String originroot, String destroot) throws IOException {
        //copy all files (recursively) from originroot to destroot
        FileUtils.copyDirectory(new File(originroot), new File(destroot), true);
    }
    
    public static void generatePython(ServiceInfo info, String pypath) throws Exception {
        printServiceInfo(info);
        String serviceName = info.getServiceName().toLowerCase();
        StringBuilder output = new StringBuilder();
        output.append(PYTHON_OUTPUT_PATH);
        //special case auth, louie, and test
        if (LOUIE_SERVICE.equals(serviceName) || AUTH_SERVICE.equals(serviceName) || TEST_SERVICE.equals(serviceName)) {
            output.append("louie/");
        } else {
            output.append(pypath);
        }
        output.append(serviceName).append("/");
        Path file = Paths.get(output.toString(), "__init__.py");
        output.append(PYTHON_CLIENT_MODULE);
        processTemplate(info, PYTHON_TEMPLATE, output.toString());
        
        //generate some __init__ files
        try {
            Files.createFile(file);
        } catch (FileAlreadyExistsException ex) {
        } catch (IOException ex) {
            System.out.println("Failed to create __init__.py file in " + output);
        }
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
