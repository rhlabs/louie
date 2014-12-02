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

    private static final Properties props = new Properties();
    
    public static Properties getProperties() {
        return props;
    }
    
//    <mail>
//         <jndi>mail/Session</jndi>
//         <custom>
//            <mail.smtp.host>mailhost.rhythm.com</mail.smpt.host>
//            <mail.smtp.port>123</mail.smtp.port>
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
                            props.put(customProp.getName(), customProp.getTextTrim());
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
