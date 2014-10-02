/*
 * AsyncIOCallable.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.async;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Callable implementation for use in streaming asynchronous multiplexed calls.
 * @author eyasukoc
 */
public class AsyncIOCallable implements Callable {

    private final AsyncIO io;
    private final List<AsyncIOBundle> bundles;
    
    public AsyncIOCallable(List<AsyncIOBundle> outgoingBundles) throws IOException {
        io = new AsyncIO();
        bundles = outgoingBundles;
    }
    
    @Override
    public List<AsyncIOBundle> call() throws Exception {
        io.execute(bundles);
        return null;
    }
        
}
