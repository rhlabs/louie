/*
 * LouieServiceFactory.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.info;

import com.rhythm.louie.ServiceProvider;
import com.rhythm.louie.service.Service;
import com.rhythm.louie.service.ServiceFactory;

/**
 *
 * @author cjohnson
 */
@ServiceProvider
public class InfoServiceFactory implements ServiceFactory {
    
    private static InfoServiceHandler service;
    
    public InfoServiceFactory() {}
    
    public static InfoServiceFactory getInstance() {
        return new InfoServiceFactory();
    }

    @Override
    public String getServiceName() {
        return InfoService.SERVICE_NAME;
    }
    
    @Override
    public Service getService() {
        if (service == null) {
            service = new InfoServiceHandler();
            service.setDelegate(new InfoDAO());
        }
        return service;
    }
    
    public InfoService getServiceClient() {
        return service.getDelegate();
    }
    
}
