/*
 * RequestContext.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.request;

import com.rhythm.pb.data.Request;

/**
 *
 * @author cjohnson
 */
public class RequestContext {
  final private static RequestContext SINGLETON = new RequestContext();
  
  ThreadLocal<Request> request;
  
  private RequestContext() {
      request = new ThreadLocal<Request>();
  }
  
  public static Request getRequest() {
      return SINGLETON.request.get();
  }
    
  public static void setRequest(Request req) {
      SINGLETON.request.set(req);
  }
  
  public static void clearRequest() {
      SINGLETON.request.remove();
  }
  
}
