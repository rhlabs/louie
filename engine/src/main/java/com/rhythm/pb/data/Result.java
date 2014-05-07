/*
 * PBResult.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.pb.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.Message;

/**
 * @author cjohnson
 * Created: Mar 1, 2011 5:12:38 PM
 */
public class Result<A,R extends Message> {
    private final List<R> messages;
    private final List<A> arguments;
    private boolean success;
    private String info;
    private long duration;
    private long execTime;
    private long size;
    private Exception ex;
    
    public Result() {
        this(true);
    }
    
    public static Result errorResult(Exception e) {
        StringBuilder stack = new StringBuilder();
        while (e.getCause() != null && e.getCause() instanceof Exception) {
            if (!(e instanceof InvocationTargetException)) {
                stack.append(e.toString()).append("\n");
            }
            e = (Exception) e.getCause();
        }

        Result result = new Result(false);
        result.setException(e);
        result.setInfo(stack.toString());

        return result;
    }
    
    public Result(boolean success) {
        this.success = success;
        messages = new ArrayList<R>();
        arguments = new ArrayList<A>();
        
        size=0;
        duration=0;
        execTime=0;
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
    
    public Collection<R> getMessages() {
        return messages;
    }

    public void addMessage(R msg) {
        if (msg!=null) {
            messages.add(msg);
        }
    }

    public void addMessages(Collection<? extends R> messages) {
        if (messages != null) {
            for (R msg : messages) {
                if (msg != null) {
                    this.messages.add(msg);
                }
            }
        }
    }
    
    public List<A> getArguments() {
        return arguments;
    }

    public void addArgument(A arg) {
        if (arg!=null) {
            arguments.add(arg);
        }
    }

    public void addArguments(Collection<? extends A> args) {
        if (args!=null) {
            arguments.addAll(args);
        }
    }

    public void addResult(A arg,R message) {
        addArgument(arg);
        addMessage(message);
    }
    
    public void addResults(Collection<? extends A> args,
            Collection<? extends R> messages) {
        addArguments(args);
        addMessages(messages);
    }
    
    public void addResults(A arg,
            Collection<? extends R> messages) {
        addArgument(arg);
        addMessages(messages);
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
}
