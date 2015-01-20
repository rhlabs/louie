/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
