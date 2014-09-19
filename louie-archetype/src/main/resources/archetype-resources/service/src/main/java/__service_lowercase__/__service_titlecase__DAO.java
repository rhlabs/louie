#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${service_lowercase};

import ${package}.${service_lowercase}.${service_titlecase}Protos.*;

import com.rhythm.louie.DAO;

@DAO
public class ${service_titlecase}DAO implements ${service_titlecase}Service {

    @Override
    public ${service_titlecase}ResponsePB basicRequest(String request) throws Exception {
        ${service_titlecase}ResponsePB resp = ${service_titlecase}ResponsePB.newBuilder()
                .setResponse("Received : " + request)
                .build();
        return resp;
    }
    
}

