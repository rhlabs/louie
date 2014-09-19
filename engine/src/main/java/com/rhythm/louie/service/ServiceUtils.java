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
        Service service = (Service) clz.getAnnotation(Service.class);
        if (service!=null) {
            return service.value();
        } else {
            return "unknown";
        }
    }
}
