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
