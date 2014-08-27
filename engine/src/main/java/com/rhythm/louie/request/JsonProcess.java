/*
 * JsonProcess.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
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
