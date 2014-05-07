/*
 * Batcher.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhythm.louie.Constants;
import com.rhythm.louie.DAOUtils;

/**
 * @author cjohnson
 * Created: Sep 6, 2011 1:26:03 PM
 */
public class Batcher {
    public static abstract class InList<A,R> {
        private final JdbcFactory jdbcFactory;
        private final String prefix;
        private final String suffix;
        private boolean update = false;
        
        public InList(JdbcFactory jdbcFactory,String queryPrefix) {
            this(jdbcFactory,queryPrefix,null);
        }
        
        public InList(JdbcFactory jdbcFactory,String queryPrefix,String querySuffix) {
            prefix = queryPrefix;
            suffix = querySuffix;
            this.jdbcFactory = jdbcFactory;
        }
        
        protected void doUpdate() {
            update = true;
        }
        
        public List<R> doQuery(Collection<A> fullargs) throws Exception {
            if (fullargs.size() <= Constants.JDBC_IN_LIMIT) {
                return doQueryBatch(fullargs);
            } else {
                List<R> results = new ArrayList<R>(fullargs.size());

                int count = 0;
                ArrayList<A> tmpList = new ArrayList<A>(Constants.JDBC_IN_LIMIT);
                for (A i : fullargs) {
                    tmpList.add(i);
                    count++;
                    if (count >= Constants.JDBC_IN_LIMIT) {
                        results.addAll(doQueryBatch(tmpList));
                        count = 0;
                        tmpList.clear();
                    }
                }
                results.addAll(doQueryBatch(tmpList));
                return results;
            }
        }

        private String getQuery(int size) {
            return DAOUtils.appendInParams(prefix, size, suffix);
        }

        private List<R> doQueryBatch(Collection<A> batchargs) throws Exception {
            if (batchargs.isEmpty()) {
                return Collections.emptyList();
            }

            JdbcService jdbc = null;
            try {
                jdbc = getService(getQuery(batchargs.size()));
                PreparedStatement ps = jdbc.getPreparedStatement();
                int i = setPreArgs(ps);
                for (A arg : batchargs) {
                    setArg(ps,i++,arg);
                }

                //ResultSet rst = jdbc.executePreparedStatement();
                if (update) {
                    jdbc.executePreparedStatementUpdate();
                    return Collections.emptyList();
                }
                
                ResultSet rst = executeQuery(jdbc);
                return processResults(rst);
            } finally {
                if (jdbc != null) { jdbc.closeAll(); }
            }
        }

        protected int setPreArgs(PreparedStatement ps) throws Exception {
            return 1;
        }
        
        protected ResultSet executeQuery(JdbcService jdbc) throws Exception {
            return jdbc.executePreparedStatement();
        }
        
        protected JdbcService getService(String query) throws Exception {
            return jdbcFactory.newService(query);
        }
        
        protected abstract void setArg(PreparedStatement ps, int index, A arg) throws Exception;
        
        protected abstract List<R> processResults(ResultSet rst) throws Exception;
        
        public static abstract class INT<R> extends InList<Integer, R> {

            public INT(JdbcFactory jdbcFactory, String queryPrefix) {
                super(jdbcFactory,queryPrefix);
            }
            public INT(JdbcFactory jdbcFactory, String queryPrefix, String querySuffix) {
                super(jdbcFactory,queryPrefix,querySuffix);
            }
            
            @Override
            protected void setArg(PreparedStatement ps, int index, Integer id) throws Exception {
                ps.setInt(index, id);
            }
        }
    }
    
    public static abstract class InMap<A,R> {
        private final JdbcFactory jdbcFactory;
        private final String prefix;
        private final String suffix;
        
        public InMap(JdbcFactory jdbcFactory,String queryPrefix) {
            this(jdbcFactory,queryPrefix,null);
        }
        
        public InMap(JdbcFactory jdbcFactory,String queryPrefix,String querySuffix) {
            prefix = queryPrefix;
            suffix = querySuffix;
            this.jdbcFactory = jdbcFactory;
        }
        
        public Map<A,List<R>> doQuery(Collection<A> fullargs) throws Exception {
            Map<A,List<R>> results = new HashMap<A,List<R>>(fullargs.size());
            if (fullargs.size() <= Constants.JDBC_IN_LIMIT) {
                doQueryBatch(fullargs,results);
            } else {
                int count = 0;
                ArrayList<A> tmpList = new ArrayList<A>(Constants.JDBC_IN_LIMIT);
                for (A i : fullargs) {
                    tmpList.add(i);
                    count++;
                    if (count >= Constants.JDBC_IN_LIMIT) {
                        doQueryBatch(tmpList,results);
                        count = 0;
                        tmpList.clear();
                    }
                }
                doQueryBatch(tmpList,results);
            }
            return results;
        }
        
        private String getQuery(int size) {
            return DAOUtils.appendInParams(prefix, size, suffix);
        }

        private void doQueryBatch(Collection<A> batchargs,Map<A,List<R>> results) throws Exception {
            if (batchargs.isEmpty()) {
                return;
            }

            JdbcService jdbc = null;
            try {
                jdbc = getService(getQuery(batchargs.size()));
                PreparedStatement ps = jdbc.getPreparedStatement();
                int i = setPreArgs(ps);
                for (A arg : batchargs) {
                    setArg(ps,i++,arg);
                }

                ResultSet rst = jdbc.executePreparedStatement();
                processResults(rst,results);
            } finally {
                if (jdbc != null) { jdbc.closeAll(); }
            }
        }

        protected int setPreArgs(PreparedStatement ps) throws Exception {
            return 1;
        }
        
        protected JdbcService getService(String query) throws Exception {
            return jdbcFactory.newService(query);
        }
        
        protected abstract void setArg(PreparedStatement ps, int index, A arg) throws Exception;
        
        protected abstract void processResults(ResultSet rst,Map<A,List<R>> results) throws Exception;
        
        public static abstract class INT<R> extends InMap<Integer, R> {

            public INT(JdbcFactory jdbcFactory, String queryPrefix) {
                super(jdbcFactory,queryPrefix);
            }
            public INT(JdbcFactory jdbcFactory, String queryPrefix, String querySuffix) {
                super(jdbcFactory,queryPrefix,querySuffix);
            }

            @Override
            protected void setArg(PreparedStatement ps, int index, Integer id) throws Exception {
                ps.setInt(index, id);
            }
        }
    }
}
