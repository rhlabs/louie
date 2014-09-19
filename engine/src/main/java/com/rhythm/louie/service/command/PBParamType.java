/*
 * PBParamTypes.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.protobuf.Descriptors.Descriptor;

import com.rhythm.louie.pb.PBType;

/**
 * @author cjohnson
 * Created: Aug 17, 2011 12:58:31 PM
 */
public class PBParamType implements Serializable, Comparable<PBParamType> {
    private static final long serialVersionUID = 1L;
    
    public static final PBParamType EMPTY = new PBParamType(0);
    
    private final ArrayList<ArgType> types;
    
    private PBParamType(int size) {
        types = new ArrayList<ArgType>(size);
    }
    private PBParamType(List<String> typeNames) {
        types = new ArrayList<ArgType>(typeNames.size());
        for(String t : typeNames) {
            types.add(new ArgType(t));
        }
    }
    private PBParamType(String typeName) {
        types = new ArrayList<ArgType>(1);
        types.add(new ArgType(typeName));
    }
    
    public static PBParamType typeForNames(List<String> typeNames) {
        return new PBParamType(typeNames);
    }
    
    public static PBParamType typeForNames(String... typeNames) {
        return new PBParamType(Arrays.asList(typeNames));
    }
    
    public static PBParamType singleType(String typeName) {
        return new PBParamType(typeName);
    }
    
    public static PBParamType typeForDescriptors(List<Descriptor> descriptors) {
        PBParamType type = new PBParamType(descriptors.size());
        for (Descriptor desc : descriptors) {
            type.types.add(new ArgType(desc));
        }
        return type;
    }
    
    public static PBParamType typeForDescriptors(Descriptor... descriptors) {
        PBParamType type = new PBParamType(descriptors.length);
        for (Descriptor desc : descriptors) {
            type.types.add(new ArgType(desc));
        }
        return type;
    }
    
    public static PBParamType singleType(Descriptor descriptor) {
        return new PBParamType(descriptor.getFullName());
    }
    
    public static PBParamType typeForPBTypes(List<PBType> types) {
        PBParamType type = new PBParamType(types.size());
        for (PBType pbt : types) {
            type.types.add(new ArgType(pbt));
        }
        return type;
    }
    
    public static PBParamType typeForPBTypes(PBType... types) {
        PBParamType type = new PBParamType(types.length);
        for (PBType pbt : types) {
            type.types.add(new ArgType(pbt));
        }
        return type;
    }
    
    public static PBParamType singleType(PBType type) {
        return new PBParamType(type.getName());
    }
    
    public static List<PBParamType> singleTypeList(List<String> typeNames) {
        List<PBParamType> types = new ArrayList<PBParamType>(typeNames.size());
        for (String name : typeNames) {
            types.add(new PBParamType(name));
        }
        return Collections.unmodifiableList(types);
    }
    
    public static List<PBParamType> singleTypeList(String... typeNames) {
        List<PBParamType> types = new ArrayList<PBParamType>(typeNames.length);
        for (String name : typeNames) {
            types.add(new PBParamType(name));
        }
        return Collections.unmodifiableList(types);
    }
    
    public static PBParamType typeForArgs(ArgType... args) {
        PBParamType type = new PBParamType(args.length);
        type.types.addAll(Arrays.asList(args));
        return type;
    }
    
    public static PBParamType typeForArgs(List<ArgType> args) {
        PBParamType type = new PBParamType(args.size());
        type.types.addAll(args);
        return type;
    }
    
    public int count() {
        return types.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PBParamType other = (PBParamType) obj;
        if (this.types != other.types && (this.types == null || !this.types.equals(other.types))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.types != null ? this.types.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return Joiner.on(",").join(types);
    }
    
    public String toDescriptorString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        int unknownCount = 0;
        for (ArgType arg : types) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            
            sb.append(arg.getType());
            sb.append(" ");
            if (arg.getName()!=null && !arg.getName().matches("\\s*")) {
                sb.append(arg.getName());
            } else {
                unknownCount++;
                sb.append("x").append(unknownCount);
            }
        }
        return sb.toString();
    }
    
    public List<ArgType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    @Override
    public int compareTo(PBParamType o) {
        int otherCount = o.getTypes().size();
        int i = 0;
        for (ArgType arg : types) {
            if (otherCount<=i) {
                return -1;
            }
            int cmp = arg.compareTo(o.getTypes().get(i));
            if (cmp!=0) {
                return cmp;
            }
            i++;
        }
        if (otherCount>i) {
            return 1;
        }
        return 0;
    }
}
