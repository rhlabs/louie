/*
 * JsonProcessor.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import com.rhythm.pb.DataTypeProtos;
import com.rhythm.pb.DataTypeProtos.IntPB;

import com.rhythm.pb.DataTypeProtos.StringListPB;
import com.rhythm.pb.DataTypeProtos.UIntPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;

import com.rhythm.louie.request.data.DataType;
import com.rhythm.louie.request.data.Param;
import com.rhythm.louie.request.data.Result;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class JsonProcessor implements JsonProcess{
    private final Logger LOGGER = LoggerFactory.getLogger(JsonProcessor.class);
    
    private static final String VERSION = "version";
    private static final String USER = "user";
    private static final String AGENT = "agent";
    private static final String SYSTEM = "system";
    private static final String METHOD = "method";
    private static final String PARAMS = "params";
    
    private static final String UINT_TYPE = UIntPB.getDescriptor().getFullName();
    private static final String INT_TYPE = IntPB.getDescriptor().getFullName();
    private static final String UINT_LISTTYPE = DataTypeProtos.UIntListPB.getDescriptor().getFullName();
    private static final String INT_LISTTYPE = DataTypeProtos.IntListPB.getDescriptor().getFullName();
    
    @Override
    public void processRequest(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        //resp.setHeader("Server","Louie");
        //resp.setHeader("X-Powered-By","Glassfish");

        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            processJsonRequest(req,resp);
        } else {
            processFormRequest(req,resp);
        }
    }
    /*
      {"version":"2",
       "user":"cjohnson",
       "agent":"JUNIT_TEST",
       "system":"scene",
       "method":"getScene",
       "params":[{"type":"rh.pb.scene.ScenePKPB",
                  "value":{"job":"ripd",
                           "scn":"rd.wwhaley"
                          }
                }]
       }";
    */
    @SuppressWarnings("deprecation")
    private void processJsonRequest(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        try {
            long start=System.nanoTime();
            
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JSONObject json = (JSONObject) JSONSerializer.toJSON(sb.toString());
            
            String version = json.optString(VERSION);
            
            String who = json.optString(USER);
            if (who == null || who.equals("")) {
                who = "unknown";
            }
            String agent = json.optString(AGENT);
            String service = json.optString(SYSTEM);
            String method = json.optString(METHOD);
            
            if (service == null || service.isEmpty()) {
                throw new Exception("Improper Request format!  Missing System.");
            }
            if(method == null || method.isEmpty()) {
                throw new Exception("Improper Request format!  Missing Method.");
            }
            
            RequestHeaderPB requestHeader = RequestHeaderPB.newBuilder()
                    .setUser(who)
                    .setAgent(req.getHeader("user-agent"))
                    .setCount(1)
                    .build();
            RequestPB.Builder reqBuilder = RequestPB.newBuilder()
                    .setId(1)
                    .setService(service)
                    .setMethod(method);
            
            List<String> args = new ArrayList<>();
            
            /****************************
             *         VERSION 1        *
             ****************************/
            if (version.isEmpty() || version.equals("1")) {
                JSONArray types = json.optJSONArray("types");
                if (types != null) {
                    for (int i = 0; i < types.size(); i++) {
                        String type = types.getString(i);
                        if (type == null || type.isEmpty()) {
                            throw new Exception("Improper Request format!  Type cannot by blank.");
                        }
                        if (type.equals(UINT_TYPE)){
                            type = INT_TYPE;
                        } else if (type.equals(UINT_LISTTYPE)) {
                            type = INT_LISTTYPE;
                        }
                        reqBuilder.addType(type);
                    }
                }
                JSONArray params = json.optJSONArray(PARAMS);
                if (params != null) {
                    if (params.size()>1) {
                        throw new Exception("Multiple Parameter sets is not supported!");
                    }
                    if (params.size()==1) {
                        JSONObject param = params.getJSONObject(0);
                        JSONArray argsArray = param.getJSONArray("arg");
                        if (argsArray != null) {
                            for (int i = 0; i < argsArray.size(); i++) {
                                String arg = argsArray.getString(i);
                                args.add(arg);
                            }
                        }
                    }
                }
                
             /****************************
             *         VERSION 2        *
             ****************************/
            } else if (version.equals("2")) {
                JSONArray params = json.optJSONArray(PARAMS);
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        JSONObject param = params.getJSONObject(i);
                        String type = param.optString("type");
                        String value = param.optString("value");
                        if (type == null || type.isEmpty()) {
                            throw new Exception("Improper Request format!  Type cannot by blank.");
                        }
                        if (type.equals(UINT_TYPE)){
                            type = INT_TYPE;
                        } else if (type.equals(UINT_LISTTYPE)) {
                            type = INT_LISTTYPE;
                        }
                        reqBuilder.addType(type);
                        args.add(value);
                    }
                }
                
            /****************************
             *         VERSION ???      *
             ****************************/
            } else {
                throw new Exception("Unable to process JSON request!  Uknown version: "+version);
            }
            
            RequestPB request = reqBuilder.build();
            
            RequestProperties props = RequestProperties.fromHttpRequest(req);

            RequestContext pbReq = new RequestContext(requestHeader, request, DataType.JSON, props);
            pbReq.addParam(Param.buildJsonParam(args));
            pbReq.setRoute(props.createRoute(request.getService()));

            if (agent != null && !agent.isEmpty()) {
                pbReq.setUserAgent(agent);
            } else {
                pbReq.setUserAgent(Strings.nullToEmpty(req.getHeader("user-agent")));
            }

            Result result = RequestHandler.processSingleRequest(pbReq);
            result.setExecTime((System.nanoTime()-start) / 1000000);
            handleResult(result,resp);
            result.setDuration((System.nanoTime()-start) / 1000000);
            
            RequestHandler.logRequest(pbReq, result);
        } catch(Exception e) {
            String errorMessage = e.getMessage()==null ? e.toString(): e.getMessage();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,errorMessage);
            LOGGER.error(errorMessage);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void processFormRequest(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        //if (true) throw new UnsupportedOperationException();
        
        try {
            LOGGER.info("processFormRequest");
            
            long start=System.nanoTime();
            
            String user = Strings.nullToEmpty(req.getParameter("user"));
            String service = req.getParameter("system");
            String method = req.getParameter("method");
            String type = req.getParameter("type");
            
            if (service == null || service.equals("") ||
                method == null || method.equals("") ) {
                throw new Exception("Must specify system,method");
            }
            
            if (user.equals("")) {
                user = "unknown";
            }
            
            if (type != null) {
                if (type.equals(UINT_TYPE)){
                    type = INT_TYPE;
                } else if (type.equals(UINT_LISTTYPE)) {
                    type = INT_LISTTYPE;
                }                          
            }
            
            RequestHeaderPB requestHeader = RequestHeaderPB.newBuilder()
                    .setUser(user)
                    .setAgent(req.getHeader("user-agent"))
                    .setCount(1)
                    .build();

            RequestPB.Builder reqBuilder = RequestPB.newBuilder()
                    .setId(1)
                    .setService(service)
                    .setMethod(method);
            
            if (type!=null && !type.equals("")) {
                reqBuilder.addType(type);
            }
            RequestPB request = reqBuilder.build();
            
            RequestProperties props = RequestProperties.fromHttpRequest(req);

            RequestContext pbReq = new RequestContext(requestHeader, request, DataType.JSON, props);
            String params = req.getParameter("params");
            if (params != null && !params.isEmpty()) {
                pbReq.addParam(Param.buildJsonParam(Arrays.asList(params.split(","))));
            }
            pbReq.setUserAgent(Strings.nullToEmpty(req.getHeader("user-agent")));

            Result result = RequestHandler.processSingleRequest(pbReq);
            result.setExecTime((System.nanoTime() - start) / 1000000);
            handleResult(result, resp);
            result.setDuration((System.nanoTime() - start) / 1000000);

            RequestHandler.logRequest(pbReq, result);
        } catch (Exception e) {
            String errorMessage = e.getMessage()==null ? e.toString(): e.getMessage();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,errorMessage);
            LOGGER.error(errorMessage,e);
        }
    }
    
    private void handleResult(Result result,HttpServletResponse resp) throws Exception {
        PrintWriter writer = resp.getWriter();
        writer.write("[");
        if (result != null && !result.getMessages().isEmpty()) {
            int totalSize = 0;
            boolean first = true;
            for (Message message : result.getMessages()) {
                if (first) {
                    first = false;
                } else {
                    writer.write(",");
                }

                String s = printToJson(message);
                writer.write(s);
                totalSize += s.length();
            }
            result.setSize(totalSize);
        }
        writer.write("]");
    }
    
    private String printToJson(Message message) {
        if (message instanceof StringListPB) {
            StringListPB list = (StringListPB) message;
            return Joiner.on("\",\"")
                    .appendTo(new StringBuilder("\""), list.getValuesList())
                    .append("\"")
                    .toString();
        }
        return JsonFormat.printToString(message);
    }
}
