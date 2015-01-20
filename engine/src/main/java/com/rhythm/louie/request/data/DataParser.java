/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
