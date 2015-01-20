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
package com.rhythm.louie.generator;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 *
 * @author cjohnson
 */
public class TypeUtils {
    private static final String DATATYPE_PREFIX = "com.rhythm.pb.DataTypeProtos.";
    
    static final Map<String,String> legacyMap;
    static final Map<String,String> pbMap;
    static final Map<String,String> typeMap;
    static {
        typeMap = new HashMap<>();
        typeMap.put(DATATYPE_PREFIX+"StringPB","String");
        typeMap.put(DATATYPE_PREFIX+"StringListPB",listOf("String"));
        
        typeMap.put(DATATYPE_PREFIX+"IntPB","Integer");
        typeMap.put(DATATYPE_PREFIX+"IntListPB",listOf("Integer"));
        
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
        
        // Build the reverse
        pbMap = new HashMap<>();
        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            pbMap.put(entry.getValue(),entry.getKey());
        }
        
        pbMap.put("Integer", DATATYPE_PREFIX+"IntPB");
        pbMap.put(listOf("Integer"), DATATYPE_PREFIX+"IntListPB");
        
        pbMap.put("Long", DATATYPE_PREFIX+"LongPB");
        pbMap.put(listOf("Long"),DATATYPE_PREFIX+"LongListPB");
    
        // A bit inefficient here, but that way we do not have to list all the types again
        legacyMap = new HashMap<>();
        for (String type : typeMap.keySet()) {
            type = type.replaceFirst(DATATYPE_PREFIX, "");
            legacyMap.put("rh.pb."+type,"louie."+type);
        }
        legacyMap.put("rh.pb.IdentityPB","louie.IdentityPB");
    }
    
    private static String listOf(String type) {
        return "java.util.List<"+type+">";
    }
    
    public static String convertFromPB(String s) {
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
    
    public static String convertToPB(String s) {
        return mapToSimplePB(s);
//        if (s.startsWith("List<") || s.startsWith("java.util.List<")) {
//            String subType = s.replaceFirst(".*List<(.*)>", "$1");
//            return s.replaceFirst(".*List<(.*)>",  mapToSimplePB(subType));
//        } else {
//            return mapSimplePB(s);
//        }
    }
    
    public static boolean hasConversionToPB(String name) {
        name = name.replaceFirst("java.lang.", "");
        return pbMap.containsKey(name);
    }
    
    public static String mapToSimplePB(String name) {
        name = name.replaceFirst("java.lang.", "");
        String type = pbMap.get(name);
        if (type == null) {
            return name;
        }
        return type;
    }
    
    static final Map<String,String> pbtypeMap;
    static {
        pbtypeMap = new HashMap<>();
        pbtypeMap.put(DATATYPE_PREFIX+"StringPB","STRING");
        pbtypeMap.put(DATATYPE_PREFIX+"StringListPB","STRING_LIST");
        
        pbtypeMap.put(DATATYPE_PREFIX+"IntPB","INT");
        pbtypeMap.put(DATATYPE_PREFIX+"IntListPB","INT_LIST");
        
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
    
    public static boolean instanceOf(Types types, TypeMirror type, Class cl) {
        return instanceOf(types,type,cl.getName());
    }
    
    public static boolean instanceOf(Types types, TypeMirror type, String className) {
        //System.out.println("instanceOf : "+type.toString()+":"+className);
        
        String baseType = type.toString();
        if (type instanceof DeclaredType) {
            baseType = ((DeclaredType)type).asElement().toString();
        }
        //System.out.println("BaseType: "+baseType);
        
        if (baseType.equals(className)) {
            return true;
        }
        TypeElement elem = (TypeElement) types.asElement(type);
        for (TypeMirror i : elem.getInterfaces()) {
            if (instanceOf(types,i,className)) {
                return true;
            }
        }
        
        TypeMirror sup =  elem.getSuperclass();
        if (sup instanceof NoType) {
            return false;
        }
        return (instanceOf(types,sup,className));
    }
    
    public static String legacyConvert(String type) {
        String converted  = legacyMap.get(type);
        return converted==null?type:converted;
    }
    
}
