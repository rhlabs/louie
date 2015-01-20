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
package com.rhythm.louie.request;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;

import com.rhythm.pb.RequestProtos.RoutePB;

/**
 *
 * @author cjohnson
 */
public class RequestProperties {
    private String remoteAddress;
    private int localPort;
    private String gateway;
    private String hostIp;

    public static RequestProperties fromHttpRequest(HttpServletRequest req, String hostIp) {
        RequestProperties props = new RequestProperties();
        props.remoteAddress = req.getRemoteAddr();
        props.localPort = req.getLocalPort();
        props.gateway = req.getContextPath().substring(1);
        props.hostIp = hostIp;
        props.remoteUser = req.getRemoteUser();
        props.authenticated = props.remoteUser != null;
        return props;
    }
    
    public static RequestProperties fromHttpRequest(HttpServletRequest req) {
        String hostIp;
        try {
            hostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LoggerFactory.getLogger(RequestProperties.class).error("Error retrieving localhost IP", ex);
            hostIp = "unknown";
        }
        return fromHttpRequest(req, hostIp); 
    }
    
    private RequestProperties() {}

    private boolean authenticated;
    private String remoteUser;
    
    /**
     * @return the remoteAddress
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * @return the localPort
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * @return the current gateway
     */
    public String getGateway() {
        return gateway;
    }
    
    /**
     * @return the hostIp
     */
    public String getHostIp() {
        return hostIp;
    }
    
    public RoutePB createRoute(String service) {
        RoutePB route = RoutePB.newBuilder()
                        .setHostIp(hostIp)
                        .setGateway(gateway)
                        .setService(service)
                        .build();
        return route;
    }
    
//    protected void setAuthenticated(String remoteUser) {
//        this.remoteUser = remoteUser;
//        this.authenticated = (this.remoteUser != null);
//    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public String getRemoteUser() {
        return remoteUser;
    }
}
