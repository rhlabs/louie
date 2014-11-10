/*
 * AnnotatedServiceLayer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.layer;

import java.lang.annotation.Annotation;
import java.util.List;

import com.rhythm.louie.CacheDelegate;
import com.rhythm.louie.Classes;
import com.rhythm.louie.DAO;
import com.rhythm.louie.Router;

/**
 *
 * @author cjohnson
 */
public class AnnotatedServiceLayer implements ServiceLayer {
    
    public static final AnnotatedServiceLayer DAO = new AnnotatedServiceLayer(DAO.class);
    public static final AnnotatedServiceLayer ROUTER = new AnnotatedServiceLayer(Router.class);
    public static final AnnotatedServiceLayer CACHE = new AnnotatedServiceLayer(CacheDelegate.class);
    
    private final Class<? extends Annotation> annotation;
    public AnnotatedServiceLayer(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }
            
    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadLayer(Class<T> service) throws Exception {
        // search for the class tagged with the annotation
        List<Class<?>> classes = Classes.getTypesAnnotatedWith(service.getPackage().getName(), annotation);
        if (classes.size() > 1) {
            throw new Exception("Multiple @" + annotation.getSimpleName() + " classes found");
        } else if (classes.isEmpty()) {
            throw new Exception("Could not find a @" + annotation.getSimpleName() + " class");
        }
        
        Class<?> layer  = classes.iterator().next();
        return (T) layer.newInstance();
    }
}
