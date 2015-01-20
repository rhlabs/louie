/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.services.auth;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.ServiceProvider;
import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.data.Param;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.service.ServiceFactory;

import com.rhythm.pb.RequestProtos.IdentityPB;

/**
 *
 * @author cjohnson
 */
@ServiceProvider
public class AuthServiceFactory implements ServiceFactory {
    private static AuthServiceHandler service;
    
    public AuthServiceFactory() {}
    
    @Override
    public String getServiceName() {
        return AuthService.SERVICE_NAME;
    }
    
    @Override
    public AuthServiceHandler getService() throws Exception {
        return loadService();
    }

    private static synchronized AuthServiceHandler loadService() {
        if (service == null) {
            AuthService dmo = AuthDMO.getInstance();
            service = new AuthPortValidator(dmo);
        }
        return service;
    }
    
    public static AuthService getServiceClient() {
        return loadService().getDelegate();
    }
    
    private static class AuthPortValidator extends AuthServiceHandler {

        protected AuthPortValidator(AuthService dmo) {
            super();
            setDelegate(dmo);
        }

        @Override
        public Result executeCommand(RequestContext req) throws Exception {

            Result result = super.executeCommand(req);

            // Store the newly created Identity into the request object 
            // since a createSession does not pass one in
            if (req.getRequest().getMethod().equals("createSession")) {
                try {
                    Param param = (Param) result.getArguments().get(0);
                    IdentityPB id = (IdentityPB) param.getParsedArg(0);
                    req.setIdentity(id);
                } catch (Exception e) {
                    LoggerFactory.getLogger(AuthServiceFactory.class).error("Error extracting Identity from createSession");
                }
            }
            return result;
        }
    }
}
