/*
 * ServiceUtils.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import com.rhythm.louie.Service;

/**
 *
 * @author cjohnson
 */
public class ServiceUtils {
    @SuppressWarnings("unchecked")
    public static String getServiceName(Class clz) {
        Service serv = (Service) clz.getAnnotation(Service.class);
        if (serv!=null) {
            return serv.name();
        } else {
            return "unknown";
        }
    }
}
