#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * ${service_titlecase}Test.java
 */

package ${package}.${service_lowercase};

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.pb.RequestProtos.IdentityPB;
import ${package}.pb.${service_lowercase}.${service_titlecase}Protos.${service_titlecase}ResponsePB;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ${service_titlecase}Test {
    
    private static ${service_titlecase}Client client;

    public ${service_titlecase}Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        IdentityPB id = Identity.createJUnitIdentity();
        LouieConnection connection = LouieConnectionFactory.getConnection("localhost", id);
        client = ${service_titlecase}ClientFactory.getClient(connection);
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
    public void basicTest() throws Exception{
        String example = "Hello World!";
        ${service_titlecase}ResponsePB response = client.basicRequest(example);
        System.out.println(response);
        assertNotNull(response);
    }
    
}
