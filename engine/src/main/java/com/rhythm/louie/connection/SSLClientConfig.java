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
        FileInputStream clientCert = null;
        try {
            clientCert = new FileInputStream(getClientCertificate());
            ksClient.load(clientCert, password); 
        } finally {
            if (clientCert != null) clientCert.close();
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); 
        kmf.init(ksClient, password); 
        KeyStore ksCACert = KeyStore.getInstance(KeyStore.getDefaultType()); 
        FileInputStream caKS = null;
        try {
            caKS = new FileInputStream(getCAKeystore());
            ksCACert.load(caKS, caPassword); 
        } finally {
            if (caKS != null) caKS.close();
        }
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
