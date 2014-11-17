/*
 * RouteUserDisable.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

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
        RequestContextManager.getRequest().enableRouteUser(false);
        return INSTANCE;
    }
    
    @Override
    public void close() throws Exception {
        RequestContextManager.getRequest().enableRouteUser(true);
    }
    
}
