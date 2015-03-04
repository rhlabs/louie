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

import com.google.protobuf.Descriptors;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author sfong
 */
public class PythonMethodInfo extends MethodInfo {

    protected static final Map<String, String> pbtypeMap;
    static {
        pbtypeMap = new HashMap<>();
        pbtypeMap.put("StringPB", "str");
        pbtypeMap.put("StringListPB", "list");
        pbtypeMap.put("DatePB", "int");
        pbtypeMap.put("DateListPB", "list");
        pbtypeMap.put("DateTimePB", "int");
        pbtypeMap.put("DateTimeListPB", "list");
        pbtypeMap.put("BoolPB", "bool");
        pbtypeMap.put("BoolListPB", "list");
        pbtypeMap.put("IntPB", "int");
        pbtypeMap.put("IntListPB", "list");
        pbtypeMap.put("UIntPB", "int");
        pbtypeMap.put("UIntListPB", "list");
        pbtypeMap.put("LongPB", "long");
        pbtypeMap.put("LongListPB", "list");
        pbtypeMap.put("ULongPB", "long");
        pbtypeMap.put("ULongListPB", "list");
        pbtypeMap.put("FloatPB", "float");
        pbtypeMap.put("FloatListPB", "list");
        pbtypeMap.put("DoublePB", "float");
        pbtypeMap.put("DoubleListPB", "list");
    }
    
    public PythonMethodInfo(Method method) {
        super(method);
    }
    
    public String getReturnType() throws Exception{
        String classname = getBaseReturnType();
        StringBuilder bldr = new StringBuilder(classname);
        int lastDotIndex = classname.lastIndexOf(".");
        bldr.replace(lastDotIndex, lastDotIndex + 1, "$" );
        classname = bldr.toString();
        return loadPBType(classname);
    }
        
    public String getType(String pbType) {
        if (!pbtypeMap.containsKey(pbType)) {
            return "";
        }
        return pbtypeMap.get(pbType);
    }
        
    private String loadPBType(String classname) throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> c = cl.loadClass(classname);
        Method staticMethod = c.getDeclaredMethod("getDescriptor");
        Descriptors.Descriptor desc = (Descriptors.Descriptor) staticMethod.invoke(null, (Object[]) null);
        String file = desc.getFile().getName();
        file = file.replaceAll("\\.proto", "_pb2");
        file = file.replaceAll("\\/", ".");
        return file;
    }
    
    public String getImportType(Class<?> param) throws Exception {
        String type = param.toString();
        type = type.replace("class ", ""); 
        return loadPBType(type);
    }
    
    public Set<String> getImports() throws Exception {
        Set<String> imports = new LinkedHashSet<>();
        for (ParamInfo param : params) {
            if (isLouieDataType(param.getPbType())) {
                imports.add("from " + getImportType(param.getParam()) + " import " + param.getPbType());
            }
        }
        
        imports.add("from " + getReturnType() + " import " + getPbReturnType());
        return imports;
    }
    
    public String getParamNameString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param.getName());
        }
        return sb.toString();
    }
}
