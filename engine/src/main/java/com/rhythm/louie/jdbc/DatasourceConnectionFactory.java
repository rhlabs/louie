/*
 * DatasourceConnectionFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author cjohnson
 */
public class DatasourceConnectionFactory implements ConnectionFactory {

    private final String datasource;

    public DatasourceConnectionFactory(String datasource) {
        this.datasource = datasource;
    }

    @Override
    public Connection createConnection() throws Exception {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup(datasource);
        return ds.getConnection();
    }
}
