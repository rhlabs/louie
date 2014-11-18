/*
 * LouieSSLClientConfig.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.rhythm.louie.server.CustomProperty;
import com.rhythm.louie.server.LouieProperties;
import com.rhythm.louie.server.Server;

/**
 *
 * @author eyasukoc
 */
public class LouieSSLClientConfig extends SSLClientConfig implements SSLConfig{

    //all gets could return 0 or null, so check appropriately during usage.
    
//    private static final String CERT_DEFAULT_PASS = "cbgbomfug";
//    private static final String CA_KEYSTORE_DEFAULT_PASS = "cbgbomfug";
    private static final String SSL_CONFIG_KEY = "ssl";
    private static final String CERT_PATH_KEY = "cert_path";
    private static final String CA_PATH_KEY = "ca_path";
    
    private String gateway,clientCert,caKeystore;
    private int port;
    
    
    public LouieSSLClientConfig(Server server) throws Exception {
        setHost(server.getHostName());
        CustomProperty sslProps = LouieProperties.getCustomProperty(SSL_CONFIG_KEY);
        String certPath = sslProps.getProperty(CERT_PATH_KEY);
        String caPath = sslProps.getProperty(CA_PATH_KEY);
//        setSSLPass();
//        setSSLCAPass();
//        String clientSSLCert = System.getProperty("clientSSLCert");
//        String clientSSLCACert = System.getProperty("clientSSLCACert");
//        if (clientSSLCert != null && clientSSLCACert != null){
//            this.clientCert = clientSSLCert;
//            this.caKeystore = clientSSLCACert;
//            //check for passwords to override the hardcoded defaults.
//            String localSSLPassword = Server.LOCAL.getSSLPassword();
//            String localSSLCAPassword = Server.LOCAL.getSSLCAPassword();
//            if (localSSLPassword != null) {
//                setSSLPass(localSSLPassword);
//            }
//            if (localSSLCAPassword != null) {
//                setSSLCAPass(localSSLCAPassword);
//            }
//            gateway = server.getSSLGateway(); 
//            port = server.getSSLPort(); 
//        } else {
//            throw new Exception("No JVM args found for ssl certificates");
//        }
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
