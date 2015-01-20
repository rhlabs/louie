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

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.Constants;
import com.rhythm.louie.exception.LouieRequestException;
import com.rhythm.louie.exception.LouieResponseException;
import com.rhythm.louie.pb.PBParam;
import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.RequestContextManager;
import com.rhythm.louie.request.data.Data;
import com.rhythm.louie.server.Server;
import com.rhythm.louie.services.auth.AuthService;
import com.rhythm.louie.stream.*;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:23:53 PM
 */
public class DefaultLouieConnection implements LouieConnection {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultLouieConnection.class);
            
    private static final String AUTH_SERVICE = AuthService.SERVICE_NAME;
    private static final AtomicInteger txId = new AtomicInteger(0);
    
    private IdentityPB identity;
    private String host;
    private SessionKey key;

    private int port = 8080;
    private int auth_port = 8787;
    private int ssl_port = 8181;
    
    private boolean authBehaviorEnabled = false;
    private boolean requestOnSSL = false;
    private SSLConfig sslConfig;
    
    private int retryWait = 2000; //milliseconds
    private int maxTimeout = 30; //seconds
    private boolean retry = true; //retry on by default
    private boolean lockoffRetry = false; //disable retries after timeout window reached. global lock that is disabled by a succesful request
    
    private String gateway = Constants.DEFAULT_GATEWAY;
    
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
        if (key != null) {
            this.key = SessionKey.newBuilder().setKey(key).build();
        }
    }
    
    protected DefaultLouieConnection(IdentityPB identity, Server server) {
        this.identity = identity;
        this.host = server.getExternalIp();
        this.gateway = server.getGateway();
        if (server.getPort() != 0) {
            this.port = server.getPort();
        }
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
            ssl_port = sslConfig.getPort();
        }
        if(sslConfig.getGateway() != null) {
            setGateway(sslConfig.getGateway());
        }
    }
    
    @Override
    public void setGateway(String gateway) {
        if (gateway==null) {
            this.gateway = Constants.DEFAULT_GATEWAY;
        } else {
            this.gateway = gateway;
        }
    }
    
    @Override
    public String getGateway() {
        return gateway;
    }
    
    @Override
    public void enableAuthPort(boolean enable){
        this.authBehaviorEnabled = enable;
    }
    
    private URL getAuthURL() {
        return getUrl("http://"+host+":"+auth_port+"/"+gateway+"/pb");
    }
    
    private URL getPBURL() {
        return getUrl("http://"+host+":"+port+"/"+gateway+"/pb");
    }
    
    private URL getSecurePBURL() {
        return getUrl("https://"+host+":"+ssl_port+"/"+gateway+"/pb");
    }
    
    private URL getJsonURL() {
        return getUrl("http://"+host+":"+port+"/"+gateway+"/json");
    }
    
    private URL getSecureJsonURL() {
        return getUrl("https://"+host+":"+ssl_port+"/"+gateway+"/json");
    }
    
    private URL getUrl(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating URL: {}\n{}",urlStr, e);
            return null;
        }
    }
    
    private Boolean first = true;
    private URLConnection getConnection() throws Exception {
        URL url;
        if (first && authBehaviorEnabled) {
            url = getAuthURL();
            first = false;
        } else {
            url = getPBURL();
        }
         
        URLConnection connection = url.openConnection();
        
        // Prepare for both input and output
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Turn off caching
        connection.setUseCaches(false);

        // Set Content Type
        connection.setRequestProperty("Content-Type", "application/x-protobuf");
        
        connection.setReadTimeout(30*1000);
        connection.setConnectTimeout(15*1000);
        
        return connection;
    }
    
    @Deprecated //in favor of swapping urls (really ports) dynamically
    private URLConnection getConnection(URL url) throws Exception {
        URLConnection connection = url.openConnection();
        
        // Prepare for both input and output
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Turn off caching
        connection.setUseCaches(false);

        // Set Content Type
        connection.setRequestProperty("Content-Type", "application/x-protobuf");
        
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
        connection.setHostnameVerifier(new LouieHostVerifier());
        connection.setSSLSocketFactory(sslConfig.getSSLSocketFactory());
        
        return connection;
    }
    
    private class LouieHostVerifier implements HostnameVerifier{

        @Override
        public boolean verify(String string, SSLSession ssls) {
            //TODO check hostname using OCSP or via some crl that we have access to somehow?
            return true;
        }
        
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
            SingleConsumer<SessionKey> con = Consumers.newSingleConsumer();
            Request<SessionKey> req = Request.newParams(con, AUTH_SERVICE, "createSession", PBParam.singleParam(getIdentity()), SessionKey.getDefaultInstance());
            executeRequest(req);
            key = con.get();
        }
        return key;
    }
    
    @Override
    public <T extends Message> Response request(Request<T> req) throws Exception {  
        long elapsedTime = 0;
        long maxTime = maxTimeout*1000;
        Response response = null;
        
        while (true){ 
            try {
                response = executeRequest(req);
                lockoffRetry = false;
                break;
            } catch (HttpException e) {
                if (e.getErrorCode()==407) {
                    key = null;
                } else {
                    LOGGER.error(e.getMessage());
                    throw e;
                }
            } catch (BouncedException e){
                if (elapsedTime >= maxTime || !retry || lockoffRetry){
                    lockoffRetry = true;
                    throw e;
                }
                LOGGER.warn("{}  ...retrying request {}:{}.{}...", 
                        e.getMessage(),host,req.getService(),req.getCommand());
                Thread.sleep(retryWait);
                elapsedTime += retryWait;
            }  
        }
       return response;
    }
    
    private <T extends Message> Response executeRequest(Request<T> req) throws HttpException, BouncedException, 
            HttpsException, IOException, LouieRequestException, LouieResponseException { 
        
        URLConnection connection;
        String service = req.getService();
        String command = req.getCommand();
        try{
            if (requestOnSSL) {
                try {
                    connection = getSecureConnection(getSecurePBURL());
                } catch (Exception e) {
                    LOGGER.error("Error creating secure connection", e);
                    throw new HttpsException("Error Connecting via HTTPS. Please verify certificates and passwords.");
                }
            } else {
                connection = getConnection();
            }
            connection.connect();
        } catch (Exception e) {
            throw new BouncedException(e);
        } 
        
        // Build and Write Request Header
        RequestHeaderPB.Builder headerBuilder = RequestHeaderPB.newBuilder();
        headerBuilder.setCount(1);
        if (key == null) {
            headerBuilder.setIdentity(getIdentity());
        } else if (!service.equals(AUTH_SERVICE) || !command.equals("createSession")) { //lame extra check
            headerBuilder.setKey(key);
        }

        RequestContext currentRequest = RequestContextManager.getRequest();
        try (OutputStream output = connection.getOutputStream()) {
            headerBuilder.build().writeDelimitedTo(output); 

            // Build and Write Request
            RequestPB.Builder reqBuilder = RequestPB.newBuilder();
            reqBuilder.setId(txId.incrementAndGet())
                      .setService(service)
                      .setMethod(command);

            if (currentRequest != null) {
                // Only send route user info if it is enabled and this is not a auth call
                if (currentRequest.isRouteUserEnabled() && !service.equals(AUTH_SERVICE)) {
                    if (currentRequest.getRequest().hasRouteUser()) {
                        reqBuilder.setRouteUser(currentRequest.getRequest().getRouteUser());
                    } else if (currentRequest.getIdentity() != null) {
                        // The identity should be set, so this check should not be needed.
                        // TODO determine how the identity could be null...
                        // (identity could be null if key in request is null, but that should not be happening either

                        // Set the Routed User, as the current User is may be "LoUIE"
                        reqBuilder.setRouteUser(currentRequest.getIdentity().getUser());
                    }
                }
                // Append any routes you been on and the current Route
                reqBuilder.addAllRoute(currentRequest.getRequest().getRouteList());
                reqBuilder.addRoute(currentRequest.getRoute());
            }
            
            for (Message message : req.getParam().getArguments()) {
                reqBuilder.addType(message.getDescriptorForType().getFullName());
            }
            reqBuilder.build().writeDelimitedTo(output);

            // Write Data
            for (Message message : req.getParam().getArguments()) {
                message.writeDelimitedTo(output);
            }
        }

        // Cast to a HttpURLConnection in order to get response codes
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
                throw new BouncedException("Server "+connection.getURL()+" returned: " + Integer.toString(respCode));
            }
            if (respCode>=400) {
                throw new HttpException(httpConnection.getResponseCode(),httpConnection.getResponseMessage());
            }
        }

        ResponsePB response;
        try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream())) {
            // Read in the Response Header
            ResponseHeaderPB responseHeader = ResponseHeaderPB.parseDelimitedFrom(input);
            if (responseHeader.getCount()!=1) {
                throw new LouieRequestException("Received more than one response! This is unsupported behavior.");
            }
            if (responseHeader.hasKey()) {
                key = responseHeader.getKey();
            }

            // Read in each Response
            response = ResponsePB.parseDelimitedFrom(input);
            if (response.hasError()) {
                throw new LouieRequestException(response.getError().getDescription());
            }

            try {
                processResponse(req, response, input);
            } catch (Exception ex) {
                throw new LouieResponseException(ex);
            }
        }
        
        if (currentRequest != null && !service.equals(AUTH_SERVICE)) {
            currentRequest.addDestinationRoutes(response.getRouteList());
        }
        
        return new Response(response);
    }
    
    private <T extends Message> void processResponse(Request<T> request, ResponsePB response, InputStream input)  throws Exception {
        T templ = request.getTemplate();
        Consumer<T> consumer = request.getConsumer();
        consumer.informMessageCount(response.getCount());
        for (int d = 0; d < response.getCount(); d++) {
            Data data = Data.readPBData(input);
            if (data!=null) {
                if (templ!=null) {
                    consumer.consume(data.parse(templ));
                } else {
                    throw new Exception("Error Parsing Data: No Template!");
                }
            }
        }
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
        retry = enable;
    }
    
    @Override
    public boolean getRetryEnable(){
        return retry;
    }

    @Override
    public URLConnection getJsonForwardingConnection() throws BouncedException, HttpsException {
        URLConnection connection;                                                                                                
        try{                                                                                                                     
            if (requestOnSSL) {                                                                                                  
                try {                                                                                                            
                    connection = getSecureConnection(getSecureJsonURL());                                                        
                } catch (Exception e) {                
                    LoggerFactory.getLogger(DefaultLouieConnection.class).error("Error Connecting via HTTPS",e);
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
                    LoggerFactory.getLogger(DefaultLouieConnection.class).error("Error Connecting via HTTPS",e);
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
        this.port = port;
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
