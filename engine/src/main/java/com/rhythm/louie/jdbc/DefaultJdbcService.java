/*
 * DefaultJdbcService.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.log4jdbc.ConnectionSpy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author cjohnson
 Created: Feb 11, 2011 2:15:31 PM
 */
public class DefaultJdbcService implements JdbcService {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultJdbcService.class);
            
    private static final String DB_KEY = "DB_KEY";
    
    //------------------------------------
    // Object VARIABLES
    //------------------------------------
    protected PreparedStatement pStatement;
    protected Connection conn;
    protected ResultSet resultSet;
    protected String sqlString;

    protected String dbKey;
    
    protected boolean AUTOGENERATE_KEYS = true;

    protected String getDbKey() {
        return dbKey;
    }

    public DefaultJdbcService(ConnectionFactory connFactory, String dbKey) throws Exception {
        conn = new ConnectionSpy(connFactory.createConnection());
        this.dbKey = dbKey;
    }

    @Override
    public String getSqlString() {
        return sqlString;
    }

    @Override
    public void setSqlString(String s) {
        sqlString = s;
        closeStatements();
    }
    
    // Returns a connection to the calling client.
    @Override
    public Connection getConnection() {
        return conn;
    }
    
    // Returns a prepared statement
    @Override
    public PreparedStatement getPreparedStatement() throws SQLException {
        MDC.put(DB_KEY, dbKey);
        
        if (pStatement == null) {
            if (AUTOGENERATE_KEYS) {
                pStatement = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
            } else {
                pStatement = conn.prepareStatement(sqlString);
            }
        }
        return pStatement;
    }
    
    @Override
    public int getSerial() throws SQLException {
        int serialID = -1;
        PreparedStatement ps = pStatement;

        if (ps != null) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                do {
                    for (int i = 1; i <= colCount; i++) {
                        serialID = rs.getInt(i);
                    }
                } while (rs.next());
            } else {
                LOGGER.error("No generated keys found!");
            }
        }

        return serialID;
    }

    @Override
    public ResultSet executeStatement() throws SQLException {
        return executePreparedStatement();
    }
    
    // Executes a prepared statement
    @Override
    public ResultSet executePreparedStatement() throws SQLException {
        resultSet =  getPreparedStatement().executeQuery();
        return resultSet;
    }

    // Executes a prepared statement
    @Override
    public void executePreparedStatementProcedure() throws SQLException {
        getPreparedStatement().execute();
    }

    // Executes a prepared statement
    @Override
    public int executePreparedStatementUpdate() throws SQLException {
        return getPreparedStatement().executeUpdate();
    }

    //CLOSE
    private void closeStatements() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
            }
            resultSet = null;
        }
        if (pStatement != null) {
            try {
                pStatement.close();
            } catch (SQLException e) {
            }
            pStatement = null;
        }
    }

    // Used to close all connections
    @Override
    public void closeAll() {
        closeStatements();

        if (conn != null) {
            try {
                conn.close();
            } catch(Exception e) {}
            conn = null;
        }
    }
}
