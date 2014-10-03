package com.rhythm.louie.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Insert On Duplicate Key Builder for constructing the mysql/mariadb type query
 * @author eyasukoc
 */


public class InsertODKUBuilder {
    
    private final JdbcFactory factory;
    private final String table;
    private final List<Field> fields;
    private final List<Field> odkus;
    private String autoIncrementField;
    private int numUpdated = -1;
    
    private boolean executed = false;
    
    public InsertODKUBuilder(JdbcFactory factory, String table) {
        this.factory = factory;
        this.table = table;
        fields = new ArrayList<>();
        odkus = new ArrayList<>();
    }
    
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
            
            for (Field f : odkus) {
                if (f.hasParam()) {
                    if (f.getSqlType() != Types.OTHER) {
                        ps.setObject(i++, f.getValue(), f.getSqlType());
                    } else {
                        ps.setObject(i++, f.getValue());
                    }
                }
            }
            executed = true;
            jdbc.executeUpdate();
        }
        try (JdbcService jdbc = factory.newService("SELECT ROW_COUNT()")) {
            ResultSet rs = jdbc.executeQuery();
            if (rs.next()) {
                numUpdated = rs.getInt(1);
                return numUpdated;
            }
        }
        return -1;
    }
    
    /**
     * After executing this builder, this method will return the number of rows
     * updated or inserted
     * @return the number of rows updated/inserted, or -1 if not executed
     * @throws Exception 
     */
    public int numUpdated() throws Exception {
        return numUpdated;
    }
    
    /**
     * Custom Mysql/MariaDB post-execute action to lookup the row ID that was 
     * inserted OR updated (it works for both!)
     * YOU MUST SET THE AUTO_INCREMENT FIELD TO GUARANTEE THIS WILL BE MEANINGFUL.
     * @return the ID of the row updated or inserted into by this Builder.
     * @throws java.lang.Exception 
     */
    public int getAutoIncrementID() throws Exception {
        if (executed) {
            if (autoIncrementField == null) {
                throw new Exception("This field could be meaningless because you didn't specify the AutoIncrement Field prior to executing.");
            }
            try (JdbcService jdbc = factory.newService("SELECT LAST_INSERT_ID()")) {
                jdbc.getPreparedStatement();
                ResultSet rs = jdbc.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return -1;
                }
            }
        }
        throw new Exception ("Cannot call getAutoIncrementID on InsertODKU prior to calling execute");
    }

    public String getQuery() throws Exception {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(table).append(" ("); 
        
        for (int i=0;i<fields.size();i++) {
            if (i!=0) {
                query.append(",");
            }
            Field f = fields.get(i);
            query.append(f.getName());
        }
        
        query.append(") VALUES (");
        
        for (int j=0; j<fields.size();j++) {
            if (j!= 0) {
                query.append(",");
            }
            Field f = fields.get(j);
            if (f.hasSpecial()) {
                query.append(f.getSpecial());
            } else {
                query.append("?");
            }
        }
        
        query.append(") ON DUPLICATE KEY UPDATE ");
        
        for (int k=0; k<odkus.size(); k++) {
            if (k!=0) {
                query.append(",");
            }
            Field f = odkus.get(k);
            query.append(f.getName());
            if (f.hasSpecial()) {
                query.append("=").append(f.getSpecial());
            } else {
                query.append("=?");
            }
        } 
        if (autoIncrementField != null) {
            query.append(", ").append(autoIncrementField);
            query.append("=LAST_INSERT_ID(").append(autoIncrementField).append(")");
        }
        
        return query.toString();
    }
    
    /**
     * Set the field name of the auto-increment field associated with this table.
     * This is MANDATORY if you want to lookup 
     * @param name name of auto-increment field
     * @throws Exception 
     */
    public void setAutoIncrementField(String name) throws Exception {
        autoIncrementField = name;
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
    
    public void setFieldNoParamSpecialWithUpdate(String name,String special) throws Exception { //such a good name.
        fields.add(Field.createNoParamField(name,special));
        odkus.add(Field.createNoParamField(name,special));
    }
    
    public void setFieldWithUpdate(String name,Object value) {
        Field ref = new Field(name,value);
        fields.add(ref);
        odkus.add(ref);
    }
    
    public void setFieldSpecialWithUpdate(String name,String special,Object value) {
        Field ref = new Field(name,special,value); 
        fields.add(ref);
        odkus.add(ref);
    }
    
    public void setFieldWithUpdate(String name,Object value,int sqlType) {
        Field ref = new Field(name,value,sqlType); 
        fields.add(ref);
        odkus.add(ref);
    }
    
    public void setFieldSpecialWithUpdate(String name,String special,Object value,int sqlType) {
        Field ref = new Field(name,special,value,sqlType);
        fields.add(ref);
        odkus.add(ref);
    }
    
    /**
     * Field added to update section only (a field contingent upon it being an
     * update and not an insert)
     * @param name
     * @param value 
     */
    public void setFieldUpdateOnly(String name, Object value) {
        odkus.add(new Field(name, value));
    }

    /**
     * Field added to update section only (a field contingent upon it being an
     * update and not an insert)
     * @param name
     * @param value 
     * @param sqlType 
     */
    public void setFieldUpdateOnly(String name, Object value, int sqlType) {
        odkus.add(new Field(name, value, sqlType));
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
