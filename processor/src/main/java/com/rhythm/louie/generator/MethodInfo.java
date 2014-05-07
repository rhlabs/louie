/*
 * MethodInfo.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.rhythm.louie.process.CommandDescriptor;

/**
 *
 * @author cjohnson
 */
public class MethodInfo {
    private final ExecutableElement method;
    private final List<ParamInfo> params = new ArrayList<ParamInfo>();
    private final CommandDescriptor command;
    
    public MethodInfo(ExecutableElement method) {
        this.method=method;
        command = method.getAnnotation(CommandDescriptor.class);
        
        if (method.getParameters()!=null) {
            for (VariableElement param : method.getParameters()) {
                params.add(new ParamInfo(param));
            }
        }
    }
    
    public boolean isDeprecated() throws Exception {
        return method.getAnnotation(Deprecated.class)!=null;
    }
    
    public List<ParamInfo> getParameters() {
        return params;
    }
    
    public TypeMirror getReturnType() {
        return method.getReturnType();
    }
    
    public boolean returnsList() {
        return getReturnType().toString().startsWith("List<") || 
               getReturnType().toString().startsWith("java.util.List<");
    }
    
    public String getBaseReturnType() {
        return getReturnType().toString().replaceFirst("(?:java\\.util\\.)?List<(.*)>","$1");
    }
    
    public String getJavadocReturnType() {
        return getReturnType().toString().replaceAll(".*\\.(.*?)", "$1");
    }
    
    public String getJavadocConvertedReturnType() {
        String returnType = getConvertedReturnType().toString();
        String baseType = returnType.replaceFirst("(?:java\\.util\\.)?List<(.*)>","$1");
        boolean list = !baseType.equals(returnType);
        baseType = baseType.replaceFirst(".*\\.(.*?)", "$1");
        if (list) {
            return "List of "+baseType;
        }
        return baseType;
    }
    
    public String getConvertedReturnType() {
        return TypeUtils.convertPBType(method.getReturnType().toString());
    }
    
    public String getReturnConvertCode(String variableName) {
        return TypeUtils.getToValueCode(getReturnType().toString(),variableName);   
    }
    
    public String getReturnBuilderCode(String variableName) {
        return TypeUtils.getPBBuilderCode(getReturnType().toString(),variableName);
    }
    
    public List<? extends TypeMirror> getThrows() {
        return method.getThrownTypes();
    }
    
    public boolean hasThrows() {
        return !method.getThrownTypes().isEmpty();
    }
    
    public String getThrowsClause() {
        if (!getThrows().isEmpty()) {
            StringBuilder throwNames = new StringBuilder();
            for (TypeMirror t : getThrows()) {
                String s = t.toString();
                if (s.equals("java.lang.Exception")) {
                    s = "Exception";
                }
                
                if (throwNames.length()!=0) {
                    throwNames.append(", ");
                }
                throwNames.append(s);
            }
            return throwNames.toString();
        }
        return "";
    }
    
    public String getName() {
        return method.getSimpleName().toString();
    }
    
    
    public boolean hasParams() {
        return !params.isEmpty();
    }
    
    public String getParamString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(param.getArgString());
        }
        return sb.toString();
    }
    
    
    public String getConvertedParamString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(param.getConvertedArgString());
        }
        return sb.toString();
    }
    
    public String getBuilderParamString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(TypeUtils.getPBBuilderCode(param.getType().toString(),param.getName()));
        }
        return sb.toString();
    }
    
    public String getConvertParamString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(TypeUtils.getToValueCode(param.getType().toString(),param.getName()));
        }
        return sb.toString();
    }
    
    public String getParamNameString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(param.getName());
        }
        return sb.toString();
    }
    
    public String getDescription() {
        return command.description();
    }
    
    public class ParamInfo {
        VariableElement param;
        public ParamInfo(VariableElement param) {
            this.param = param;
        }
        
        public TypeMirror getType() {
            return param.asType();
        }
        
        public String getConvertedType() {
            return TypeUtils.convertPBType(param.asType().toString());
        }
        
        public String getName() {
            return param.getSimpleName().toString();
        }
        
        public String getArgString() {
            return param.asType().toString() + " " + getName();
        }
        
        public String getConvertedArgString() {
            return getConvertedType() + " " + getName();
        }
    }
}
