/*
 * SSLClientConfiguration.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author eyasukoc
 */
public abstract class SSLClientConfig implements SSLConfig{
    
    private SSLSocketFactory socketFactory = null;
    
    private String sslPass = null;
    private String sslCAPass = null;
    
    private String hostName = null;
    
    @Override
    public SSLSocketFactory getSSLSocketFactory() throws IOException, CertificateException, 
            GeneralSecurityException {
        if (socketFactory != null){
            return socketFactory;
        } else {
            socketFactory = generateSocketFactory();
            return socketFactory;
        }
    }
    
    private SSLSocketFactory generateSocketFactory() throws IOException, CertificateException, 
            GeneralSecurityException {

        
        char[] password,caPassword;
        password = sslPass.toCharArray();
        caPassword = sslCAPass.toCharArray();
        
        KeyStore ksClient = KeyStore.getInstance("pkcs12"); 
        ksClient.load(new FileInputStream(getClientCertificate()), password); 
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); 
        kmf.init(ksClient, password); 
        KeyStore ksCACert = KeyStore.getInstance(KeyStore.getDefaultType()); 
        ksCACert.load(new FileInputStream(getCAKeystore()), caPassword); 
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(ksCACert); 
        SSLContext context = SSLContext.getInstance("TLS"); 
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null); 
        SSLSocketFactory sslSocketFactory = context.getSocketFactory(); 

        return sslSocketFactory;
    }
    
    @Override
    public void setSSLPass(String pass) {
        sslPass = pass;
    }
    
    @Override
    public void setSSLCAPass(String pass) {
        sslCAPass = pass;
    }
    
    @Override
    public void setHost(String host) {
        hostName = host;
    }
    
    @Override
    public String getHost() {
        return hostName;
    }
    
}
