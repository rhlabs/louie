/*
 * ServiceUtils.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import com.rhythm.louie.Classes;
import com.rhythm.louie.Delegate;
import com.rhythm.louie.service.layer.AnnotatedServiceLayer;
import com.rhythm.louie.service.layer.ServiceLayer;

/**
 *
 * @author cjohnson
 */
public class ServiceUtils {
    /**
     * Determines the class in the service hierarchy that is tagged with @Service
     * Returns the basename of the class, ie "Service" stripped off the end
     * 
     * Note: method is fairly expensive, as it is crawling the class hierarchy 
     * If you need the value often, it is recommended to cache the the value
     * 
     * @param cl
     * @return 
     */
    public static String getServiceBaseName(Class<?> cl) {
        Class annCl = Classes.findAnnotatedClass(cl, com.rhythm.louie.Service.class);
        if (annCl==null) {
            return "Unknown";
        }
        return annCl.getName().replaceAll(".*\\.(.*)Service$", "$1");
    }
    
    /**
     * Determines the class in the service hierarchy that is tagged with @Service
     * Returns the lowercased basename of the class
     * 
     * Note: method is fairly expensive, as it is crawling the class hierarchy 
     * If you need the value often, it is recommended to cache the the value
     * 
     * @param cl
     * @return 
     */
    public static String getServiceName(Class cl) {
        return getServiceBaseName(cl).toLowerCase();
    }
    

    private static final List<ServiceLayer> DEFAULT_LAYERS;
    static {
        List<ServiceLayer> defaults = new ArrayList<>();
        defaults.add(AnnotatedServiceLayer.CACHE);
        defaults.add(AnnotatedServiceLayer.ROUTER);
        defaults.add(AnnotatedServiceLayer.DAO);
        DEFAULT_LAYERS = Collections.unmodifiableList(defaults);
    }
    
    /**
     * Load the service from the layer stack, assigning the lower layers as delegates
     * to the parent layers. If layers is empty, loading a default layer configuration
     * will be attempted.
     * 
     * Layers must all implement T.
     * The last layer must NOT implement Delegate&lt;T&gt;.
     * All other layers MUST implement Delegate&lt;T&gt;.
     * 
     * @param <T>
     * @param layers the list of layers to be loaded, if empty loads default layers
     * @param service the target implementation of the service layers
     * @return The first layer, loaded and linked to child delegates, if present
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadService(List<ServiceLayer> layers, Class<T> service) throws Exception {
        // Handle unspecified layers differently, optionally loading the defaults
        boolean optionalLayers = false;
        if (layers ==null || layers.isEmpty()) {
            layers = DEFAULT_LAYERS;
            optionalLayers = true;
        }
        
        // Traverse through the lists in reverse order, since you need to supply 
        // the lower layers to the parent delegate
        T resultLayer = null;
        for (ServiceLayer layer : Lists.reverse(layers)) {
            T currentLayer;
            try {
                currentLayer = layer.loadLayer(service);
            } catch (Exception e) {
                // if optional, just ignore this and attempt to load the next layer
                if (optionalLayers) {
                    continue;
                }
                throw e;
            }
            
            // Ensure that the loaded layer is compatible
            if (!service.isAssignableFrom(currentLayer.getClass())) {
                throw new Exception(currentLayer.getClass().getName() 
                        + " does not implement " + service.getName());
            }

            // A previous layer was loaded, so this layer must be a delagate
            if (resultLayer != null) {
                if (!Delegate.class.isAssignableFrom(currentLayer.getClass())) {
                    throw new Exception(currentLayer.getClass().getName() 
                            + " must implement Delegate<"+service.getName()+">");
                }
                for (Type genericInterface : currentLayer.getClass().getGenericInterfaces()) {
                    if (genericInterface instanceof Delegate) {
                        Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                        if (genericTypes.length!=1 || !service.isInstance(genericTypes[0])) {
                            throw new Exception(currentLayer.getClass().getName()
                                    + " must implement Delegate<" + service.getName() + ">");
                        }
                        break;
                    }
                }
                // Validated that this is Delegate<T> so should be safe now
                ((Delegate<T>)currentLayer).setDelegate(resultLayer);
            }
            resultLayer = currentLayer;
        }
        
        if (resultLayer==null) {
            throw new Exception("No service layers found for: "+service.getName());
        }
        return resultLayer;
    }
}
