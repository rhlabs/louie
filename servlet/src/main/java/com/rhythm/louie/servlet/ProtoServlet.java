/*
 * ProtoServlet.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.servlet;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
@WebServlet(name = "ProtoServlet", urlPatterns = {"/proto"})
public class ProtoServlet extends HttpServlet {
    private final Logger LOGGER = LoggerFactory.getLogger(ProtoServlet.class);

    private final Map<String,File> packageMap = Collections.synchronizedMap(new HashMap<String,File>());
    private final Map<String,File> pathMap = Collections.synchronizedMap(new HashMap<String,File>());
    private final Map<String,File> pbMap = new ConcurrentHashMap<>();
    
    @Override
    public void init() throws ServletException {
            super.init();
    }
    
    private void processPbDir(File dir, String prepkg) {
        Pattern pathPattern = Pattern.compile("(?:.*)("+prepkg+".*)$");
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                processPbDir(f,prepkg);
            } else if (f.getName().endsWith(".proto")) {
                String path = f.getParent(); 
                Matcher match = pathPattern.matcher(f.getParent());
                if (match.matches()) {
                    path = match.replaceFirst("$1");
                } else if (f.getParent().endsWith("louie") && f.getName().equals("datatype.proto")) {
                    path = "louie";
                }
                
                pathMap.put(path, f);
                String pkg = path.replaceAll("/", ".");
                for (String pbname : parseProtoFile(f)) {
                    pbMap.put(pkg+"."+pbname, f);
                }
                packageMap.put(pkg, f);
            }
        }
    }
    
    private final Pattern messagePattern = Pattern.compile("message\\s*(\\w+)\\s*\\{");
    private List<String> parseProtoFile(File proto) {
        List<String> pbs = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(proto))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = messagePattern.matcher(line);
                if (m.matches()) {
                    pbs.add(m.group(1).trim());
                } else {
                }
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger(ProtoServlet.class).error(ex.toString());
        }
        return pbs;
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text");
        try (PrintWriter writer = response.getWriter()) {
            String pb = request.getParameter("pb");
            
            if (pb!=null && !pb.isEmpty()) {
                String pbpkg = getPackage(pb);
                File target = pbMap.get(pb);
                if (target == null) {
                    String pbfile = packageToFile(pbpkg);
                    URL base = getClass().getClassLoader().getResource(pbfile);
                    File fullDir = new File(base.getPath());
                    processPbDir(fullDir,pbfile);
                    target = pbMap.get(pb);
                }
                writeTextFile(target,writer);
            } else {
                writer.write("Error! Must specify a package,file, or pb.\n");
            }
        }
    }
    
    private String packageToFile(String pkg){
        return pkg.replaceAll("\\.", "\\/");
    }
    
    private final Pattern protoPattern = Pattern.compile("(.*)(?:\\..+?)$");
    private String getPackage(String pb) {
        Matcher match = protoPattern.matcher(pb);
        if (match.matches()) {
            return match.replaceAll("$1");
        }
        return pb;
    }
    
    private void processProto(HttpServletRequest request, PrintWriter writer) {
        try {
            String proto = request.getParameter("proto");
            if (proto == null || proto.isEmpty()) {
                writer.write("Error! Must specify a proto.\n");
            } else {
                Class<?> protoClass = Class.forName(proto,true,this.getClass().getClassLoader());
                
                Descriptor desc = getDescriptor(protoClass);
                writeProto(desc,writer);
            }

//            writeProto(ToolInfoBPB.getDescriptor(), writer);
//            writer.write("\n");
//            writeProto(ToolVersionPB.getDescriptor(), writer);
        } catch (Exception e) {
            writer.write(e.toString()+"\n");
        } finally {
            writer.close();
        }
    }
    
    private static Descriptor getDescriptor(Class<?> cl) throws Exception {
        if (cl==null) {
            return null;
        }
        
        Method descMeth = cl.getMethod("getDescriptor");
        if (!Modifier.isStatic(descMeth.getModifiers())
                || !Modifier.isPublic(descMeth.getModifiers())) {
            throw new Exception("Not a PB!");
        }
        Object o = descMeth.invoke(cl);
        if (!(o instanceof Descriptor)) {
            throw new Exception("Not a PB Descriptor!");
        }
        return (Descriptor) o;
    }
    
    static public void writeProto(Descriptor desc, PrintWriter writer) {
        writer.write(desc.getFullName()+"\n");
        
        writer.write("message " + desc.getName()+ " {\n");
        for (FieldDescriptor field : desc.getFields()) {
            writer.write("    " + getLabelName(field.toProto().getLabel()));
            writer.write("    " + getTypeName(field.toProto()));
            writer.write("    " + field.getName());

            writer.write("    = " + field.getNumber());
            writer.write(";\n");
        }
        writer.write("}\n");
    }

    static final Map<Label,String> labelMap = new EnumMap<Label, String>(Label.class);
    static final Map<Type,String> typeMap = new EnumMap<Type, String>(Type.class);
    
    static {
        for (Label l : Label.values()) {
            labelMap.put(l, l.name().replace("LABEL_", "").toLowerCase());
        }
        
        for (Type t : Type.values()) {
            typeMap.put(t, t.name().replace("TYPE_", "").toLowerCase());
        }
    }
    
    static public String getLabelName(Label l) {
        return labelMap.get(l);
    }
    static public String getTypeName(FieldDescriptorProto proto) {
        Type t = proto.getType();
        if (t==Type.TYPE_MESSAGE || t==Type.TYPE_ENUM) {
            String name = proto.getTypeName();
            if (name.startsWith(".")) {
                return name.replaceFirst("\\.", "");
            } else {
                return name;
            }
        }
        
        return typeMap.get(t);
    }
    
    private void writeTextFile(File f, PrintWriter writer) {
        if (f==null) {
            writer.write("No such file.");
            return;
        }
        
        FileReader reader = null;
        try {
            reader = new FileReader(f);
            
            char[] buf = new char[1024];
            int count = 0;
            while((count = reader.read(buf)) >= 0) {
                writer.write(buf, 0, count);
            }
            
        } catch (Exception ex) {
            LOGGER.error("Error writing Text file", ex);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ex) {
                LOGGER.error("Error closing reader", ex);
            }
        }
    }
    
//    public void writeText() {
//        // Set content type
//        response.setContentType("text");
//
//        // Set content size
//        //File file = new File(filename);
//        //response.setContentLength((int) file.length());
//
//        // Open the file and output streams
//
//        InputStream in = null;
//        OutputStream out = response.getOutputStream();
//        try {
//            ClassLoader CLDR = this.getClass().getClassLoader();
//            in = CLDR.getResourceAsStream(filename);
//             
//            // Copy the contents of the file to the output stream
//            byte[] buf = new byte[1024];
//            int count = 0;
//            while ((count = in.read(buf)) >= 0) {
//                out.write(buf, 0, count);
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//            PrintWriter writer = response.getWriter();
//            try {
//                writer.write("Error Retrieving File!\n");
//                writer.write(e.toString());
//            } finally {
//                writer.close();
//            }
//        } finally {
//            if (in != null) {
//                in.close();
//            }
//            out.close();
//        }
//    }
    
//    public void readTextFromJar(String s,PrintWriter out) {
//        String thisLine;
//        try {
//            //InputStream is = getClass().getResourceAsStream(s);
//            
//            ClassLoader CLDR = this.getClass().getClassLoader();
//            InputStream is = CLDR.getResourceAsStream(s);
//    
//            
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            while ((thisLine = br.readLine()) != null) {
//                out.println(thisLine);
//            }
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
