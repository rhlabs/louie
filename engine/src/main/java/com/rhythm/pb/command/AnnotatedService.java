/*
 * AnnotatedService.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.pb.command;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.ServiceProperties;
//import com.rhythm.louie.generator.ProcessorUtils;
import com.rhythm.louie.process.Disabled;
import com.rhythm.louie.process.Private;
import com.rhythm.louie.process.ServiceCall;

import com.rhythm.pb.data.RequestContext;
import com.rhythm.pb.data.Result;

/**
 * @author cjohnson
 * Created: Oct 21, 2011 4:42:23 PM
 */
public abstract class AnnotatedService implements Service {
    private final Logger LOGGER = LoggerFactory.getLogger(AnnotatedService.class);
            
    private final Map<PBCommandType,PBCommand<?,?>> commandMap;
    private final String name;
//    private final Map<String,StoredMethodProps> props;
    
    protected AnnotatedService(String name) {
        this.name = name;
        commandMap = new ConcurrentHashMap<PBCommandType,PBCommand<?,?>>();
//        props = ProcessorUtils.readStoredProps(getServiceInterface());
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
        //if (cl.isAnnotationPresent(ServiceFacade.class)) {
            getCommandsForClass(cl);
       // }
        
//        for (Class<?> iface : cl.getInterfaces()) {
//            processClass(iface);
//        }
//        
//        if (cl.getSuperclass()!=Object.class) {
//            processClass(cl.getSuperclass());
//        }
    }
    
    private void getCommandsForClass(Class<?> cl) {
        for (Method meth : cl.getDeclaredMethods()) {
            try {
                if (!Modifier.isStatic(meth.getModifiers())
                        && Modifier.isPublic(meth.getModifiers())
                        && meth.isAnnotationPresent(ServiceCall.class)) {
                    
//                    String methKey = ProcessorUtils.getStoredMethodKey(meth);
//                    StoredMethodProps methProps = props.get(methKey);
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
    public Collection<PBCommand<?,?>> getCommands() {
        return Collections.unmodifiableCollection(commandMap.values());
    }
    
    public PBCommand getCommand(String command,PBParamType params) {
        return commandMap.get(PBCommandType.valueOf(command, params));
    }
    
    @Override
    public Result executeCommand(RequestContext req) throws Exception {
         PBCommand cmd = getCommand(req.getRequest().getMethod(),req.getType());
         if (cmd==null) {
             throw new Exception("Command Does Not Exist! - "
                     +getServiceName()+":"+req.getRequest().getMethod()
                     +"("+req.getType()+")");
         }
         String serviceName = req.getRequest().getService();
         ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
         if (cmd.isUpdate() && props.isReadOnly()){
             throw new UnsupportedOperationException("Louie Service "+serviceName+" is set to read-only mode.");
         }
         Result r = cmd.execute(req);
         r.setStreaming(cmd.isStreaming());
         return r;
    }
}