/*
 * SSLConfigInterface.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author eyasukoc
 */
public interface SSLConfig {

    public SSLSocketFactory getSSLSocketFactory() throws IOException, CertificateException, 
            GeneralSecurityException;
    
    public void setSSLPass(String pass);
    
    public void setSSLCAPass(String pass);
    
    public void setHost(String host);
    
    public String getHost();
    
    public int getPort();
    
    public String getGateway();
    
    public String getClientCertificate();
    
    public String getCAKeystore();
    
}
