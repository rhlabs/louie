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
