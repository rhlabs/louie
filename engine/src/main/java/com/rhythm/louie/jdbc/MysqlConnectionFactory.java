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
