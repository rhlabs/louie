/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
       
    private final List<String> importList = new ArrayList<>();
    
    private final String packageDir;
    private final String packageName;
    private final String baseName;
    private final String inputFile;
    private final String serviceName;
    
    private final TypeElement cl;
    private final Service service;
    private final boolean factory;
    
    private final ProcessingEnvironment processingEnv;
    
    private final List<MethodInfo> methods = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    public ServiceInfo(ProcessingEnvironment processingEnv, TypeElement cl) throws Exception {
        this.processingEnv = processingEnv;
        this.cl = cl;
        
        inputFile=cl.getQualifiedName().toString().replaceAll("\\.", "\\/")+".java";
        packageDir = inputFile.replaceAll("(.*)/.*\\.java", "$1");
        packageName = packageDir.replaceAll("\\/", "\\.");
        
        service = cl.getAnnotation(Service.class);
        factory = cl.getAnnotation(NoFactory.class)==null;
        
        baseName = cl.getQualifiedName().toString().replaceAll(".*\\.(.*)Service$", "$1");
        serviceName = baseName.toLowerCase();
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
