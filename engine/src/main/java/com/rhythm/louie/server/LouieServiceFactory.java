/*
 * LouieServiceFactory.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import com.rhythm.pb.command.Service;
import com.rhythm.pb.command.ServiceFactory;

/**
 *
 * @author cjohnson
 */
public class LouieServiceFactory implements ServiceFactory {
    
    
    private static LouieServiceHandler service;
    
    public static LouieServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getServiceName() {
        return LouieService.SERVICE_NAME;
    }

    private static class Holder {
        private static final LouieServiceFactory INSTANCE = new LouieServiceFactory();
    }         
    
    @Override
    public Service getService() {
        if (service == null) {
            service = new LouieServiceHandler();
            service.setDelegate(new LouieDAO());
        }
        return service;
    }
    
    public LouieService getServiceClient() {
        return service.getDelegate();
    }
    
}
