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
public class LouieServiceFactory implements ServiceFactory{
    
    private static final String serviceName = "louie";
    
    public static LouieServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    private static class Holder {
        private static final LouieServiceFactory INSTANCE = new LouieServiceFactory();
    }         
    
    @Override
    public Service getService() {
        return new LouieServiceHandler(new LouieDAO());
    }
    
}
