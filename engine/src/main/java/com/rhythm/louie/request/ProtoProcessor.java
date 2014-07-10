/*
 * RequestProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.rhythm.louie.auth.AuthUtils;
import com.rhythm.louie.auth.SessionStat;
import com.rhythm.louie.auth.UnauthorizedSessionException;
import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.RoutePB;
import com.rhythm.pb.RequestProtos.SessionKey;
import com.rhythm.pb.data.DataType;
import com.rhythm.pb.data.RequestContext;
import com.rhythm.pb.data.Result;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class ProtoProcessor implements ProtoProcess{
    private final Logger LOGGER = LoggerFactory.getLogger(ProtoProcessor.class);

    public static void main(String args[]) {
        try {
            String localRoute = InetAddress.getLocalHost().getHostAddress();
            System.out.println(localRoute);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(ProtoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws UnauthorizedSessionException, IOException, Exception {
        long start = System.nanoTime();

        List<Result> results = new ArrayList<Result>();
        
        RequestHeaderPB header = RequestHeaderPB.parseDelimitedFrom(input);
        if (header.getCount()>1) {
            throw new Exception("Batching Requests is not yet supported!");
        }
        
        IdentityPB identity = null;
        SessionKey sessionKey = null;
        if (header.hasKey()) {
            SessionStat session = AuthUtils.accessSession(header.getKey());
            identity = session.getIdentity();
        } else { //initial request, we will handle creating and returning a key
            if (header.hasIdentity()) { //to make backwards compatible
                identity = header.getIdentity();
                sessionKey = AuthUtils.createKey(identity);
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
            
            RequestContext pbReq = null;
            Result result = null;

            RoutePB localRoute = props.createRoute(request.getService());
            for (RoutePB route : request.getRouteList()) {
                if (route.equals(localRoute)) {
                    throw new Exception("Route Loop Detected!");
                }
            }
            
            try {
                pbReq = new RequestContext(header, request,DataType.PB);
                if (header.hasIdentity()) {
                    pbReq.setSessionKey(sessionKey);
                }
                pbReq.setIdentity(identity);
                pbReq.readPBParams(input);
                pbReq.setRemoteAddress(props.getRemoteAddress());
                pbReq.setLocalPort(props.getLocalPort());
                pbReq.setRoute(localRoute);
                result = RequestHandler.processSingleRequest(pbReq);
                result.setExecTime((System.nanoTime()-start) / 1000000);
                handleResult(pbReq, result, output);
            } catch (Exception e) {
                String errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                LOGGER.error("ProtoProcessor caught error: {}",errorMessage);
                if (result != null) {
                    result.addError(e);
                } else {
                    result = Result.errorResult(e);
                }
            } finally {
                long end = System.nanoTime();
                if (pbReq == null) {
                    LOGGER.error("Unknown Error, Request is null");
                } else {
                    if (result == null) {
                        result = Result.errorResult(null);
                    }
                    result.setDuration((end - start) / 1000000);
                    try {
                        RequestHandler.logRequest(pbReq, result);
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
            responseBuilder.setError(ErrorPB.newBuilder()
                    .setCode(500)
                    .setType(result.getException().getClass().getSimpleName())
                    .setDescription(result.getException().getMessage()));
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
