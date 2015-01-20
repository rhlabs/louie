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

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

/**
 * @author cjohnson
 * Created: Mar 1, 2011 5:12:38 PM
 */
public class Result {
    private final List<? extends Message> messages;
    private final List<? extends Object> arguments;
    private boolean success = true;
    private String info;
    private long duration;
    private long execTime;
    private long size;
    private Exception ex;
    private boolean streaming = false;
    
    public static Result emptyResult() {
        List<?> args = Collections.emptyList();
        List<? extends Message> results = Collections.emptyList();
        return new Result(true, args, results);
    }
    
    public static Result results(Object arg, List<? extends Message> results) {
        return new Result(true, Collections.singletonList(arg), results);
    }
    
    public static Result results(Object arg, Message result) {
        List<? extends Message> results;
        if (result==null) {
            results = Collections.emptyList();
        } else {
            results = Collections.singletonList(result);
        }
        return new Result(true, Collections.singletonList(arg), results);
    }
    
    // TODO should deprecate/remove this as it is not a supported workflow
    public static Result multiArgResults(Map<? extends Object,List<Message>> results) {
        List<Message> messages = new ArrayList<>();
        for (List<? extends Message> values : results.values()) {
            messages.addAll(values);
        }
        
        return new Result(true, new ArrayList<>(results.keySet()), messages);
    }
    
    public static Result errorResult(Exception e) {
        return errorResult("",e);
    }

    public static Result errorResult(String message, Exception e) {
        List<?> args = Collections.emptyList();
        List<? extends Message> results = Collections.emptyList();
        
        Result result = new Result(false, args, results);
        if (e != null) {
            result.setException(e);
            StringBuilder stack = new StringBuilder();
            while (e.getCause() != null && e.getCause() instanceof Exception) {
                if (!(e instanceof InvocationTargetException)) {
                    stack.append(e.toString()).append("\n");
                }
                e = (Exception) e.getCause();
            }
            result.setInfo(message+stack.toString());
        }

        return result;
    }
    
    private Result(boolean success, List<? extends Object> args, List<? extends Message> messages) {
        this.success = success;
        this.messages = sanitizeMessages(messages);
        this.arguments = args;
        
        size=0;
        duration=0;
        execTime=0;
    }
 
    private static <T extends Message> List<T> sanitizeMessages(List<T> messages) {
        // Sanitize the messages for null entries, since that will blow up the client
        int nullCount = 0;
        for (Message m : messages) {
            if (m==null) {
                nullCount++;
            }
        }
        if (nullCount==0) {
            return messages;
        } else {
            List<T> sanitized = new ArrayList<>(messages.size()-nullCount);
            for (T m : messages) {
                if (m!=null) {
                    sanitized.add(m);
                }
            }
            return sanitized;
        }
    }
    
    public void setInfo(String info) {
        this.info=info;
    }
    
    public void addError(Exception e){
        success = false;
    }
    
    public boolean isError() {
        return !success;
    }

    public String getInfo() {
        return info;
    }
    
    public List<? extends Message> getMessages() {
        return messages;
    }

    public List<?> getArguments() {
        return arguments;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * @return the execTime
     */
    public long getExecTime() {
        return execTime;
    }

    /**
     * @param duration the time in ms to fully complete the call
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @param time the time in ms to execute the request
     */
    public void setExecTime(long time) {
        this.execTime = time;
    }

    
    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the ex
     */
    public Exception getException() {
        return ex;
    }

    /**
     * @param ex the ex to set
     */
    public void setException(Exception ex) {
        this.ex = ex;
    }
    
    /**
     * @return true if the result is meant to be streamed
     */
    public boolean isStreaming() {
        return streaming;
    }
    
    /**
     * @param streaming enable result streaming
     */
    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }
}
