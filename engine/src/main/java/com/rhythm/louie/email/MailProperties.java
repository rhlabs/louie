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
