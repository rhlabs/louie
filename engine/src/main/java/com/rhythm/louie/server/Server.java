/*
 * Server.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import com.rhythm.louie.Constants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.info.InfoProtos.ServerPB;

/**
 *
 * @author cjohnson
 */
public class Server {
    public static final Server UNKNOWN = new Server("Unknown");
    static {
        UNKNOWN.host = "localhost";
        UNKNOWN.location = "UNKNOWN";
        UNKNOWN.timezone = TimeZone.getDefault().getDisplayName();
    }
    
    private static String DEFAULT_GATEWAY = Constants.DEFAULT_GATEWAY;
    public static void setDefaultGateway(String gateway) {
        DEFAULT_GATEWAY = gateway;
    }
    
    public static Server LOCAL = UNKNOWN;
    
    private static Server CENTRAL_AUTH = null;
    private static Server ROUTER = null;
    
    private static final Map<ServerKey,Server> SERVERS_BY_ADDRESS = 
            Collections.synchronizedMap(new HashMap<ServerKey,Server>());
    private static final Map<String,Server> SERVERS = 
            Collections.synchronizedMap(new HashMap<String,Server>());
    private static final Map<String,Server> SERVERS_BY_LOCATION = 
            Collections.synchronizedMap(new HashMap<String,Server>());
    
    private static List<Server> ALL_SERVERS = Collections.emptyList();
    private static List<ServerPB> ALL_SERVER_PBS = Collections.emptyList();
    private static List<String> SERVER_LOCATIONS = Collections.emptyList();
    
    private final String name;
    private String timezone;
    private String host;
    private String display;
    private String location;
    private String gateway;
    private boolean mutualSSL;
    private String sslPass;
    private String sslCAPass;
    private int sslPort;
    private String sslGateway;
    private String ip;
    private boolean router;
    
    private ServerPB pb;
    
    public static void processServerProperties(Properties props) {
        synchronized(SERVERS) {
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
            
                String[] keyParts = key.split("\\.",2);
                String serverName = keyParts[0];
                
                Server server = SERVERS.get(serverName);
                if (server==null) {
                    server = new Server(serverName);
                    SERVERS.put(serverName,server);
                }
                
                if (keyParts.length==1) {
                    server.host=value;
                } else {
                    String attribute = keyParts[1];
                    if (attribute.equals("display")) {
                        server.display=value;
                    } else if (attribute.equals("timezone")) {
                        server.timezone=value;
                    } else if (attribute.equals("location")) {
                        server.location=value;
                    } else if (attribute.equals("gateway")) {
                        server.gateway=value;
                    } else if (attribute.equals("mutual_ssl")) {
                        server.mutualSSL=value.equals("true");
                    } else if (attribute.equals("ssl_port")) {
                        server.sslPort=Integer.parseInt(value);
                    } else if (attribute.equals("ssl_gateway")) {
                        server.sslGateway=value;
                    } else if (attribute.equals("ssl_pass")) {
                        server.sslPass=value;
                    } else if (attribute.equals("ssl_ca_pass")) {
                        server.sslCAPass=value;
                    } else if (attribute.equals("ip")){
                        server.ip=value;
                    } else if (attribute.equals("router")) {
                        server.router=Boolean.parseBoolean(value);
                        if (server.router) {
                            ROUTER = server;
                        }
                     }else if (attribute.equals("central_auth")) {
                        CENTRAL_AUTH = server;
                    } else {
                        LoggerFactory.getLogger(Server.class)
                                .warn("Warning! Unknown Server Property: {}", key);
                    }
                }
            }
            
            if (SERVERS.isEmpty()) {
                LoggerFactory.getLogger(Server.class)
                        .warn("No servers found, setting to localhost");
                Server server = new Server("LOCAL");
                server.host = "localhost";
                server.location="LOCAL";
                server.timezone=TimeZone.getDefault().getDisplayName();
                SERVERS.put("localhost", server);
                
                Server.LOCAL = server;
            }
            
            List<String> disabled = new ArrayList<String>();
            
            for (Server server : SERVERS.values()) {
                try {
                    String addr = InetAddress.getByName(server.getHostName()).getHostAddress();
                    SERVERS_BY_ADDRESS.put(new ServerKey(addr,server.getGateway()), server);
                } catch (UnknownHostException ex) {
                    LoggerFactory.getLogger(Server.class)
                            .warn("Failed to resolve hostname {} into ip, will "
                            + "try to put any specified IP.",server.getHostName());
                    if (server.getIp() != null) {
                        SERVERS_BY_ADDRESS.put(new ServerKey(server.getIp(), server.getGateway()), server);
                    }
                }
                String location = server.getLocation();
                if (location.isEmpty()) {
                    LoggerFactory.getLogger(Server.class)
                            .error("Server {} does not specify a location!  DISABLING!", server.getName());
                    disabled.add(server.getName());
                } else {
                    if (LOCAL != null) {
                        if (LOCAL.getLocation().equals(location)) {
                            if (LOCAL.getHostName().equals(server.getHostName())) {     //lame way to ensure only THIS server is keyed for THIS location (routing machinery)
                                SERVERS_BY_LOCATION.put(server.getLocation(),server);                            
                            } else {
                                LoggerFactory.getLogger(Server.class)
                                        .info("Additional server {} found for location {}",server.getHostName(),location);
                            }
                        } else {
                            SERVERS_BY_LOCATION.put(server.getLocation(),server);
                        }
                    }
//                    if (previous!=null) {
//                        LOGGER.warn("WARNING! Multiple Servers have the same location: "+server.getLocation());
//                    }
                }
            }
            
            for (String disableName : disabled) {
                SERVERS.remove(disableName);
            }
            
            ALL_SERVERS = Collections.unmodifiableList(new ArrayList<Server>(SERVERS.values()));
            List<ServerPB> serverPBs = new ArrayList<ServerPB>(ALL_SERVERS.size());
            for (Server server : Server.allServers()) {
                server.pb = ServerPB.newBuilder()
                        .setName(server.getName())
                        .setHost(server.getHostName())
                        .setLocation(server.getLocation())
                        .setTimezone(server.getTimezone())
                        .setDisplay(server.getDisplay())
                        .build();
                serverPBs.add(server.pb);
            }
            ALL_SERVER_PBS = Collections.unmodifiableList(serverPBs);
            SERVER_LOCATIONS = Collections.unmodifiableList(new ArrayList<String>(SERVERS_BY_LOCATION.keySet()));
            
            //Print
            StringBuilder sb = new StringBuilder();
            sb.append("\nServers:\n\n");
            for (Server server : Server.allServers()) {
                sb.append(server.getName()).append(":").append(server.getHostName());
                sb.append(" - ").append(server.getLocation());
                if (server.isSSLMutual()) {
                    sb.append(" (SSL)");
                }
                sb.append("\n");
                if (server.getIp().equals(LocalConstants.IP)) {
                    Server.LOCAL = server;
                }
            }
            sb.append("\n");
            
            if (Server.LOCAL!=UNKNOWN) {
                sb.append("Current Server: ").append(Server.LOCAL.getName());
            }
            LoggerFactory.getLogger(Server.class).info(sb.toString());
            
            if (Server.LOCAL==UNKNOWN) {
                LoggerFactory.getLogger(Server.class)
                        .error("This Server: {} is UNKNOWN! Disabling all Services!", LocalConstants.HOSTDOMAIN);
            }
        }
    }
    
    /**
     * The preferred server lookup, this accommodates gateways properly, as well
     * as fully resolving the provided hostname into an IP to avoid naming issues.
     * @param host
     * @param gateway
     * @return
     * @throws UnknownHostException 
     */
    public static Server getResolvedServer(String host, String gateway) throws UnknownHostException {
        String addr;
        try { 
            addr = InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException ex) {
            LoggerFactory.getLogger(Server.class.getName()).error("Could not resolve host name: {} via Server.getResolvedServer()", host);
            throw ex;
        }
        return SERVERS_BY_ADDRESS.get(new ServerKey(addr,gateway));
    }
    
    /**
     * Similar to getResolvedServer, but assumes you have already collected 
     * the valid IP address of the target host
     * @param ipAddress
     * @param gateway
     * @return
     * @throws UnknownHostException 
     */
    public static Server getServer(String ipAddress, String gateway) throws UnknownHostException {
        return SERVERS_BY_ADDRESS.get(new ServerKey(ipAddress,gateway));
    }
    
    public static Server getServer(String name) {
        return SERVERS.get(name);
    }
    
    public static Server getServerForLocation(String location) {
        return SERVERS_BY_LOCATION.get(location);
    }
    
    public static List<String> getServerLocations() {
        return SERVER_LOCATIONS;
    }
    
    private Server(String name) {
        this.name = name;
        this.host = "";
        this.ip = null;
        this.display = "";
        this.timezone = "";
        this.location = "";
        this.mutualSSL = false;
        this.sslCAPass = null;
        this.sslPass = null;
        this.sslPort = 0;
        this.sslGateway = null;
        this.gateway = DEFAULT_GATEWAY;
        this.router = false;
    }
     
    public static List<Server> allServers() {
        return ALL_SERVERS;
    }
    
    public static List<ServerPB> allServerPbs() {
        return ALL_SERVER_PBS;
    }
    
    public boolean isARouter() {
        return router;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplay() {
        return display;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public String getHostName() {
        return host;
    }
    
    public String getIp() {
        if (ip == null) {
            try {
                ip = InetAddress.getByName(host).getHostAddress();
            } catch (UnknownHostException ex) {
                LoggerFactory.getLogger(Server.class)
                        .error("ERROR getting IP for " + host+" : "+ex.toString());
            }
        }
        return ip;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getGateway() {
        return gateway;
    }
    
    public boolean isSSLMutual() {
        return mutualSSL;
    }

    public int getSSLPort() {
        return sslPort;
    }
    
    public String getSSLPassword() {
        return sslPass;
    }
    
    public String getSSLCAPassword() {
        return sslCAPass;
    }
    
    public String getSSLGateway() {
        return sslGateway;
    }
    
    public ServerPB toPB() {
        return pb;
    }
    
    public static Server getCentralAuth() {
        return CENTRAL_AUTH;
    }
    
    public static Server getRouter() {
        return ROUTER;
    }
    
    private static class ServerKey {
        String address;
        String gateway;

        public ServerKey(String address, String gateway) {
            this.address = address;
            this.gateway = gateway;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.address != null ? this.address.hashCode() : 0);
            hash = 59 * hash + (this.gateway != null ? this.gateway.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServerKey other = (ServerKey) obj;
            if ((this.address == null) ? (other.address != null) : !this.address.equals(other.address)) {
                return false;
            }
            if ((this.gateway == null) ? (other.gateway != null) : !this.gateway.equals(other.gateway)) {
                return false;
            }
            return true;
        }
        
    }
    
}
