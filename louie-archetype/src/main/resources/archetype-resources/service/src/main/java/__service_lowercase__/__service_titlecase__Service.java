#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${service_lowercase};

import ${package}.${service_lowercase}.${service_titlecase}Protos.*;

import com.rhythm.louie.Service;
import com.rhythm.louie.service.ServiceUtils;

@Service("${service_lowercase}")
public interface ${service_titlecase}Service {
    final String SERVICE_NAME = ServiceUtils.getServiceName(${service_titlecase}Service.class);
    
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

