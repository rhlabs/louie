/*
 * HttpProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import com.rhythm.louie.server.Server;
import com.rhythm.louie.auth.UnauthorizedSessionException;
import com.rhythm.louie.exception.LouieRouteException;

import com.rhythm.pb.data.Result;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class HttpProcessor {
     private final Logger LOGGER = LoggerFactory.getLogger(HttpProcessor.class);
            
     private final String localIp;
     
     private final ProtoProcess processor;
     public HttpProcessor() {
        if (Server.LOCAL.isARouter()) {
            LOGGER.debug("Instantiating a protorouter");
            processor = new ProtoRouter();
        } else {
            LOGGER.debug("Instantiating a protoprocessor");
            processor = new ProtoProcessor();
        }
        
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LOGGER.error(ex.toString());
            ip = "UNKNOWN_ADDRESS";
        }
        localIp = ip;
     }
    
     public void processRequest(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("application/x-protobuf");

                if (req.getContentType() == null || !req.getContentType().startsWith("application/x-protobuf")) {
                    throw new Exception("This servlet is not configured to deal with content of type: "+req.getContentType());
                }
                RequestProperties props = new RequestProperties();
                props.setRemoteAddress(req.getRemoteAddr());
                props.setLocalPort(req.getLocalPort());
                props.setHostIp(localIp);
                props.setGateway(req.getContextPath().substring(1));
                
                List<Result> results = processor.processRequest(req.getInputStream(),resp.getOutputStream(),props);
                //sendHttpError(results,resp);
        } catch (LouieRouteException ex) {
            LOGGER.error(ex.toString());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (UnauthorizedSessionException ex) {
            LOGGER.error(ex.toString());
            resp.sendError(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED,"Invalid Session Key");
        } catch(Exception e) {
            String errorMessage = e.getMessage()==null ? e.toString(): e.getMessage();
            LOGGER.error("Error Processing Request",e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,errorMessage);
        }
     }
     
    private void sendHttpError(List<Result> results, HttpServletResponse resp) {
        for (Result result : results) {
            if (result.isError()) {
                Exception e = result.getException();
                try {
                    int errorcode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    if (e instanceof NoSuchMethodException) {
                        errorcode = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
                    }
                    if (e instanceof LouieRouteException) {                     //logically, yes. name-wise, no.
                        errorcode = HttpServletResponse.SC_NOT_FOUND;           //trigger a retry from clients
                    }
                    
                    String message = "Unknown Error";
                    if (e!=null) {
                        message = e.getMessage() == null ? e.toString() : e.getMessage();
                    }
                    
                    resp.sendError(errorcode, message);
                } catch (Exception e2) {
                    LOGGER.error("Error Writing - Exception: {}", e2.toString());
                }
            }
        }
    }

}
