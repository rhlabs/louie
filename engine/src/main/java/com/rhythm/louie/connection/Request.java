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
package com.rhythm.louie.connection;

import com.rhythm.louie.stream.Consumer;
import com.rhythm.louie.stream.ListConsumer;
import com.rhythm.louie.pb.PBParam;

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
