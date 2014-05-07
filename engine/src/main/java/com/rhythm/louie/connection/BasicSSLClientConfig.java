/*
 * BasicSSLClientConfig.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

/**
 *
 * @author eyasukoc
 */
public class BasicSSLClientConfig extends SSLClientConfig implements SSLConfig{

    private String gateway,clientCert,caKeystore;
    private int port;
    
    public BasicSSLClientConfig(String host) {
        setHost(host);
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public int getPort() {
        return port;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
    
    @Override
    public String getGateway() {
        return gateway;
    }

    public void setClientCertificate(String certificatePath) {
        this.clientCert = certificatePath;
    }
    
    @Override
    public String getClientCertificate() {
        return clientCert;
    }

    public void setCAKeyStore(String keystorePath) {
        this.caKeystore = keystorePath;
    }
    
    @Override
    public String getCAKeystore() {
        return caKeystore;
    }
    
}
