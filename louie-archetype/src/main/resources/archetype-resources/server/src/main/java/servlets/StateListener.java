#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * ServletListener.java
 */
package ${package}.servlets; 

import com.rhythm.louie.ServiceManager;

import com.rhythm.pb.command.ServiceFactory;

import ${package}.${service_lowercase}.${service_titlecase}ServiceFactory;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * @author cjohnson
 */
public class StateListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            
            // Init services
            ServiceManager.addService(${service_titlecase}ServiceFactory.getInstance());

            try {
                Class c = Class.forName("com.rhythm.swagr.SwagrServiceFactory");
                @SuppressWarnings("unchecked")
                Method factoryMethod = c.getDeclaredMethod("getInstance");
                ServiceFactory swagrFactory = (ServiceFactory) factoryMethod.invoke(null,(Object[]) null);
                ServiceManager.addService(swagrFactory);
            } catch (Exception ex){
                LoggerFactory.getLogger(StateListener.class.getName()).error("SWAGr Service was not loaded");
            }
            
            ServiceManager.initialize(sce.getServletContext());
        } catch (Exception ex) {
            Logger.getLogger(StateListener.class.getName()).log(Level.SEVERE, 
                    "ERROR Initializing Services", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServiceManager.shutdown();
    }
    
}
