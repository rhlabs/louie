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

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.rhythm.louie.server.ServiceManager;
import com.rhythm.louie.server.ServiceProperties;
import com.rhythm.louie.service.Service;
import com.rhythm.louie.service.command.PBCommand;

/**
 *
 * @author cjohnson
 */
@WebServlet(urlPatterns = {"/services"})
public class ServicesServlet extends HttpServlet {
      
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String service = request.getParameter("service");
        String config = request.getParameter("configs");
        
        if (service==null || service.isEmpty()) {
            listAllServices(request,response);
        } else {
            listServiceCalls(service,request,response);
        }
    }
    
    private void listAllServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,Object> properties = new HashMap<>();
        
        properties.put("services", ServiceManager.getServices());
        properties.put("calls", false);
        
        InfoUtils.writeTemplateResponse(request, response,"services.vm", properties);
    } 
    
    private void listServiceCalls(String serviceName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,Object> properties = new HashMap<>();
        
        properties.put("services", ServiceManager.getServices());
        properties.put("calls", true);
        
        Service service;
        try {
            service = ServiceManager.getService(serviceName);
            if (service == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "Error Looking up Service: " + serviceName +"! Service does not exist.");
                return;
            }
            properties.put("service", service);
            
            Map<String, ServiceProperties> servProps = new HashMap<>();
            for (ServiceProperties prop : ServiceProperties.getAllServiceProperties()) {
                servProps.put(prop.getName(), prop);
            }
            properties.put("serviceconf", servProps);

            Map<String, List<PBCommand>> groupedCalls = groupServiceCalls(service);
            properties.put("groups", groupedCalls);

        } catch (Exception ex) {
            String error = "Error Looking up Service: " + serviceName + " " + ex.toString();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
        }

        InfoUtils.writeTemplateResponse(request, response,"services.vm", properties);
    } 
    
    private Map<String, List<PBCommand>> groupServiceCalls(Service service) throws Exception {
        Map<String, List<PBCommand>> groupedMethods = new TreeMap<>();
        
        List<PBCommand> pbcommands = new ArrayList<>(service.getCommands());
        Collections.sort(pbcommands, new Comparator<PBCommand>() {

            @Override
            public int compare(PBCommand o1, PBCommand o2) {
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
        
        for (PBCommand command : pbcommands) {
            if (command.isInternal()) {
                continue;
            }
            
            String grouping = command.getGroup();
            
            List<PBCommand> groupList = groupedMethods.get(grouping);
            if (groupList == null){
                groupList = new ArrayList<>();
                groupedMethods.put(grouping, groupList);
            }
            groupList.add(command);
        }
        
        Map<String, List<PBCommand>> fixed = new LinkedHashMap<>(groupedMethods);
        List<PBCommand> ungrouped = fixed.remove("");
        if (ungrouped!=null) {
            String label = fixed.isEmpty()?"Service Methods":"Ungrouped";
            fixed.put(label, ungrouped);
        }
        
        return fixed;
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
        return "Louie Service Info";
    }// </editor-fold>
}
