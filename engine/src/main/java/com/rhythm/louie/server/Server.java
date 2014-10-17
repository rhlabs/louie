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
    private static String DEFAULT_GATEWAY = Constants.DEFAULT_GATEWAY;
    protected static void setDefaultGateway(String gateway) { 
        if (gateway==null) {
            DEFAULT_GATEWAY = Constants.DEFAULT_GATEWAY;
        } else {
            DEFAULT_GATEWAY = gateway;
        }
    }
    
    public static final Server UNKNOWN = new Server("Unknown");
    static {
        UNKNOWN.host = "localhost";
        UNKNOWN.location = "UNKNOWN";
        UNKNOWN.timezone = TimeZone.getDefault().getDisplayName();
        // set gateway again here just to be sure it gets set correctly on initialize
        UNKNOWN.gateway = Constants.DEFAULT_GATEWAY;
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
    
    private final Map<String,String> properties;
    
    private static List<Server> ALL_SERVERS = Collections.emptyList();
    private static List<ServerPB> ALL_SERVER_PBS = Collections.emptyList();
    private static List<String> SERVER_LOCATIONS = Collections.emptyList();
    
    private final String name;
    private String timezone;
    private String host;
    private String display;
    private String location;
    private String gateway;
    private String ip;
    private boolean router;
    private boolean centralAuth;
    private int port;
    
    private static String defaultTimezone;
    private static String defaultHost;
    private static String defaultDisplay;
    private static String defaultLocation;
    private static String defaultIP;
    private static boolean defaultRouter;
    private static int defaultPort;
    
    private ServerPB pb;
    

    protected static void processServers(List<Server> servers) {
        for (Server server : servers) {
            SERVERS.put(server.getName(), server);
            if (server.centralAuth) CENTRAL_AUTH = server;
            if (server.router) ROUTER = server;
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

        List<String> disabled = new ArrayList<>();

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
                        //lame way to ensure only THIS server is keyed for THIS location (routing machinery)
                        if (LOCAL.getHostName().equals(server.getHostName())) {     
                            SERVERS_BY_LOCATION.put(server.getLocation(),server);                            
                        } else {
                            LoggerFactory.getLogger(Server.class)
                                    .info("Additional server {} found for location {}",server.getHostName(),location);
                        }
                    } else {
                        SERVERS_BY_LOCATION.put(server.getLocation(),server);
                    }
                }
            }
        }

        for (String disableName : disabled) {
            SERVERS.remove(disableName);
        }

        ALL_SERVERS = Collections.unmodifiableList(new ArrayList<>(SERVERS.values()));
        List<ServerPB> serverPBs = new ArrayList<>(ALL_SERVERS.size());
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
        SERVER_LOCATIONS = Collections.unmodifiableList(new ArrayList<>(SERVERS_BY_LOCATION.keySet()));

        //Print
        StringBuilder sb = new StringBuilder();
        sb.append("\nServers:\n\n");
        for (Server server : Server.allServers()) {
            sb.append(server.getName()).append(":").append(server.getHostName());
            sb.append(" - ").append(server.getLocation());
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

    protected void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    protected void setHost(String host) {
        this.host = host;
    }

    protected void setDisplay(String display) {
        this.display = display;
    }

    protected void setLocation(String location) {
        this.location = location;
    }

    protected void setGateway(String gateway) {
        this.gateway = gateway;
    }

    protected void setIp(String ip) {
        this.ip = ip;
    }

    protected void setRouter(boolean router) {
        this.router = router;
    }

    protected void setCentralAuth(boolean centralAuth) {
        this.centralAuth = centralAuth;
    }
    
    protected void setPort(int port) {
        this.port = port;
    }
    
    protected static void setDefaultTimezone(String timezone) {
        defaultTimezone = timezone;
    }

    protected static void setDefaultHost(String host) {
        defaultHost = host;
    }

    protected static void setDefaultDisplay(String display) {
        defaultDisplay = display;
    }

    protected static void setDefaultLocation(String location) {
        defaultLocation = location;
    }

    protected static void setDefaultIP(String IP) {
        defaultIP = IP;
    }

    protected static void setDefaultRouter(boolean router) {
        defaultRouter = router;
    }
    
    protected static void setDefaultPort(int port) {
        defaultPort = port;
    }
    
    protected void addCustomProperty(String key, String value) {
        properties.put(key, value);
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
    
    protected Server(String name) {
        this.name = name;
        this.host = defaultHost;
        this.ip = (defaultIP == null || defaultIP.isEmpty()) ? null : defaultIP;
        this.display = defaultDisplay;
        this.timezone = defaultTimezone;
        this.location = defaultLocation;
        this.gateway = DEFAULT_GATEWAY;
        this.router = defaultRouter;
        this.port = defaultPort;
        properties = new HashMap<>();
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
    
    public int getPort() {
        return port;
    }
    
    public String getCustomProperty(String key) {
        return properties.get(key);
    }
    
    public String getCustomProperty(String key, String def) {
        String value = properties.get(key);
        if (value == null) return def;
        return value;
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
            if (!Objects.equals(this.address, other.address)) {
                return false;
            }
            if (!Objects.equals(this.gateway, other.gateway)) {
                return false;
            }
            return true;
        }
        
    }
    
}
