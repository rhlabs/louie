/*
 * RequestProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.request.data.DataType;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.server.Server;
import com.rhythm.louie.services.auth.*;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.RoutePB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class ProtoProcessor implements ProtoProcess {
    private final Logger LOGGER = LoggerFactory.getLogger(ProtoProcessor.class);
    private final Pattern userCN;
    private final boolean secured;
    
    public ProtoProcessor() {
        secured = Server.LOCAL.isSecure();
        userCN = Pattern.compile(".*CN=([\\w\\s]+),*.*");
    }
    
    @Override
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws UnauthorizedSessionException, IOException, Exception {
        long start = System.nanoTime();

        List<Result> results = new ArrayList<>();
        
        RequestHeaderPB header = RequestHeaderPB.parseDelimitedFrom(input);
        if (header.getCount()>1) {
            throw new Exception("Batching Requests is not supported!");
        }
        
        IdentityPB identity = null;
        SessionKey sessionKey = null;
        if (header.hasKey()) {
            SessionStat session = AuthUtils.accessSession(header.getKey());
            identity = session.getIdentity();
        } else { //initial request, we will handle creating and returning a key
            if (header.hasIdentity()) { //to make backwards compatible
                identity = header.getIdentity();
                if (secured) {
                    Matcher match = userCN.matcher(props.getRemoteUser());
                    if (match.matches()) {
                        if (identity.getUser().equalsIgnoreCase(match.group(1))) {
                            sessionKey = AuthUtils.createKey(identity);
                        }
                    }
                } else {
                    sessionKey = AuthUtils.createKey(identity);
                }
            }
            if (sessionKey == null && secured) {
                throw new UnauthenticatedException("Unable to create a Session Key, likely due to authentication failure");
            }
        }
        
        ResponseHeaderPB.Builder responseHeader = ResponseHeaderPB.newBuilder();
        responseHeader.setCount(header.getCount());
        if (sessionKey != null) {
            responseHeader.setKey(sessionKey);
        }
        responseHeader.build().writeDelimitedTo(output);
        
        for (int r = 0; r < header.getCount(); r++) {
            RequestPB request = RequestPB.parseDelimitedFrom(input);
            if (request == null) {
                throw new Exception("Improper Request format! Reached EOF prematurely! @ProtoProcessor.processRequest()");
            }
            if (request.hasRouteUser() && (identity == null || !"LoUIE".equals(identity.getUser()))) {
                throw new Exception("User Route Permission Denied!");
            }
            
            RequestContext requestContext = null;
            Result result = null;

            RoutePB localRoute = props.createRoute(request.getService());
            for (RoutePB route : request.getRouteList()) {
                if (route.equals(localRoute)) {
                    throw new Exception("Route Loop Detected! "+route.getHostIp()+"/"+route.getGateway()+" visited twice!");
                }
            }
            
            try {
                requestContext = new RequestContext(header, request, DataType.PB, props);
                if (header.hasIdentity()) {
                    requestContext.setSessionKey(sessionKey);
                }
                requestContext.setIdentity(identity);
                requestContext.readPBParams(input);
                requestContext.setRoute(localRoute);
                result = RequestHandler.processSingleRequest(requestContext);
                result.setExecTime((System.nanoTime() - start) / 1000000);
                handleResult(requestContext, result, output);
            } catch (Exception e) {
                String errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                LOGGER.error("ProtoProcessor caught error: "+errorMessage,e);
                if (result != null) {
                    result.addError(e);
                } else {
                    result = Result.errorResult(e);
                }
            } finally {
                long end = System.nanoTime();
                if (requestContext == null) {
                    LOGGER.error("Unknown Error, Request is null");
                } else {
                    if (result == null) {
                        result = Result.errorResult(null);
                    }
                    result.setDuration((end - start) / 1000000);
                    try {
                        RequestHandler.logRequest(requestContext, result);
                    } catch (Exception le) {
                        LOGGER.error("Error Logging: {}", le.getMessage());
                    }
                }
                start = end;
                results.add(result);
            }
        }
        return results;
    }

    private void handleResult(RequestContext requestContext,Result result,OutputStream output) throws Exception {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();
        responseBuilder.setId(requestContext.getRequest().getId());
        
        if (result.isError()) {
            ErrorPB.Builder error = ErrorPB.newBuilder();
            error.setCode(500);
            Exception ex = result.getException();
            if (ex!=null) {
                error.setType(ex.getClass().getSimpleName());
                if (ex.getMessage()!=null) {
                    error.setDescription(ex.getMessage());
                }
            } else {
                error.setType("Unknown Exception");
            }
            responseBuilder.setError(error);
        }
        
        responseBuilder.addRouteBuilder()
                .setRoute(requestContext.getRoute())
                .addAllPath(requestContext.getDesinationRoutes());
        
        if (result.getMessages().isEmpty()) {
            responseBuilder.setCount(0);
            ResponsePB response = responseBuilder.build();
            codedOutput.writeRawVarint32(response.getSerializedSize());
            response.writeTo(codedOutput);
        } else {
            responseBuilder.setCount(result.getMessages().size());
            boolean first = true;
            long totalSize = 0;
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
                if (result.isStreaming()) {
                    codedOutput.flush();
                    output.flush();
                }
                totalSize+=serializedSize;
            }
            result.setSize(totalSize);
        }
        codedOutput.flush();
        output.flush();
    }
}
