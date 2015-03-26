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

package com.rhythm.louie.clientgen;

import java.util.*;

/**
 *
 * @author eyasukoc
 */
public class JSServiceInfo extends ServiceInfo{

    private final Map<String,ProtoInfo> protoInfos;
    private boolean usesDatatypes = false;
    
    public JSServiceInfo(Class<?> cl, String host, String gateway, List<JSMethodInfo> methods) throws Exception {
        super(cl, host, gateway, methods);
        protoInfos = new HashMap<>();
        for (JSMethodInfo method : methods) {
            for (ProtoInfo pInfo : method.getProtos()) {
                String protoKey = pInfo.getProtoFile();
                if (protoKey.contains("datatype.proto")) {
                    usesDatatypes = true;
                } else {
                    if (protoInfos.containsKey(protoKey)) {
                        protoInfos.get(protoKey).merge(pInfo);
                    } else {
                        protoInfos.put(protoKey, pInfo);
                    }
                }
            }
        }
    }
     
    public boolean usesDatatypes() {
        return usesDatatypes;
    }
    
    public List<ProtoInfo> getProtos() {
        return new ArrayList<>(protoInfos.values());
    }
    
}
