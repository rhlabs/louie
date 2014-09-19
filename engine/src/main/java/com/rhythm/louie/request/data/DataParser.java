/*
 * PBDataParser.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request.data;

import com.google.protobuf.AbstractMessage.Builder;

/**
 * @author cjohnson
 * Created: Aug 17, 2011 5:39:57 PM
 */
public interface DataParser<A> {
    public abstract A parseData(Data data) throws Exception;
    
    public class BuilderParser<A> implements DataParser<A> {
        Builder builder;
        
        public BuilderParser(Builder builder) {
            this.builder = builder.clone().clear();
        }
                
        @Override
        @SuppressWarnings("unchecked")
        public A parseData(Data data) throws Exception {
            return(A) data.merge(builder.clone()).build();
        }
    }
}
