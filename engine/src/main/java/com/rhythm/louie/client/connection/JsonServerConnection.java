/*
 * JsonServerConnection.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.client.connection;

import com.rhythm.louie.connection.Identity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;

import com.rhythm.pb.JsonReader;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponsePB;

/**
 *
 * @author cjohnson
 */
@Deprecated
public class JsonServerConnection {
    private Identity identity;
    private URL url;
    private static final List<GeneratedMessage> EMPTY_MESSAGE_LIST =
            Collections.emptyList();

    public JsonServerConnection(URL url, Identity identity) {
        this.url = url;
        this.identity = identity;
    }
    
    public PBServerResponse processSingleRequest(String system,String cmd) throws Exception {
        return processSingleRequest(system,cmd,EMPTY_MESSAGE_LIST);
    }
    
     public PBServerResponse processSingleRequest(String system,String cmd,GeneratedMessage message) throws Exception {
        List<? extends GeneratedMessage> messages;
        if (message == null) {
            messages = Collections.emptyList();
        } else {
            messages = Collections.singletonList(message);
        }
        return processSingleRequest(system,cmd,messages);
    }

    public PBServerResponse processSingleRequest(String system,String cmd, List<? extends GeneratedMessage> messages) throws Exception {
        // Build and Write Request Header
        RequestHeaderPB.Builder headerBuilder = RequestHeaderPB.newBuilder();
        headerBuilder.setCount(1);
        //headerBuilder.setUser(identity.getUser());
        
        // Build and Write Request
        RequestPB.Builder reqBuilder = RequestPB.newBuilder();
        reqBuilder.setId(1)
                  .setSystem(system)
                  .setMethod(cmd);
        if (messages != null && !messages.isEmpty()) {
            GeneratedMessage first = messages.get(0);
            reqBuilder.addType(first.getDescriptorForType().getFullName())
                      .setParamCount(messages.size());
        } else {
            reqBuilder.setParamCount(0);
        }
        
        RequestPB req = reqBuilder.build();
        
        
        StringWriter writer = new StringWriter(); 
        // Write Data
        if (messages != null && !messages.isEmpty()) {
            for (GeneratedMessage message: messages) {
                writer.write(JsonFormat.printToString(message));
            }
        }
        
        writer.flush();
        writer.close();

        URLConnection connection = url.openConnection();

        // Prepare for both input and output
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Turn off caching
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        
        String data = "user=cjohnson"
                + "&system="+URLEncoder.encode(req.getSystem(), "UTF-8")
                + "&method="+URLEncoder.encode(req.getMethod(), "UTF-8")
                + "&type="+URLEncoder.encode(Joiner.on(",").join(req.getTypeList()), "UTF-8")
                + "&params="+URLEncoder.encode(writer.toString(), "UTF-8");
        
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(data);
        wr.flush();
        wr.close();
        
        // Cast to a HttpURLConnection
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if (httpConnection.getResponseCode()>=400) {
                System.err.println(httpConnection.getResponseCode()+" : "+httpConnection.getResponseMessage());
                throw new Exception(httpConnection.getResponseMessage());
            }
        }

        BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        JsonReader reader = new JsonReader(buf);
        ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();
        responseBuilder.setCount(1);
        ResponsePB response = responseBuilder.build();
        PBServerResponse result = new PBServerResponse(response);
        while (true) {//reader.hasMore()) {
            String json = reader.readJson();
            if (json == null) {
                break;
            }
            result.addBytes(json.getBytes());
        }
        return result;
    }
}
