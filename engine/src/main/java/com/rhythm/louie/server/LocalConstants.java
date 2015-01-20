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
