/*
 * ProcessorUtils.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.util.*;


/**
 *
 * @author cjohnson
 */
public class ProcessorUtils {
    public static final Set<String> IMPORT_EXCLUDES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                "com.rhythm.pb.command.Service",
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
}
