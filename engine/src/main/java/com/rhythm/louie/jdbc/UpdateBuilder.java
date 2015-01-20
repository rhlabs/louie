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
package com.rhythm.louie.jdbc;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper for constructing update queries.  Requires that you specify at 
 * least 1 where clause.
 *
 * @author cjohnson
 */
public class UpdateBuilder {
    private final JdbcFactory factory;
    private final String table;
    private final List<Field> fields;
    private final List<QueryClause> whereClauses = new ArrayList<>();
    
    public UpdateBuilder(JdbcFactory factory, String table) {
        this.factory = factory;
        this.table = table;
        fields = new ArrayList<>();
    }
    
    /**
     * Adds a clause in the form of: field=? 
     * 
     * @param fieldName the name of the field
     * @param value value to be assigned on execution
     * @param sqlType java.sql.Types identifier
     * 
     * @see java.sql.Types
     */
    public void addWhereFieldClause(String fieldName, Object value, int sqlType) {
        whereClauses.add(QueryClause.createFieldClause(fieldName, value, sqlType));
    }
    
    /**
     * Adds a clause in the form of: field IN (?,?)
     * 
     * @param fieldName the name of the field
     * @param values values to be assigned on execution
     * @param sqlType java.sql.Types identifier
     * 
     * @see java.sql.Types
     */
    public void addWhereInClause(String fieldName, Collection<?> values, int sqlType) {
        whereClauses.add(QueryClause.createInClause(fieldName, values, sqlType));
    }
    
     /**
     * Adds a custom clause that must contain one and only one ?
     * 
     * @param clause a custom clause string like
     *     id IN (select id from other_type where value=?)
     * @param value value to be assigned on execution
     * @param sqlType java.sql.Types identifier
     * @throws java.lang.Exception if the clause does not contain one and only one ?
     * 
     * @see java.sql.Types
     */
    public void addWhereClause(String clause, Object value, int sqlType) throws Exception {
        whereClauses.add(QueryClause.createClause(clause, value, sqlType));
    }
    
    /**
     * Returns the service created to perform the query
     * 
     * @param query
     * @return a jdbc service
     * @throws Exception 
     */
    protected JdbcService getService(String query) throws Exception {
        return factory.newService(query);
    }
    
    /**
     * Perform the database update.  This call automatically cleans up after itself
     * 
     * @return the number of rows that were updated
     * @throws Exception 
     */
    public int execute() throws Exception {
        try (JdbcService jdbc = getService(getQuery())) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            int i=1;
            for (Field f : fields) {
                if (f.hasParam()) {
                    if (f.getSqlType() != Types.OTHER) {
                        ps.setObject(i++, f.getValue(), f.getSqlType());
                    } else {
                        ps.setObject(i++, f.getValue());
                    }
                }
            }
            
            if (whereClauses.isEmpty()) {
                throw new Exception("Cannot execute update for an empty Where clause!");
            }
            for (QueryClause clause : whereClauses) {
                if (clause.hasParam()) {
                    if (clause.isList()) {
                        for (Object value : clause.getValuesList()) {
                            if (clause.getSqlType() != Types.OTHER) {
                                ps.setObject(i++, value, clause.getSqlType());
                            } else {
                                ps.setObject(i++, value);
                            }
                        }
                    } else {
                        if (clause.getSqlType() != Types.OTHER) {
                            ps.setObject(i++, clause.getValue(), clause.getSqlType());
                        } else {
                            ps.setObject(i++, clause.getValue());
                        }
                    }
                }
            }
            return jdbc.executeUpdate();
        }
    }

    /**
     * Returns the query used to perform the update
     * 
     * @return the query string
     * @throws Exception 
     */
    public String getQuery() throws Exception {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE ").append(table).append(" SET ");
        
        for (int i=0;i<fields.size();i++) {
            if (i!=0) {
                query.append(",");
            }
            Field f = fields.get(i);
            query.append(f.getName());
            if (f.hasSpecial()) {
                query.append("=").append(f.getSpecial());
            } else {
                query.append("=?");
            }
        }
        
        if (whereClauses.isEmpty()) {
            throw new Exception("Cannot execute update for an empty Where clause!");
        }
        query.append(" WHERE ");
        
        boolean first = true;
        for (QueryClause clause : whereClauses) {
            if (!first) {
                query.append(" AND ");
            } else {
                first = false;
            }
            query.append(clause.getClause());
        }
        
        return query.toString();
    }
    
    /**
     * Sets a field equals to the special clause that does not contain a ?
     * 
     * @param name the name of the field
     * @param special the clause to set the field to
     * @throws Exception if special contains a ?
     */
    public void setFieldNoParamSpecial(String name,String special) throws Exception {
        fields.add(Field.createNoParamField(name,special));
    }
    
    /**
     * Creates a clause in the form of: field=? 
     * 
     * @param name the name of the field
     * @param value the new value of the field
     */
    public void setField(String name,Object value) {
        fields.add(new Field(name,value));
    }
    
    /**
     * Sets a field using the special clause.  Resultant clause will look like field=SPECIAL.
     * SPECIAL must contain a single ?
     * 
     * @param name the name of the field 
     * @param special the clause containing a single ?
     * @param value the value to inject into the prepared statement
     * @throws java.lang.Exception if you do not specify one and only one ?
     * 
     * @see java.sql.Types
     */
    public void setFieldSpecial(String name,String special,Object value) throws Exception {
        setFieldSpecial(name, special, value, Types.OTHER);
    }
  
    /**
     * Creates a clause in the form of: field=? 
     * 
     * @param name the name of the field
     * @param value the new value of the field
     * @param sqlType the type of the value
     * 
     * @see java.sql.Types
     */
    public void setField(String name, Object value, int sqlType) {
        fields.add(new Field(name, value, sqlType));
    }

    /**
     * Sets a field using the special clause.  Resultant clause will look like field=SPECIAL.
     * SPECIAL must contain a single ?
     * 
     * @param name the name of the field 
     * @param special the clause containing a single ?
     * @param value the value to inject into the prepared statement
     * @param sqlType the type of the value
     * @throws java.lang.Exception if you do not specify one and only one ?
     * 
     * @see java.sql.Types
     */
    public void setFieldSpecial(String name,String special,Object value,int sqlType) throws Exception {
        int param = special.indexOf('?');
        if (param==-1) {
            throw new Exception("Must specify a ? param!");
        } else if (special.indexOf('?', param+1)!=-1) {
            throw new Exception("Must only specify a single ? param!");
        }
        
        fields.add(new Field(name,special,value,sqlType));
    }
    
    /**
     * Returns true if there has not been any fields set
     * 
     * @return true if no fields are set
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }
    
    static private class Field {
        private final String name;
        private final String special;
        private final Object value;
        private final int sqlType;
        private boolean hasParam;
        
        Field(String name,Object value) {
            this(name,null,value,Types.OTHER);
        }
        Field(String name,Object value,int sqlType) {
            this(name,null,value,sqlType);
        }
        Field(String name,String special,Object value) {
            this(name,special,value,Types.OTHER);
        }
        Field(String name,String special,Object value,int sqlType) {
            this.name=name;
            this.special=special;
            this.value=value;
            this.sqlType=sqlType;
            hasParam=true;
        }
        
        public static Field createNoParamField(String name,String special) throws Exception {
            if (special.contains("?")) {
                throw new Exception("Update Builder Error: Cannot add a no param field that declares a param!");
            }
            Field f = new Field(name,special,null);
            f.hasParam = false;
            return f;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        public boolean hasParam() {
            return hasParam;
        }

        /**
         * @return the special
         */
        public String getSpecial() {
            return special;
        }
        
        /**
         * @return the special
         */
        public boolean hasSpecial() {
            return special!=null;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }
        
        public int getSqlType() {
            return sqlType;
        }
    }
}