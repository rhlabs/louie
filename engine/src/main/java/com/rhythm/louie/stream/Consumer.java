/*
 * Consumer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.stream;

/**
 *
 * @author eyasukoc
 * @param <T> Type of the returned object(s)
 */
public abstract class Consumer <T> {
    
    public void informMessageCount(int count) {}

    abstract public void consume(T object);

}
