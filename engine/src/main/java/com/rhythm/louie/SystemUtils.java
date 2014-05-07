/*
 * SystemUtils.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import com.rhythm.pb.SystemProtos.SystemPropertiesPB;

/**
 *
 * @author cjohnson
 */
public class SystemUtils {
    public static SystemPropertiesPB currentTimeProperty() {
        return SystemPropertiesPB.newBuilder()
                .setServerTime(System.currentTimeMillis()).build();
    }
    
    public static SystemPropertiesPB currentTimeProperty(boolean sparse) {
        return SystemPropertiesPB.newBuilder()
                .setServerTime(System.currentTimeMillis())
                .setSparse(sparse)
                .build();
    }
        
}
