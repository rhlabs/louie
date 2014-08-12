/*
 * PythonMethodInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.plugins;

import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author sfong
 */
public class PythonMethodInfo extends MethodInfo {

    protected static final Map<String, String> pbtypeMap;
    static {
        pbtypeMap = new HashMap<String, String>();
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
    
    public String getReturnType() {
        String baseReturnType = getBaseReturnType();
        if (baseReturnType.contains("DataTypeProtos")) {
            return "rh.pb.datatype_pb2";
        }
        baseReturnType = baseReturnType.replaceFirst("com.rhythm", "rh");
        baseReturnType = baseReturnType.replaceAll("\\.\\w*Protos", "");
        String[] split = baseReturnType.split("\\.");

        String suffix = split[split.length - 2] + "_pb2";

        List<String> l = new ArrayList<String>();

        int i = 0;
        for (String s : split) {
            if (i == split.length - 1) {
                l.add(suffix);
            } else {
                l.add(s);
            }
            i++;
        }
        return StringUtils.join(l, ".");
    }
        
    public String getType(String pbType) {
        if (!pbtypeMap.containsKey(pbType)) {
            return "";
        }
        return pbtypeMap.get(pbType);
    }
        
    public String getImportType(Class<?> param) {
        String type = param.toString();
        if (type.contains("DataTypeProtos")) {
            return "rh.pb.datatype_pb2";
        }
        type = type.replaceFirst("com.rhythm", "rh");
        type = type.replaceAll("\\.\\w*Protos", "");
        String[] split = type.split("\\.");

        String suffix = split[split.length - 2] + "_pb2";

        List<String> l = new ArrayList<String>();

        int i = 0;
        for (String s : split) {
            if (i == split.length - 1) {
                l.add(suffix);
            } else {
                l.add(s);
            }
            i++;
        }
        return StringUtils.join(l, ".");
    }
    
    public Set<String> getImports() {
        Set<String> imports = new LinkedHashSet<String>();
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
