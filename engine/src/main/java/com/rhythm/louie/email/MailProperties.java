/*
 * MailProperties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.email;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;

import org.jdom2.Element;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class MailProperties {
    
     // Properties
    
    private static final String JNDI = "jndi";
    private static final String CUSTOM = "custom";
    private static final String PROPERTY = "property";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private static final Properties props = new Properties();
    
    public static Properties getProperties() {
        return props;
    }
    
//    <mail>
//         <jndi name="mail/Sesssion" />
//         <custom>
//            <property name="mail.smtp.host" value="mailhost.rhythm.com" />
//            <property name="mail.smtp.port" value="123" />
//         </custom>
//    </mail>
//                   
    private static String jndi = null;

    public static void processProperties(Element email) {
        for (Element prop : email.getChildren()) {
            String propName = prop.getName().toLowerCase();
            if (null != propName) {
                switch (propName) {
                    case JNDI:
                        jndi = prop.getText().trim();
                        break;
                    case CUSTOM:
                        for (Element customProp : prop.getChildren()) {
                            if (customProp.getName().equals(PROPERTY)) {
                                String name = customProp.getAttributeValue(NAME);
                                String value = customProp.getAttributeValue(VALUE);
                                if (name == null || value == null) {
                                    LoggerFactory.getLogger(MailProperties.class)
                                            .warn("Unable to set property, must specify name and value:{}={}", name, value);
                                } else {
                                    props.put(name.trim(), value.trim());
                                }
                            } else {
                                LoggerFactory.getLogger(MailProperties.class)
                                        .warn("Unknown Custom Element:{}", customProp.getName());
                            }
                        }
                        break;
                    default:
                        LoggerFactory.getLogger(MailProperties.class)
                                .warn("Unknown Mail Element:{}", propName);
                        break;

                }
            }
        }
    }

    // Session
    public static Session getSession() throws MessagingException {
        if (jndi != null) {
            try {
                InitialContext ic = new InitialContext();
                return (Session) ic.lookup(jndi);
            } catch (Exception e) {
                throw new MessagingException("Unable to load mail session from JNDI", e);
            }
        } else {
            return loadSessionForProps();
        }
    }
    
    private static Session session = null;
    private static synchronized Session loadSessionForProps() {
        if (session ==null) {
            session = Session.getInstance(props);
        }
        return session;
    }
}
