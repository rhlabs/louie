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
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rhythm.louie.ServiceProperties;
import com.rhythm.louie.jms.MessageHandler;
import com.rhythm.louie.process.CommandDescriptor;
import com.rhythm.louie.process.Disabled;
import com.rhythm.louie.process.ServiceFacade;

import com.rhythm.pb.data.Request;
import com.rhythm.pb.data.Result;

/**
 * @author cjohnson
 * Created: Oct 21, 2011 4:42:23 PM
 */
public class AnnotatedService implements Service {
    private final Logger LOGGER = LoggerFactory.getLogger(AnnotatedService.class);
            
    private final Map<PBCommandType,PBCommand<?,?>> commandMap;
    private final String name;
    
    protected AnnotatedService(String name) {
        this.name = name;
        Map<PBCommandType,PBCommand<?,?>> tmpMap = new TreeMap<PBCommandType,PBCommand<?,?>>();
        
        processClass(this.getClass(),tmpMap);
        
        commandMap = Collections.synchronizedMap(tmpMap);
    }
    
    private void processClass(Class<?> cl, Map<PBCommandType,PBCommand<?,?>> map) {
        getCommandsForClass(cl,map);
        
        for (Class<?> facade : cl.getInterfaces()) {
            if (facade.isAnnotationPresent(ServiceFacade.class)) {
                getCommandsForClass(facade,map);
            }
        }
        
        if (cl.getSuperclass()!=Object.class) {
            processClass(cl.getSuperclass(),map);
        }
    }
    
    
    private void getCommandsForClass(Class cl,Map<PBCommandType,PBCommand<?,?>> tmpMap) {
        for (Method meth : cl.getDeclaredMethods()) {
            try {
                if (!Modifier.isStatic(meth.getModifiers())
                        && Modifier.isPublic(meth.getModifiers())
                        && meth.isAnnotationPresent(CommandDescriptor.class)
                        && !meth.isAnnotationPresent(Disabled.class)) {
                    ReflectCommand command = new ReflectCommand(this, meth);
                    if (!tmpMap.containsKey(command.getCommandType())) {
                        tmpMap.put(command.getCommandType(),command);
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
    public Result executeCommand(Request req) throws Exception {
         PBCommand cmd = getCommand(req.getRequest().getMethod(),req.getType());
         if (cmd==null) {
             throw new Exception("Command Does Not Exist! - "
                     +getServiceName()+":"+req.getRequest().getMethod()
                     +"("+req.getType()+")");
         }
         String serviceName = req.getRequest().getSystem();
         ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
         if (cmd.isUpdate() && props.isReadOnly()){
             throw new UnsupportedOperationException("Louie Service "+serviceName+" is set to read-only mode.");
         }
         return cmd.execute(req);
    }

    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public void initialize() throws Exception {
        // write your own hooks here
    }

    @Override
    public void shutdown() throws Exception {
        // write your own hooks here
    }
    
    @Override
    public MessageHandler getMessageHandler() {
        // write your own hooks here
        return null;
    }
}