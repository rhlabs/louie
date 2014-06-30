/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.request;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.protobuf.Message;   
import com.googlecode.protobuf.format.JsonFormat;
import com.rhythm.louie.Server;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.topology.Route;               
import com.rhythm.pb.DataTypeProtos;                
import com.rhythm.pb.RequestProtos;                
import com.rhythm.pb.data.DataType;                   
import com.rhythm.pb.data.Param;                 
import com.rhythm.pb.data.RequestContext;                  
import com.rhythm.pb.data.Result;              
import java.io.ByteArrayOutputStream;                        
import java.io.IOException;                        
import java.io.InputStream;                        
import java.io.OutputStream;                       
import java.io.PrintWriter;                        
import java.net.URLConnection;                     
import java.util.ArrayList;                        
import java.util.Arrays;                           
import java.util.List;                             
import javax.servlet.ServletException;             
import javax.servlet.http.HttpServletRequest;      
import javax.servlet.http.HttpServletResponse;     
import net.sf.json.JSONArray;                      
import net.sf.json.JSONObject;                     
import net.sf.json.JSONSerializer;                 

/**
 * 
 * @author eyasukoc
 */                


public class JsonRouter implements JsonProcess{

    private static final String VERSION = "version";
    private static final String USER = "user";      
    private static final String AGENT = "agent";    
    private static final String SYSTEM = "system";  
    private static final String VARIANT = "variant";
    private static final String METHOD = "method";  
    private static final String PARAMS = "params";  
                                                    
    @Override                                       
    public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() != null && req.getContentType().contains("application/json")) {                       
            processJsonRequest(req,resp);                                                                              
        } else {                                                                                                       
            processFormRequest(req,resp);                                                                              
        }                                                                                                              
    }                                                                                                                  

    @SuppressWarnings("deprecation")
    private void processJsonRequest(HttpServletRequest req,
            HttpServletResponse resp) throws IOException { 
        try {                                              
            long start=System.currentTimeMillis();         

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                                     
                                                                     
            StringBuilder sb = new StringBuilder();                  
                                                                     
            byte[] buf = new byte[1024];                             
            String charEnc = req.getCharacterEncoding();             
            int count;                                               
            while ((count = req.getInputStream().read(buf)) >= 0) {  
                baos.write(buf,0,count);                             
            }                                                        
            baos.flush();                                            
            sb.append(new String(baos.toByteArray(),charEnc));       
                                                                     
            JSONObject json = (JSONObject) JSONSerializer.toJSON(sb.toString());
                                                                                
            String version = json.optString(VERSION);                           
                                                                                
            String who = json.optString(USER);                                  
            if (who == null || who.equals("")) {                                
                who = "unknown";                                                
            }                                                                   
            String agent = json.optString(AGENT);                               
            String system = json.optString(SYSTEM);                             
            String method = json.optString(METHOD);                             
                                                                                
            if (system == null || system.isEmpty()) {                           
                throw new Exception("Improper Request format!  Missing System.");
            }                                                                    
            if (method == null || method.isEmpty()) {                            
                throw new Exception("Improper Request format!  Missing Method.");
            }                                                                    
                                                                                 
            Server target = Route.get(system);
            if (target == null) {
                target = Route.get("default");
            }
            
            if (!Server.LOCAL.equals(target)) {                                                                                 
                proxyRequest(target,baos,req,resp);                                                          
                return;                                                                                   
            }                                                                                             
            try {                                                                                         
                baos.close();                                                                             
            } catch (NullPointerException ex) {}                                                          
            resp.setContentType("application/json");                                                      

            
            RequestProtos.RequestHeaderPB requestHeader = RequestProtos.RequestHeaderPB.newBuilder()
                    .setUser(who)                                                                   
                    .setAgent(req.getHeader("user-agent"))                                          
                    .setCount(1)                                                                    
                    .build();                                                                       
            RequestProtos.RequestPB.Builder reqBuilder = RequestProtos.RequestPB.newBuilder()       
                    .setId(1)                                                                       
                    .setService(system)                                                              
                    .setMethod(method);                                                             
                                                                                                    
            List<String> args = new ArrayList<String>();                                            
                                                                                                    
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
                                                                                                    
            RequestProtos.RequestPB request = reqBuilder.build();                                   
                                                                                                    
            RequestContext pbReq = new RequestContext(requestHeader,request,DataType.JSON);                       
            pbReq.addParam(Param.buildJsonParam(args));                                             
            pbReq.setRemoteAddress(req.getRemoteAddr());                                            
                                                                                                    
            if (agent !=null && !agent.isEmpty()) {                                                 
                pbReq.setUserAgent(agent);                                                          
            } else {                                                                                
                pbReq.setUserAgent(Strings.nullToEmpty(req.getHeader("user-agent")));               
            }                                                                                       
                                                                                                    
            Result result = RequestHandler.processSingleRequest(pbReq);                             
            handleResult(result,resp);                                                              
                                                                                                    
            long end=System.currentTimeMillis();                                                    
            result.setDuration(end-start);                                                          
                                                                                                    
            RequestHandler.logRequest(pbReq, result);                                               
        } catch(Exception e) {                   
            String errorMessage = e.getMessage()==null ? e.toString(): e.getMessage();              
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,errorMessage);                        
            System.err.println(errorMessage);                                                       
        }                                                                                           
    }                                                                                               
                                 
    @SuppressWarnings("deprecation")
    private void processFormRequest(HttpServletRequest req,                                         
            HttpServletResponse resp) throws IOException {                                          
        resp.setContentType("application/json");                                                    
                                                                                                    
        try {                                                                                       
            long start=System.currentTimeMillis();                                                  
                                                                                                    
            String user = Strings.nullToEmpty(req.getParameter("user"));                            
            String system = req.getParameter("system");                                             
            String method = req.getParameter("method");                                             
            String type = req.getParameter("type");                                                 
                                                                                                    
            if (system == null || system.equals("") ||                                              
                method == null || method.equals("") ) {                                             
                throw new Exception("Must specify system,method");                                  
            }                                                                                       
                                                                                                    
            Server target = Route.get(system);
            if (target == null) {
                target = Route.get("default");
            }
            
            if (Server.LOCAL.equals(target)) {                                                                                 
                LouieConnection louieConn = LouieConnectionFactory.getConnectionForServer(target);
               
                if (louieConn == null) {                                                                  
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND,"The requested LoUIE service was not found.");
                }                                                                                                 
                ////////////////// PUT DATA INTO THAT CONNECTION ///////////////////                              
                URLConnection urlConn = louieConn.getJsonForwardingConnection();                                  

                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("user-agent", req.getHeader("user-agent")); 
                urlConn.setRequestProperty("user", user);                              
                urlConn.setRequestProperty("system", system);                          
                urlConn.setRequestProperty("method", method);                          
                urlConn.setRequestProperty("type", type);                              
                urlConn.setRequestProperty("params", req.getParameter("params"));      
                urlConn.connect(); //is that enough?                                   
                byte[] buf = new byte[1024];                                           
                int count;                                                             
                InputStream input2 = urlConn.getInputStream();                         
                OutputStream output2 = resp.getOutputStream();                         
                while ((count = input2.read(buf)) >= 0) {                              
                    output2.write(buf, 0, count);                                      
                }                                                                      
                output2.flush();                                                       
                try {input2.close();} catch (NullPointerException ex) {}               
                try {output2.close();} catch (NullPointerException ex) {}              
                                                                                       
                return;                                                                
            }                                                                          
                                                                                       
            if (user.equals("")) {                                                     
                user = "unknown";                                                      
            }                                                                          
                                                                                       
            RequestProtos.RequestHeaderPB requestHeader = RequestProtos.RequestHeaderPB.newBuilder()
                    .setUser(user)                                                                  
                    .setAgent(req.getHeader("user-agent"))                                          
                    .setCount(1)                                                                    
                    .build();                                                                       

            RequestProtos.RequestPB.Builder reqBuilder = RequestProtos.RequestPB.newBuilder()
                    .setId(1)                                                                
                    .setService(system)                                                       
                    .setMethod(method);                                                      
                                                                                             
            if (type!=null && !type.equals("")) {                                            
                reqBuilder.addType(type);                                                    
            }                                                                                
            RequestProtos.RequestPB request = reqBuilder.build();                            
                                                                                             
            RequestContext pbReq = new RequestContext(requestHeader, request,DataType.JSON);               
            String params = req.getParameter("params");                                      
            if (params!=null && !params.isEmpty()) {                                         
                pbReq.addParam(Param.buildJsonParam(Arrays.asList(params.split(","))));      
            }                                                                                
            pbReq.setRemoteAddress(req.getRemoteAddr());                                     
            pbReq.setUserAgent(Strings.nullToEmpty(req.getHeader("user-agent")));            
                                                                                             
            Result result = RequestHandler.processSingleRequest(pbReq);                      
            handleResult(result,resp);                                                       
                                                                                             
            long end = System.currentTimeMillis();                                           
            result.setDuration(end-start);                                                   
            start = end;                                                                     

            RequestHandler.logRequest(pbReq, result);
        } catch(Exception e) {                       
            String errorMessage = e.getMessage()==null ? e.toString(): e.getMessage();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,errorMessage);          
            System.err.println(errorMessage);                                         
        }                                                                             
    }                                                                                 
                                                                                      
    private void handleResult(Result<?,?> result,HttpServletResponse resp) throws Exception {
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
        if (message instanceof DataTypeProtos.StringListPB) {
            DataTypeProtos.StringListPB list = (DataTypeProtos.StringListPB) message;
            return (Joiner.on(",").join(list.getValuesList(),true));                 
        }                                                                            
        return JsonFormat.printToString(message);                                    
    }                                                                                
                                                                                     
    /**
     * Proxies a received request to a target host, determined via the ServiceKey
     * @param key The ServiceKey for the necessary service, used to fetch a LouieConnection
     * @param baos The ByteArrayOutputStream which contains all of the buffered input stream
     * @param req  The HttpServletRequest object from the initial request
     * @param resp The HttpServletResponse object from the initial request
     * @throws Exception
     */
    private void proxyRequest(Server server, ByteArrayOutputStream baos, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        byte[] buf = new byte[1024];
        int count;
        LouieConnection louieConn = LouieConnectionFactory.getConnectionForServer(server);
        if (louieConn == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,"The requested LoUIE service was not found.");
            return; //valid to return here on failure?
        }

        ////////////////// PUT DATA INTO THAT CONNECTION ///////////////////
        URLConnection urlConn = louieConn.getJsonForwardingConnection();

        urlConn.setRequestProperty("Content-Type", "application/json");         // NOTE, you cannot set the request properties if the connection has already been "connected"
        urlConn.setRequestProperty("user-agent", req.getHeader("user-agent"));
        urlConn.connect();
        OutputStream output1 = urlConn.getOutputStream();
        output1.write(baos.toByteArray());
        output1.flush();

        InputStream input2 = urlConn.getInputStream();
        OutputStream output2 = resp.getOutputStream();
        while ((count = input2.read(buf)) >= 0) {
            output2.write(buf, 0, count);
        }
        output2.flush();

        try {                                                           // This is garbage
            baos.close();
        } catch (NullPointerException ex) {
            System.out.println("You unnecessarily closed tee stream");
        }
        try {
            output1.close();
        } catch (NullPointerException ex) {
            System.out.println("You unnecessarily closed output1 stream");
        }
        try {
            input2.close();
        } catch (NullPointerException ex) {
            System.out.println("You unnecessarily closed intput2 stream");
        }
        try {
            output2.close();
        } catch (NullPointerException ex) {
            System.out.println("You unnecessarily closed output2 stream");
        }
    }

}
