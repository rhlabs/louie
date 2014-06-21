/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.request;

import com.rhythm.pb.data.Result;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author eyasukoc
 */
public interface ProtoProcess {
    
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws Exception;

}
