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
package com.rhythm.louie.clientgen;

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
