/*
 * MessageManager.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;


import java.util.*;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.jms.JmsProtos.*;
import com.rhythm.louie.jms.JmsProtos.ContentPB;
import com.rhythm.louie.jms.JmsProtos.MessageBPB;
import com.rhythm.louie.request.data.Data;
import com.rhythm.louie.server.CustomProperty;
import com.rhythm.louie.server.LouieProperties;
import com.rhythm.louie.server.ServiceProperties;

/**
 *
 * @author cjohnson
 */
public class MessageManager {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 61616;
    private static final String SYSTEM_PROP_KEY = "com.rhythm.louie.jmsadapter";
    
    private static JmsAdapter jmsAdapter = null;
    
    private final Map<String, List<MessageProcessor>> messageProcessors 
            = Collections.synchronizedMap(new HashMap<String, List<MessageProcessor>>());
    
    private final List<ManagedListener> listeners = new ArrayList<ManagedListener>();
    
    private MessageManager() {}
    
    public static MessageManager getInstance() {
        return MessageManagerHolder.INSTANCE;
    }

    private static class MessageManagerHolder {
        private static final MessageManager INSTANCE = new MessageManager();
    }
    
    synchronized private static void loadJMSAdapterIfNeeded() throws MessageAdapterException {
        if (jmsAdapter == null) {
            loadJMSAdapter();
        }
    }
    
    private static void loadJMSAdapter() throws MessageAdapterException {
        CustomProperty jmsProp = LouieProperties.getCustomProperty("messaging");
//        ServiceProperties defaultProps = ServiceProperties.getDefaultServiceProperties();
        
        String adapterClass = jmsProp.getProperty("jmsadapter");
        if (adapterClass == null) {
            adapterClass = System.getProperty(SYSTEM_PROP_KEY);
            if (adapterClass == null) {
                throw new MessageAdapterException("A message server adapter class "
                    + "must be specified in the service configs!");
            }
        }
        try {
            jmsAdapter = (JmsAdapter) Class.forName(adapterClass).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new MessageAdapterException(ex);
        } catch (InstantiationException ex) {
            throw new MessageAdapterException(ex);
        } catch (IllegalAccessException ex) {
            throw new MessageAdapterException(ex);
        }
        
        Map<String,String> configHash = new HashMap<String,String>();
        configHash.put(JmsAdapter.HOST_KEY, jmsProp.getProperty(JmsAdapter.HOST_KEY, DEFAULT_HOST));
        configHash.put(JmsAdapter.PORT_KEY, jmsProp.getProperty(JmsAdapter.PORT_KEY, Integer.toString(DEFAULT_PORT)));
        configHash.put(JmsAdapter.FAILOVER_KEY, jmsProp.getProperty(JmsAdapter.FAILOVER_KEY, "true"));
        jmsAdapter.configure(configHash);
    }
    
    public static JmsAdapter getAdapter() {
        return jmsAdapter;
    }
    
    public void listenToTopic(String topicName) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();
        listeners.add(new ManagedTopicListener(topicName));
    }
    
    public void listenToTopic(String topicName,Collection<MessageType> messageTypes) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();
        listeners.add(new ManagedTopicListener(topicName,messageTypes));
    }
    
    public void listenToQueue(String queueName) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();
        listeners.add(new ManagedQueueListener(queueName));
    }
    
    public void addMessageHandler(MessageHandler mh) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();
        for(MessageProcessor processor : mh.getMessageProcessors() ) {
            addMessageProcessor(processor.getType(), processor);
        }
    }
    
    private void addMessageProcessor(String type, MessageProcessor messageProcessor) {
        List<MessageProcessor> messageProcessorList = messageProcessors.get(type);
        if (messageProcessorList == null) {
            messageProcessorList = new ArrayList<MessageProcessor>();
        }
        messageProcessorList.add(messageProcessor);
        messageProcessors.put(type, messageProcessorList);
    }
   
    private void processMessage(MessageAction action, String type, Data data) {
        List<MessageProcessor> messageProcessorList = messageProcessors.get(type);
        if (messageProcessorList != null) {
            for (MessageProcessor mp : messageProcessorList) {
                try {
                    mp.execute(action, data);
                } catch (Exception ex) {
                    LOGGER.error("Error processing message", ex);
                }
            }
        }
    }
    
    private void processBytesMessage(BytesMessage message) {
        try {
            byte[] by = new byte[(int) message.getBodyLength()];
            message.readBytes(by);

            MessageBPB messageBPB = MessageBPB.parseFrom(by);

            MessageInfo msgInfo = new MessageInfo(messageBPB);
            MessageContext.setMessage(msgInfo);
            
            while (msgInfo.hasMoreContent()) {
                ContentPB content = msgInfo.getNextContent();
                Data data = Data.newPBData(content.getContent().toByteArray());
                String type = content.getType();
                processMessage(msgInfo.getAction(), type, data);
            }
        } catch (Exception e) {
            LOGGER.error("Error processBytesMessage", e);
        } finally {
            MessageContext.clearMessage();
        }
    }
    
    public void shutdown() {
        for (ManagedListener listener : listeners) {
            listener.stop();
        }
        listeners.clear();
        messageProcessors.clear();
    }
    
    // Message Listeners
    
    private abstract class ManagedListener extends AbstractMessageListener {
        protected final String destName;

        public ManagedListener(String destName) {
            this.destName = destName;
            super.connect();
        }

        public ManagedListener(String destName, Collection<MessageType> messageTypes) {
            this.destName = destName;
            super.connect(messageTypes);
        }

        public ManagedListener(String destName, String messageSelector) {
            this.destName = destName;
            super.connect(messageSelector);
        }
        
        @Override
        public void onMessage(Message msg) {
            try {
                if (msg instanceof BytesMessage) {
                    processBytesMessage((BytesMessage) msg);
                } else {
                    LOGGER.error("Unsupported message type: {}", msg.getClass().getName());
                }
                msg.acknowledge();
            } catch (Exception e) {
            }
        }
    }

    private class ManagedQueueListener extends ManagedListener {
        public ManagedQueueListener(String destName) {
            super(destName);
        }

        public ManagedQueueListener(String destName, Collection<MessageType> messageTypes) {
            super(destName,messageTypes);
        }

        public ManagedQueueListener(String destName, String messageSelector) {
            super(destName,messageSelector);
        }
        
        @Override
        public Destination getDestination(Session session) throws Exception {
            return session.createQueue(destName);
        }
    }
    
    private class ManagedTopicListener extends ManagedListener {
        public ManagedTopicListener(String destName) {
            super(destName);
        }

        public ManagedTopicListener(String destName, Collection<MessageType> messageTypes) {
            super(destName,messageTypes);
        }

        public ManagedTopicListener(String destName, String messageSelector) {
            super(destName,messageSelector);
        }
        
        @Override
        public Destination getDestination(Session session) throws Exception {
            return session.createTopic(destName);
        }
    }
}
