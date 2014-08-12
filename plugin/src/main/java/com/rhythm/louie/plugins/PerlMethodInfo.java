/*
 * PerlMethodInfo.java
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
public class PerlMethodInfo extends MethodInfo {

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
    
    public PerlMethodInfo(Method method) {
        super(method);
    }

    public String getReturnType() {
        String baseReturnType = getBaseReturnType();
        baseReturnType = baseReturnType.replaceFirst("com.rhythm", "RH");
        baseReturnType = baseReturnType.replaceAll("\\.\\w*Protos", "");
        baseReturnType = baseReturnType.replaceAll("\\$", ".");
        String[] split = baseReturnType.split("\\.");

        List<String> l = new ArrayList<String>();
        
        for (String s : split) {
            if (s.equals("rh")) {
                l.add("RH");
            } else {
                l.add(Character.toUpperCase(s.charAt(0)) + s.substring(1));
            }
        }
        return StringUtils.join(l, "::");
    }
        
    public String getType(String pbType) {
        if (!pbtypeMap.containsKey(pbType)) {
            return "";
        }
        return pbtypeMap.get(pbType);
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
            sb.append("$").append(param.getName());
        }
        return sb.toString();
    }
}
