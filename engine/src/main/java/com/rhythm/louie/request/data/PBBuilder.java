/*
 * PBBuilder.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request.data;

import com.google.protobuf.Message;

/**
 *
 * @author cjohnson
 */
public interface PBBuilder<A,M extends Message> {
    public M build(A a);
}
