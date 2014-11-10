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
    public RemoteServiceLayer(String host) {
        this.host = host;
    }
    
    public String getHost() {
        return host;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadLayer(Class<T> service) throws Exception {
        Server remote = Server.getServer(host);
        if (remote == null) {
            throw new Exception("Cannot create remote service! Unknown server: " + host);
        }

        // Not ideal and will break easily, but gets the job done
        String remoteService = service.getPackage().getName() + "." 
                + ServiceUtils.getServiceBaseName(service)+"RemoteService";

        T remoteLayer = (T) Class.forName(remoteService).newInstance();
        if (remoteLayer instanceof Connectable) {
            LouieConnection connection = LouieConnectionFactory.getConnectionForServer(remote);
            ((Connectable)remoteLayer).setConnection(connection);
        } else {
            throw new Exception("Remote Service Must implement Connectable!");
        }
        return remoteLayer;
    }
    
}
