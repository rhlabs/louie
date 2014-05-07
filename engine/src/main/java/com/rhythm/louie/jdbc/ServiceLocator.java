package com.rhythm.louie.jdbc;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

/**
 *  This class is an implementation of the Service Locator pattern. It is
 *  used to looukup resources such as EJBHomes, JMS Destinations, etc.
 */
public class ServiceLocator {
    private transient InitialContext  ic;
    
    public ServiceLocator() throws ServiceLocatorException  {
        try {
            ic = new InitialContext();
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * will get the ejb Local home factory.
     * clients need to cast to the type of EJBHome they desire
     *
     * @return the Local EJB Home corresponding to the homeName
     */
    public EJBLocalHome getLocalHome(String jndiHomeName) throws ServiceLocatorException {
        try {
            return (EJBLocalHome) ic.lookup(jndiHomeName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * will get the ejb Remote home factory.
     * clients need to cast to the type of EJBHome they desire
     *
     * @return the EJB Home corresponding to the homeName
     */
    public EJBHome getRemoteHome(String jndiHomeName, Class className) throws ServiceLocatorException {
        try {
            Object objref = ic.lookup(jndiHomeName);
            return (EJBHome) PortableRemoteObject.narrow(objref, className);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * @return the factory for the factory to get queue connections from
     */
    public  QueueConnectionFactory getQueueConnectionFactory(String qConnFactoryName)
    throws ServiceLocatorException {
        try {
            return (QueueConnectionFactory) ic.lookup(qConnFactoryName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * @return the Queue Destination to send messages to
     */
    public  Queue getQueue(String queueName) throws ServiceLocatorException {
        try {
            return (Queue)ic.lookup(queueName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * This method helps in obtaining the topic factory
     * @return the factory for the factory to get topic connections from
     */
    public  TopicConnectionFactory getTopicConnectionFactory(String topicConnFactoryName) throws ServiceLocatorException {
        try {
            return (TopicConnectionFactory) ic.lookup(topicConnFactoryName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * This method obtains the topc itself for a caller
     * @return the Topic Destination to send messages to
     */
    public  Topic getTopic(String topicName) throws ServiceLocatorException {
        try {
            return (Topic)ic.lookup(topicName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * This method obtains the datasource itself for a caller
     * @return the DataSource corresponding to the name parameter
     */
    public DataSource getDataSource(String dataSourceName) throws ServiceLocatorException {
        try {
            return (DataSource) PortableRemoteObject.narrow(ic.lookup(dataSourceName),DataSource.class);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * @return the String value corresponding
     * to the env entry name.
     */
    public String getString(String envName) throws ServiceLocatorException {
        try {
            return (String)ic.lookup("java:comp/env/" + envName);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }
    /**
     * @returns a refernce to any class requested and deifined
     * in the ejb xml file.
     */
    public Object loadObject(String objectName) throws ServiceLocatorException {
        try {
            String path = (String) ic.lookup("java:comp/env/param/" + objectName);
            //Must use the correct class loader
            return Class.forName( path, true, Thread.currentThread().getContextClassLoader()).newInstance();
            //return Class.forName(path).newInstance();
        } catch (Exception ne) {
            throw new ServiceLocatorException(ne);
        }
    }
}
