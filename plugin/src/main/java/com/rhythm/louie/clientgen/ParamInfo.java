/*
 * ParamInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.clientgen;

/**
 *
 * @author sfong
 */
public class ParamInfo {

    private final Class<?> param;
    private final String name;

    public ParamInfo(Class<?> param, String name) {
        this.param = param;
        this.name = name;
    }

    public Class<?> getParam() {
        return param;
    }
    
    public String getName() {
        return name;
    }

    public String getPbType() {
        return param.getSimpleName();
    }
}
