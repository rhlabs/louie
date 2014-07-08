/*
 * SingleConsumer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.stream;

/**
 * @author eyasukoc
 * @param <T> Type of the returned object(s)
 */
public class SingleConsumer<T> extends Consumer<T>{

    private T item;
    
    public SingleConsumer() {};
    
    @Override
    public void consume(T item) {
        this.item = item;
    }
    
    public T get() {
        return item;
    }
    
}
