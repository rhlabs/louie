/*
 * Delegate.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie;

/**
 *
 * @author cjohnson
 * @param <T>
 */
public interface Delegate<T> {
    void setDelegate(T delegate);
    T getDelegate();
}
