/*
 * Request.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.connection;

import com.rhythm.louie.stream.Consumer;
import com.rhythm.louie.stream.ListConsumer;
import com.rhythm.pb.PBParam;

/**
 *
 * @author eyasukoc
 */
public class Request <T>{
    private final String service,cmd;
    private final PBParam param;
    private final T template;
    private final Consumer<T> consumer;
    
    @Deprecated
    public static <E> Request<E> newParams(String system, String cmd, PBParam param, E template) {
        return new Request<E>(system,cmd,param,template);
    }
    
    @Deprecated
    public static <E> Request<E> newParams(String system, String cmd, E template) {
        return new Request<E>(system,cmd,PBParam.EMPTY,template);
    }
    
    public static <E> Request<E> newParams(Consumer<E> consumer, String system, String cmd, PBParam param, E template) {
        return new Request<E>(consumer, system, cmd, param, template);
    }
    
    public static <E> Request<E> newParams(Consumer<E> consumer, String system, String cmd, E template) {
        return new Request<E>(consumer, system, cmd, PBParam.EMPTY, template);
    }
    
    private Request(String service, String cmd, PBParam param, T template) {
        this.service = service;
        this.cmd = cmd;
        this.param = param;
        this.template = template;
        this.consumer = new ListConsumer<T>();                                     //TODO unsure if this is appropriate to instantiate here
    }
    
    private Request(Consumer<T> consumer, String service, String cmd, PBParam param, T template) {
        this.service = service;
        this.cmd = cmd;
        this.param = param;
        this.template = template;
        this.consumer = consumer;
    }

    public String getService() {
        return service;
    }

    public String getCommand() {
        return cmd;
    }

    public PBParam getParam() {
        return param;
    }

    public T getTemplate() {
        return template;
    }
    
    public Consumer<T> getConsumer() {
        return consumer;
    }
    
}
