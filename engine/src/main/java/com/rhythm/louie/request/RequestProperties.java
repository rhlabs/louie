/*
 * RequestProperties.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

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

    /**
     * @return the remoteAddress
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * @param remoteAddress the remoteAddress to set
     */
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * @return the localPort
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * @param localPort the localPort to set
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    /**
     * @param gateway record the gateway
     */
    public void setGateway(String gateway) {
        this.gateway =gateway;
    }

    /**
     * @return the current gateway
     */
    public String getGateway() {
        return gateway;
    }
    
    /**
     * @param ip record the ip of the server
     */
    public void setHostIp(String ip) {
        this.hostIp = ip;
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
