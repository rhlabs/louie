/*
 * Difference.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.util;

import com.google.protobuf.Descriptors.FieldDescriptor;

/** 
 * An object containing relevant data for a field which is going to be updated
 * @author eyasukoc
 * @param  The type of the PB value object being added
 */
public class Difference {
    
    private Object newValue;
    private Object oldValue;
    private FieldDescriptor descriptor;
    private String dbColumnName;
    
    public Difference(FieldDescriptor field, Object newVal, Object oldVal){
        descriptor = field;
        newValue = newVal;
        oldValue = oldVal;
    }

    public Difference(FieldDescriptor field, Object newVal, Object oldVal, String dbColumn) {
        descriptor = field;
        newValue = newVal;
        oldValue = oldVal;
        dbColumnName = dbColumn;
    }
    
    public String getFieldName(){
        return descriptor.getName();
    }
    
    public Object getNewValue(){
        return newValue;
    }
    
    public Object getOldValue(){
        return oldValue;
    }
    
    public FieldDescriptor getFieldDescriptor(){
        return descriptor;
    }
    
    /**
     * 
     * @return The corresponding database table column name
     */
    public String getDBColumnName(){
        return dbColumnName;
    }
}
