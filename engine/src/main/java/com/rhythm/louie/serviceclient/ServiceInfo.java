/*
 * ServiceInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.serviceclient;

import java.util.*;

import com.rhythm.louie.process.ServiceFacade;

/**
 *
 * @author sfong
 */
public class ServiceInfo {
    private final String host;
    private final String gateway;
    
    private final List<String> importList = new ArrayList<String>();
    
    private final String packageDir;
    private final String packageName;
    private final String baseName;
    private final String inputFile;

    private final ServiceFacade service;

    private final Set<MethodInfo> methods = new TreeSet<MethodInfo>();
    
    public ServiceInfo(Class<?> cl, String host, String gateway, Collection<MethodInfo> methods) throws Exception {
        this.host = host;
        this.gateway = gateway;
        
        this.inputFile = cl.getName().replaceAll("\\.", "\\/") + ".java";
        this.baseName = cl.getName().replaceAll(".*\\.(.*)Service", "$1");
        this.packageDir = inputFile.replaceAll("(.*)/.*\\.java", "$1");
        this.packageName = packageDir.replaceAll("\\/", "\\.");

        this.service = cl.getAnnotation(ServiceFacade.class);

        this.methods.addAll(methods);
    }
    
    public String getHost() {
        return host;
    }
    
    public String getGateway() {
        return gateway;
    }

    public String getPackageName() {
        return packageName;
    }
    
    public String getPackageDir() {
        return packageDir;
    }
    
    public ServiceFacade getServiceFacade() {
        return service;
    }
    
    public List<String> getImports() {
        return importList;
    }
    
    public Set<MethodInfo> getMethodInfos() {
        return methods;
    }

    public String getInputFile() {
        return inputFile;
    }
    
    public String getBaseName() {
        return baseName;
    }
    
    public String getServiceName() {
        return baseName.toLowerCase();
    }
}
