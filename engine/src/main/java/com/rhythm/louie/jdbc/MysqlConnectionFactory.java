/*
 * MysqlConnectionFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author cjohnson
 */
public class MysqlConnectionFactory implements ConnectionFactory {
    private final String host;
    private final String db;
    private final String user;
    private final String pw;
    
    public MysqlConnectionFactory(String host, String db) {
        this (host,db,"","");
    }
    
    public MysqlConnectionFactory(String host, String db, String user, String pw) {
        this.host = host;
        this.db = db;
        this.user = user;
        this.pw = pw;
    }

    @Override
    public Connection createConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://"+host+"/"+db,user,pw);
    }
}
