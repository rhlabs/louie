/*
 * QueryBuilder.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jdbc;

import java.io.Closeable;
import java.io.IOException;

import com.rhythm.louie.jdbc.query.ResultProcessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhythm.louie.jdbc.query.ResultMapper;

/**
 * A QueryBuilder aids in constructing and executing dynamic queries. Each portion
 * of a where clause can be appended with its corresponding argument.  This allows
 * the continued use of PreparedStatements with on-the-fly construction.  PreparedStatements
 * utilize the underlying JDBC driver caching and simplify the Database parsing, 
 * both of which are essential to performance.
 * <p>
 * Each QueryClause can be added using the various add*Clause utility methods.  
 * <p>
 * Calling execute will perform the query, returning the ResultSet.  After processing
 * has been completed, you must clean up to avoid connection leaks by calling closeAll()
 */

public class QueryBuilder implements Closeable {

    private final String prefix;
    private String suffix;
    boolean whereAdded;
    boolean hasBatch = false;
    final List<QueryClause> clauses = new ArrayList<>();
    private final JdbcFactory factory;
    private JdbcService jdbc = null;

    /**
     * @param factory the factory to create the db connection from
     * @param prefix should only be up to table clause
     * 
     * Defaults where clauseAdded to be false
     */
    public QueryBuilder(JdbcFactory factory, String prefix) {
        this(factory, prefix, false);
    }

    /**
     * @param factory the factory to create the db connection from
     * @param prefix should only be up to table clause
     * @param whereAdded tells the builder to add a where clause or not
     */
    public QueryBuilder(JdbcFactory factory, String prefix, boolean whereAdded) {
        this.prefix = prefix;
        this.whereAdded = whereAdded;
        this.factory = factory;
    }

    /**
     * Sets the suffix to be appended to the end of the query, such as
     * "order by 1,2"
     * 
     * NOTE! Specifying a suffix for batched queries may not work as desired,
     * as each batch will specify the suffix independently, instead of the whole result.
     * 
     * Cannot contain parameters
     * 
     * @param suffix
     * @throws java.lang.Exception if suffix contains a ?
     */
    public void setSuffix(String suffix) throws Exception {
        if (suffix.indexOf('?')!=-1) {
            throw new Exception("Must NOT specify a ? param!");
        }
        this.suffix = suffix;
    }
    
    @Override
    public String toString() {
        return getQuery();
    }

     /**
     * Builds the query string, to be created as a prepared statement.
     * Will only create the porting of the query for the specific batch offset.
     * 
     * @param batch the batch offset, 0 for a non-batched query returns the full query
     * @return the query string, or null of the batch offset is outside the range of values
     */
    public String getBatchedQuery(int batch) {
        if (!hasBatch) {
            if (batch==0) {
                getQuery();
            } else {
                return null;
            }
        }
        
        StringBuilder query = new StringBuilder(prefix);
        boolean needsWhere = !whereAdded;
        for (QueryClause clause : clauses) {
            if (needsWhere) {
                query.append(" WHERE ");
                needsWhere=false;
            } else {
                query.append(" AND ");
            }
            if (clause.isBatched()) {
                String batchClause = clause.getClauseForBatch(batch);
                if (batchClause==null) {
                    return null;
                }
                query.append(batchClause);
            } else {
                query.append(clause.getClause());
            }
        }
        if (suffix!=null) {
            query.append(" ").append(suffix);
        }
        return query.toString();
    }
    
    /**
     * Builds the query string, to be created as a prepared statement.
     * This does not account for batches
     * @return the query string
     */
    public String getQuery() {
        StringBuilder query = new StringBuilder(prefix);
        boolean needsWhere = !whereAdded;
        for (QueryClause clause : clauses) {
            if (needsWhere) {
                query.append(" WHERE ");
                needsWhere=false;
            } else {
                query.append(" AND ");
            }
            query.append(clause.getClause());
        }
        if (suffix!=null) {
            query.append(" ").append(suffix);
        }
        return query.toString();
    }

    /**
     * Returns true if there are no clauses
     * @return true if their are no clauses specified
     */
    public boolean isEmpty() {
        return clauses.isEmpty();
    }
    
     /**
     * Returns the list of clauses.  Useful if you would like to apply the same
     * clauses to a second query
     * @return an unmodifiable list of all the clauses
     */
    public List<QueryClause> getClauses() {
        return Collections.unmodifiableList(clauses);
    }

    /**
     * Adds a clause in the form of: field IN (?,?,?) 
     * @param field the name of the field
     * @param values the data to be injected into the prepared statement
     * @param sqlType the java.sql.Type of the field
     * @throws java.lang.Exception
     * 
     * @see java.sql.Types
     */
    public void addInClause(String field, Collection<?> values, int sqlType) throws Exception {
        addClause(QueryClause.createInClause(field, values, sqlType));
    }
    
    /**
     * Adds a clause in the form of: field IN (?,?,?), batch query into multiple lookups
     * @param field the name of the field
     * @param values the data to be injected into the prepared statement
     * @param sqlType the java.sql.Type of the field
     * @throws java.lang.Exception
     * 
     * @see java.sql.Types
     */
    public void addBatchedInClause(String field, List<?> values, int sqlType) throws Exception {
        addClause(QueryClause.createBatchedInClause(field, values, sqlType));
    }
    
    /**
     * Creates a clause in the form of: (field LIKE ? OR field LIKE ?)
     * Only takes strings, automatically sets the type to VARCHAR
     * @param field
     * @param values the data to be injected into the prepared statement
     * @param useInIfPossible if this is set, it will check all values for %, 
     * if none are found an IN statement will be used instead
     * @throws java.lang.Exception
     */
    public void addInLikeClause(String field, Collection<String> values, boolean useInIfPossible) throws Exception {
        addClause(QueryClause.createInLikeClause(field, values, useInIfPossible));
    }
    
    /**
     * Adds a clause in the form of: field IN (?,?,?) 
     * @param field the name of the field, including table identifier if needed
     * @param values the data to be injected into the prepared statement
     * @param sqlType the java.sql.Type of the field
     * @throws java.lang.Exception
     * 
     * @see java.sql.Types
     */
    public void addNotInClause(String field, Collection<?> values, int sqlType) throws Exception {
        addClause(QueryClause.createNotInClause(field, values, sqlType));
    }

     /**
     * Adds a custom clause 
     * 
     * @param clause full text of a clause with a single ? for a value
     * @param value the data to be injected into the prepared statement
     * @param sqlType the java.sql.Type of the field
     * @throws java.lang.Exception if you do not specify one and only one ?
     * 
     * @see java.sql.Types
     */
    public void addClause(String clause, Object value, int sqlType) throws Exception {
        addClause(QueryClause.createClause(clause, value, sqlType));
    }
    
    /**
     * Adds a custom clause 
     * Uses the JDBC Standard mapping to determine the SQL Type
     * 
     * @param clause full text of a clause with a single ? for a value
     * @param value the data to be injected into the prepared statement
     * @throws java.lang.Exception if you do not specify one and only one ?
     * 
     * @see java.sql.Types
     */
    public void addClause(String clause, Object value) throws Exception {
        addClause(QueryClause.createClause(clause, value));
    }

    /**
     * Adds a clause in the form of: field=? 
     * 
     * @param fieldName the name of the field
     * @param value the data to be injected into the prepared statement
     * @param sqlType the java.sql.Type of the field
     * @throws java.lang.Exception
     * 
     * @see java.sql.Types
     */
    public void addFieldClause(String fieldName, Object value, int sqlType) throws Exception {
        addClause(QueryClause.createFieldClause(fieldName, value, sqlType));
    }
    
    /**
     * Adds a clause in the form of: field=? 
     * Uses the JDBC Standard mapping to determine the SQL Type
     * 
     * @param fieldName the name of the field
     * @param value the data to be injected into the prepared statement
     * @throws java.lang.Exception
     */
    public void addFieldClause(String fieldName, Object value) throws Exception {
        addClause(QueryClause.createFieldClause(fieldName, value));
    }
    
    /**
     * Adds a clause that does not contain any ?, such as "t.visible"
     * @param clause textual representation of a clause
     * @throws java.lang.Exception if you specify a ?
     */
    public void addNoParamClause(String clause) throws Exception {
        int param = clause.indexOf('?');
        if (param!=-1) {
            throw new Exception("Must NOT specify a ? param!");
        }
        addClause(QueryClause.createNoParamClause(clause));
    }
    
    /**
     * Adds a externally created clause
     * @param clause the QueryClause to be added to the statement
     * @throws Exception if there are any issues adding clauses, 
     * such as more than one batched clause
     */
    public void addClause(QueryClause clause) throws Exception {
        if (clause.isBatched()) {
            if (hasBatch) {
                throw new Exception("Query already has a batched clause!");
            }
            hasBatch = true;
        }
        clauses.add(clause);
    }

    /**
     * Adds a collection of externally created clauses
     * @param clauses the QueryClauses to be added to the statement
     * @throws Exception if there are any issues adding clauses, 
     * such as more than one batched clause
     */
    public void addClauses(Collection<QueryClause> clauses) throws Exception {
        for (QueryClause clause : clauses) {
            addClause(clause);
        }
    }
    
    /**
     * Executes the internal query using a PreparedStatement
     * <p>
     * Upon completion of processing the results, closeAll() must be called
     * in order to cleanup the connection.
     * @return the ResultSet of the query execution
     * @throws Exception any SQL exceptions will get bubbled up
     */ 
    public ResultSet execute() throws Exception {
        String query = getQuery();
        jdbc = factory.newService(query);
        PreparedStatement ps = jdbc.getPreparedStatement();
        int i = 1;
        for (QueryClause f : clauses) {
            if (f.hasParam()) {
                if (f.isList()) {
                    for (Object value : f.getValuesList()) {
                        if (f.getSqlType() != Types.OTHER) {
                            ps.setObject(i++, value, f.getSqlType());
                        } else {
                            ps.setObject(i++, value);
                        }
                    }
                } else {
                    if (f.getSqlType() != Types.OTHER) {
                        ps.setObject(i++, f.getValue(), f.getSqlType());
                    } else {
                        ps.setObject(i++, f.getValue());
                    }
                }
            }
        }
        return jdbc.executeQuery();
    }
    
    /**
     * Executes the query in batches, if applicable.  The processor is used to
     * convert the ResultSet into objects, stored in the results collection.
     * 
     * The closeAll is called automatically after execution
     * 
     * @param <K> type of key in result map
     * @param <V> type of value in result map
     * @param processor the processor used to convert the ResultSet into objects
     * @return a Map created to store the results
     * @throws Exception any SQL exceptions will get bubbled up
     */
    public <K,V> Map<K,V> execute(ResultMapper<K,V> processor) throws Exception {
        Map<K,V> results = new HashMap<>();
        execute(processor, results);
        return results;
    }
    
    /**
     * Executes the query in batches, if applicable.  The processor is used to
     * convert the ResultSet into objects, stored in the results collection.
     * 
     * The closeAll is called automatically after execution
     * 
     * @param <K> type of key in result map
     * @param <V> type of value in result map
     * @param processor the processor used to convert the ResultSet into objects
     * @param results the Map to store results of processing the data
     * @throws Exception any SQL exceptions will get bubbled up
     */
    public <K,V> void execute(ResultMapper<K,V> processor, Map<K,V> results) throws Exception {
        int batch = 0;
        String query;
        while ((query = getBatchedQuery(batch)) != null) {
            try {
                processor.processResults(executeBatch(query,batch), results);
                batch++;
            } finally {
                closeAll();
            }
        }
    }
    
    /**
     * Executes the query in batches, if applicable.  The processor is used to
     * convert the ResultSet into objects, stored in the results collection.
     * 
     * The closeAll is called automatically after execution
     * 
     * @param <T> type of results
     * @param processor the processor used to convert the ResultSet into objects
     * @return a List created to store the results
     * @throws Exception any SQL exceptions will get bubbled up
     */
    public <T> List<T> execute(ResultProcessor<T> processor) throws Exception {
        List<T> results = new ArrayList<>();
        execute(processor, results);
        return results;
    }
    
    /**
     * Executes the query in batches, if applicable.  The processor is used to
     * convert the ResultSet into objects, stored in the results collection.
     * 
     * The closeAll is called automatically after execution
     * 
     * @param <T> type of results
     * @param processor the processor used to convert the ResultSet into objects
     * @param results the List to store results of processing the data
     * @throws Exception any SQL exceptions will get bubbled up
     */
    public <T> void execute(ResultProcessor<T> processor, Collection<T> results) throws Exception {
        int batch = 0;
        String query;
        while ((query = getBatchedQuery(batch)) != null) {
            try {
                processor.processResults(executeBatch(query,batch), results);
                batch++;
            } finally {
                close();
            }
        }
    }
    
    private ResultSet executeBatch(String query, int batch) throws Exception {
        jdbc = factory.newService(query);
        PreparedStatement ps = jdbc.getPreparedStatement();
        int i = 1;
        for (QueryClause f : clauses) {
            if (f.hasParam()) {
                if (f.isBatched() || f.isList()) {
                    Collection<?> values = f.isBatched()
                            ? f.getValuesForBatch(batch) : f.getValuesList();
                    for (Object value : values) {
                        if (f.getSqlType() != Types.OTHER) {
                            ps.setObject(i++, value, f.getSqlType());
                        } else {
                            ps.setObject(i++, value);
                        }
                    }
                } else {
                    if (f.getSqlType() != Types.OTHER) {
                        ps.setObject(i++, f.getValue(), f.getSqlType());
                    } else {
                        ps.setObject(i++, f.getValue());
                    }
                }
            }
        }
        return jdbc.executeQuery();
    }


    /**
     * Cleans up the internal JDBC Connection, must be called after execute()
     */ 
    @Deprecated
    public void closeAll() {
        if (jdbc != null) jdbc.closeAll();
        jdbc = null;
    }

    @Override
    public void close() throws IOException {
        if (jdbc != null) jdbc.close();
        jdbc = null;
    }
    
}
