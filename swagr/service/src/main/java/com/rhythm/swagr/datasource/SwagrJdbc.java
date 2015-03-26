/*
 * SwagrJdbc.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.swagr.datasource;

import com.rhythm.louie.jdbc.DatasourceFactory;
import com.rhythm.louie.jdbc.JdbcFactory;

public class SwagrJdbc {
    static final String DB_KEY = "swagr";
    static final String DATASOURCE = "jdbc/swagrDS";
    
    private SwagrJdbc() {}
    
    /**
     * Returns a factory that creates connections using JNDI
     * @return JdbcFactory  
     */
    public static JdbcFactory getFactory() {
        return new DatasourceFactory(DATASOURCE, DB_KEY);
    }
}
