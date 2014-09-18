/*
 * LouieSSLClientConfig.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.rhythm.louie.server.Server;

/**
 *
 * @author eyasukoc
 */
public class LouieSSLClientConfig extends SSLClientConfig implements SSLConfig{

    //all gets could return 0 or null, so check appropriately during usage.
    
    private static final String CERT_DEFAULT_PASS = "cbgbomfug";
    private static final String CA_KEYSTORE_DEFAULT_PASS = "cbgbomfug";
    
    private String gateway,clientCert,caKeystore;
    private int port;
    
    
    public LouieSSLClientConfig(Server server) throws Exception {
        setHost(server.getHostName());
        setSSLPass(CERT_DEFAULT_PASS);
        setSSLCAPass(CA_KEYSTORE_DEFAULT_PASS);
        String clientSSLCert = System.getProperty("clientSSLCert");
        String clientSSLCACert = System.getProperty("clientSSLCACert");
        if (clientSSLCert != null && clientSSLCACert != null){
            this.clientCert = clientSSLCert;
            this.caKeystore = clientSSLCACert;
            //check for passwords to override the hardcoded defaults.
            String localSSLPassword = Server.LOCAL.getSSLPassword();
            String localSSLCAPassword = Server.LOCAL.getSSLCAPassword();
            if (localSSLPassword != null) {
                setSSLPass(localSSLPassword);
            }
            if (localSSLCAPassword != null) {
                setSSLCAPass(localSSLCAPassword);
            }
            gateway = server.getSSLGateway(); 
            port = server.getSSLPort(); 
        } else {
            throw new Exception("No JVM args found for ssl certificates");
        }
    }
    
    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    @Override
    public String getClientCertificate() {
        return clientCert;
    }

    @Override
    public String getCAKeystore() {
        return caKeystore;
    }
    
    
}
