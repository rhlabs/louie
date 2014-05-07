/*
 * Utils.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.tools.JavaFileObject;

import org.joda.time.LocalDate;

/**
 *
 * @author cjohnson
 */
public class Utils {
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
    
    public static String generateHeader(String className, ServiceInfo info, Collection<String> imports) {
        Set<String> empty = Collections.emptySet();
        return generateHeader(className,info,imports,empty);
    }
    public static String generateHeader(String className, ServiceInfo info, Collection<String> imports, Collection<String> excludes) {
        StringBuilder buffer = new StringBuilder();
        
        int year = new LocalDate().getYear();
        buffer.append("/*** GENERATED FROM ").append(info.getInputFile()).append(" - DO NOT EDIT  ***/\n\n");
        
        buffer.append("/*\n");
        buffer.append(" * ").append(className).append(".java\n");
        buffer.append(" *\n");
        buffer.append(" * Copyright (c) ").append(year).append(" Rhythm & Hues Studios. All rights reserved.\n");
        buffer.append(" */\n");
        
        buffer.append("package ").append(info.getPackageName()).append(";\n\n");
        
        for (String i : getImports(info,imports,excludes)) {
            buffer.append("import ").append(i).append(";\n");
        }
        
        return buffer.toString();
    }
    
    public static JavaFileObject createSourceFile(String className,ServiceInfo info) throws Exception {
        return info.getProcessingEnv().getFiler().
                createSourceFile(info.getPackageName()+"."+className,info.getTypeElement().getEnclosingElement());
    }
    
    // Thanks to http://dzone.com/snippets/get-all-classes-within-package
    
    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class<?>[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory The base directory
     * @param packageName The package name for classes found inside the base
     * directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String classname=packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(classname,false,Thread.currentThread().getContextClassLoader()));
            }
        }
        return classes;
    }
}
