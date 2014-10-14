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

import ${package}.${service_lowercase}.${service_titlecase}Protos.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class ${service_titlecase}Test {
    
    private final ${service_titlecase}Client client;

    public ${service_titlecase}Test() {
        IdentityPB id = Identity.createJUnitIdentity();
        LouieConnection connection = LouieConnectionFactory.getConnection("localhost", id);
        connection.setGateway("${rootArtifactId}");

        client = ${service_titlecase}ClientFactory.getClient(connection);
    }
    
    @Test
    public void basicTest() throws Exception {
        String example = "Hello World!";
        ${service_titlecase}ResponsePB response = client.basicRequest(example);
        System.out.println(response);
        assertNotNull(response);
    }
    
}
