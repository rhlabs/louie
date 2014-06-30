/*
 * ListConsumer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.stream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eyasukoc
 */
public class ListConsumer<T> implements Consumer<T>{

    private final List<T> list = new ArrayList<T>();
    
    public ListConsumer() {}
    
    @Override
    public void informMessageCount(int count) {}

    @Override
    public void consume(T item) {
        list.add(item);
    }
    
    public List<T> get() {
        return list;
    }
    
}
