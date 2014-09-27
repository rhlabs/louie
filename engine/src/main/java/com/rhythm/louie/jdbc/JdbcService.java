/*
 * JdbcService.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author cjohnson
 */
public interface JdbcService extends AutoCloseable {

    Connection getConnection();

    void closeAll();

    String getSqlString();

    void setSqlString(String s);

    PreparedStatement getPreparedStatement() throws SQLException;

    ResultSet executePreparedStatement() throws SQLException;

    void executePreparedStatementProcedure() throws SQLException;

    int executePreparedStatementUpdate() throws SQLException;

    ResultSet executeStatement() throws SQLException;

    int getSerial() throws SQLException;
}
