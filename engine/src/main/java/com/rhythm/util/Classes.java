/*
 * Classes.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class Classes {
    public static List<Class<?>> getRecursiveTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        return findTypesAnnotatedWith(packageName, annotation, true);
    }

    public static List<Class<?>> getTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        return findTypesAnnotatedWith(packageName, annotation, false);
    }

    private static List<Class<?>> findTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation, boolean recursive) throws IOException {
        ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
    
        List<Class<?>> found = new ArrayList<Class<?>>();
        Set<ClassInfo> infos;
        if (recursive) {
            infos = cp.getTopLevelClassesRecursive(packageName);
        } else {
            infos = cp.getTopLevelClasses(packageName);
        }

        for (ClassInfo info : infos) {
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
