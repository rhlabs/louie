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
package com.rhythm.louie.service;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.RequestContextManager;

/**
 * AutoCloseable switch to temporarily disable the route user for the current thread.
 * This purpose of this is to allow the current service layer to issue a request 
 * to another layer or server with the scope of the server, rather than the scope
 * of the user who initiated the request. This is particularly useful with user
 * based permissions in a cached environment.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * try (RouteUserDisable d = RouteUserDisable.disable()) {
 *     return getDelegate().getData();
 *  }
 * }
 * </pre>
 */
public class RouteUserDisable implements AutoCloseable {
    private static final RouteUserDisable INSTANCE = new RouteUserDisable();
    private RouteUserDisable() {}
    
    /**
     * Switches the route user off for the current thread. The returned object is
     * merely for the AutoClose hook to switch the routeUser back on after the call
     * is complete.
     * 
     * @return a shared instance in order to perform the AutoClose.
     */
    public static RouteUserDisable disable() {
        setEnabled(false);
        return INSTANCE;
    }
    
    @Override
    public void close() throws Exception {
        setEnabled(true);
    }
    
    private static void setEnabled(boolean enable) {
        RequestContext request = RequestContextManager.getRequest();
        if (request!=null) {
            request.enableRouteUser(enable);
        }
    }
    
}
