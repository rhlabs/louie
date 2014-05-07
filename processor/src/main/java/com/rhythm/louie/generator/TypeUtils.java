/*
 * TypeUtils.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cjohnson
 */
public class TypeUtils {
    private static final String DATATYPE_PREFIX = "com.rhythm.pb.DataTypeProtos.";
    
    static final Map<String,String> typeMap;
    static {
        typeMap = new HashMap<String,String>();
        typeMap.put(DATATYPE_PREFIX+"StringPB","String");
        typeMap.put(DATATYPE_PREFIX+"StringListPB",listOf("String"));
        
        typeMap.put(DATATYPE_PREFIX+"UIntPB","Integer");
        typeMap.put(DATATYPE_PREFIX+"UIntListPB",listOf("Integer"));
        typeMap.put(DATATYPE_PREFIX+"IntPB","Integer");
        typeMap.put(DATATYPE_PREFIX+"IntListPB",listOf("Integer"));
        
        typeMap.put(DATATYPE_PREFIX+"ULongPB","Long");
        typeMap.put(DATATYPE_PREFIX+"ULongListPB",listOf("Long"));
        typeMap.put(DATATYPE_PREFIX+"LongPB","Long");
        typeMap.put(DATATYPE_PREFIX+"LongListPB",listOf("Long"));
        
        typeMap.put(DATATYPE_PREFIX+"FloatPB","Float");
        typeMap.put(DATATYPE_PREFIX+"FloatListPB",listOf("Float"));
        typeMap.put(DATATYPE_PREFIX+"DoublePB","Double");
        typeMap.put(DATATYPE_PREFIX+"DoubleListPB",listOf("Double"));
        
        typeMap.put(DATATYPE_PREFIX+"BoolPB","Boolean");
        typeMap.put(DATATYPE_PREFIX+"BoolListPB",listOf("Boolean"));

        typeMap.put(DATATYPE_PREFIX+"DateTimePB","java.util.Date");
        typeMap.put(DATATYPE_PREFIX+"DateTimeListPB",listOf("java.util.Date"));
        
        typeMap.put(DATATYPE_PREFIX+"DatePB","org.joda.time.LocalDate");
        typeMap.put(DATATYPE_PREFIX+"DateListPB",listOf("org.joda.time.LocalDate"));
    }
    
    private static String listOf(String type) {
        return "java.util.List<"+type+">";
    }
    
    public static String convertPBType(String s) {
        if (s.startsWith("List<") || s.startsWith("java.util.List<")) {
            String subType = s.replaceFirst(".*List<(.*)>", "$1");
            s = "java.util.List<" + mapSimplePB(subType) + ">";
        } else {
            s = mapSimplePB(s);
        }
        return s;
    }
    
    public static String mapSimplePB(String name) {
        String type = typeMap.get(name);
        if (type == null) {
            type = typeMap.get(DATATYPE_PREFIX+name);
        }
        if (type == null) {
            return name;
        }
        return type;
    }
    
    
    static final Map<String,String> pbtypeMap;
    static {
        pbtypeMap = new HashMap<String,String>();
        pbtypeMap.put(DATATYPE_PREFIX+"StringPB","STRING");
        pbtypeMap.put(DATATYPE_PREFIX+"StringListPB","STRING_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"UIntPB","UINT");
        pbtypeMap.put(DATATYPE_PREFIX+"UIntListPB","UINT_LIST");
        pbtypeMap.put(DATATYPE_PREFIX+"IntPB","INT");
        pbtypeMap.put(DATATYPE_PREFIX+"IntListPB","INT_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"ULongPB","ULONG");
        pbtypeMap.put(DATATYPE_PREFIX+"ULongListPB","ULONG_LIST");
        pbtypeMap.put(DATATYPE_PREFIX+"LongPB","LONG");
        pbtypeMap.put(DATATYPE_PREFIX+"LongListPB","LONG_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"FloatPB","FLOAT");
        pbtypeMap.put(DATATYPE_PREFIX+"FloatListPB","FLOAT_LIST");
        pbtypeMap.put(DATATYPE_PREFIX+"DoublePB","DOUBLE");
        pbtypeMap.put(DATATYPE_PREFIX+"DoubleListPB","DOUBLE_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"BoolPB","BOOL");
        pbtypeMap.put(DATATYPE_PREFIX+"BoolListPB","BOOL_LIST");

        pbtypeMap.put(DATATYPE_PREFIX+"DateTimePB","DATETIME");
        pbtypeMap.put(DATATYPE_PREFIX+"DateTimeListPB","DATETIME_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"DatePB","DATE");
        pbtypeMap.put(DATATYPE_PREFIX+"DateListPB","DATE_LIST");
    }
    
    public static String getPBBuilderCode(String t,String name) {
        String pbtype = pbtypeMap.get(t);
        if (pbtype == null) {
            pbtype = pbtypeMap.get(DATATYPE_PREFIX+t);
        }
        if (pbtype==null) {
            return name;
        } else {
            return "PBType."+pbtype+".build("+name+")";
        }
    }
    
    public static String getToValueCode(String t,String name) {
        String pbtype = pbtypeMap.get(t);
        if (pbtype == null) {
            pbtype = pbtypeMap.get(DATATYPE_PREFIX+t);
        }
        if (pbtype==null) {
            return name;
        } else {
            String method = "getValue";
            if (pbtype.startsWith("DATE")) {
                return "PBType."+pbtype+".toValue("+name+")";
            }
            if (pbtype.endsWith("LIST")) {
                return name+"."+method+"sList()";
            } else {
                return name+"."+method+"()";
            }
        }
    }
}
