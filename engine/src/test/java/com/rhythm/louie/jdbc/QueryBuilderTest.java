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

import com.rhythm.louie.jdbc.query.ResultProcessor;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import org.junit.Test;

import com.rhythm.louie.jdbc.query.ResultMapper;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class QueryBuilderTest {
    JdbcFactory jdbc = null;
    
    private static final String HOST = "localhost";
    private static final String DB = "test";
    private static final String USER = "root";
    private static final String PW = "";
    
    private static final String SELECT_PREFIX = "SELECT label FROM node";
    public QueryBuilderTest() {
    }

    @Test
    public void testSetSuffix() throws Exception {
        System.out.println("setSuffix");
        
        String suffix = "order by 1";
        QueryBuilder builder = new QueryBuilder(jdbc,SELECT_PREFIX,false);
        builder.setSuffix(suffix);
        String expected = SELECT_PREFIX+" "+suffix;
        assertEquals(builder.getQuery(), expected);
    }

    @Test
    public void testGetBatchedQuery() throws Exception {
        System.out.println("getBatchedQuery");
        
        List<?> values = Arrays.asList(1,2,3,4,5,6,7);
        
        QueryBuilder builder = new QueryBuilder(jdbc,SELECT_PREFIX,false);
        builder.addClause(QueryClause.createBatchedInClause("id", values, Types.INTEGER, 5));
        
        String expected = SELECT_PREFIX+" WHERE id IN (?,?,?,?,?)";
        assertEquals(builder.getBatchedQuery(0), expected);
        
        expected = SELECT_PREFIX+" WHERE id IN (?,?)";
        assertEquals(builder.getBatchedQuery(1), expected);
    }

    @Test
    public void testExecute_0args() throws Exception {
    }

    @Test
    public void testExecute_ResultProcessor() throws Exception {
    }

    @Test
    public void testBatchExecute() throws Exception {
        System.out.println("batchExecute");
        
        List<?> values = Arrays.asList(1,2,3,4,5,6,7);
        
        JdbcFactory direct = new StandardJdbcFactory(new MysqlConnectionFactory(HOST, DB, USER, PW), "test");
        QueryBuilder builder = new QueryBuilder(direct,SELECT_PREFIX,false);
        builder.addClause(QueryClause.createBatchedInClause("id", values, Types.INTEGER, 5));
        
        ResultProcessor<String> processor = new ResultProcessor<String>() {
            @Override
            public void processResults(ResultSet rst, Collection<String> results) throws Exception {
                while (rst.next()) {
                    results.add(rst.getString(1));
                }
            }
        };
        
        Collection<String> results = builder.execute(processor);
        
        System.out.println("Found "+results.size()+" Results!");
        System.out.println(Joiner.on(",").join(results));

        assertTrue(results.size()==values.size());
    }
    
    @Test
    public void testBatchExecute2() throws Exception {
        System.out.println("batchExecute");
        
        List<?> values = Arrays.asList(1,2,3,4,5,6,7);
        
        JdbcFactory direct = new StandardJdbcFactory(new MysqlConnectionFactory(HOST, DB, USER, PW), "test");
        QueryBuilder builder = new QueryBuilder(direct,SELECT_PREFIX,false);
        builder.addClause(QueryClause.createBatchedInClause("id", values, Types.INTEGER, 10));
        
        ResultProcessor<String> processor = new ResultProcessor<String>() {
            @Override
            public void processResults(ResultSet rst, Collection<String> results) throws Exception {
                while (rst.next()) {
                    results.add(rst.getString(1));
                }
            }
        };
        
        List<String> results = new ArrayList<>();
        builder.execute(processor, results);
        
        System.out.println("Found "+results.size()+" Results!");
        System.out.println(Joiner.on(",").join(results));

        assertTrue(results.size()==values.size());
    }
    
    @Test
    public void testBatchExecute3() throws Exception {
        System.out.println("batchExecute");
        
        List<?> values = Arrays.asList(1,2,3,4,5,6,7);
        
        JdbcFactory direct = new StandardJdbcFactory(new MysqlConnectionFactory(HOST, DB, USER, PW), "test");
        QueryBuilder builder = new QueryBuilder(direct,"SELECT id,label FROM node",false);
        builder.addClause(QueryClause.createBatchedInClause("id", values, Types.INTEGER, 5));
        
        ResultMapper<Integer,String> processor = new ResultMapper<Integer,String>() {
            @Override
            public void processResults(ResultSet rst, Map<Integer,String> results) throws Exception {
                while (rst.next()) {
                    results.put(rst.getInt(1),rst.getString(2));
                }
            }
        };
        
        Map<Integer,String> results = new HashMap<>();
        builder.execute(processor, results);
        
        System.out.println("Found "+results.size()+" Results!");
        System.out.println(Joiner.on(",").join(results.entrySet()));

        assertTrue(results.size()==values.size());
    }

    @Test
    public void testBatchExecute4() throws Exception {
        System.out.println("batchExecute");
        
        List<?> values = Arrays.asList(1);
        
        JdbcFactory direct = new StandardJdbcFactory(new MysqlConnectionFactory(HOST, DB, USER, PW), "test");
        QueryBuilder builder = new QueryBuilder(direct,"SELECT id,label FROM node",false);
        builder.addClause(QueryClause.createInClause("id", values, Types.INTEGER));
        
        ResultMapper<Integer,String> processor = new ResultMapper<Integer,String>() {
            @Override
            public void processResults(ResultSet rst, Map<Integer,String> results) throws Exception {
                while (rst.next()) {
                    results.put(rst.getInt(1),rst.getString(2));
                }
            }
        };
        
        Map<Integer,String> results = new HashMap<>();
        builder.execute(processor, results);
        
        System.out.println("Found "+results.size()+" Results!");
        System.out.println(Joiner.on(",").join(results.entrySet()));

        assertTrue(results.size()==values.size());
    }

}
