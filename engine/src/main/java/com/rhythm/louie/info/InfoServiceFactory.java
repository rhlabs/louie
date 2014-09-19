/*
 * LouieServiceFactory.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.info;

import com.rhythm.louie.service.Service;
import com.rhythm.louie.service.ServiceFactory;

/**
 *
 * @author cjohnson
 */
public class InfoServiceFactory implements ServiceFactory {
    
    
    private static InfoServiceHandler service;
    
    public static InfoServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getServiceName() {
        return InfoService.SERVICE_NAME;
    }

    private static class Holder {
        private static final InfoServiceFactory INSTANCE = new InfoServiceFactory();
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
