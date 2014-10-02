/*
 * PlaceHolderIOBundle.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.async;

import com.google.protobuf.Message;


/**
 * Generic placeholder bundle, meant for pipe action for remote requests
 * @author eyasukoc
 * @param <T>
 */
public class PipeIOBundle<T extends Message> extends AsyncIOBundle<T>{

    public PipeIOBundle(String address) {
        super(null, address, 0);
    }
    
    public void setResponseMessage(T msg) {
        super.responseMsg = msg;
    }
    
}
