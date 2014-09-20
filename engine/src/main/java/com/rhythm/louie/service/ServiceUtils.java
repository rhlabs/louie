/*
 * ServiceUtils.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import com.rhythm.louie.Classes;

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
    
}
