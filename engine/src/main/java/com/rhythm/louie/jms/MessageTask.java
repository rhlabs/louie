/*
 * MessageTask.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.*;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.jms.JmsProtos.*;

/**
 *
 * @author sfong
 */
public class MessageTask implements Runnable {
    private static final String TYPE = "type";
    
    private final Logger LOGGER = LoggerFactory.getLogger(MessageTask.class);
            
    private final String service;
    private final JmsAdapter jmsAdapter;
    private final MessageAction action;
    private final Collection<Message> pbList;
    private final Map<String,String> headers;

    public MessageTask(String service, JmsAdapter jmsAdapter, MessageAction action, 
            Collection<Message> pbList, Map<String,String> headers) {
        this.service = service;
        this.jmsAdapter = jmsAdapter;
        this.action = action;
        this.pbList = pbList;
        if (headers==null) {
            this.headers = Collections.emptyMap();
        } else {
            this.headers = headers;
        }
    }
    
    public MessageBPB createMessage(MessageAction action, Message pb) {
        return createMessage(action, Collections.singletonList(pb));
    }
    
    public MessageBPB createMessage(MessageAction action, Collection<Message> pbList) {
        List<ContentPB> contentList = new ArrayList<>();
        for (Message pb : pbList) {
            ContentPB content = ContentPB.newBuilder()
                .setType(pb.getDescriptorForType().getFullName())
                .setContent(pb.toByteString())
                .build();
            contentList.add(content);
        }
        
        MessageBPB message = MessageBPB.newBuilder()
                .setAction(action.getAction())
                .addAllContent(contentList)
                .setTimestamp(System.currentTimeMillis())
                .build();
        return message;
    }
    
    @Override
    public void run() {
        Connection connect = null;
        try {
            Session session;
            Destination dest;
            String destinationName = MessagingProperties.getUpdatePrefix()+service;
            String destinationType = MessagingProperties.getUpdateType();
            // TODO ideally this should be more modular
            switch (destinationType) {
                case "queue":
                    QueueConnection queueConnect = jmsAdapter.getQueueConnectionFactory().createQueueConnection();
                    connect = queueConnect;
                    session = queueConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                    dest = session.createQueue(destinationName);
                    break;
                case "topic":
                    TopicConnection topicConnect = jmsAdapter.getTopicConnectionFactory().createTopicConnection();
                    connect = topicConnect;
                    session = topicConnect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                    dest = session.createTopic(destinationName);
                    break;
                default:
                    LOGGER.error("Unable to create message! Unknown Message Type: {}", destinationType);
                    return;
            }
            
            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            
            StringBuilder type = new StringBuilder();
            for (Message pb : pbList) {
                type.append(pb.getDescriptorForType().getFullName()).append(" ");
            }
            MessageBPB pb = createMessage(action, pbList);
            int size = pb.getSerializedSize();
            byte[] data = new byte[size];

            CodedOutputStream codedOutput = CodedOutputStream.newInstance(data);
            pb.writeTo(codedOutput);

            BytesMessage msg = session.createBytesMessage();
            msg.writeBytes(data);
            msg.setStringProperty(TYPE, type.toString());
            
            for (Map.Entry<String,String> header : headers.entrySet()) {
                if (header.getKey().equals(TYPE)) {
                    LOGGER.warn("Skipping setting of reserved message property {}={}",
                            header.getKey(),header.getValue());
                } else {
                    msg.setStringProperty(header.getKey(), header.getValue());
                }
            }
            
            // TODO this should be a property
            //set message time-out to 24 hours
            producer.send(msg, DeliveryMode.PERSISTENT, 4, 86400000);
        } catch (JMSException | IOException e) {
            LOGGER.error("Error sending message",e);
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
}
