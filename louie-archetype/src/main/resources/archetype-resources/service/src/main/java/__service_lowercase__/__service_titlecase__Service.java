#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${service_lowercase};

import ${package}.${service_lowercase}.${service_titlecase}Protos.*;

import com.rhythm.louie.Service;

@Service
public interface ${service_titlecase}Service {

/**
    * An example service method which accepts a String  
    * and returns a ${service_titlecase}ResponsePB
    *
    * @param request
    * @return ${service_titlecase}ResponsePB
    * @throws Exception
    */
    ${service_titlecase}ResponsePB basicRequest(String request) throws Exception;

}

