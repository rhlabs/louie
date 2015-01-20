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
import java.util.List;

/**
 * @deprecated use {@link com.rhythm.louie.jdbc.UpdateBuilder} instead
 * 
 * @author cjohnson
 * Created: Nov 14, 2011 6:16:45 PM
 */
@Deprecated
public abstract class Updater {
    protected final String table;
    protected final String where;
    final List<Field> fields;
    protected final JdbcFactory factory;
    
    public Updater(JdbcFactory factory, String table, String where) {
        this.factory = factory;
        this.table = table;
        this.where = where;
        fields = new ArrayList<>();
    }
    
    abstract public void setWhere(PreparedStatement ps, int index) throws Exception;
    
    protected JdbcService getService(String query) throws Exception {
        return factory.newService(query);
    }
    
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
            setWhere(ps,i);
            return jdbc.executeUpdate();
        }
    }

    public String getQuery() {
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
        
        query.append(" WHERE ").append(where);
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
    
    static protected class Field {
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
