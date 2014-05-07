/*
 * UpdateBuilder.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
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
    private final List<QueryClause> whereClauses = new ArrayList<QueryClause>();
    
    public UpdateBuilder(JdbcFactory factory, String table) {
        this.factory = factory;
        this.table = table;
        fields = new ArrayList<Field>();
    }
    
    /**
     * Adds a clause in the form of: field=? 
     * @param fieldName the name of the field
     * @param value value to be assigned on execution
     * @param sqlType java.sql.Types identifier
     */
    public void addWhereFieldClause(String fieldName, Object value, int sqlType) {
        whereClauses.add(QueryClause.createFieldClause(fieldName, value, sqlType));
    }
    
    /**
     * Adds a clause in the form of: field IN (?,?)
     * @param fieldName the name of the field
     * @param values values to be assigned on execution
     * @param sqlType java.sql.Types identifier
     */
    public void addWhereInClause(String fieldName, Collection<?> values, int sqlType) {
        whereClauses.add(QueryClause.createInClause(fieldName, values, sqlType));
    }
    
     /**
     * Adds a custom clause 
     * @param clause a custom clause string like
     *     id IN (select id from other_type where value=?)
     * @param value value to be assigned on execution
     * @param sqlType java.sql.Types identifier
     */
    public void addWhereClause(String clause, Object value, int sqlType) {
        whereClauses.add(new QueryClause(clause, value, sqlType));
    }
    
    protected JdbcService getService(String query) throws Exception {
        return factory.newService(query);
    }
    
    public int execute() throws Exception {
        JdbcService jdbc = null;
        try {
            String query = getQuery();
            jdbc = getService(query);
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
            return jdbc.executePreparedStatementUpdate();
        } finally {
            if (jdbc!=null) jdbc.closeAll();
        }
    }

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
    
    public void setFieldNoParamSpecial(String name,String special) throws Exception {
        fields.add(Field.createNoParamField(name,special));
    }
    
    public void setField(String name,Object value) {
        fields.add(new Field(name,value));
    }
    public void setFieldSpecial(String name,String special,Object value) {
        fields.add(new Field(name,special,value));
    }
    
    public void setField(String name,Object value,int sqlType) {
        fields.add(new Field(name,value,sqlType));
    }
    public void setFieldSpecial(String name,String special,Object value,int sqlType) {
        fields.add(new Field(name,special,value,sqlType));
    }
    
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