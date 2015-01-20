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
package com.rhythm.louie.pb;

import com.rhythm.louie.request.data.Data;
import com.rhythm.louie.request.data.Param;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.MessageOrBuilder;

import com.rhythm.pb.DataTypeProtos;

import org.joda.time.LocalDate;

/**
 * @author cjohnson
 * Created: Mar 3, 2011 8:56:09 AM
 */
public class PBUtils {
    
//    static public GeneratedMessage.Builder parseMessage(GeneratedMessage.Builder builder,String data) {
//            
//        return message;
//    }
//    
    /**
     * Checks that all possible fields for this message are set.
     * If a field is of String type, it will also ensure it is != ""
     *
     * This method will throw an exception if a field is missing, else does nothing.
     *
     * @param message the message object to check
     * @param errorHeader an optional prefix to the exception string
     * @param excludes a list of excluded field names
     * @throws Exception
     */
    static public void ensureAllFields(MessageOrBuilder message, String errorHeader, Set<String> excludes) throws Exception {
        for (FieldDescriptor f : message.getDescriptorForType().getFields()) {
            if ((!message.hasField(f) || 
                    (f.getJavaType().equals(JavaType.STRING) && message.getField(f).equals("")) ) 
                && !excludes.contains(f.getName()) ) {
                    if (errorHeader!=null && !errorHeader.equals("")) {
                        errorHeader+=" ";
                    }
                    throw new Exception(errorHeader+"Missing field: "+f.getName());
            }
        }
    }
    
//    static public GeneratedMessage diff(GeneratedMessage m1, GeneratedMessage m2) {
//        for (FieldDescriptor field : m1.getDescriptorForType().getFields()) {
//            
//        }
//    }
    
    /**
     * Checks that all possible fields for this message are set.
     * If a field is of String type, it will also ensure it is != ""
     *
     * This method will throw an exception if a field is missing, else does nothing.
     *
     * @param message the message object to check
     * @param errorHeader an optional prefix to the exception string
     * @throws Exception
     */
    static public void ensureAllFields(MessageOrBuilder message, String errorHeader) throws Exception {
        Set<String> empty = Collections.emptySet();
        ensureAllFields(message,errorHeader,empty);
    }
    
    static public List<String> unmodifiablePBList(String... a) {
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(a));
        return Collections.unmodifiableList(list);
    }
    
    static public DataTypeProtos.DateTimePB createDateTimePB(Date date) {
         DataTypeProtos.DateTimePB.Builder builder = DataTypeProtos.DateTimePB.newBuilder();
         if (date != null) {
             builder.setTime(date.getTime());
         }
         return builder.build();
    }
    
    static public DataTypeProtos.DateTimePB createDateTimePB(Timestamp timestamp) {
         DataTypeProtos.DateTimePB.Builder builder = DataTypeProtos.DateTimePB.newBuilder();
         if (timestamp != null) {
             builder.setTime(timestamp.getTime());
         }
         return builder.build();
    }
    
    static public DataTypeProtos.DatePB createDatePB(Date date) {
         DataTypeProtos.DatePB.Builder builder = DataTypeProtos.DatePB.newBuilder();
         if (date != null) {
             builder.setTime(date.getTime());
         }
         return builder.build();
    }
    
    static public DataTypeProtos.DatePB createDatePB(LocalDate date) {
         DataTypeProtos.DatePB.Builder builder = DataTypeProtos.DatePB.newBuilder();
         if (date != null) {
             builder.setTime(date.toDateTimeAtStartOfDay().getMillis());
         }
         return builder.build();
    }
    
    static public DataTypeProtos.DateTimePB createDateTimePB(long time) {
         DataTypeProtos.DateTimePB.Builder builder = DataTypeProtos.DateTimePB.newBuilder();
         if (time != -1) {
             builder.setTime(time);
         }
         return builder.build();
    }
    
    static public DataTypeProtos.DatePB createDatePB(long time) {
         DataTypeProtos.DatePB.Builder builder = DataTypeProtos.DatePB.newBuilder();
         if (time != -1) {
             builder.setTime(time);
         }
         return builder.build();
    }
    
    static public List<Data> flattenSingleArgs(List<Param> params) {
        List<Data> data = new ArrayList<Data>(params.size());
        for (Param param : params) {
            data.add(param.getArg(0));
        }
        return data;
    }
}
