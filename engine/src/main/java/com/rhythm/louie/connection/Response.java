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
package com.rhythm.louie.connection;

import java.util.List;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.RoutePathPB;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:36:16 PM
 */
 public class Response {
    private final ResponsePB response;
    
    public Response(ResponsePB response) {
        this.response = response;
    }
    
    public List<RoutePathPB> getRouteList() {
        return response.getRouteList();
    }
    
    public ErrorPB getError() {
        return response.getError();
    }
    
    public int getResultCount() {
        return response.getCount();
    }
     
}
