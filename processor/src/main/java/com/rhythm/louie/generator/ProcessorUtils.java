/*
 * ProcessorUtils.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;


/**
 *
 * @author cjohnson
 */
public class ProcessorUtils {
    public static final Set<String> IMPORT_EXCLUDES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                "com.rhythm.pb.command.ServiceFacade",
                "com.rhythm.pb.command.CommandDescriptor"
                )));
    public static final Set<String> IMPORT_INCLUDES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                "java.util.List"
                )));
    
    public static Set<String> getImports(ServiceInfo info, Collection<String> imports) {
        Set<String> empty = Collections.emptySet();
        return getImports(info,imports,empty);
    }
    
    public static Set<String> getImports(ServiceInfo info, Collection<String> imports, Collection<String> excludes) {
        Set<String> doneImports = new HashSet<String>();
        doneImports.addAll(IMPORT_EXCLUDES);
        doneImports.addAll(excludes);
        
        Set<String> todoImports = new TreeSet<String>();
        todoImports.addAll(IMPORT_INCLUDES);
        todoImports.addAll(info.getImports());
        todoImports.addAll(imports);
        
        todoImports.removeAll(doneImports);
        
        return todoImports;
    }
    
    public static String extractDescriptionFromJavadoc(String javadoc) {
        StringBuilder desc = new StringBuilder();
        for (String line : javadoc.split("\n")) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("@")) {
                if (desc.length() != 0) {
                    desc.append("\n");
                }
                desc.append(line);
            }
        }
        return desc.toString();
    }
    
//    public static String getStoredMethodKey(Method meth) {
//        StringBuilder propKey = new StringBuilder(meth.getName());
//        propKey.append("(");
//        int argId = 0;
//        for (Class<?> arg : meth.getParameterTypes()) {
//            if (argId++ != 0) {
//                propKey.append(",");
//            }
//            propKey.append(arg.getName());
//        }
//        propKey.append(")");
//        return propKey.toString();
//    }

//    /**
//     * Do a simplified parse on the data file to grab the meta values.
//     * Note: this is not bulletproof, but does not require more sophisticated libraries
//     * 
//     * @param cl
//     * @return 
//     */
//    public static Map<String,StoredMethodProps> readStoredProps(Class<?> cl) {
//        Map<String,StoredMethodProps> props = new HashMap<String,StoredMethodProps>();
//        
//        BufferedReader reader = null;
//        try {
//            String method = null;
//            Map<String,StringBuilder> methodValues = null;
//            StringBuilder value = null;
//            
//            InputStream is = cl.getResourceAsStream(cl.getSimpleName()+".data");
//            if (is==null) {
//                return props;
//            }
//            reader = new BufferedReader(new InputStreamReader(is));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (method==null) {
//                    String[] parts = line.split(":");
//                    method = parts[0];
//                    if (!parts[1].equals("{")) {
//                        throw new Exception("Error parsing method data file: expected {");
//                    }
//                    methodValues = new HashMap<String,StringBuilder>();
//                } else if (line.equals("}")) {
//                    if (method==null || methodValues == null) {
//                        throw new Exception("Error parsing method data file: found an unexpected }");
//                    }
//                    props.put(method, new StoredMethodProps(methodValues));
//                    method = null;
//                    methodValues = null;
//                } else if (line.matches("\\w+:")) {
//                    if (methodValues == null) {
//                        throw new Exception("Error parsing method data file: Method map not instantiated");
//                    }
//                    String[] parts = line.split(":");
//                    value = new StringBuilder(parts[1]);
//                    methodValues.put(parts[0], value);
//                } else {
//                    if (value!=null) {
//                        value.append(line);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (reader!=null) {
//                try {
//                    reader.close();
//                } catch (IOException ex) {}
//            }
//        }
//        
//        return props;
//    }
}
