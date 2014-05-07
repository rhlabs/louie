/*
 * StandardJdbcFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

/**
 *
 * @author cjohnson
 */
public class StandardJdbcFactory implements JdbcFactory {
    private final String dbKey;
    private final ConnectionFactory connFactory;
    public StandardJdbcFactory(ConnectionFactory connFactory, String dbKey) {
        this.connFactory = connFactory;
        this.dbKey = dbKey;
    }

    @Override
    public JdbcService newService(String sql) throws Exception {
        DefaultJdbcService service = new DefaultJdbcService(connFactory,dbKey);
        service.setSqlString(sql);
        return service;
    }
}
