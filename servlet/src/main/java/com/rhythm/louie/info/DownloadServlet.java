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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
@WebServlet(urlPatterns = {"/downloads"})
public class DownloadServlet extends HttpServlet{
    
    private String pythonDownload = null;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        if (pythonDownload == null) {
            pythonDownload = findPythonFile();
        }
        
        String download = request.getParameter("dl");
        
        if (download == null) {
            generatePage(request,response);
        } else {
            if ("python".equals(download)) {
                downloadFile(request, response, pythonDownload);
            }
        }
        
    }
    
    private void generatePage(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> props = new HashMap<>();
        props.put("pyError", pythonDownload.isEmpty() ? "Failed to locate python zip file!" : "");
        InfoUtils.writeTemplateResponse(request, response,"downloads.vm", props);
    }    
    
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, String filePath) throws IOException {
        if (filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File file = new File(filePath);
        int length = 0;
        try (ServletOutputStream outStream = response.getOutputStream()) {
            ServletContext context  = getServletConfig().getServletContext();
            
            String mimetype = context.getMimeType(filePath);
            
            // sets response content type
            if (mimetype == null) {
                mimetype = "application/octet-stream";
            }
            response.setContentType(mimetype);
            response.setContentLength((int)file.length());
            String fileName = (new File(filePath)).getName();
            
            // sets HTTP header
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            byte[] byteBuffer = new byte[4096];
            // reads the file's bytes and writes them to the response stream
            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
                // reads the file's bytes and writes them to the response stream
                while ((in != null) && ((length = in.read(byteBuffer)) != -1))
                {
                    outStream.write(byteBuffer,0,length);
                }
            }
        }
    }
    
    private String findPythonFile() {
        ServletContext context  = getServletConfig().getServletContext();
        String rp = context.getRealPath(".");
        
        File dir = new File(rp);
        FileFilter fileFilter = new WildcardFileFilter("*-python.zip");
        File[] files = dir.listFiles(fileFilter);
        if (files.length == 1) {
            return files[0].getAbsolutePath(); //if there are multiple things that match that pattern, we have bigger issues
        } else if (files.length > 1) {
            LoggerFactory.getLogger(DownloadServlet.class).error("Multiple files matching *-python.zip were located. Cannot resolve.");
        }
        return "";
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
        return "Build Information";
    }// </editor-fold>
    
    
}
