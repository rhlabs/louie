/*
 * DatasourceConnectionFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;

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
        ServiceLocator sl = new ServiceLocator();
        return sl.getDataSource(datasource).getConnection();
    }
}
