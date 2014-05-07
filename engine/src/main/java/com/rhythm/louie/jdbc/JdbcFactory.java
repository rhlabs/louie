/*
 * JdbcService.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jdbc;

/**
 * @author cjohnson
 * Created: Nov 14, 2011 6:57:09 PM
 */
public interface JdbcFactory {
    public JdbcService newService(String sql) throws Exception;
}
