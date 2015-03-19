/*
 * DrjJdbc.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.swagr.datasource;

import com.rhythm.louie.jdbc.DatasourceFactory;
import com.rhythm.louie.jdbc.JdbcFactory;

public class DrjJdbc {
    static final String DB_KEY = "drj";
    static final String DATASOURCE = "jdbc/DBstatistics";
    
    private DrjJdbc() {}
    
    /**
     * Returns a factory that creates connections using JNDI
     * @return JdbcFactory  
     */
    public static JdbcFactory getFactory() {
        return new DatasourceFactory(DATASOURCE, DB_KEY);
    }
}
