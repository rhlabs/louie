/*
 * ServiceInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.plugins;

import java.util.*;

import com.rhythm.louie.process.ServiceHandler;

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

    private final ServiceHandler service;

    private final List<MethodInfo> methods = new ArrayList<MethodInfo>();
    
    public ServiceInfo(Class<?> cl, String host, String gateway, Collection<MethodInfo> methods) throws Exception {
        this.host = host;
        this.gateway = gateway;
        
        this.inputFile = cl.getName().replaceAll("\\.", "\\/") + ".java";
        this.baseName = cl.getName().replaceAll(".*\\.(.*)ServiceHandler", "$1");
        this.packageDir = inputFile.replaceAll("(.*)/.*\\.java", "$1");
        this.packageName = packageDir.replaceAll("\\/", "\\.");

        this.service = cl.getAnnotation(ServiceHandler.class);
        
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
    
    public List<String> getImports() {
        return importList;
    }
    
    public List<MethodInfo> getMethodInfos() {
        return methods;
    }

    public String getInputFile() {
        return inputFile;
    }
    
    public String getBaseName() {
        return baseName;
    }
    
    public String getServiceName() {
        return service.value();
    }
}
