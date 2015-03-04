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
        Map<String, MessageProcessor> tmpMap = new TreeMap<>();
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
    
    private static class AnnotatedMessageProcessor implements MessageProcessor {

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
        return new BuilderParser<>((Builder) o);
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
