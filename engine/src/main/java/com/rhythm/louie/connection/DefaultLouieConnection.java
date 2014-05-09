/*
 * LouieConnection.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.pb.PBParam;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.SessionKey;
import com.rhythm.pb.data.Request;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:23:53 PM
 */
public class DefaultLouieConnection implements LouieConnection {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultLouieConnection.class);
            
    public static final int PORT = 8080;
    public static final int AUTH_PORT = 8787;
    private static int SSL_PORT = 8181;
    
    private static final boolean AUTH_ENABLED = true;
    private static final String AUTH_SYSTEM = "auth";
    private static final AtomicInteger txId = new AtomicInteger(0);
    
    private IdentityPB identity;
    private String host;
    private SessionKey key;
    private URL louieURL;
    private URL authURL;
    private URL sslURL;
//    private String SSLCert;
//    private String SSLCACert;
    private boolean requestOnSSL = false;
    private SSLConfig sslConfig;
//    private SSLSocketFactory sslSocketFactory = null;
//    private String sslCertPass = null;
//    private String sslCAPass = null;
    
    private int maxTimeout = 30; //seconds
    private boolean enableRetry = true; //retry on by default
    private boolean lockoffRetry = false; //disable retries after timeout window reached. global lock that is disabled by a succesful request
    
    private String gateway = "louie/pb";
    
    protected DefaultLouieConnection(String host) {
        this(null, host, null);
    }

    protected DefaultLouieConnection(String host, String key) {
        this(null, host, key);
    }

    protected DefaultLouieConnection(IdentityPB identity,String host) {
        this(identity,host,null);
    }
    
    protected DefaultLouieConnection(IdentityPB identity,String host,String key) {
        this(identity,host,key,null);
    }
    
    protected DefaultLouieConnection(IdentityPB identity,String host,String key,String gateway) {
        this.identity = identity;
        this.host = host;
        if (gateway != null) {
            this.gateway = gateway + "/pb";
        }
        this.louieURL = getLouieURL(this.host, PORT);
        this.authURL = getLouieURL(this.host, AUTH_PORT);
        this.sslURL = getSecureLouieURL(this.host, SSL_PORT);
        if (key != null) {
            this.key = SessionKey.newBuilder().setKey(key).build();
        }
    }
    
    static public int sslPort() {
        return SSL_PORT;
    }

//    protected DefaultLouieConnection(IdentityPB identity, String host, String SSLCert, String SSLCACert, String sslPass, String sslCAPass, int sslPort) {
//        this(identity,host,null);
//        this.SSLCert = SSLCert;
//        this.SSLCACert = SSLCACert;
//        this.requestOnSSL = true;
//        if (sslCertPass != null) {
//            this.sslCertPass = sslPass;
//        }
//        if (sslCAPass != null) {
//            this.sslCAPass = sslCAPass;
//        }
//        if (sslPort != 0) {
//            SSL_PORT = sslPort;
//            setSSLURL(sslPort);
//        }
//    }
    
    protected DefaultLouieConnection(IdentityPB identity, SSLConfig sslConfig) {
        this(identity,sslConfig.getHost(),null);
        processSSLConfigs(sslConfig);
    }
    
    protected DefaultLouieConnection(IdentityPB identity, SSLConfig sslConfig, String key) {
        this(identity,sslConfig.getHost(),key);
        processSSLConfigs(sslConfig);
    }
    
    private void processSSLConfigs(SSLConfig sslConfig){
        this.requestOnSSL = true;
        this.sslConfig = sslConfig;
        if(sslConfig.getPort() != 0) {
            SSL_PORT = sslConfig.getPort();
            sslURL = getSecureLouieURL(host, SSL_PORT);
        }
        if(sslConfig.getGateway() != null) {
            setGateway(sslConfig.getGateway());
        }
    }
    
    @Override
    public void setGateway(String gateway) {
        this.gateway = gateway + "/pb";
        louieURL = getLouieURL(host, PORT);
        authURL = getLouieURL(host, AUTH_PORT);
        sslURL = getSecureLouieURL(host, SSL_PORT);
    }
    
//    private void setSSLURL(int port) {
//        sslURL = getSecureLouieURL(host, port);
//    }
    
    private URL getLouieURL(String host, int port) {
        String url = "http://"+host+":"+port+"/"+gateway;
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating Secure LoUIE URL!!!!! "+url, e);
            return null;
        }
    }
    
    private URL getSecureLouieURL(String host, int port) {
        String url = "https://"+host+":"+port+"/"+gateway;
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating Secure LoUIE URL!!!!! "+url, e);
            return null;
        }
    }
    
    private URLConnection getConnection(URL url) throws Exception {
        URLConnection connection = url.openConnection();
        
        // Prepare for both input and output
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Turn off caching
        connection.setUseCaches(false);

        // Set Content Type
        connection.setRequestProperty("Content-Type", "application/x-protobuf");
        //connection.setRequestProperty(, who)
        
        connection.setReadTimeout(30*1000);
        connection.setConnectTimeout(15*1000);
        
        connection.connect();
        
        return connection;
    }
    
    private URLConnection getSecureConnection(URL url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        //set configs for basic http stuff (see getConnection)
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/x-protobuf");
        connection.setReadTimeout(30*1000);
        connection.setConnectTimeout(15*1000);
        
//        if (sslSocketFactory == null) {
//            char[] password,caPassword;
//            if (sslCertPass != null) {
//                password = sslCertPass.toCharArray();
//            } else {
//                password = "cbgbomfug".toCharArray();
//            }
//            if (sslCAPass != null) {
//                caPassword = sslCAPass.toCharArray();
//            } else {
//                caPassword = "cbgbomfug".toCharArray();
//            }
//            KeyStore ksClient = KeyStore.getInstance("pkcs12"); 
//            ksClient.load(new FileInputStream(SSLCert), password); 
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); 
//            kmf.init(ksClient, password); 
//            KeyStore ksCACert = KeyStore.getInstance(KeyStore.getDefaultType()); 
//            ksCACert.load(new FileInputStream(SSLCACert), caPassword); 
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init(ksCACert); 
//            SSLContext context = SSLContext.getInstance("TLS"); 
//            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null); 
//            sslSocketFactory = context.getSocketFactory(); 
//        }
        connection.setSSLSocketFactory(sslConfig.getSSLSocketFactory());
        
        connection.connect();
        return connection;
    }
    
    @Override
    public IdentityPB getIdentity() {
        if (identity==null) {
            return Identity.getIdentity();
        }
        return identity;
    }
    
    @Override
    synchronized public SessionKey getSessionKey() throws Exception {
        // TODO add check for identity changes, and pass up new identity if needed
        
        if (key==null) {
           // AuthRemoteClient authClient = new AuthRemoteClient(this);
            key = performRequest(AUTH_SYSTEM,"createSession", PBParam.singleParam(getIdentity()),
                SessionKey.getDefaultInstance()).getSingleResult();
        }
        return key;
    }

    @Override
    public <T extends Message> Response<T> request(String system,String cmd, PBParam param,T template) throws Exception { 
        boolean requestFailure = true;
        int elapsedTime = 0;
        while (requestFailure){ 
            try {
                Response<T> ret = performRequest(system,cmd,param,template);
                lockoffRetry = false;
                return ret;
            } catch (HttpException e) {
                if (e.getErrorCode()==407) {
                    key = null;
                    return performRequest(system,cmd,param,template);
                } else {
                    LOGGER.error(e.getMessage());
                    throw e;
                }
                
            } catch (BouncedException e){
                if (elapsedTime >= maxTimeout || !enableRetry || lockoffRetry){
                    lockoffRetry = true;
                    throw e;
                }
                LOGGER.warn("{}  ...retrying request {}:{}.{}...", 
                        e.getMessage(),host,system,cmd);
                Thread.sleep(5000);
                elapsedTime += 5;
            }  
        }
        return null;
    }
    
    private <T extends Message> Response<T> performRequest(String system,String cmd, PBParam param,T template) throws Exception { 
        if (param==null) {
            param = PBParam.EMPTY;
        }

        URLConnection connection;
        try{
            if (requestOnSSL) {
                try {
                    connection = getSecureConnection(sslURL);
                } catch (Exception e) {
                    LOGGER.error("Error creating secure connection", e);
                    throw new HttpsException("Error Connecting via HTTPS. Please verify certificates and passwords.");
                }
            } else if (system.equals(AUTH_SYSTEM)) {
                try {
                    connection = getConnection(authURL);
                } catch (Exception e) {
                    LOGGER.warn("Error Connecting to Auth Port, Falling back to louieport");
                    connection = getConnection(louieURL);
                }
            } else {
                connection = getConnection(louieURL);
            }
        } catch (HttpsException e) {
            throw e;
        } catch (Exception e) {
            throw new BouncedException(e);
        } 
        
        // Build and Write Request Header
        RequestHeaderPB.Builder headerBuilder = RequestHeaderPB.newBuilder();
        headerBuilder.setCount(1);
        //headerBuilder.setAgent("Unknown");
        if (AUTH_ENABLED && !system.equals(AUTH_SYSTEM)) {
            headerBuilder.setKey(getSessionKey()); 
            
            // Only send routing info if this is not a auth call
            Request currentRequest = RequestContext.getRequest();
            if (currentRequest != null) {
                // The identity should be set, so this check should not be needed.
                // TODO determine how the identity could be null...
                // (identity could be null if key in request is null, but that should not be happening either
                if (currentRequest.getIdentity()!=null) {
                    // Set the Routed User, as the current User is most like "LoUIE"
                    headerBuilder.setRouteUser(currentRequest.getIdentity().getUser());
                }
                // Append any routes you been on and the current Route
                headerBuilder.addAllRoute(currentRequest.getHeader().getRouteList());
                headerBuilder.addRoute(currentRequest.getRoute());
            }
        }
        headerBuilder.build().writeDelimitedTo(connection.getOutputStream()); 

        // Build and Write Request
        RequestPB.Builder reqBuilder = RequestPB.newBuilder();
        reqBuilder.setId(txId.incrementAndGet())
                  .setSystem(system)
                  .setMethod(cmd);
            
        for (Message message : param.getArguments()) {
            reqBuilder.addType(message.getDescriptorForType().getFullName());
        }
        reqBuilder.build().writeDelimitedTo(connection.getOutputStream());

        // Write Data
        for (Message message : param.getArguments()) {
            message.writeDelimitedTo(connection.getOutputStream());
        }
        
        connection.getOutputStream().close();

        // Cast to a HttpURLConnection
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int respCode = 0;
            try{
                respCode = httpConnection.getResponseCode();
            } catch (SocketTimeoutException se){
                throw se;
            } catch (IOException e){
                throw new BouncedException(e);
            }
                
            if (respCode == 404 || respCode == 503){
                httpConnection.disconnect();
                throw new BouncedException("Server returned: " + Integer.toString(respCode));
            }
            if (httpConnection.getResponseCode()>=400) {
                throw new HttpException(httpConnection.getResponseCode(),httpConnection.getResponseMessage());
            }
        }

        List<Response<T>> allResponses = new ArrayList<Response<T>>();

        BufferedInputStream input = new BufferedInputStream(connection.getInputStream());

        // Read in the Response Header
        ResponseHeaderPB responseHeader = ResponseHeaderPB.parseDelimitedFrom(input);
        for (int r=0; r<responseHeader.getCount();r++) {
            // Read in each Response
            ResponsePB response = ResponsePB.parseDelimitedFrom(input);
            if (response.hasError()) {
                throw new Exception(response.getError().getDescription());
            }
            Response<T> result = new LouieResponse<T>(response,template,input);
            allResponses.add(result);
        }

        input.close();
        return allResponses.get(0);
    }
    
    @Override
    public void setMaxTimeout(int seconds) {
        maxTimeout = seconds;
    }
    
    @Override
    public int getMaxTimeout() {
        return maxTimeout;
    }
    
    @Override
    public void setRetryEnable(boolean enable){
        enableRetry = enable;
    }
    
    @Override
    public boolean getRetryEnable(){
        return enableRetry;
    }
    
    class HttpException extends Exception {
        private int httpCode;
        private String httpMessage;
        public HttpException(int code,String message) {
            super("HTTP Error "+code+" : " +message);
            httpCode = code;
            httpMessage = message;
        }
        
        public int getErrorCode() {
            return httpCode;
        }
        
        public String getErrorMessage() {
            return httpMessage;
        }
    }
    
    public class BouncedException extends Exception {
        public BouncedException(String message) {
            super(message);
        }

        private BouncedException(Exception e) {
            super(e.toString());
        }
    }
    
    class HttpsException extends Exception {
        public HttpsException(String message) {
            super(message);
        }
        
        public HttpsException(Exception e) {
            super(e.toString());
        }
    }
}