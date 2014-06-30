/*
 * Consumers.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.stream;

/**
 *
 * @author eyasukoc
 */
public class Consumers {
    
    public static <T> ListConsumer<T> newListConsumer() {
        return new ListConsumer<T>();
    }
    
    public static <T> SingleConsumer<T> newSingleConsumer() {
        return new SingleConsumer<T>();
    }
}
