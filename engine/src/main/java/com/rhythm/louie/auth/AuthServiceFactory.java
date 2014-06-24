/*
 * AuthServiceFactory.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.command.Service;
import com.rhythm.pb.data.Param;
import com.rhythm.pb.data.Request;
import com.rhythm.pb.data.Result;

import com.rhythm.louie.connection.DefaultLouieConnection;

import com.rhythm.pb.command.ServiceFactory;

/**
 *
 * @author cjohnson
 */
public class AuthServiceFactory implements ServiceFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthServiceFactory.class);
    
    private static final String serviceName = "auth";
    private static AuthServiceHandler service;
    
    private AuthServiceFactory() {}
    
    public static AuthServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    private static class Holder {
        private static final AuthServiceFactory INSTANCE = new AuthServiceFactory();
    }     
    
    @Override
    public Service getService() {
        if (service == null) {
            AuthClient dmo = AuthDMO.getInstance();
            service = new AuthPortValidator(dmo);
        }
        return service;
    }
    
    public AuthClient getServiceClient() {
        getService();
        return service.getClient();
    }
    
    private class AuthPortValidator extends AuthServiceHandler {

        protected AuthPortValidator(AuthClient dmo) {
            super(dmo);
        }

        @Override
        public Result executeCommand(Request req) throws Exception {
            if (req.getLocalPort()!=DefaultLouieConnection.AUTH_PORT && 
                req.getLocalPort()!=DefaultLouieConnection.sslPort()) {
                     LOGGER.warn("Warning: Calls to Auth service on insecure port: {}", req.getLocalPort());
            }
            
            Result result = super.executeCommand(req);

            // SUPER HACK
            if (req.getRequest().getMethod().equals("createSession")) {
                try {
                    Param param = (Param) result.getArguments().get(0);
                    IdentityPB id = (IdentityPB) param.getParsedArg(0);
                    req.setIdentity(id);
                } catch (Exception e) {
                    LOGGER.error("Error extracting Identity from createSession");
                }
            }
            return result;
        }
    }
}
