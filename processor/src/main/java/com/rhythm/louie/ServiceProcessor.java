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
package com.rhythm.louie;

import java.io.*;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

import com.rhythm.louie.generator.*;

import com.rhythm.louie.process.ServiceHandler;

import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 *
 * @author cjohnson
 */
@SupportedAnnotationTypes("com.rhythm.louie.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ServiceProcessor extends AbstractProcessor {
    final Set<String> RESERVED = new HashSet<>();    
    public ServiceProcessor() {
        // Load list of RESERVED words
        try {
            InputStream is = getClass().getResourceAsStream("/reserved_words");
            if (is == null) {
                throw new Exception("Reserved List Not Found!");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line=br.readLine())!=null) {
                RESERVED.add(line.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ProcessingEnvironment pe) {
        super.init(pe);
        processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,"\n"+
                        "################################################\n"+
                        "#############  SERVICE PROCESSING  #############\n"+
                        "################################################\n");
    }

    public static final String SERVICE_PROVIDER_FILE = "META-INF/resources/louie-serviceproviders";
    public static final String SERVICE_HANDLER_FILE = "META-INF/resources/louie-servicehandlers";
    
    private StringBuilder serviceProviders = new StringBuilder();
    private StringBuilder serviceHandlers = new StringBuilder();
    
    @Override
    public boolean process(Set annotations,
            RoundEnvironment roundEnv) {
        Types types = processingEnv.getTypeUtils();
        
        for (Element e : roundEnv.getElementsAnnotatedWith(Service.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "Service must be an Interface!", e);
                continue;
            }
            
            TypeElement cl = (TypeElement) types.asElement(e.asType());
            
            try {
                ServiceInfo info = processService(cl, processingEnv);
                Generator.generate(info);
            } catch (Exception exc) {
                processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Error Processing Class: "+cl.getQualifiedName()+"\n"
                        + exc.toString(), e);
                exc.printStackTrace();
            }
        }
        
        for (Element e : roundEnv.getElementsAnnotatedWith(ServiceHandler.class)) {
            TypeElement cl = (TypeElement) types.asElement(e.asType());
            serviceHandlers.append(cl.getQualifiedName().toString()).append("\n");
        }
        if (roundEnv.processingOver() && serviceHandlers.length() > 0 ) {
            writeServiceClassProps(serviceHandlers, processingEnv, SERVICE_HANDLER_FILE);
        }
        
        for (Element e : roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
            TypeElement cl = (TypeElement) types.asElement(e.asType());
            serviceProviders.append(cl.getQualifiedName().toString()).append("\n");
        }
        if (roundEnv.processingOver() && serviceProviders.length() > 0 ) {
            writeServiceClassProps(serviceProviders, processingEnv, SERVICE_PROVIDER_FILE);
        }
        
        return true;
    }
    
    private void writeServiceClassProps(StringBuilder services, ProcessingEnvironment env, String file) {
        try {
            FileObject jfo = env.getFiler().createResource(CLASS_OUTPUT, "", file);
            try (Writer writer = jfo.openWriter()) {
                writer.append(services.toString());
            }
        } catch (IOException ex) {
            env.getMessager().printMessage(Diagnostic.Kind.WARNING, "The " + file 
                    + " file was not appended to correctly while trying to write " 
                    + services.toString() +"\n" + ex.toString());
        }
    }
    
    private ServiceInfo processService(TypeElement cl, ProcessingEnvironment processingEnv) throws Exception {
        ServiceInfo info = new ServiceInfo(processingEnv, cl);
        
        Types types = processingEnv.getTypeUtils();
        
        Map<String, List<String>> methods = new HashMap<>();
        
        for (Element e : cl.getEnclosedElements()) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            
            info.addMethod(new MethodInfo(processingEnv, (ExecutableElement) e));
            
            String methodName = e.getSimpleName().toString();
            List<String> methodParams = methods.get(methodName);
            if (methodParams == null) {
                methodParams = new ArrayList<>();
                methods.put(methodName, methodParams);
            }

            try {
                StringBuilder params = new StringBuilder();
                ExecutableElement meth = ExecutableElement.class.cast(e);
                
                if (e.getAnnotation(Internal.class)!=null) {
                    continue;
                }
            
                TypeMirror returnType = meth.getReturnType();
                if (TypeUtils.instanceOf(types,returnType,Collection.class)) {
                    if (TypeUtils.instanceOf(types,returnType, Map.class)) {
                        throw new Exception("Return Type cannot be a Map!");
                    }
                    
                    if (!(returnType instanceof DeclaredType)) {
                        throw new Exception("Return Type Collection is not a DeclaredType!");
                    }
                    DeclaredType decType = (DeclaredType) returnType;
                    if (decType.getTypeArguments().size() != 1) {
                        throw new Exception("Return Type must have one and only one Parameter type");
                    }
                    TypeMirror returnParamType =  decType.getTypeArguments().get(0);
                    if (!isValidType(types,returnParamType)) {
                         throw new Exception("Return Type Parameter is not valid!  Must be a GeneratedMessage or be a supported dataType");
                    }
                } else if (!isValidType(types,returnType)) {
                    throw new Exception("Return Type is not a valid type!  Must be a GeneratedMessage or be a supported dataType");
                } 
                
                for (VariableElement param : meth.getParameters()) {
                    if (params.length() > 0) {
                        params.append(",");
                    }
                    String paramName = param.getSimpleName().toString();
                    if (RESERVED.contains(paramName)) {
                        throw new Exception("Argument is a reserved word!");
                    }
                    
                    params.append(abbreviatedName(param.asType()));
                    if (!isValidType(types, param.asType())) {
                        throw new Exception("Argument :"+param.asType()+" is not a valid type!  Must be a GeneratedMessage or be a supported dataType");
                    }
                    
//                    if (argcl == null) {
//                        throw new Exception("Argument is not a GeneratedMessage: Not a Declared Type");
//                    }
//                    if (argcl.getSuperclass() == null) {
//                        throw new Exception("Argument is not a GeneratedMessage: No Superclass");
//                    }
//                    if (argcl.getSuperclass() instanceof NoType) {
//                        throw new Exception("Argument is not a GeneratedMessage: No Type");
//                    }
//                    TypeElement sup = (TypeElement) types.asElement(argcl.getSuperclass());
//                    if (!sup.toString().equals("com.google.protobuf.GeneratedMessage")) {
//                        throw new Exception("Argument is not a GeneratedMessage");
//                    }
                }
                
                Deprecated dep = e.getAnnotation(Deprecated.class);
                methodParams.add(params.toString()  +(dep!=null?" (Deprecated)":""));
            } catch (Exception exc) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Error Processing Method: " + e.getSimpleName().toString() + "\n"
                        + exc.getMessage(), e);
            }
        }
        for (Map.Entry<String, List<String>> method : methods.entrySet()) {
            if (method.getValue().size() > 1) {
                
                StringBuilder message = new StringBuilder();
                message.append(cl.getSimpleName()).append(".").append(method.getKey())
                        .append(":  Polymorphic methods are not supported!");
                for (String meth : method.getValue()) {
                    message.append("\n        ").append(meth);
                }
                
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        message.toString());
            }
        }
        
        return info;
    }
    
    private String abbreviatedName(TypeMirror type) {
        String name = type.toString();
        
        String typeArgs = name.replaceFirst(".*<(.*)>", "$1");
        if (name.equals(typeArgs)) {
            typeArgs = "";
        } else {
            name = name.replaceFirst("(.*)<.*>", "$1");
        }
        name = name.replaceFirst(".*\\.(.*)", "$1");
        
        if (!typeArgs.isEmpty()) {
            name+="<"+typeArgs.replaceFirst(".*\\.(.*)","$1")+">";
        }
        return name;
    }
    
    public boolean isValidType(Types types, TypeMirror type) {
        if (TypeUtils.instanceOf(types,type,"com.google.protobuf.GeneratedMessage")) {
            return true;
        }
        
        return TypeUtils.hasConversionToPB(type.toString());
    }
}
