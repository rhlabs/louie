/*
 * CacheServlet.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
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

/**
 *
 * @author cjohnson
 */
@WebServlet(urlPatterns = {"/cache"})
public class CacheServlet extends HttpServlet {
      
    private final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private final Pattern HOST = Pattern.compile("(\\w+):.*");
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clearCache = request.getParameter("clear");
        if (clearCache!=null && !clearCache.isEmpty()) {
            String result = clearCache(clearCache);
            try (PrintWriter out = response.getWriter()) {
                if (result == null){
                    out.println("NONE");
                } else {
                    out.println(clearCache);
                }
            }
        } else {
            String manager = request.getParameter("manager");
            if (manager == null || manager.isEmpty()) {
                showCacheList(request, response);
            } else {
                showCacheList(manager, request, response);
            }
        }
    }
    
    private void showCacheList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,Object> properties = new HashMap<>();
        
        String serverName = request.getHeader("Host");
        Matcher m = HOST.matcher(serverName);
        if (m.matches()) {
            serverName = m.group(1);
        }
        properties.put("serverName", serverName);
        
        properties.put("managers", CacheManager.getCacheManagers());
        properties.put("showCaches", false);
        
        InfoUtils.writeTemplateResponse(request, response,"cache.vm", properties);
        
    }
    
    private void showCacheList(String managerName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,Object> properties = new HashMap<>();

        String serverName = request.getHeader("Host");
        Matcher m = HOST.matcher(serverName);
        if (m.matches()) {
            serverName = m.group(1);
        }
        properties.put("serverName", serverName);
        

        List<String> cacheNames = new ArrayList<>();
        cacheNames.add("ALL");
        cacheNames.add(managerName);
        
        CacheManager manager = CacheManager.getCacheManager(managerName);
        properties.put("manager", manager);
        
        for (Cache<?, ?> cache : manager.getCaches()) {
            cacheNames.add(cache.getCacheName());
        }
        
        properties.put("cacheNames", cacheNames);
        
        properties.put("managers", CacheManager.getCacheManagers());
        properties.put("showCaches", true);

        InfoUtils.writeTemplateResponse(request, response, "cache.vm", properties);
    }
    
    @SuppressWarnings("unchecked")
    private String clearCache(String targetCache){
        logger.info("ATTEMPTING CLEAR of "+targetCache);
        
        Collection<CacheManager> managers = Collections.emptyList();
        String cacheName = null;
        
        if (targetCache.equals("ALL")) {
            managers = CacheManager.getCacheManagers();
        } else {
            CacheManager manager = CacheManager.getCacheManager(targetCache);
            if (manager!=null) {
                managers = Collections.singleton(manager);
            } else {
                Pattern p = Pattern.compile("(.*)\\[(.*)\\]");
                Matcher m = p.matcher(targetCache);
                if (m.matches()) {
                    logger.info("Manager: "+m.group(1)+" Cache: " + m.group(2));
                    manager = CacheManager.getCacheManager(m.group(1));
                    if (manager!=null) {
                        managers = Collections.singleton(manager);
                    }
                    cacheName=m.group(2);
                }
            }
        }
        
        int cleared = 0;
        boolean success = true;
        for (CacheManager manager : managers) {
            Collection<Cache<?,?>> caches;
            if (cacheName == null) {
                caches = manager.getCaches();
            } else {
                Cache<?,?> cache = manager.getCache(cacheName);
                if (cache!=null) {
                    caches = (Collection<Cache<?,?>>) Collections.singleton(cache);
                } else {
                    caches = Collections.emptyList();
                }
            }
            for (Cache<?,?> cache : caches) {
                try {
                    cache.clear();
                    cleared++;
                    logger.info("Cache " + manager.getName() + "->" + cache.getCacheName() + " has been cleared locally!");
                } catch (Exception ex) {
                    success = false;
                    logger.error(ex.toString());
                }
            }
        }
        if (success && cleared>0) {
            return targetCache;
        }
        return null;
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Cache Management";
    }// </editor-fold>
}
