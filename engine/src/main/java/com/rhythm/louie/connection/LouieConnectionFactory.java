/*
 * LouieConnectionFactory.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.connection;

import com.google.protobuf.Message;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.Server;
import com.rhythm.pb.PBParam;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionKey;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cjohnson
 * Created: Jan 17, 2012 2:21:54 PM
 */
public class LouieConnectionFactory {
            
    private static final String BETAHOST = "louiebeta";
    private static final String LOCALHOST = "localhost";
    
    private static final Map<Server, LouieConnection> connections = new ConcurrentHashMap<Server, LouieConnection>();
    
    private LouieConnectionFactory() {}
    
    private static final SharedConnection CONNECTION = new SharedConnection();
    public static synchronized LouieConnection getSharedConnection() {
        return CONNECTION;
    }
    
    public static synchronized void shareConnection(LouieConnection connection) {
        CONNECTION.setSharedConnection(connection);
    }
    
    static class SharedConnection implements LouieConnection {
        LouieConnection delegate;
        public SharedConnection() {
            delegate = getConnection(System.getProperty("com.rhythm.louiehost", "localhost"));
        }
        
        public void setSharedConnection(LouieConnection conn) {
            delegate = conn;
        }

        public LouieConnection getSharedConnection() {
            return delegate;
        }
        
        @Override
        public IdentityPB getIdentity() {
            if (delegate==null) {
                return null;
            } else {
                return delegate.getIdentity();
            }
        }

        @Override
        public SessionKey getSessionKey() throws Exception {
            return getDelegate().getSessionKey();
        }

        @Override
        public <T extends Message> Response<T> request(String system, String cmd, PBParam params, T template) throws Exception {
            return getDelegate().request(system,cmd,params,template);
        }
        
        private LouieConnection getDelegate() throws Exception {
            if (delegate==null) {
                throw new Exception("Shared Connection has not been setup.");
            }
            return delegate;
        }

        @Override
        public void setMaxTimeout(int seconds) {
            try {
                getDelegate().setMaxTimeout(seconds);
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error setting MaxTimeout", ex);
            }
        }

        @Override
        public int getMaxTimeout() {
            try {
                return getDelegate().getMaxTimeout();
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error getting MaxTimeout", ex);
            }
            return 0;
        }

        @Override
        public void setRetryEnable(boolean enable) {
            try {
                getDelegate().setRetryEnable(enable);
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error setting RetryEnable", ex);
            }
        }

        @Override
        public boolean getRetryEnable() {
            try {
                return getDelegate().getRetryEnable();
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error getting RetryEnable", ex);
            }
            return true; //default value
        }

        @Override
        public void setGateway(String gateway) {
            try {
                getDelegate().setGateway(gateway);
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error setting Gateway", ex);
            }
        }

        @Override
        public URLConnection getJsonForwardingConnection() throws Exception {
            return getDelegate().getJsonForwardingConnection();
        }

        @Override
        public URLConnection getForwardingConnection() throws Exception {
            return getDelegate().getForwardingConnection();
        }

        @Override
        public void setPort(int port) {
            try {
                getDelegate().setPort(port);
            } catch (Exception ex) {
                LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error(ex.toString());
            }
        }
    }
    
    public static LouieConnection getConnection(String host) {
        return new DefaultLouieConnection(Identity.getIdentity(),host);
    }
    
    public static LouieConnection getConnection(String host,IdentityPB id) {
        return new DefaultLouieConnection(id,host);
    }
    
    public static LouieConnection getConnection(SSLConfig config) {
        return new DefaultLouieConnection(Identity.getIdentity(),config);
    }
    
    public static LouieConnection getConnection(SSLConfig config, IdentityPB id) {
        return new DefaultLouieConnection(id,config);
    }
    
    public static LouieConnection getLocalConnection() {
        return new DefaultLouieConnection(Identity.getIdentity(),LOCALHOST);
    }
    public static LouieConnection getLocalConnection(IdentityPB id) {
        return new DefaultLouieConnection(id,LOCALHOST);
    }
    public static LouieConnection getLocalConnection(IdentityPB id, String key) {
        return new DefaultLouieConnection(id,LOCALHOST,key);
    }
    
    
    public static LouieConnection getBetaConnection() {
        return new DefaultLouieConnection(Identity.getIdentity(),BETAHOST);
    }
    public static LouieConnection getBetaConnection(IdentityPB id) {
        return new DefaultLouieConnection(id,BETAHOST);
    }
    
    
    public static LouieConnection getConnectionForLocation(String location) throws Exception {
        return getConnectionForLocation(location,Identity.getIdentity());
    }
    public static LouieConnection getConnectionForLocation(String location,IdentityPB id) throws Exception {
        Server server = Server.getServerForLocation(location);
        if (server == null) {
            throw new Exception("No Server found for location: "+location);
        }
        return getMutualSSLConnection(id, server);
    }
    
    
    public static LouieConnection getConnectionForServer(Server server) {
        return getConnectionForServer(server,Identity.getIdentity());
    }
    
    public static LouieConnection getConnectionForServer(Server server,IdentityPB id) {
        return getMutualSSLConnection(id, server);
    }
    
    private static LouieConnection getMutualSSLConnection(IdentityPB id, Server server) {
        LouieConnection conn = connections.get(server);
        if (conn == null) {
            // check for ssl 
            if (server.isSSLMutual()) { 
                SSLConfig sslConfig;
                try {
                    sslConfig = new LouieSSLClientConfig(server);
                } catch (Exception ex) {
                    LoggerFactory.getLogger(LouieConnectionFactory.class)
                        .error("Error creating SSL config", ex);
                    return new DefaultLouieConnection(id,server.getIp(),null,server.getGateway());
                }
                return new DefaultLouieConnection(id,sslConfig);
            }
            //else attempt a regular http connection, what the heck!
            conn = new DefaultLouieConnection(id,server.getIp(),null,server.getGateway());
            connections.put(server, conn);
        }
        return conn;
    }
    
    public final void removeConnection(Server server){
        connections.remove(server);
    }
}
