/*
 * JsonReader.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.client.connection;

import java.io.BufferedReader;

/**
 * @author cjohnson
 * Created: Jun 13, 2011 3:18:34 PM
 */
public class JsonReader {
    private BufferedReader reader;
    
    public JsonReader(BufferedReader reader) {
        this.reader = reader;
    }
    
    public String readJson() throws Exception {
        int i = reader.read();
        char b = (char) i;

        if (i==-1) {
            return null;
        } else if (b!='{') {
            throw new Exception("JSON Parse Exception, Missing \"{\"!\n");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(b);
        
        int level=1;
        while (level>0) {
            i = reader.read();
            
            if (i==-1) {
                throw new Exception("JSON Parse Exception, EOF reached unexpectedly\n");
            }
            
            b = (char) i;
            if (b=='{') {
                level++;
            } else if (b=='}') {
                level--;
            }
            sb.append(b);
        }
        String json = sb.toString();
        return json;
    }
}
