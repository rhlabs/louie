/*
 * InfoServlet.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rhythm.louie.ExternalProperties;
import com.rhythm.louie.ServiceManager;
import com.rhythm.louie.ServiceProperties;

import com.rhythm.pb.command.ArgType;
import com.rhythm.pb.command.PBCommand;
import com.rhythm.pb.command.PBParamType;
import com.rhythm.pb.command.Service;

/**
 *
 * @author cjohnson
 */
public class InfoServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String service = request.getParameter("service");
        if (service==null || service.isEmpty()) {
            listAllServices(request,response);
        } else if (service.equals("all")) {
            showAllServiceCalls(request,response);
        } else {
            showServiceCalls(service,request,response);
        }
    }
    
    private void showServiceCalls(String serviceName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>LoUIE Services</title>");  
            out.println("<link rel=\"shortcut icon\" href=\"./favicon.ico\" />"); 
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>" + serviceName+ "&nbsp;&nbsp;&nbsp;&nbsp;</h1>");
            out.println("<h3>"+getPropertyString(serviceName)+"</h3>");
            
            out.println("<a href=\"./info\">Service List</a><br><br>");
            
            try {
                Service service = ServiceManager.getService(serviceName);
                if (service == null) {
                    out.println("No Such Service: " + serviceName);
                } else {
                    writeGroupedServiceCalls(out, service,false);
                }
            } catch (Exception ex) {
                String error = "Error Looking up Service: "+serviceName+" "+ex.toString();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
                out.println(error);
            }
            
            out.println("</body>");
            out.println("</html>");
        } finally { 
            out.close();
        }
    }
    
    private String getPropertyString(String serviceName) {
        StringBuilder sb = new StringBuilder();
        ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
        boolean hasProps = false;

        if (props.isCentralized()) {
            sb.append("Centralized");
            hasProps = true;
        }

        if (props.isReadOnly()) {
            if (hasProps) {
                sb.append(" / ");
            }
            sb.append("Read Only");
            hasProps = true;
        }

        if (!props.isCachingOn()) {
            if (hasProps) {
                sb.append(" / ");
            }
            sb.append("Caching OFF");
            hasProps = true;
        }

        return sb.toString();
    }
    
    private void writeGroupedServiceCalls(PrintWriter out, Service service) throws Exception {
        writeGroupedServiceCalls(out,service,true);
    }
    
    
    private void writeGroupedServiceCalls(PrintWriter out, Service service, boolean showServiceName) throws Exception {
        String serviceName = service.getServiceName();
        ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
        
        if (showServiceName) {
            out.println("<h3>" + serviceName+ "&nbsp;&nbsp;&nbsp;&nbsp;</h3>");
            out.println(getPropertyString(serviceName)+"<br>");
        }
        Map<String, List<String>> groupedMethods = new TreeMap<String, List<String>>();
        
        List<PBCommand<?,?>> pbcommands = new ArrayList<PBCommand<?,?>>(service.getCommands());
        Collections.sort(pbcommands, new Comparator<PBCommand<?,?>>() {

            @Override
            public int compare(PBCommand<?, ?> o1, PBCommand<?, ?> o2) {
                int cmp = Boolean.compare(o1.isDeprecated(), o2.isDeprecated());
                if (cmp == 0) {
                    cmp = String.CASE_INSENSITIVE_ORDER.compare(o1.getGroup(), o2.getGroup());
                }
                if (cmp == 0) {
                    cmp = Integer.compare(o1.getGroupOrder(), o2.getGroupOrder());
                }
                if (cmp == 0) {
                    cmp = o1.getCommandName().compareTo(o2.getCommandName());
                }
                return cmp;
            }
            
        });
        for (PBCommand<?, ?> command : pbcommands) {
            if (command.isPrivate()) {
                continue;
            }
            StringBuilder methodHTML = new StringBuilder();
            
            methodHTML.append("<table><tr><td valign='top'>");
            if (command.isUpdate()){
                methodHTML.append("<img src=\"icons/pencil.png\">&nbsp");
            } else {
                methodHTML.append("<img src=\"icons/book-open.png\">&nbsp");
            }
            if (command.returnList()) {
                methodHTML.append("List<");
            }
            methodHTML.append(makePbLink(command.getReturnType().trim()));
            if (command.returnList()) {
                methodHTML.append(">");
            }
            
            methodHTML.append(" ");
            
            if (command.isUpdate() && props.isReadOnly()) {
                methodHTML.append("<font color='red'>");
            }
            if (command.isDeprecated() || command.getClass().isAnnotationPresent(Deprecated.class)) {
                methodHTML.append("<strike>").append(command.getCommandName()).append("</strike>");
            } else {
                methodHTML.append(command.getCommandName());
            }
            if (command.isUpdate() && props.isReadOnly()) {
                methodHTML.append("</font>");
            }

            methodHTML.append("</td><td valign='top'>");
            for (PBParamType param : command.getArguments()) {
                methodHTML.append("(");
                int count = 0;
                for (ArgType arg : param.getTypes()) {
                    if (count > 0) {
                        methodHTML.append(", ");
                    }
                    methodHTML.append(makePbLink(arg.toString()));
                    if (arg.getName().isEmpty()) {
                        methodHTML.append(" x").append(count);
                    } else {
                        methodHTML.append(" ").append(arg.getName());
                    }

                    count++;
                }
                methodHTML.append(")<br>");
            }

            String description = command.getDescription();

            methodHTML.append("</td><td valign='top'> - <font color='gray'>").append(description).append("</font></td></tr></table>");
            
            String grouping = command.getGroup();
            
            List<String> current = groupedMethods.get(grouping);
            if (current != null){
                current.add(methodHTML.toString());
                groupedMethods.put(grouping, current);
            } else {
                List<String> groupList = new ArrayList<String>();
                groupList.add(methodHTML.toString());
                groupedMethods.put(grouping, groupList);
            }
            
        }
        
        List<String> ungroupedList = null;
        for (String group : groupedMethods.keySet()){
            if (group.equals("")){
                ungroupedList = groupedMethods.get(group);
            } else {
                out.println("<b>" + group + "</b>");
                for (String line : groupedMethods.get(group)){
                    out.println(line);
                }
                out.println("<br>");
            }
        }
        if (ungroupedList != null) { //wanted to move ungrouped listing to the bottom of page
            out.println("<b>Ungrouped methods</b>");
            for (String line : ungroupedList){
                out.println(line);
            }
        }
        
    }
    
    private void showAllServiceCalls(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>LoUIE Services</title>");  
            out.println("<link rel=\"shortcut icon\" href=\"./favicon.ico\" />"); 
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>LoUIE Services</h1>");
            
            out.println("<a href=\"./info\">Service List</a><br>");
            
            for (Service service : ServiceManager.getServices()) {
                try {
                    writeGroupedServiceCalls(out, service);
                } catch (Exception ex) {
                    out.println("Error Looking up Service: " + service.getServiceName()+"\n"+ex.toString());
                }
            }
            out.println("</body>");
            out.println("</html>");
        } finally { 
            out.close();
        }
    } 
    
    private void listAllServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ExternalProperties extProps = ExternalProperties.getInstance();
            out.println("<html>");
            out.println("<head>");
            out.println("<title>LoUIE Services</title>");
            out.println("<link rel=\"shortcut icon\" href=\"./favicon.ico\" />"); 
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>LoUIE Services</h1>");
            out.println("<h3> Loaded Jars: </h3>");
            Map<String,String> gitVersions = extProps.getGitVersionMap();
            Map<String,String> compileDates = extProps.getCompileDateMap();
            for (String impl : gitVersions.keySet()) {
                if("LoUIE Processor".equals(impl)) continue; //hardcoded processor skip
                out.println("<b>&nbsp&nbsp " + impl + "</b>: ");
                out.println(gitVersions.get(impl));
                out.println(" (" + compileDates.get(impl) + ")<br>");
            }
            out.println("<br><br><a href=\"?service=all\">Show All Services</a><br>");
            
            out.println("<ul>");
            for (Service service : ServiceManager.getServices()) {
                try {
                    out.println("<li><a href=\"?service="+service.getServiceName()+"\">"+service.getServiceName() +"</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                    out.println(getPropertyString(service.getServiceName()));
                    out.println("</li>");
                } catch (Exception ex) {
                    out.println("Error Looking up Service: " + service.getServiceName()+"\n"+ex.toString());
                }
            }
            out.println("</ul>");
            out.println("</body>");
            out.println("</html>");
        } finally { 
            out.close();
        }
    } 
    
    private String makePbLink(String pb) {
        return "<a href=\"proto?pb="+pb+"\" title=\""+pb+"\">"+pb.replaceAll(".*\\.", "") +"</a>";
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
        return "Short description";
    }// </editor-fold>

}
