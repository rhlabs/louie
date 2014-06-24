/*
 * Request.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.connection;

import com.rhythm.pb.PBParam;

/**
 *
 * @author eyasukoc
 */
public class RequestParams <T>{
    private final String system,cmd;
    private final PBParam param;
    private final T template;
    
    public static <E> RequestParams<E> newParams(String system, String cmd, PBParam param, E template) {
        return new RequestParams<E>(system,cmd,param,template);
    }
    
    public static <E> RequestParams<E> newParams(String system, String cmd, E template) {
        return new RequestParams<E>(system,cmd,PBParam.EMPTY,template);
    }
    
    private RequestParams(String system, String cmd, PBParam param, T template) {
        this.system = system;
        this.cmd = cmd;
        this.param = param;
        this.template = template;
    }

    public String getSystem() {
        return system;
    }

    public String getCmd() {
        return cmd;
    }

    public PBParam getParam() {
        return param;
    }

    public T getTemplate() {
        return template;
    }
    
    
}
