#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * StateListener.java
 */
package ${package}.servlets; 

import com.rhythm.louie.service.ServiceFactory;
import com.rhythm.louie.servlet.ServiceRegister;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.annotation.WebListener;

import ${package}.${service_lowercase}.${service_titlecase}ServiceFactory;

/**
 * LoUIE Service Registration Hook
 */
@WebListener()
public class StateListener extends ServiceRegister {
    @Override
    public Collection<ServiceFactory> loadFactories() {
        Collection<ServiceFactory> factories = new ArrayList<>();
        
        factories.add(new ${service_titlecase}ServiceFactory());
        
        return factories;
    }
}
