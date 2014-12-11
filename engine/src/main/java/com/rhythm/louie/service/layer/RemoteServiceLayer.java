/*
 * RemoteServiceLayer.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.service.layer;

import com.rhythm.louie.connection.Connectable;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.server.Server;
import com.rhythm.louie.service.ServiceUtils;

/**
 *
 * @author cjohnson
 */
public class RemoteServiceLayer implements ServiceLayer {

    private final String host;
    private final String gateway;
    private final int port;
    private final String server;
    
    public RemoteServiceLayer(String server) {
        this.server = server;
        this.host = null;
        this.gateway = null;
        this.port = 0;
    }
    
    public RemoteServiceLayer(String host, String gateway, int port) {
        this.host = host;
        this.gateway = gateway;
        this.port = port;
        this.server = null;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getGateway() {
        return gateway;
    }
    
    public int getPort() {
        return port;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadLayer(Class<T> service) throws Exception {
        LouieConnection connection;
        
        if (server != null) {
            Server remote = Server.getServer(server);
            if (remote == null) {
                throw new Exception("Cannot create remote service! Unknown server: " + host);
            }
            connection = LouieConnectionFactory.getConnectionForServer(remote);
        } else {
            connection = LouieConnectionFactory.getConnection(host);
            connection.setGateway(gateway);
            connection.setPort(port);
        }

        // Not ideal and will break easily, but gets the job done
        String remoteService = service.getPackage().getName() + "." 
                + ServiceUtils.getServiceBaseName(service)+"RemoteService";

        T remoteLayer = (T) Class.forName(remoteService).newInstance();
        if (remoteLayer instanceof Connectable) {
            ((Connectable)remoteLayer).setConnection(connection);
        } else {
            throw new Exception("Remote Service Must implement Connectable!");
        }
        return remoteLayer;
    }
    
}
