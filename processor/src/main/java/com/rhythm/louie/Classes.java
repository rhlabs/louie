/*
 * Classes.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class Classes {
    
    public static List<Class<?>> getAnnotatedSpecialized(List<String> prefixes, 
            Class<? extends Annotation> annotation, List<String> whitelist, 
            List<String> blacklist) throws IOException {
        return findTypesAnnotatedWith(prefixes, annotation, true, whitelist, blacklist);
    }
    
    public static List<Class<?>> getRecursiveTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        List<String> pckgs = new ArrayList<String>();
        pckgs.add(packageName);
        return findTypesAnnotatedWith(pckgs, annotation, true, null, null);
    }

    public static List<Class<?>> getTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        List<String> pckgs = new ArrayList<String>();
        pckgs.add(packageName);
        return findTypesAnnotatedWith(pckgs, annotation, false, null, null);
    }

    private static List<Class<?>> findTypesAnnotatedWith(List<String> packages, Class<? extends Annotation> annotation, boolean recursive, List<String> whitelist, List<String> blacklist) throws IOException {
        ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
        
        List<Class<?>> found = new ArrayList<Class<?>>();
        Set<ClassInfo> infos = new HashSet<ClassInfo>();
        if (packages == null || packages.isEmpty()) {
            infos = cp.getTopLevelClasses();
        } else if (recursive) {
            for (String pkg : packages) {
                infos.addAll(cp.getTopLevelClassesRecursive(pkg));
            }
        } else {
            for (String pkg : packages) {
                infos.addAll(cp.getTopLevelClasses(pkg));
            }
        }
        
        for (ClassInfo info : infos) {
            if (blacklist != null && blacklist.contains(info.getPackageName())) {
                if (whitelist != null) {
                    if (!whitelist.contains(info.getName())) {
                        continue; //blacklisted, but not whitelisted
                    }
                } else {
                    continue;
                }
            }
            try {
                Class<?> cl = info.load();
                Annotation ann = cl.getAnnotation(annotation);
                if (ann != null) {
                    found.add(cl);
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(Classes.class).warn("Error Checking Class: "+info.getName(),e);
            }
        }
        
        return found;
    }
}
