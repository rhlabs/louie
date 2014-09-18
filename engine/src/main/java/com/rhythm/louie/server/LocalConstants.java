/*
 * LocalConstants.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author cjohnson
 * Created: Jul 21, 2011 5:39:57 PM
 */
public class LocalConstants {
    public static final String HOST;
    public static final String HOSTDOMAIN;
    public static final String DOMAIN;
    public static final String IP;
    
    static {
        String host = "";
        String hostdomain = "";
        String domain = "";
        String address = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
            String[] components = host.split("\\.");
            host = components[0];
            InetAddress me = InetAddress.getLocalHost();
            hostdomain = me.getCanonicalHostName();
            address = me.getHostAddress();
            domain = hostdomain.replaceFirst(".*?\\.", "");
        } catch (UnknownHostException ex) {}
        HOST=host;
        HOSTDOMAIN=hostdomain;
        DOMAIN=domain;
        IP = address;
    }
}
