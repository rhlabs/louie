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

import java.util.*;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/**
 * @author cjohnson
 * Created: Jan 25, 2011 11:21:49 AM
 */
public class DAOUtils {
    public static final Set<String> PK_FIELD;
    public static final Set<String> PK_ID_FIELD;

    private DAOUtils() {};

    static {
        Set<String> tmp = new HashSet<String>();
        tmp.add("pk");
        PK_FIELD = Collections.unmodifiableSet(tmp);
        tmp.add("id");
        PK_ID_FIELD = Collections.unmodifiableSet(tmp);
    }
    
    public static String appendInParams(String queryPrefix, int count) {
         return appendInParams(queryPrefix,count,"");
    }
    
    public static String appendInParams(String queryPrefix, int count, String suffix) {
        StringBuilder query = new StringBuilder(queryPrefix);
        return appendInParams(query,count,suffix).toString();
    }
    
    public static StringBuilder appendInParams(StringBuilder query, int count) {
        return appendInParams(query,count,"");
    }
    public static StringBuilder appendInParams(StringBuilder query, int count, String suffix) {
        if (count<=0) {
            throw new UnsupportedOperationException("Cannot generate IN query for 0 arguments.");
        }
        
        query.append("(");
        query.append("?");
        for (int i = 1; i < count; i++) {
            query.append(",?");
        }
        query.append(")");
        if (suffix !=null && !suffix.isEmpty()) {
            if (!suffix.startsWith(" ")) {
                query.append(" ");
            }
            query.append(suffix);
        }
        return query;
    }
    
    /**
     * Returns a list of Difference Objects,
     * each of which specifies a FieldDescriptor, a DB field name,
     * and a new and old field value.
     * @param newPB The new version of the data (To be updated into the table)
     * @param oldPB The old version of the data (For comparison against)
     * @return diff A list of Difference objects for examination
     */
    public static List<Difference> findDifferences(Message newPB, Message oldPB){
        List<Difference> diffs = new ArrayList<Difference>();
        Map<FieldDescriptor,Object> newFieldSet = newPB.getAllFields();
        Map<FieldDescriptor,Object> oldFieldSet = oldPB.getAllFields();
        for(Map.Entry<FieldDescriptor,Object> fieldEntry : newFieldSet.entrySet()){
            FieldDescriptor descriptor = fieldEntry.getKey();
            String newValue = fieldEntry.getValue().toString();
            if(oldFieldSet.containsKey(descriptor)){
                String oldValue = oldFieldSet.get(descriptor).toString();
                if(!oldValue.equals(newValue)){
                    diffs.add(new Difference(descriptor, newFieldSet.get(descriptor), oldFieldSet.get(descriptor)));
                }
            }else{
                //The field has not yet been populated in the DB
                diffs.add(new Difference(descriptor, newFieldSet.get(descriptor), null));
            }
        }
        return diffs;
    }
    
    /**
     * Returns a list of Difference Objects,
     * each of which specifies a FieldDescriptor, a DB field name,
     * and a new and old field value.
     * @param newPB The new version of the data (To be updated into the table)
     * @param oldPB The old version of the data (For comparison against)
     * @param descriptorToDB A map of FieldDescriptor Name : table column name. 
     * If a FieldDescriptor does not have a corresponding table column, 
     * it should be added to the map as a null value and it will not be compared.
     * @return diff A list of Difference objects for examination
     */
    public static List<Difference> findDifferences(Message newPB, Message oldPB, Map<String,String> descriptorToDB){
        List<Difference> diffs = new ArrayList<Difference>();
        Map<FieldDescriptor,Object> newFieldSet = newPB.getAllFields();
        Map<FieldDescriptor,Object> oldFieldSet = oldPB.getAllFields();
        for(Map.Entry<FieldDescriptor,Object> fieldEntry : newFieldSet.entrySet()){
            FieldDescriptor descriptor = fieldEntry.getKey();
            String dbFieldName = descriptorToDB.get(descriptor.getName());
            if(dbFieldName != null){
                String newValue = fieldEntry.getValue().toString();
                if(oldFieldSet.containsKey(descriptor)){
                    String oldValue = oldFieldSet.get(descriptor).toString();
                    if(!oldValue.equals(newValue)){
                        diffs.add(new Difference(descriptor, newFieldSet.get(descriptor), oldFieldSet.get(descriptor), dbFieldName));
                    }
                }else{
                    //The field has not yet been populated in the DB
                    diffs.add(new Difference(descriptor, newFieldSet.get(descriptor), null, dbFieldName));
                }   
            }
        }
        return diffs;
    }
}
