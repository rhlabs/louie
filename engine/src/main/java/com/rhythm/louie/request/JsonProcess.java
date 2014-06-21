/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.request;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eyasukoc
 */
public interface JsonProcess {
    
    public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

}
