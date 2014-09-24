/*
 * CacheInfoServlet.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.cache.Cache;
import com.rhythm.louie.cache.CacheManager;

//import net.sf.ehcache.CacheManager;


/**
 *
 * @author cjohnson
 */
@WebServlet(name = "CacheInfoServlet", urlPatterns = {"/cache"})
public class CacheInfoServlet extends HttpServlet {
    
    private final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private final Pattern HOST = Pattern.compile("(\\w+):.*");
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<String> cacheNames = new ArrayList<String>();
        
        String serverName = request.getHeader("Host");
        Matcher m = HOST.matcher(serverName);
        if(m.matches()){
            serverName = m.group(1);
        }
        
        response.setContentType("text/html;charset=UTF-8");

        String targetCache = request.getReader().readLine();
        PrintWriter out = response.getWriter();
        
        if (targetCache != null){
            String result = clearCache(targetCache);
            try{
                if (result == null){
                    out.println("NONE");
                } else {
                    out.println(targetCache);
                }
            } finally {
                out.close();
            }
        } else {
            cacheNames.add("ALL");
            try {
                out.println("<html>");
                out.println("<head>");
                out.println("<link rel=\"shortcut icon\" href=\"favicon.ico\" />");
                out.println("<title> " + serverName + " LoUIE Cache Management</title>");  
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>LoUIE cache manager for server: " + serverName + "</h1>");
                out.println("<input type=\"button\" id=\"ALL\" value = \"Clear ALL for this server\" /><br>");
                Collection<CacheManager> managerList = CacheManager.getCacheManagers();
                for (CacheManager manager : managerList){
                    String managerName = manager.getName();
                    cacheNames.add(managerName);
                    out.println("<br><br><b>" + managerName + "</b> &nbsp&nbsp<input type=\"button\" id=\"" 
                            + managerName + "\" value = \"Clear ALL for this service\" /><br>");
                    for (Cache<?,?> cache : manager.getCaches()){
                        String cacheName = cache.getCacheName();
                        cacheNames.add(cacheName);
                        String clz = cache.getClass().getSimpleName();
                        out.println("<br>&nbsp&nbsp " + cacheName + " &nbsp&nbsp("
                                +clz+") &nbsp&nbsp"+cache.getSize()+" &nbsp&nbsp"
                                + "<input type=\"button\" id=\""
                                + cacheName + "\" value = \"Clear cache\" />");
                    }
                }
                out.println("</body>");
                out.println("<script type='text/javascript'>");
                for(String cacheName : cacheNames){
                    out.println("document.getElementById(\"" + cacheName + "\").onclick = function(){ ");
                    out.println("    var confirmation = confirm(\"Clear cache " +  cacheName + "?\")");
                    out.println("    if (confirmation==true){ executeRequest(\"" + cacheName + "\"); }");
                    out.println("}");
                }
                out.println("function executeRequest(cacheName) {");
                out.println("    var xreq = new XMLHttpRequest();");
                out.println("    xreq.open(\"POST\", \"" + request.getRequestURI() + "\", true);");
                out.println("    xreq.setRequestHeader('Content-Type', \"text/html\");");
                out.println("    xreq.onreadystatechange = function() {");
                out.println("        if (xreq.readyState != 4) { return; }");
                out.println("        var xresp = \"CLEARED: \";");
                out.println("        xresp = xresp.concat(xreq.responseText)");
                out.println("        alert(xresp)");
                out.println("    };");
                out.println("    xreq.send(cache=cacheName)");
                out.println("};");
                out.println("</script>");
                out.println("</html>");
            } finally {            
                out.close();
            }
        }
    }
    
    private String clearCache(String targetCache){
        boolean all = false;
        boolean success = false;
        if (targetCache.equals("ALL")){
                all = true;
        }
        for (CacheManager manager : CacheManager.getCacheManagers()){
            String managerName = manager.getName();
            boolean managerAll = false;
            if (targetCache.equals(managerName)){
                managerAll = true;
            }
            for (Cache cache : manager.getCaches()){
                String cacheName = cache.getCacheName();
                if (managerAll || all || targetCache.equals(cacheName)) {
                    try {
                        cache.clear();
                        success = true;
                        logger.info("Cache " + managerName + "->" + cacheName + " has been cleared locally!");
                    } catch (Exception ex) {
                        success = false;
                        logger.error(ex.toString());
                    }
                }
            }
        }
        if (success) return targetCache;
        return null;
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
