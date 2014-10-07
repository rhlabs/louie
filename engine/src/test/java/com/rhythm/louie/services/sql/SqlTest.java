/*
 * SqlTest.java
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
