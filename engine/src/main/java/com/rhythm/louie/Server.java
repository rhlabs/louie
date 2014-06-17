/*
 * Server.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.slf4j.LoggerFactory;

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
    
    public static String DEFAULT_GATEWAY = "louie";
    public static void setDefaultGateway(String gateway) {
        DEFAULT_GATEWAY = gateway;
    }
    
    public static Server LOCAL = UNKNOWN;
    
    private static final Map<String,Server> SERVERS = 
            Collections.synchronizedMap(new HashMap<String,Server>());
     private static final Map<String,Server> SERVERS_BY_LOCATION = 
            Collections.synchronizedMap(new HashMap<String,Server>());
    private static List<Server> ALL_SERVERS = Collections.emptyList();
    
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
                if (server.getLocation().isEmpty()) {
                    LoggerFactory.getLogger(Server.class)
                            .error("Server {} does not specify a location!  DISABLING!", server.getName());
                    disabled.add(server.getName());
                } else {
                    Server previous = SERVERS_BY_LOCATION.put(server.getLocation(),server);
                    if (previous!=null) {
                        LoggerFactory.getLogger(Server.class)
                                .warn("WARNING! Multiple Servers have the same location: "+server.getLocation());
                    }
                }
            }
            
            for (String disableName : disabled) {
                SERVERS.remove(disableName);
            }
            
            ALL_SERVERS = Collections.unmodifiableList(new ArrayList<Server>(SERVERS.values()));
            
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
                if (server.getIp().equals(Constants.IP)) {
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
                        .error("This Server: {} is UNKNOWN! Disabling all Services!", Constants.HOSTDOMAIN);
            }
        }
    }
    
    public static Server getServer(String name) {
        return SERVERS.get(name);
    }
    
    public static Server getServerForLocation(String location) {
        return SERVERS_BY_LOCATION.get(location);
    }
    
    public static Set<String> getServerLocations() {
        return SERVERS_BY_LOCATION.keySet();
    }
    
    private Server(String name) {
        this.name = name;
        this.host = "";
        this.ip = "";
        this.display = "";
        this.timezone = "";
        this.location = "";
        this.mutualSSL = false;
        this.sslCAPass = null;
        this.sslPass = null;
        this.sslPort = 0;
        this.sslGateway = null;
        this.gateway = DEFAULT_GATEWAY;
    }
     
    public static List<Server> allServers() {
        return ALL_SERVERS;
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
        if (ip == null || ip.isEmpty()) {
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
}
