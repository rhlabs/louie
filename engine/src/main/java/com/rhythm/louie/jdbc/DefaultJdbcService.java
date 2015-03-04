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

import java.io.IOException;
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

    // Executes a prepared statement
    @Override
    public boolean execute() throws SQLException {
        return getPreparedStatement().execute();
    }
    
    // Executes a prepared statement
    @Override
    public int executeUpdate() throws SQLException {
        return getPreparedStatement().executeUpdate();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return getPreparedStatement().executeQuery();
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return getPreparedStatement().getResultSet();
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        return getPreparedStatement().getUpdateCount();
    }
    
    //CLOSE
    private void closeStatements() {
        if (pStatement != null) {
            try {
                pStatement.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing JDBC statement", e);
            }
            pStatement = null;
        }
    }

    @Override
    public void close() throws IOException {
        closeStatements();

        if (conn != null) {
            try {
                conn.close();
            } catch(Exception e) {
                LOGGER.error("Error closing JDBC Connection", e);
            }
            conn = null;
        }
    }
}
