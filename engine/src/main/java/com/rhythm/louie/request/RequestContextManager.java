/*
 * RequestContext.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.request;

/**
 *
 * @author cjohnson
 */
public class RequestContextManager {

    final private static RequestContextManager SINGLETON = new RequestContextManager();

    ThreadLocal<RequestContext> request;

    private RequestContextManager() {
        request = new ThreadLocal<>();
    }

    public static RequestContext getRequest() {
        return SINGLETON.request.get();
    }

    protected static void setRequest(RequestContext req) {
        SINGLETON.request.set(req);
    }

    protected static void clearRequest() {
        SINGLETON.request.remove();
    }

}
