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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.protobuf.Message;

import com.rhythm.pb.RequestProtos.ResponsePB;

import com.rhythm.louie.request.data.Data;

/**
 * @author cjohnson
 * Created: Mar 15, 2011 8:58:08 AM
 */
public class PBServerResponse<T extends Message> {
    private final List<byte[]> bytes;
    private ResponsePB response;

    private final List<Data> data;
    private final List<T> results;
    
    private final T template;
    
    @Deprecated
    public PBServerResponse(ResponsePB response) {
        this.response = response;
        this.template = null;
        bytes = new ArrayList<byte[]>(response.getCount());
        data = Collections.emptyList();
        results = Collections.emptyList();
    }
    
    public PBServerResponse(ResponsePB response,T template,InputStream input) throws Exception {
        this.response = response;
        this.template = template;
        bytes = Collections.emptyList();
        data = new ArrayList<Data>(response.getCount());
        results = new ArrayList<T>(response.getCount());
        
        for (int d = 0; d < response.getCount(); d++) {
            addData(Data.readPBData(input));
        }
    }

    public ResponsePB getResponse() {
        return response;
    }

    @Deprecated
    public List<byte[]> getByteList() {
        return bytes;
    }

    @Deprecated
    public void addBytes(byte[] b) {
        if (b!=null) {
            bytes.add(b);
        }
    }
    
    protected final void addData(Data d) throws Exception {
        if (d!=null) {
            data.add(d);
            if (template!=null) {
                results.add(d.parse(template));
            }
        }
    }
    
    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }
    
    public T getSingleResult() {
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
}
