/*
 * AnnotatedService.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.server.ServiceProperties;
import com.rhythm.louie.process.ServiceCall;
import com.rhythm.louie.process.ServiceHandler;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.server.AccessManager;
import com.rhythm.louie.service.command.PBCommand;
import com.rhythm.louie.service.command.PBCommandType;
import com.rhythm.louie.service.command.PBParamType;
import com.rhythm.louie.service.command.ReflectCommand;

/**
 * @author cjohnson
 * Created: Oct 21, 2011 4:42:23 PM
 */
public abstract class AnnotatedService implements Service {
    private final Logger LOGGER = LoggerFactory.getLogger(AnnotatedService.class);
            
    private final Map<PBCommandType,PBCommand> commandMap;
    private final String name;
    
    protected AnnotatedService(String name) {
        this.name = name;
        commandMap = new ConcurrentHashMap<>();
    }
    
    @Override
    public void initialize() throws Exception {
        processClass(this.getClass());
    }
    
    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public void shutdown() throws Exception {}
    
    private void processClass(Class<?> cl) {
        if (cl.getAnnotation(ServiceHandler.class)!=null) {
            getCommandsForClass(cl);
        }
        
        if (cl.getSuperclass() != Object.class) {
            processClass(cl.getSuperclass());
        }
    }
    
    private void getCommandsForClass(Class<?> cl) {
        for (Method meth : cl.getDeclaredMethods()) {
            try {
                if (!Modifier.isStatic(meth.getModifiers())
                        && Modifier.isPublic(meth.getModifiers())
                        && meth.isAnnotationPresent(ServiceCall.class)) {
                    
                    ReflectCommand command = new ReflectCommand(this, meth);
                    if (!commandMap.containsKey(command.getCommandType())) {
                        commandMap.put(command.getCommandType(),command);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error Constructing Command: "+meth.getName(), ex);
            }
        }
    }
    
    @Override
    public Collection<PBCommand> getCommands() {
        return Collections.unmodifiableCollection(commandMap.values());
    }
    
    public PBCommand getCommand(String command,PBParamType params) {
        return commandMap.get(PBCommandType.valueOf(command, params));
    }
  
    @Override
    public Result executeCommand(RequestContext req) throws Exception {
        PBCommand cmd = getCommand(req.getRequest().getMethod(), req.getType());
        if (cmd == null) {
            throw new Exception("Command Does Not Exist! - "
                    + getServiceName() + ":" + req.getRequest().getMethod()
                    + "(" + req.getType() + ")");
        }
        
        if (cmd.adminAccess()) {
            if (!AccessManager.isAdminUser(req.getRequester())) {
                throw new UnsupportedOperationException(req.getRequester()+" does not have admin privileges to access this method.");
            }
        }
        if (cmd.restrictedAccess()) {
            if (!AccessManager.canUserAccessService(
                    req.getRequester(), req.getRequest().getService())) {
                throw new UnsupportedOperationException(req.getRequester()+" does not have privileges to access this method.");
            }
        } 
        
        if (cmd.isUpdate()) {
            ServiceProperties props = ServiceProperties.getServiceProperties(name);
            if (props.isReadOnly()) {
                throw new UnsupportedOperationException("Louie Service " + name + " is set to read-only mode.");
            }
        }
        Result r = cmd.execute(req);
        r.setStreaming(cmd.isStreaming());
        return r;
    }
    
    @Override
    public boolean isReserved() {
        return ServiceProperties.getServiceProperties(name).isReserved();
    }
}
