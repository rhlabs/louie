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
public interface Consumer <T> {
    
    public void informMessageCount(int count);

    public void consume(T object);

}
