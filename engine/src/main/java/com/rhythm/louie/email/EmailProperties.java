/*
 * EmailProperties.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.email;

import org.jdom2.Element;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class EmailProperties {
    // property names
    private static final String SMTP_HOST = "smtp_host";

    // properties
    
    private static String smtpHost = "";
    
    public static String getSmtpHost() {
        return smtpHost;
    }
    
    public static void processEmailProperties(Element email) {
        for (Element prop : email.getChildren()) {
            String propName = prop.getName().toLowerCase();
            if (null != propName) {
                switch (propName) {
                    case SMTP_HOST:
                        smtpHost = prop.getText().trim();
                        break;
                    default:
                        LoggerFactory.getLogger(EmailProperties.class)
                                .warn("Unknown Email Property:{}", propName);
                        break;
                }
            }
        }
    }
}
