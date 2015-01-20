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
package com.rhythm.louie.services.sql;

import java.util.List;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;

import com.rhythm.pb.RequestProtos.IdentityPB;

import com.rhythm.louie.sql.SqlProtos.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SqlTest {
    
    private static SqlClient client;

    public SqlTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        IdentityPB id = Identity.createJUnitIdentity();
        LouieConnection connection = LouieConnectionFactory.getConnection("localhost", id);
        connection.setGateway("sql");

        client = SqlClientFactory.getClient(connection);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void selectTest() throws Exception {
        SqlStatementPB statement = SqlStatementPB.newBuilder()
                .setSql("Select * from content")
                .build();
        List<SqlResultPB> response = client.query(statement);
        System.out.println(response);
        assertNotNull(response);
    }
    
    @Test
    public void updateTest() throws Exception {
        SqlStatementPB statement = SqlStatementPB.newBuilder()
                .setSql("UPDATE content set state='A' where id=1")
                .build();
        List<SqlResultPB> response = client.query(statement);
        System.out.println(response);
        assertNotNull(response);
    }
    
}
