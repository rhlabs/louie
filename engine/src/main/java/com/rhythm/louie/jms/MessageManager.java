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


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.jms.JmsProtos.ContentPB;
import com.rhythm.louie.jms.JmsProtos.MessageBPB;
import com.rhythm.louie.request.data.Data;

/**
 *
 * @author cjohnson
 */
public class MessageManager {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    
    private static JmsAdapter jmsAdapter = null;
    
    private final Map<String, List<MessageProcessor>> messageProcessors = new ConcurrentHashMap<>();
    
    private final List<ManagedListener> listeners = new ArrayList<>();
    
    private MessageManager() {}
    
    public static MessageManager getInstance() {
        return MessageManagerHolder.INSTANCE;
    }

    private static class MessageManagerHolder {
        private static final MessageManager INSTANCE = new MessageManager();
    }
    
    synchronized private void loadJMSAdapterIfNeeded() throws MessageAdapterException {
        if (jmsAdapter == null) {
            loadJMSAdapter();
        }
    }
    
    private void loadJMSAdapter() throws MessageAdapterException {
        if (MessagingProperties.getAdapterClass() == null) {
            throw new MessageAdapterException("A message server adapter class "
                + "must be specified in the service configs!");
        }
        LOGGER.info("Loading JmsAdapter: {}", MessagingProperties.getAdapterClass());
       
        try {
            jmsAdapter = (JmsAdapter) Class.forName(MessagingProperties.getAdapterClass()).newInstance();
        } catch (ClassCastException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new MessageAdapterException(ex);
        }
        
        Map<String,String> configHash = new HashMap<>();
        configHash.put(JmsAdapter.HOST_KEY, MessagingProperties.getHost());
        configHash.put(JmsAdapter.PORT_KEY, MessagingProperties.getPort());
        configHash.put(JmsAdapter.FAILOVER_KEY, MessagingProperties.getFailover());
        jmsAdapter.configure(configHash);
    }
    
    public static JmsAdapter getAdapter() {
        return jmsAdapter;
    }
    
    // TODO toggle between server and client registration by an internal flag, rather than exposing
    // both interfaces.  Should just have a single registerListener call
    public void registerClientListener(String service, MessageHandler mh) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();

        listenTo(MessagingProperties.getClientType(),
                 MessagingProperties.getClientPrefix()+ service);
        
        addMessageHandler(mh);
    }
    
    public void registerServerListener(String service, MessageHandler mh) throws MessageAdapterException {
        loadJMSAdapterIfNeeded();

        listenTo(MessagingProperties.getServerType(),
                 MessagingProperties.getServerPrefix()+ service);
        
        addMessageHandler(mh);
    }
    
    private void listenTo(String destinationType, String destinationName) {
        switch (destinationType) {
            case "queue":
                listeners.add(new ManagedQueueListener(destinationName));
                break;
            case "topic":
                listeners.add(new ManagedTopicListener(destinationName));
                break;
            default:
                LOGGER.error("Unable to create message! Unknown Message Type: {}", destinationType);
        }
    }
    
    private void addMessageHandler(MessageHandler mh) throws MessageAdapterException {
        for(MessageProcessor processor : mh.getMessageProcessors() ) {
            addMessageProcessor(processor.getType(), processor);
        }
    }
    
    private void addMessageProcessor(String type, MessageProcessor messageProcessor) {
        List<MessageProcessor> messageProcessorList = messageProcessors.get(type);
        if (messageProcessorList == null) {
            messageProcessorList = new ArrayList<>();
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
