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

import com.rhythm.louie.process.ServiceFacade;

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
    private final ServiceFacade service;
    
    ProcessingEnvironment processingEnv;
    
    List<MethodInfo> methods = new ArrayList<MethodInfo>();
    
    @SuppressWarnings("unchecked")
    public ServiceInfo(ProcessingEnvironment processingEnv, TypeElement cl) throws Exception {
        
        this.inputFile=cl.getQualifiedName().toString().replaceAll("\\.", "\\/")+".java";
        this.baseName = cl.getQualifiedName().toString().replaceAll(".*\\.(.*)Service", "$1");
        this.packageDir = inputFile.replaceAll("(.*)/.*\\.java", "$1");
        this.packageName = packageDir.replaceAll("\\/", "\\.");
        this.cl = cl;
        this.processingEnv = processingEnv;
        
        service = cl.getAnnotation(ServiceFacade.class);
        
        serviceName = !service.name().isEmpty()?service.name():baseName.toLowerCase();
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
    
    public ServiceFacade getServiceFacade() {
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
}
