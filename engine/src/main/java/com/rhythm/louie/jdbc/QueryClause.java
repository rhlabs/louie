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

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.rhythm.louie.util.DAOUtils;

/**
 * This class is to be used in conjunction with QueryBuilder to aid in constructing 
 * and executing dynamic queries. A QueryClause encapsulates a single clause in a 
 * Database query, meaning the conditions in the WHERE portion of a query, separated by ANDs
 * <p>
 * Examples:<br>
 * uname=?<br>
 * id IN (123,456)<br>
 * not active<br>
 * <p>
 * Fields and clauses must be fully qualified, meaning containing a prefixing alias
 * if the table name is aliased in the query.  This class is merely a utility helper.
 * <p>
 * Example Query:<br>
 * SELECT g.name,e.uname FROM goals g,employee e WHERE g.assignment=e.code AND g.job=?
 * <p>
 * g.job=? would be an example QueryClause.  You could utilize that in code like
 * <p>
 * QueryClause clause = QueryClause.createFieldClause("g.job","ripd",Types.VARCHAR);
 * <p>
 * This would then inject g.job="ripd" into the prepared statement upon execution. 
 */
public class QueryClause {

    private final String clause;
    private final Object value;
    private final int sqlType;
    private boolean hasParam;
    private boolean isList;
    private boolean batched = false;

    private QueryClause(String clause, Object value) {
        this(clause, value, Types.OTHER);
    }

    private QueryClause(String clause, Object value, int sqlType) {
        this.clause = clause;
        this.value = value;
        this.sqlType = sqlType;
        hasParam = true;
        isList = false;
    }

    /**
     * Creates a custom clause that must contain one and only one ?
     * 
     * @param clause
     * @param value
     * @return the created QueryClause
     * @throws Exception 
     */
    public static QueryClause createClause(String clause, Object value) throws Exception {
        return createClause(clause, value, Types.OTHER);
    }
    
    /**
     * Creates a custom clause that must contain one and only one ?
     * 
     * @param clause the custom clause
     * @param value the value to be injected into the query
     * @param sqlType the type of the value
     * 
     * @return the created QueryClause
     * @throws Exception 
     */
    public static QueryClause createClause(String clause, Object value, int sqlType) throws Exception {
        int param = clause.indexOf('?');
        if (param==-1) {
            throw new Exception("Must specify a ? param!");
        } else if (clause.indexOf('?', param+1)!=-1) {
            throw new Exception("Must only specify a single ? param!");
        }
        return new QueryClause(clause, value, sqlType);
    }
    
    /**
     * Creates a clause that does not contain any ?, such as "t.visible"
     * 
     * @param clause
     * @return
     * @throws Exception 
     */
    public static QueryClause createNoParamClause(String clause) throws Exception {
        if (clause.contains("?")) {
            throw new Exception("Update Builder Error: Cannot add a no param field that declares a param!");
        }
        QueryClause f = new QueryClause(clause, null);
        f.hasParam = false;
        return f;
    }

    /**
     * Creates a clause in the form of: field IN (?,?,?) 
     * 
     * @param field
     * @param values
     * @param sqlType
     * @return 
     * 
     * @see java.sql.Types
     */
    public static QueryClause createInClause(String field, Collection<?> values, int sqlType) {
        String clause = DAOUtils.appendInParams(field + " IN ", values.size());
        QueryClause f = new QueryClause(clause, values, sqlType);
        f.isList = true;
        return f;
    }
    
    /**
     * Creates a clause in the form of: field NOT IN (?,?,?) 
     * 
     * @param field
     * @param values
     * @param sqlType
     * @return 
     * 
     * @see java.sql.Types
     */
    public static QueryClause createNotInClause(String field, Collection<?> values, int sqlType) {
        String clause = DAOUtils.appendInParams(field + " NOT IN ", values.size());
        QueryClause f = new QueryClause(clause, values, sqlType);
        f.isList = true;
        return f;
    }
    
    /**
     * Creates a clause in the form of: (field LIKE ? OR field LIKE ?)
     * Only takes strings, automatically sets the type to VARCHAR
     * If useInIfPossible is set to true, it will scan the values to see if any contain a %,
     * if none do, it will use an IN statement instead
     * 
     * @param field
     * @param values
     * @param useInIfPossible
     * @return 
     */
    public static QueryClause createInLikeClause(String field, Collection<String> values, boolean useInIfPossible) {
        
        if (useInIfPossible) {
            boolean found = false;
            for (String s : values) {
                if (s.contains("%")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return createInClause(field, values, Types.VARCHAR);
            }
        }
        
        StringBuilder clause = new StringBuilder();
        clause.append("(");
        for (int i=0;i<values.size();i++) {
            if (i!=0) {
                clause.append(" OR ");
            }
            clause.append(field).append(" LIKE ?");
        }
        clause.append(")");
        
        QueryClause f = new QueryClause(clause.toString(), values, Types.VARCHAR);
        f.isList = true;
        return f;
    }
    

    /**
     * Creates a clause in the form of: field=? 
     * 
     * @param field
     * @param value
     * @param sqlType
     * @return 
     * 
     * @see java.sql.Types
     */
    public static QueryClause createFieldClause(String field, Object value, int sqlType) {
        return new QueryClause(field + "=?", value, sqlType);
    }
    
    /**
     * Creates a clause in the form of: field=? 
     * 
     * @param field
     * @param value
     * @return 
     * 
     * @see java.sql.Types
     */
    public static QueryClause createFieldClause(String field, Object value) {
        return new QueryClause(field + "=?", value, Types.OTHER);
    }
    
    /**
     * Creates a custom clause that has a list of values of the same type
     * This method is a bit of a hack work around in order to add a clause like:
     * (job is null OR job in (?,?,?))
     * 
     * @param clause
     * @param value
     * @param sqlType
     * @return 
     * 
     * @see java.sql.Types
     */
    public static QueryClause createCustomListClause(String clause, Object value, int sqlType) {
        QueryClause f = new QueryClause(clause, value, sqlType);
        f.isList = true;
        return f;
    }

    /**
     * @return the clause
     */
    public String getClause() {
        return clause;
    }

    public boolean hasParam() {
        return hasParam;
    }

    public boolean isList() {
        return isList;
    }
    
    public boolean isBatched() {
        return batched;
    }
    
    /** 
     * Returns the clause for the batch offset specified. 
     * If this is not a batched query, the entire clause is always returned
     * 
     * @param batch the batch offset
     * @return the query clause
     */
    public String getClauseForBatch(int batch) {
        return getClause();
    }
    
    /**
     * Return the values for the batch offset specified.
     * If this is not a batched query, the empty list is returned
     * 
     * @param batch the batch offset
     * @return a list of values
     */
    public List<?> getValuesForBatch(int batch) {
        return Collections.emptyList();
    }
    
     /**
     * @return a collection of values, if the isList=true, if this is not a list
     * it will throw an Exception
     * 
     * @throws java.lang.Exception
     */
    public Collection<?> getValuesList() throws Exception {
        if (!isList) {
            throw new Exception("This is not a list!");
        }
        return (Collection<?>) value;
    }
    
    /**
     * @return the value, may be a in the form of a Collection if isList=true
     * You should use getValuesList() instead
     */
    public Object getValue() {
        return value;
    }

    public int getSqlType() {
        return sqlType;
    }
    
     /**
     * Creates a batched clause in the form of: field IN (?,?,?) 
     * 
     * @param field the name of the field
     * @param values the values to be set in the sql
     * @param sqlType the type of the values
     * @return the newly created clause 
     * 
     * @see java.sql.Types
     */
    public static BatchedInClause createBatchedInClause(String field, List<?> values, int sqlType) {
        return new BatchedInClause(field, values, sqlType);
    }
     /**
     * Creates a batched clause in the form of: field IN (?,?,?)
     * 
     * @param field the name of the field
     * @param values the values to be set in the sql
     * @param sqlType the type of the values
     * @param batchSize the max size of the in clauses, if the size of 
     * values is > batchSize then multiple queries will be performed
     * @return the newly created clause 
     * 
     * @see java.sql.Types
     */
    public static BatchedInClause createBatchedInClause(String field, List<?> values, int sqlType, int batchSize) {
        return new BatchedInClause(field, values, sqlType, batchSize);
    }
    public static class BatchedInClause extends QueryClause {
        private int batchSize;
        private List<?> values;
        public BatchedInClause(String field, List<?> values, int sqlType) {
            this(field,values,sqlType,JdbcConstants.JDBC_IN_LIMIT);
        }
        public BatchedInClause(String field, List<?> values, int sqlType, int batchSize) {
            super(field+" IN ", values, sqlType);
            super.batched = true;
            super.isList = true;
            this.batchSize = batchSize;
            this.values = values;
        }
        
        private int getBatchSize(int batch) {
            int startIndex = batch*batchSize;
            if (startIndex>values.size()) {
                return 0;
            } else if (startIndex+batchSize>values.size()) {
                return values.size()-startIndex;
            } else {
                return batchSize;
            }
        }
        
        @Override
        public String getClauseForBatch(int batch) {
            int size = getBatchSize(batch);
            if (size==0) {
                return null;
            } else {
                return DAOUtils.appendInParams(super.clause, size);
            }
        }
        
        @Override
        public List<?> getValuesForBatch(int batch) {
            int size = getBatchSize(batch);
            if (size==0) {
                return Collections.emptyList();
            } else {
                int startIndex = batch*batchSize;
                return values.subList(startIndex, startIndex+size);
            }
        }
        
        @Override
        public String getClause() {
            return DAOUtils.appendInParams(super.clause, values.size());
        }
    }
}
