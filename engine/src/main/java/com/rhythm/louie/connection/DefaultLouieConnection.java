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

import javax.net.ssl.HttpsURLConnection;

import java.util.concurrent.atomic.AtomicInteger;

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
            
    private static int PORT = 8080;
    public static final int AUTH_PORT = 8787;
    private static int SSL_PORT = 8181;
    
    private static final String AUTH_SERVICE = "auth";
    private static final AtomicInteger txId = new AtomicInteger(0);
    
    private IdentityPB identity;
    private String host;
    private SessionKey key;
    private URL louieURL;
    private URL authURL;
    private URL sslURL;
    
    private boolean requestOnSSL = false;
    private SSLConfig sslConfig;
    
    private int retryWait = 2000; //milliseconds
    private int maxTimeout = 30; //seconds
    private boolean enableRetry = true; //retry on by default
    private boolean lockoffRetry = false; //disable retries after timeout window reached. global lock that is disabled by a succesful request
    
    private String gateway = "louie";
    
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
            this.gateway = gateway;
        }
        this.louieURL = getPBURL();
        this.authURL = getAuthURL();
        this.sslURL = getSecurePBURL();
        if (key != null) {
            this.key = SessionKey.newBuilder().setKey(key).build();
        }
    }
    
    static public int sslPort() {
        return SSL_PORT;
    }
    
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
            sslURL = getSecurePBURL();
        }
        if(sslConfig.getGateway() != null) {
            setGateway(sslConfig.getGateway());
        }
    }
    
    @Override
    public void setGateway(String gateway) {
        this.gateway = gateway;
        louieURL = getPBURL();
        authURL = getAuthURL();
        sslURL = getSecurePBURL();
    }
    

    private URL getAuthURL() {
        return getUrl("http://"+this.host+":"+AUTH_PORT+"/"+this.gateway+"/pb");
    }
    
    private URL getPBURL() {
        return getUrl("http://"+this.host+":"+PORT+"/"+this.gateway+"/pb");
    }
    
    private URL getSecurePBURL() {
        return getUrl("https://"+this.host+":"+SSL_PORT+"/"+this.gateway+"/pb");
    }
    
    private URL getJsonURL() {
        return getUrl("http://"+this.host+":"+PORT+"/"+this.gateway+"/json");
    }
    
    private URL getSecureJsonURL() {
        return getUrl("https://"+this.host+":"+SSL_PORT+"/"+this.gateway+"/json");
    }
    
    private URL getUrl(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating URL: {}\n{}",urlStr, e);
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
            key = performRequest(AUTH_SERVICE,"createSession", PBParam.singleParam(getIdentity()),
                SessionKey.getDefaultInstance()).getSingleResult();
        }
        return key;
    }
    
    @Override
    public <T extends Message> Response<T> request(com.rhythm.louie.connection.RequestParams<T> req) throws Exception {  //TODO replace. This is a hack while I update some shit.
        return this.request(req.getSystem(), req.getCmd(), req.getParam(), req.getTemplate());
    }
    
    @Override
    public <T extends Message> Response<T> request(String service,String cmd, PBParam param,T template) throws Exception { 
        boolean requestFailure = true;
        long elapsedTime = 0;
        long maxTime = maxTimeout*1000;
        
        while (requestFailure){ 
            try {
                Response<T> ret = performRequest(service,cmd,param,template);
                lockoffRetry = false;
                return ret;
            } catch (HttpException e) {
                if (e.getErrorCode()==407) {
                    key = null;
                    return performRequest(service,cmd,param,template);
                } else {
                    LOGGER.error(e.getMessage());
                    throw e;
                }
                
            } catch (BouncedException e){
                if (elapsedTime >= maxTime || !enableRetry || lockoffRetry){
                    lockoffRetry = true;
                    throw e;
                }
                LOGGER.warn("{}  ...retrying request {}:{}.{}...", 
                        e.getMessage(),host,service,cmd);
                Thread.sleep(retryWait);
                elapsedTime += retryWait;
            }  
        }
        return null;
    }
    
    private <T extends Message> Response<T> performRequest(String service,String cmd, PBParam param,T template) throws Exception { 
        if (param==null) {
            param = PBParam.EMPTY;
        }

        URLConnection connection;
        try{
            if (requestOnSSL) {
                try {
                    connection = getSecureConnection(getSecurePBURL());
                } catch (Exception e) {
                    LOGGER.error("Error creating secure connection", e);
                    throw new HttpsException("Error Connecting via HTTPS. Please verify certificates and passwords.");
                }
            } else if (service.equals(AUTH_SERVICE)) {
                try {
                    connection = getConnection(getAuthURL());
                } catch (Exception e) {
                    LOGGER.warn("Error Connecting to Auth Port, Falling back to louieport");
                    connection = getConnection(getPBURL());
                }
            } else {
                connection = getConnection(getPBURL());
            }
            connection.connect();
        } catch (HttpsException e) {
            throw e;
        } catch (Exception e) {
            throw new BouncedException(e);
        } 
        
        // Build and Write Request Header
        RequestHeaderPB.Builder headerBuilder = RequestHeaderPB.newBuilder();
        headerBuilder.setCount(1);
        //headerBuilder.setAgent("Unknown");
        if (!service.equals(AUTH_SERVICE) || !cmd.equals("createSession")) {
            headerBuilder.setKey(getSessionKey()); 
        }
        headerBuilder.build().writeDelimitedTo(connection.getOutputStream()); 

        // Build and Write Request
        RequestPB.Builder reqBuilder = RequestPB.newBuilder();
        reqBuilder.setId(txId.incrementAndGet())
                  .setService(service)
                  .setMethod(cmd);
        
        // Only send routing info if this is not a auth call
        Request currentRequest = RequestContext.getRequest();
        if (currentRequest != null && !service.equals(AUTH_SERVICE)) {
            if (currentRequest.getRequest().hasRouteUser()) {
                reqBuilder.setRouteUser(currentRequest.getRequest().getRouteUser());
            } else if (currentRequest.getIdentity() != null) {
                    // The identity should be set, so this check should not be needed.
                // TODO determine how the identity could be null...
                // (identity could be null if key in request is null, but that should not be happening either

                // Set the Routed User, as the current User is may be "LoUIE"
                reqBuilder.setRouteUser(currentRequest.getIdentity().getUser());
            }
            // Append any routes you been on and the current Route
            reqBuilder.addAllRoute(currentRequest.getRequest().getRouteList());
            reqBuilder.addRoute(currentRequest.getRoute());
        }
            
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

        BufferedInputStream input = new BufferedInputStream(connection.getInputStream());

        // Read in the Response Header
        ResponseHeaderPB responseHeader = ResponseHeaderPB.parseDelimitedFrom(input);
        
        if (responseHeader.getCount()!=1) {
            throw new Exception("Received more than one response! This is unsupported behavior.");
        }

        // Read in each Response
        ResponsePB response = ResponsePB.parseDelimitedFrom(input);
        if (response.hasError()) {
            throw new Exception(response.getError().getDescription());
        }
        
        Response<T> result = new LouieResponse<T>(response, template, input);
        
        input.close();

        if (currentRequest != null && !service.equals(AUTH_SERVICE)) {
            currentRequest.addDestinationRoutes(response.getRouteList());
        }
        
        return result;
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

    @Override
    public URLConnection getJsonForwardingConnection() throws BouncedException, HttpsException {
        URLConnection connection;                                                                                                
        try{                                                                                                                     
            if (requestOnSSL) {                                                                                                  
                try {                                                                                                            
                    connection = getSecureConnection(getSecureJsonURL());                                                        
                } catch (Exception e) {                                                                                          
                    e.printStackTrace();                                                                                         
                    throw new HttpsException("Error Connecting via HTTPS. Please verify certificates and passwords and incoming/outgoing ports.");
                }                                                                                                                                 
            } else {                                                                                                                              
                connection = getConnection(getJsonURL());                                                                                         
            }                                                                                                                                     
        } catch (HttpsException e) {                                                                                                              
            throw e;                                                                                                                              
        } catch (Exception e) {                                                                                                                   
            throw new BouncedException(e);                                                                                                        
        }                                                                                                                                         

        return connection;
    }

    @Override
    public URLConnection getForwardingConnection() throws BouncedException, HttpsException {
                                                                     
        URLConnection connection;                                   
        try{                                                         
            if (requestOnSSL) {                                      
                try {                                                
                    connection = getSecureConnection(getSecurePBURL());
                } catch (Exception e) {                              
                    e.printStackTrace();                             
                    throw new HttpsException("Error Connecting "
                            + "via HTTPS. Please verify certificates and passwords and incoming/outgoing ports.");
                }                                                                                                                                 
            } else {                                                                                                                              
                connection = getConnection(getPBURL());                                                                                             
            }                                                                                                                                     
        } catch (HttpsException e) {                                                                                                              
            throw e;                                                                                                                              
        } catch (Exception e) {                                                                                                                   
            throw new BouncedException(e);                                                                                                        
        }                                                                                                                                         

        return connection;

    }

    @Override
    public void setPort(int port) {
        PORT = port;
        louieURL = getPBURL();
        authURL = getAuthURL();
        sslURL = getSecurePBURL();
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
