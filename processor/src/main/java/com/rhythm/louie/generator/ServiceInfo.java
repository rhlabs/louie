/*
 * ServiceInfo.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import com.rhythm.louie.NoFactory;
import com.rhythm.louie.Service;

/**
 *
 * @author cjohnson
 */
public class ServiceInfo {
       
    private final List<String> importList = new ArrayList<String>();
    
    private final String packageDir;
    private final String packageName;
    private final String baseName;
    private final String inputFile;
    private final String serviceName;
    
    private final TypeElement cl;
    private final Service service;
    private boolean factory;
    
    ProcessingEnvironment processingEnv;
    
    List<MethodInfo> methods = new ArrayList<MethodInfo>();
    
    @SuppressWarnings("unchecked")
    public ServiceInfo(ProcessingEnvironment processingEnv, TypeElement cl) throws Exception {
        this.processingEnv = processingEnv;
        this.cl = cl;
        
        inputFile=cl.getQualifiedName().toString().replaceAll("\\.", "\\/")+".java";
        packageDir = inputFile.replaceAll("(.*)/.*\\.java", "$1");
        packageName = packageDir.replaceAll("\\/", "\\.");
        
        service = cl.getAnnotation(Service.class);
        factory = cl.getAnnotation(NoFactory.class)==null;
        
        if (service.value().isEmpty()) {
            baseName = cl.getQualifiedName().toString().replaceAll(".*\\.(.*)Service", "$1");
            serviceName = baseName.toLowerCase();
        } else {
            serviceName = service.value();
            baseName = Character.toUpperCase(serviceName.charAt(0)) + serviceName.substring(1);
        }
    }
    
    public void addMethod(MethodInfo meth) {
        methods.add(meth);
    }
    
    public String getServiceClassName() {
        return cl.getSimpleName().toString();
    }
    
    public TypeElement getTypeElement() {
        return cl;
    }
    
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getPackageDir() {
        return packageDir;
    }
    
    public Service getService() {
        return service;
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
        return serviceName;
    }
    
    public boolean createFactory() {
        return factory;
    }
}
