/*
 * RequestProperties.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
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
    
}
