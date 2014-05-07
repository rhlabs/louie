/*
 * MessageTask.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jms.*;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.pb.jms.JmsProtos.ContentPB;
import com.rhythm.pb.jms.JmsProtos.MessageBPB;

/**
 *
 * @author sfong
 */
public class MessageTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageTask.class);
            
    private final String service;
    private final ActiveMQConnectionFactory louieTCF;
    private final MessageAction action;
    private final Collection<Message> pbList;

    public MessageTask(String service, ActiveMQConnectionFactory louieTCF, MessageAction action, Collection<Message> pbList) {
        this.service = service;
        this.louieTCF = louieTCF;
        this.action = action;
        this.pbList = pbList;
    }
    
    public MessageBPB createMessage(MessageAction action, Message pb) {
        return createMessage(action, Collections.singletonList(pb));
    }
    
    public MessageBPB createMessage(MessageAction action, Collection<Message> pbList) {
        List<ContentPB> contentList = new ArrayList<ContentPB>();
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
        QueueConnection connect = null;
        try {
            connect = louieTCF.createQueueConnection();
            QueueSession session = connect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(service + "Update"));

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
            msg.setStringProperty("type", type.toString());
            
            //set message time-out to 24 hours
            producer.send(msg, DeliveryMode.PERSISTENT, 4, 86400000);
        } catch (Exception e) {
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
