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
package com.rhythm.louie.info;

import java.io.*;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
@WebServlet(urlPatterns = {"/proto"})
public class ProtoServlet extends HttpServlet {
    private final Map<String,File> packageMap = Collections.synchronizedMap(new HashMap<String,File>());
    private final Map<String,File> pathMap = Collections.synchronizedMap(new HashMap<String,File>());
    private final Map<String,File> pbMap = new ConcurrentHashMap<>();
    
    
     /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pb = request.getParameter("pb");

        File file = null;
        if (pb != null && !pb.isEmpty()) {
            String pbpkg = getPackage(pb);
            file = pbMap.get(pb);
            if (file == null) {
                String pbfile = packageToFile(pbpkg);
                URL base = getClass().getClassLoader().getResource(pbfile);
                File fullDir = new File(base.getPath());
                processPbDir(fullDir, pbfile);
                file = pbMap.get(pb);
            }
        }

        String filename = "";
        List<String> fileContents = Collections.emptyList();
        String error = "";
        
        if (file==null) {
            error = "Error! Must specify a package,file, or pb";
        } else if (!file.exists()) {
            error = "Error! File does not exist";
        } else {
            fileContents = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            filename = file.getName();
        }
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("file", fileContents);
        properties.put("filename", filename);
        properties.put("error", error);

        InfoUtils.writeTemplateResponse(request, response, "proto.vm", properties);
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
        
        try (InputStream in = new FileInputStream(proto);
                Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(r)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = messagePattern.matcher(line);
                if (m.matches()) {
                    pbs.add(m.group(1).trim());
                }
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger(ProtoServlet.class).error(ex.toString());
        }
        return pbs;
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

    private static final Map<Label,String> labelMap = new EnumMap<>(Label.class);
    private static final Map<Type,String> typeMap = new EnumMap<>(Type.class);
    
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
        return "Display Proto Files";
    }// </editor-fold>
}
