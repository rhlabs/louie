/*
 * ServiceProcessor.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights RESERVED.
 */
package com.rhythm.louie.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.rhythm.louie.generator.Generator;
import com.rhythm.louie.generator.ServiceInfo;

/**
 *
 * @author cjohnson
 */
@SupportedAnnotationTypes("com.rhythm.louie.process.ServiceFacade")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ServiceProcessor extends AbstractProcessor {
    final Set<String> RESERVED = new HashSet<String>();    
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

    @Override
    public boolean process(Set annotations,
            RoundEnvironment roundEnv) {
        
        for (Element e : roundEnv.getElementsAnnotatedWith(ServiceFacade.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "Service must be an Interface!", e);
                continue;
            }
            
            Types types = processingEnv.getTypeUtils();
            TypeElement cl = (TypeElement) types.asElement(e.asType());
            
            try {
                ServiceInfo info = new ServiceInfo(processingEnv,cl);
                Generator.generate(info);
            } catch (Exception exc) {
                processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Error Processing Class: "+cl.getQualifiedName()+"\n"
                        + exc.toString(), e);
            }
            
            try {
                checkCommands(cl);
            } catch (Exception exc) {
                processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Error Checking Commands: "+cl.getQualifiedName()+"\n"
                        + exc.toString(), e);
            }
        }
        return true;
    }
    
    private void checkCommands(TypeElement cl) throws Exception {
        Map<String, List<String>> methods = new HashMap<String, List<String>>();

        for (Element e : cl.getEnclosedElements()) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            CommandDescriptor command = e.getAnnotation(CommandDescriptor.class);
            if (command==null) {
                continue;
            }
            
            String methodName = e.getSimpleName().toString();
            List<String> methodParams = methods.get(methodName);
            if (methodParams == null) {
                methodParams = new ArrayList<String>();
                methods.put(methodName, methodParams);
            }

            try {
                StringBuilder params = new StringBuilder();
                ExecutableElement meth = ExecutableElement.class.cast(e);
                Types types = processingEnv.getTypeUtils();
                
                // special check if for no parameters, since args gets populated with 1 blank element always
                if (meth.getParameters().size()!=command.args().length && 
                    (!(meth.getParameters().isEmpty() && command.args().length==1 && command.args()[0].isEmpty()))) {
                    throw new Exception("Descriptor arg count does not much parameter count");
                }
                
                int index = 0;
                for (VariableElement param : meth.getParameters()) {
                    if (params.length() > 0) {
                        params.append(",");
                    }
                    String paramName = param.getSimpleName().toString();
                    if (RESERVED.contains(paramName)) {
                        throw new Exception("Argument is a reserved word!");
                    }
                    
                    String argName = command.args()[index++];
                    if (!paramName.equals(argName)) {
                        throw new Exception("Argument name does not match param name!");
                    }
                    
                    TypeElement argcl = (TypeElement) types.asElement(param.asType());
                    String[] paramParts = argcl.getQualifiedName().toString().split("\\.");
                    params.append(paramParts[paramParts.length-1]);
                    
                    if (argcl == null) {
                        throw new Exception("Argument is not a GeneratedMessage: Not a Declared Type");
                    }
                    if (argcl.getSuperclass() == null) {
                        throw new Exception("Argument is not a GeneratedMessage: No Superclass");
                    }
                    if (argcl.getSuperclass() instanceof NoType) {
                        throw new Exception("Argument is not a GeneratedMessage: No Type");
                    }
                    TypeElement sup = (TypeElement) types.asElement(argcl.getSuperclass());
                    if (!sup.toString().equals("com.google.protobuf.GeneratedMessage")) {
                        throw new Exception("Argument is not a GeneratedMessage");
                    }
                }
                
                Deprecated dep = e.getAnnotation(Deprecated.class);
                methodParams.add(params.toString()  +(dep!=null?" (Deprecated)":""));
            } catch (Exception exc) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Error Processing Method: " + e.getSimpleName().toString() + "\n"
                        + exc.toString(), e);
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
    }
}
