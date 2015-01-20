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

import java.io.InputStream;

import com.google.protobuf.Message.Builder;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author cjohnson
 * Created: Jul 31, 2011 7:59:56 PM
 */
public abstract class Data {
    abstract public <T extends Builder> T merge(T Builder) throws Exception;
    abstract public <T extends Message> T parse(T template) throws Exception;
    
    private static class PBDataImpl extends Data {
        private final byte[] bytes;
        
        public PBDataImpl(byte[] bytes) {
            this.bytes = bytes;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Builder> T merge(T builder) throws Exception {
            return (T) builder.mergeFrom(bytes);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Message> T parse(T template) throws Exception {
            return (T) template.newBuilderForType().mergeFrom(bytes).build();
        }
    };
    
    private static class JsonDataImpl extends Data {
        private final String json;
        
        public JsonDataImpl(String json) {
            this.json = json;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Builder> T merge(T builder) throws Exception {
            JsonFormat.merge(json, builder);
            return builder;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Message> T parse(T template) throws Exception {
            Builder builder = template.newBuilderForType();
            JsonFormat.merge(json, builder);
            return (T) builder.build();
        }
    };
    
    public static Data newJsonData(String json) {
        return new JsonDataImpl(json);
    }
    
    public static Data newPBData(byte[] bytes) {
        return new PBDataImpl(bytes);
    }
    
    public static Data readPBData(InputStream input) throws Exception {
        int firstByte = input.read();
        if (firstByte == -1) {
            throw new Exception("Improper Request format!  Reached EOF prematurely!");
        }
        int size = CodedInputStream.readRawVarint32(firstByte, input);
        
        byte[] bytes = new byte[size];
        readFully(input,bytes,size);
        
        return new PBDataImpl(bytes);
    }
    
    public static Data readPBData(InputStream input, int size) throws Exception {
        byte[] bytes = new byte[size];
        readFully(input,bytes,size);
        return new PBDataImpl(bytes);
    }
    
    private static void readFully(InputStream input, byte[] bytes, int size) throws Exception {
        int offset = 0;
        while (offset<size) {
            int readSize = input.read(bytes,offset,size-offset);
            offset+=readSize;
            if (readSize<0) {
                throw new Exception("Improper Response format!  Reached EOF prematurely!");
            }
        }
    }
}
