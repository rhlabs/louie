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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhythm.louie.util.DAOUtils;

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
            if (fullargs.size() <= JdbcConstants.JDBC_IN_LIMIT) {
                return doQueryBatch(fullargs);
            } else {
                List<R> results = new ArrayList<>(fullargs.size());

                int count = 0;
                ArrayList<A> tmpList = new ArrayList<>(JdbcConstants.JDBC_IN_LIMIT);
                for (A i : fullargs) {
                    tmpList.add(i);
                    count++;
                    if (count >= JdbcConstants.JDBC_IN_LIMIT) {
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

            try (JdbcService jdbc = getService(getQuery(batchargs.size()))) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                int i = setPreArgs(ps);
                for (A arg : batchargs) {
                    setArg(ps,i++,arg);
                }

                //ResultSet rst = jdbc.executeQuery();
                if (update) {
                    jdbc.executeUpdate();
                    return Collections.emptyList();
                }
                
                ResultSet rst = executeQuery(jdbc);
                return processResults(rst);
            }
        }

        protected int setPreArgs(PreparedStatement ps) throws Exception {
            return 1;
        }
        
        protected ResultSet executeQuery(JdbcService jdbc) throws Exception {
            return jdbc.executeQuery();
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
            Map<A,List<R>> results = new HashMap<>(fullargs.size());
            if (fullargs.size() <= JdbcConstants.JDBC_IN_LIMIT) {
                doQueryBatch(fullargs,results);
            } else {
                int count = 0;
                ArrayList<A> tmpList = new ArrayList<>(JdbcConstants.JDBC_IN_LIMIT);
                for (A i : fullargs) {
                    tmpList.add(i);
                    count++;
                    if (count >= JdbcConstants.JDBC_IN_LIMIT) {
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

            try (JdbcService jdbc = getService(getQuery(batchargs.size()))) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                int i = setPreArgs(ps);
                for (A arg : batchargs) {
                    setArg(ps,i++,arg);
                }

                ResultSet rst = jdbc.executeQuery();
                processResults(rst,results);
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
