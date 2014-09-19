/*
 * AnnotatedMessageHandler.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Descriptors.Descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.request.data.Data;
import com.rhythm.louie.request.data.DataParser;
import com.rhythm.louie.request.data.DataParser.BuilderParser;

/**
 *
 * @author sfong
 */
public class AnnotatedMessageHandler implements MessageHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(AnnotatedMessageHandler.class);

    private final Map<String,MessageProcessor> actionMap;
    protected AnnotatedMessageHandler() {
        Map<String, MessageProcessor> tmpMap = new TreeMap<String, MessageProcessor>();
        for (Method meth : this.getClass().getDeclaredMethods()) {
            try {
                if (!Modifier.isStatic(meth.getModifiers())
                        && Modifier.isPublic(meth.getModifiers())
                        && meth.isAnnotationPresent(MessageOperation.class)) {
                    MessageOperation descriptor = meth.getAnnotation(MessageOperation.class);
                    AnnotatedMessageProcessor processor = new AnnotatedMessageProcessor(this, meth, descriptor);
                    tmpMap.put(processor.getType(), processor);
                }
            } catch (Exception ex) {
                LOGGER.error("Error Constructing Processor: {}" + meth.getName(), ex);
            }
        }
        actionMap = Collections.synchronizedMap(tmpMap);
    }
    
    @Override
    public void executeMessageHandler(MessageAction action, String type, Data data) throws Exception {
        MessageProcessor mh = actionMap.get(type);
        if (mh != null) {
            mh.execute(action, data);
        }
    }
    
    @Override
    public Collection<MessageProcessor> getMessageProcessors() {
        return Collections.unmodifiableCollection(actionMap.values());
    }
    
    private class AnnotatedMessageProcessor implements MessageProcessor {

        private final Method meth;
        private final MessageOperation action;
        private DataParser<?> parser;
        private Descriptor descriptor;
        private Class<?> paramType;
        private final AnnotatedMessageHandler handler;

        public AnnotatedMessageProcessor(final AnnotatedMessageHandler handler, 
                Method meth, MessageOperation action) throws Exception {
            this.meth = meth;
            this.action = action;
            this.handler = handler;
            if (meth.getParameterTypes().length != 2) {
                throw new Exception("MessageOperation methods must have 2 and only 2 arguments!");
            }
            paramType = meth.getParameterTypes()[0];
            if (!paramType.getName().equals(MessageAction.class.getName())) {
                throw new Exception("MessageOperation 1st argument must be of type MessageAction");
            }
            paramType = meth.getParameterTypes()[1];
            descriptor = lookupDescriptor(paramType);
            parser = getParser(paramType);
        }

        @Override
        public void execute(MessageAction action, Data data) throws Exception {
            meth.invoke(handler, action, parser.parseData(data));
        }

        public Descriptor getDescriptor() {
            return descriptor;
        }
        
        @Override
        public String getType() {
            return getDescriptor().getFullName();
        }
    }

    private static BuilderParser getParser(Class<?> cl) throws Exception {
        if (cl == null) {
            return null;
        }
        Method meth = cl.getMethod("newBuilder");
        if (!Modifier.isStatic(meth.getModifiers())
                || !Modifier.isPublic(meth.getModifiers())) {
            throw new Exception("Not a PB!");
        }
        Object o = meth.invoke(cl);
        if (!(o instanceof Builder)) {
            throw new Exception("Not a PB Descriptor!");
        }
        return new BuilderParser<Object>((Builder) o);
    }

    private static Descriptor lookupDescriptor(Class<?> cl) throws Exception {
        if (cl == null) {
            return null;
        }
        try {
            Method descMeth = cl.getMethod("getDescriptor");
            if (!Modifier.isStatic(descMeth.getModifiers())
                    || !Modifier.isPublic(descMeth.getModifiers())) {
                throw new Exception("Method getDescriptor is not public static.");
            }

            final Object o = descMeth.invoke(cl);
            if (!(o instanceof Descriptor)) {
                throw new Exception("Method getDescriptor did not return a PB Desriptor!");
            }
            return (Descriptor) o;
        } catch (Exception e) {
            throw new Exception("Class " + cl.getName() + " is not a PB!", e);
        }
    }
}
