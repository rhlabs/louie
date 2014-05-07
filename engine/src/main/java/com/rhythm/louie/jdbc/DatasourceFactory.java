/*
 * DatasourceFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

/**
 *
 * @author cjohnson
 */
public class DatasourceFactory implements JdbcFactory {
    private final String dbKey;
    private final String datasource;
    private final ConnectionFactory connFactory;
    
    public DatasourceFactory(String datasource, String dbKey) {
        this.datasource = datasource;
        this.dbKey = dbKey;
        connFactory = new DatasourceConnectionFactory(datasource);
    }
    
    @Override
    public JdbcService newService(String sql) throws Exception {
        DefaultJdbcService service = new DefaultJdbcService(connFactory,dbKey);
        service.setSqlString(sql);
        return service;
    }
    
}
