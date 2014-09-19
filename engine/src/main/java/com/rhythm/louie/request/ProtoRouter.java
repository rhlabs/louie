/*                                                                                                                                                                     
 * ProtoProcessorLite.java                                                                                                                                                                                                                   
 *                                                                                                                                                                                                                                           
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.                                                                                                                                                                            
 */                                                                                                                                                                                                                                          
package com.rhythm.louie.request;                                                                                                                                                                                                            
                                                                                                                                                                                                                                             
                                                                                                                                                                                                                                             
import com.google.protobuf.CodedInputStream;                                                                                                                                                                                                 
import com.google.protobuf.CodedOutputStream;                                                                                                                                                                                                
import com.google.protobuf.Message;                                                                                                                                                                                                          

import com.rhythm.louie.server.Server;
import com.rhythm.louie.auth.AuthUtils;
import com.rhythm.louie.auth.SessionStat;
import com.rhythm.louie.auth.UnauthorizedSessionException;
import com.rhythm.louie.connection.LouieConnection;       
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.exception.LouieRouteException;
import com.rhythm.louie.topology.Route;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;      
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;      
import com.rhythm.pb.RequestProtos.ResponsePB;                 
import com.rhythm.pb.RequestProtos.RoutePB;                  
import com.rhythm.pb.RequestProtos.SessionKey;                   

import com.rhythm.louie.request.data.DataType;                            
import com.rhythm.louie.request.data.Result;                     

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProtoRouter allows a Louie instance to act as a "Louie Router" which is an instance
 * of Louie that can route calls to other Louie instances                             
 *                                                                                     
 * @author eyasukoc                                                                    
 */                                                                                    
public class ProtoRouter implements ProtoProcess{                                      
    
    Logger log = LoggerFactory.getLogger(ProtoRouter.class.getName());
                                                                      
    /**                                                               
     * This method is designed purely to split and forward calls to a set of 
     * hosts designated by the routing properties file ( RoutingProperties tentatively). 
     * The responses are collected and bundled together to be sent back at once on       
     * one OutputStream                                                                  
     *                                                                                   
     * For an explanation of what this replaces in a normal louie instance, see          
     * ProtoProcessor.processRequest                                                     
     *                                                                                   
     * @param externalInput InputStream from the originating client                      
     * @param externalOutput OutputStream from the originating client                    
     * @param props RequestProperties object from originating client                     
     * @return A list of Results containing any errors caught                            
     * @throws UnauthorizedSessionException                                              
     * @throws IOException                                                               
     * @throws Exception                                                                 
     */                                                                                  
    @Override                                                                            
    public List<Result> processRequest(InputStream externalInput, OutputStream externalOutput, RequestProperties props) throws Exception{
        long start = System.nanoTime();                                                                                                                                     
        long begin = start;                                                                                                                                                          
        List<Result> results = new ArrayList<Result>();                                                                                                                              
                                                                                                                                                                                     
        CodedInputStream externalCodedInput = CodedInputStream.newInstance(externalInput);                                                                                           
//        CodedOutputStream externalCodedOutput = CodedOutputStream.newInstance(externalOutput);                                                                                       
                                                                                                                                                                                     
        RequestHeaderPB header = RequestHeaderPB.parseDelimitedFrom(externalInput);                                                                                                                                                                            

        IdentityPB identity = null;
        SessionKey sessionKey = null;
        if (header.hasKey()) {     
            SessionStat session = AuthUtils.accessSession(header.getKey()); //there is something flawed here, about the way that i am trying to send in data, vs the way it's actually picking up a session
            identity = session.getIdentity();                              
        } else {
            if (header.hasIdentity()) {
                identity = header.getIdentity();
                sessionKey = AuthUtils.createKey(identity);
            }
        }                                                                  
                                                                           
        ResponseHeaderPB.Builder responseHeader = ResponseHeaderPB.newBuilder();
        responseHeader.setCount(header.getCount());
        if (sessionKey != null) {
            responseHeader.setKey(sessionKey);
        }
        responseHeader.build().writeDelimitedTo(externalOutput);
//        externalCodedOutput.writeRawVarint32(responseHeader.getSerializedSize());
//        responseHeader.writeTo(externalCodedOutput);                             
//        externalCodedOutput.flush();                                             
                                                                                 
                                                                                 
        for (int i = 0; i<header.getCount(); i++) {                              
            // per requestPB                                                     
            RequestPB request = RequestPB.parseDelimitedFrom(externalInput);     
            if (request == null) {
                throw new Exception("Improper Request format!  Reached EOF prematurely! @ProtoRouter.processRequest()");
            }

            /////////////////////// FIND A CONNECTION /////////////////////////
                                                                               
            String destinationService = request.getService();                   
            log.debug("ProtoRouter processing request");                       
            Server target = Route.get(destinationService);
            if (target == null) {
                target = Route.get("default");
            }
            
            RoutePB localRoute = props.createRoute(request.getService());
            for (RoutePB route : request.getRouteList()) {
                if (route.equals(localRoute)) {
                    throw new Exception("Route Loop Detected!");
                }
            }
            
            if (Server.LOCAL.equals(target)) {                                                                                  
                log.debug("Service {} was determined to be local!",destinationService);                                         
                // Treat as normal local service fetch                                                    
                RequestContext pbReq = null;                                                                     
                Result result = null;                                                                     

                try {
                    pbReq = new RequestContext(header, request,DataType.PB);
                    if (header.hasIdentity()) {
                        pbReq.setSessionKey(sessionKey);
                    }
                    pbReq.setIdentity(identity);                     
                    pbReq.readPBParams(externalInput);                         
                    pbReq.setRemoteAddress(props.getRemoteAddress());          
                    pbReq.setLocalPort(props.getLocalPort());                 
                    pbReq.setRoute(localRoute);
                    result = RequestHandler.processSingleRequest(pbReq);       
                    result.setExecTime((System.nanoTime()-start)/1000000);
                    handleResult(pbReq, result, externalOutput);           
                } catch (Exception e) {                                         
                    String errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                    log.error(errorMessage);                                            
                    if (result != null) {                                                        
                        result.addError(e);                                                      
                    } else {                                                                     
                        result = Result.errorResult(e);
                    }                                                                            
                } finally {                                                                      
                    long end = System.nanoTime();                                       
                    if (pbReq == null) {                                                         
                        log.error("Unknown Error, Request is null");                    
                    } else {                                                                     
                        if (result == null) {                                                    
                            result = Result.errorResult(null);
                        }                                                                        
                        result.setDuration((end - start)/1000000);                                         
                        try {                                                                    
                            RequestHandler.logRequest(pbReq, result);                            
                        } catch (Exception le) {                                                 
                            log.debug("Error Logging: {}",le.getMessage());                      
                        }                                                                        
                    }                                                                            
                    start = end;                                                                 
                }                                                                                
                results.add(result);                                                             
                                                                                                 
            } else {                                                                             
                log.debug("Service {} was determined NOT to be local",destinationService);

                CodedOutputStream externalCodedOutput = CodedOutputStream.newInstance(externalOutput);                                                                                                                 
                /* Connection legend:
                    * externalCodedInput = from client, to HERE
                    * externalCodedOutput = from HERE, to client
                    * responseInputStream = from target LoUIE, to HERE 
                    * codedOutput = from HERE, going to target LoUIE
                */
                long startTime = System.nanoTime();                                     
                LouieConnection louieConn = LouieConnectionFactory.getConnectionForServer(target);
//                louieConn = TopologyManager.getConnectionForService(key); // this get will fail prior to returning something into louieConn, i believe.
                if (louieConn == null) { //Service doesn't exist or something went wrong in forming it                                                 
                    log.error("Unable to create a LouieConnection to a target host");
                    System.out.println("Couldn't create a LouieConnection to target host");
                    //This might be unreachable due to service default mapping concept
                    ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();                                                                      
                    responseBuilder.setId(request.getId());                                                                                            

                    log.debug("No Such Service {}",destinationService);
                    responseBuilder.setError(ErrorPB.newBuilder()      
                            .setCode(500)                              
                            .setType("Exception")                      
                            .setDescription("No such service: " + destinationService));

                    ResponsePB response = responseBuilder.build();
                    externalCodedOutput.writeRawVarint32(response.getSerializedSize());
                    response.writeTo(externalCodedOutput);                             
                    externalCodedOutput.flush();                                       
                    continue;                                                          
                }                                                                      
                ////////////////// PUT DATA INTO THAT CONNECTION ///////////////////   
                URLConnection urlConn = louieConn.getForwardingConnection();
                try {
                    urlConn.connect();
                } catch (IOException ex) {
                    throw new LouieRouteException(ex);
                }
                
                CodedOutputStream codedOutput = CodedOutputStream.newInstance(urlConn.getOutputStream());

                RequestHeaderPB singleHeader = header.toBuilder()
                        .setCount(1)
                        .build();
                
                codedOutput.writeRawVarint32(singleHeader.getSerializedSize());
                singleHeader.writeTo(codedOutput); //add in the header         

                codedOutput.writeRawVarint32(request.getSerializedSize());
                request.writeTo(codedOutput); //add in the request        


                int typeCount = request.getTypeCount();  
                for (int k = 0; k<typeCount; k++) {  
                    int byteCount = externalCodedInput.readRawVarint32();
                    codedOutput.writeRawVarint32(byteCount);             
                    codedOutput.writeRawBytes(externalCodedInput.readRawBytes(byteCount));
                }                                                                         
                codedOutput.flush();                                                          

                /////////////// PREPARE RESULT OBJECTS FROM HTTP ERRORS ////////////
                if (urlConn instanceof HttpURLConnection) {                         
                    HttpURLConnection httpConn = (HttpURLConnection) urlConn;       
                    try {                                                           
                        int respCode = httpConn.getResponseCode();                  
                        if (respCode != 200) {      
                            Result res;
                            if (respCode == 404) { // Confirmed, works for catching a target currently down. 
                                res = Result.errorResult(new LouieRouteException(httpConn.getResponseMessage()));
                            } else { 
                                res = Result.errorResult(new Exception(httpConn.getResponseMessage()));
                            }
                            results.add(res);                                              
                        }                                                                  
                    } catch (IOException e) {   
                        log.error(e.toString());
                        results.add(Result.errorResult(new LouieRouteException(httpConn.getResponseMessage())));
                    }                                                                      
                }                                                                          

                ////////////////// GET RESPONSE FROM CONNECTION ////////////////////
                InputStream responseInputStream;
                try {
                    responseInputStream = urlConn.getInputStream();         
                } catch (IOException ex) {
                    continue;
                }

                ResponseHeaderPB.parseDelimitedFrom(responseInputStream); //just run through these bytes

                byte[] buf = new byte[1024];
                int count;                  
                while ((count = responseInputStream.read(buf)) >= 0) {
                    externalCodedOutput.writeRawBytes(buf, 0, count); 
                    externalCodedOutput.flush();
                    externalOutput.flush();
                }                                                     

                start = System.nanoTime();
                                                                                                                                                                                                               
                responseInputStream.close();                                                                                                                                                                   

                externalCodedOutput.flush();
                log.debug("Router HTTP turn around time: {}",(System.nanoTime() - startTime)/1000000);
            }                                                                                         
        }                                                                                             
        log.debug("Router total time cost in ms = {}",(System.nanoTime()-begin)/1000000);            
        return results;                                                                               
                                                                                                     
    }                                                                                                 

    private void handleResult(RequestContext requestContext,Result result, OutputStream output) throws Exception {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();
        responseBuilder.setId(requestContext.getRequest().getId());

        if (result.isError()) {
            log.debug("Error Found in handleResult");
            responseBuilder.setError(ErrorPB.newBuilder()
                    .setCode(500)
                    .setType(result.getException().getClass().getSimpleName())
                    .setDescription(result.getException().getMessage()));
        }

        responseBuilder.addRouteBuilder()
                .setRoute(requestContext.getRoute())
                .addAllPath(requestContext.getDesinationRoutes());
        
        if (result.getMessages().isEmpty()) {
            log.debug("Result has no messages");
            responseBuilder.setCount(0);
            ResponsePB response = responseBuilder.build();
            codedOutput.writeRawVarint32(response.getSerializedSize());
            response.writeTo(codedOutput);
        } else {
            log.debug("Response appears to have been received correctly");
            responseBuilder.setCount(result.getMessages().size());

            boolean first = true;
            for (Object oMessage : result.getMessages()) {
                Message message = (Message) oMessage;
                if (first) {
                    responseBuilder.setType(message.getDescriptorForType().getFullName());

                    ResponsePB response = responseBuilder.build();
                    codedOutput.writeRawVarint32(response.getSerializedSize());
                    response.writeTo(codedOutput);
                    first = false;
                }

                int serializedSize = message.getSerializedSize();
                codedOutput.writeRawVarint32(serializedSize);
                message.writeTo(codedOutput);
                codedOutput.flush();
            }
        }
        codedOutput.flush();
    }

}
