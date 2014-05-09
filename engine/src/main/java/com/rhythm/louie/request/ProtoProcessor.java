/*
 * RequestProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.rhythm.pb.data.DataType;
import com.rhythm.pb.data.Request;
import com.rhythm.pb.data.Result;

/**
 *
 * @author cjohnson
 */
public class ProtoProcessor {
    private final Logger LOGGER = LoggerFactory.getLogger(ProtoProcessor.class);

    public static void main(String args[]) {
        try {
            String localRoute = InetAddress.getLocalHost().getHostAddress();
            System.out.println(localRoute);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(ProtoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws UnauthorizedSessionException, IOException, Exception {
        long start = System.currentTimeMillis();

        List<Result> results = new ArrayList<Result>();
        
        RequestHeaderPB header = RequestHeaderPB.parseDelimitedFrom(input);

        IdentityPB identity = null;
        if (header.hasKey()) {
            SessionStat session = AuthUtils.accessSession(header.getKey());
            identity = session.getIdentity();
        }
        
        if (header.hasRouteUser() && !"LoUIE".equals(identity.getUser())) {
            throw new Exception("User Route Permission Denied!");
        }
        
        ResponseHeaderPB responseHeader = ResponseHeaderPB.newBuilder()
                .setCount(header.getCount()).build();
        responseHeader.writeDelimitedTo(output);
        for (int r = 0; r < header.getCount(); r++) {
            RequestPB request = RequestPB.parseDelimitedFrom(input);
            if (request == null) {
                throw new Exception("Improper Request format!  Reached EOF prematurely!");
            }

            Request pbReq = null;
            Result result = null;

            RoutePB localRoute = props.createRoute(request.getSystem());
            for (RoutePB route : header.getRouteList()) {
                if (route.equals(localRoute)) {
                    throw new Exception("Route Loop Detected!");
                }
            }
            
            try {
                pbReq = new Request(header, request,DataType.PB);
                pbReq.setIdentity(identity);
                pbReq.readPBParams(input);
                pbReq.setRemoteAddress(props.getRemoteAddress());
                pbReq.setLocalPort(props.getLocalPort());
                pbReq.setRoute(localRoute);
                result = RequestHandler.processSingleRequest(pbReq);
                result.setExecTime(System.currentTimeMillis()-start);
                handleResult(pbReq, result, output);
            } catch (Exception e) {
                String errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                LOGGER.error(errorMessage);
                if (result != null) {
                    result.addError(e);
                } else {
                    result = new Result(false);
                }
            } finally {
                long end = System.currentTimeMillis();
                if (pbReq == null) {
                    LOGGER.error("Unknown Error, Request is null");
                } else {
                    if (result == null) {
                        result = new Result(false);
                    }
                    result.setDuration(end - start);
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

    private void handleResult(Request pbReq,Result result,OutputStream output) throws Exception {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();
        responseBuilder.setId(pbReq.getRequest().getId());
        
        if (result.isError()) {
            responseBuilder.setError(ErrorPB.newBuilder()
                    .setCode(500)
                    .setType(result.getException().getClass().getSimpleName())
                    .setDescription(result.getException().getMessage()));
        }
        
        if (result.getMessages().isEmpty()) {
            responseBuilder.setCount(0);
            ResponsePB response = responseBuilder.build();
            codedOutput.writeRawVarint32(response.getSerializedSize());
            response.writeTo(codedOutput);
        } else {
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
            }
        }
        codedOutput.flush();
    }
}