/*
 * MethodInfo.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.rhythm.louie.Disabled;
import com.rhythm.louie.Grouping;
import com.rhythm.louie.Internal;
import com.rhythm.louie.process.ServiceCall;
import com.rhythm.louie.Streaming;
import com.rhythm.louie.Updating;

/**
 *
 * @author cjohnson
 */
public class MethodInfo {
    private final ExecutableElement method;
    private final List<ParamInfo> params = new ArrayList<ParamInfo>();
    private String javaDoc;
    
    private final boolean returnsCollection;
    private final Internal internal;
    private final Deprecated deprecated;
    private final Disabled disabled;
    private final Updating updating;
    private final Streaming streaming;
    private final Grouping grouping;
    
    private TypeMirror baseReturnType;
    
    public MethodInfo(ProcessingEnvironment processingEnv, ExecutableElement method) {
        this.method=method;
        
        if (method.getParameters()!=null) {
            for (VariableElement param : method.getParameters()) {
                params.add(new ParamInfo(param));
            }
        }
        
        internal = method.getAnnotation(Internal.class);
        disabled = method.getAnnotation(Disabled.class);
        deprecated = method.getAnnotation(Deprecated.class);
        updating = method.getAnnotation(Updating.class);
        streaming = method.getAnnotation(Streaming.class);
        grouping = method.getAnnotation(Grouping.class);
                
        Elements elems = processingEnv.getElementUtils();
        javaDoc = elems.getDocComment(method);
        if (javaDoc == null) {
            javaDoc = "No Docs";
        }
        
        TypeMirror returnType = method.getReturnType();
         
        Types types = processingEnv.getTypeUtils();
        returnsCollection = TypeUtils.instanceOf(types,returnType,Collection.class);
        
        baseReturnType = returnType;
        
        if (returnsCollection && returnType instanceof DeclaredType) {
            DeclaredType decType = (DeclaredType) returnType;
            if (decType.getTypeArguments().size() == 1) {
                baseReturnType = decType.getTypeArguments().get(0);
            }
        }
    }
    
    public boolean isInternal() {
        return internal!=null;
    }
    
    public boolean isDeprecated() {
        return deprecated!=null;
    }
    
    public boolean isDisabled() {
        return disabled!=null;
    }
    
    public boolean isUpdating() {
        return updating!=null;
    }
    
    public boolean isStreaming() {
        return streaming!=null;
    }
    
    public boolean isClientAccess() {
        return !isInternal() && !isDisabled();
    }
    
    public List<ParamInfo> getParameters() {
        return params;
    }
    
    public String getJavadoc() {
        return javaDoc;
    }
    
    public TypeMirror getReturnType() {
        return method.getReturnType();
    }
    
    public String getPbReturnType() {
        return TypeUtils.convertToPB(method.getReturnType().toString());
    }
    
    public String getClientPbReturnType() {
        String pbReturnType = TypeUtils.convertToPB(method.getReturnType().toString());
        if (pbReturnType.equals(method.getReturnType().toString()) && returnsList()) {
            if (!pbReturnType.startsWith("java.util.List")) {
                return pbReturnType.replaceFirst(".*<(.*)>", "java.util.List<$1>");
            }
        }
        return pbReturnType;
    }
    
    public boolean returnsList() {
        return returnsCollection;
    }
    
    public boolean returnsPbList() {
        // TODO not really bullet proof here... but gets the job done
        return returnsCollection && getPbReturnType().matches(".*<(.*)>");
    }
    
    public TypeMirror getBaseReturnType() {
        return baseReturnType;
    }
    
    public String getBasePbReturnType() {
        return TypeUtils.convertToPB(baseReturnType.toString());
    }
    
    public String getClientReturnType() {
        if (returnsCollection) {
            return "java.util.List<"+getBaseReturnType()+">";
        }
        return getReturnType().toString();
    }
    
    public String getReturnPbCode(String variableName) {
        return TypeUtils.getToValueCode(getPbReturnType(),variableName);   
    }
    
    public String getReturnBuilderCode(String variableName) {
        return TypeUtils.getPBBuilderCode(getPbReturnType(),variableName);
    }
    
    public List<? extends TypeMirror> getThrows() {
        return method.getThrownTypes();
    }
    
    public boolean hasThrows() {
        return !method.getThrownTypes().isEmpty();
    }
    
    public String getThrowsClause() {
        if (!getThrows().isEmpty()) {
            int i=0;
            StringBuilder throwNames = new StringBuilder(" throws ");
            for (TypeMirror t : getThrows()) {
                String s = t.toString();
                if (s.equals("java.lang.Exception")) {
                    s = "Exception";
                }
                if (i++!=0) {
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
    
    public String getPbParamString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(param.getPbArgString());
        }
        return sb.toString();
    }
    
    public String getTypeString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(",");
            }
            sb.append(param.getType());
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
            sb.append(TypeUtils.getPBBuilderCode(param.getPbType(),param.getName()));
        }
        return sb.toString();
    }
    
    public String getPbToArgString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(TypeUtils.getToValueCode(param.getPbType(),param.getName()));
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
    
    private void appendSimpleAnnotation(StringBuilder sb, String indent, Class cl, boolean newLine) {
        if (sb.length()!=0) {
            sb.append(indent);
        }
        sb.append("@").append(cl.getName());
        if (newLine) {
            sb.append("\n");
        }
    }
    
    public String getServiceCallString(String indent) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            appendSimpleAnnotation(sb, indent, Deprecated.class, true);
        }
        if (isStreaming()) {
            appendSimpleAnnotation(sb, indent, Streaming.class, true);
        }
        if (isUpdating()) {
            appendSimpleAnnotation(sb, indent, Updating.class, true);
        }
        if (grouping != null) {
            appendSimpleAnnotation(sb, indent, Grouping.class, false);
            sb.append("(");
            if (grouping.groupOrder()>=0) {
                sb.append("groupOrder=").append(grouping.groupOrder()).append(", ");
            }
            sb.append("group=\"").append(grouping.group()).append("\")\n");
        }
        appendSimpleAnnotation(sb, indent, ServiceCall.class, false);
        sb.append("(javadoc=");
        int i=0;
        for(String line : getJavadoc().split("\n")) {
            line = line.replaceAll("\\\"", "\\\\\"");
            if (i++>0) {
                sb.append("\\n\"\n").append(indent).append(indent).append(indent).append(" + ");
            }
            sb.append("\"").append(line.trim());
        }
        sb.append("\"");
        if (!getParameters().isEmpty()) {
            sb.append(",\n").append(indent).append(indent).append("args={");
            i=0;
            for (ParamInfo param : getParameters()) {
                if (i++>0) {
                    sb.append(", ");
                }
                sb.append("\"").append(param.getName()).append("\"");
            }
            sb.append("}");
        }
        sb.append(")");
        return sb.toString();
    }
    
    public class ParamInfo {
        VariableElement param;
        public ParamInfo(VariableElement param) {
            this.param = param;
        }
        
        public TypeMirror getType() {
            return param.asType();
        }
        
        public String getPbType() {
            return TypeUtils.convertToPB(param.asType().toString());
        }
        
        public String getName() {
            return param.getSimpleName().toString();
        }
        
        public String getArgString() {
            return param.asType().toString() + " " + getName();
        }
        
        public String getPbArgString() {
            return getPbType() + " " + getName();
        }
    }
}
