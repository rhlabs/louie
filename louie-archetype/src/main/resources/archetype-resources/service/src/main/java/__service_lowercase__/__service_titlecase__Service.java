#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${service_lowercase};

import ${package}.pb.${service_lowercase}.${service_titlecase}Protos.${service_titlecase}ResponsePB;
import com.rhythm.louie.process.Service;

@Service
public interface ${service_titlecase}Service {

    /**
    * An example service method which accepts a String  
    * and returns an ${service_titlecase}ResponsePB
    *
    * @param request
    * @return ${service_titlecase}ResponsePB
    * @throws Exception
    */
    ${service_titlecase}ResponsePB basicRequest(String request) throws Exception;

}

