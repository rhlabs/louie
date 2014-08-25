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
import com.rhythm.pb.data.RequestContext;
import com.rhythm.pb.data.Result;

import com.rhythm.louie.connection.DefaultLouieConnection;

import com.rhythm.pb.command.ServiceFactory;

/**
 *
 * @author cjohnson
 */
public class AuthServiceFactory implements ServiceFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthServiceFactory.class);
    
    private static AuthServiceHandler service;
    
    private AuthServiceFactory() {}
    
    public static AuthServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getServiceName() {
        return AuthService.SERVICE_NAME;
    }

    private static class Holder {
        private static final AuthServiceFactory INSTANCE = new AuthServiceFactory();
    }     
    
    @Override
    public Service getService() {
        if (service == null) {
            AuthService dmo = AuthDMO.getInstance();
            service = new AuthPortValidator(dmo);
        }
        return service;
    }
    
    public AuthService getServiceClient() {
        getService();
        return service.getDelegate();
    }
    
    private class AuthPortValidator extends AuthServiceHandler {

        protected AuthPortValidator(AuthService dmo) {
            super();
            setDelegate(dmo);
        }

        @Override
        public Result executeCommand(RequestContext req) throws Exception {
            if (req.getLocalPort()!=DefaultLouieConnection.AUTH_PORT && 
                req.getLocalPort()!=DefaultLouieConnection.sslPort()) {
                     LOGGER.warn("Warning: Calls to Auth service on insecure port: {}", req.getLocalPort());
            }
            
            Result result = super.executeCommand(req);

            // Store the newly created Identity into the request object 
            // since a createSession does not pass one in
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
