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
