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
        // set gateway again here just to be sure it gets set correctly on initialize
        UNKNOWN.gateway = Constants.DEFAULT_GATEWAY;
    }
    
    public static Server LOCAL = UNKNOWN;
    
    private static Server CENTRAL_AUTH = null;
    
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
    private String externalIp;
    private boolean centralAuth;
    private boolean secure = false;
    private int port;
    
    private static boolean liveIsDefaultGateway = false;
    private static String defaultGateway;
    private static String defaultTimezone;
    private static String defaultHost;
    private static String defaultDisplay;
    private static String defaultLocation;
    private static String defaultIP;
    private static int defaultPort;
    private static boolean defaultSecure;
    
    private ServerPB pb;
    

    protected static void processServers(List<Server> servers) {
        for (Server server : servers) {
            SERVERS.put(server.getName(), server);
            if (server.centralAuth) CENTRAL_AUTH = server;
        }
        
        if (SERVERS.isEmpty()) {                                        //shouldn't this be picking up what was in defaults?
            LoggerFactory.getLogger(Server.class)
                    .warn("No servers found, setting to localhost");
            Server server = new Server("LOCAL");
            server.host = "localhost";
            server.location="LOCAL";
            server.timezone=TimeZone.getDefault().getDisplayName();
            SERVERS.put("localhost", server);
            server.secure = defaultSecure;
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
                    .setGateway(server.getGateway())
                    .setIp(server.getIp())
                    .setExternalIp(server.getExternalIp())
                    .setPort(server.getPort())
                    .build();
            serverPBs.add(server.pb);
        }
        ALL_SERVER_PBS = Collections.unmodifiableList(serverPBs);
        SERVER_LOCATIONS = Collections.unmodifiableList(new ArrayList<>(SERVERS_BY_LOCATION.keySet()));

        //Print
        StringBuilder sb = new StringBuilder();
        sb.append("\n**** Louie Servers ****\n");
        for (Server server : Server.allServers()) {
            if (server.getIp().equals(LocalConstants.IP)) {
                Server.LOCAL = server;
                sb.append("*");
                if (liveIsDefaultGateway) {
                    server.setGateway(defaultGateway); //blindly reset it
                }
            }
            sb.append(server.getName()).append(":").append(server.getHostName());
            sb.append(" - ").append(server.getLocation());
            sb.append("\n");
        }
        LoggerFactory.getLogger(Server.class).info(sb.toString());

        if (Server.LOCAL==UNKNOWN) {
            LoggerFactory.getLogger(Server.class)
                    .error("This Server: {} is UNKNOWN! Disabling all Services!", LocalConstants.HOSTDOMAIN);
        }
        
    }
    
    protected static void setDefaultGateway(String gateway) { 
        defaultGateway = gateway;
    }
    
    protected static void setLiveGateway(String gateway) {
        defaultGateway = gateway;
        liveIsDefaultGateway = true; //that is such shit
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
    
    protected void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }
    
    protected void setSecure(boolean secure) {
        this.secure = secure;
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

    protected static void setDefaultPort(int port) {
        defaultPort = port;
    }
    
    protected static void setDefaultSecure(boolean secure) {
        defaultSecure = secure;
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
        this.gateway = defaultGateway;
        this.port = defaultPort;
        this.secure = defaultSecure;
        properties = new HashMap<>();
    }
     
    public static List<Server> allServers() {
        return ALL_SERVERS;
    }
    
    public static List<ServerPB> allServerPbs() {
        return ALL_SERVER_PBS;
    }
    
    public boolean isSecure() {
        return secure;
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
                InetAddress targetAddr = InetAddress.getByName(host);
                if (targetAddr.isLoopbackAddress()) {
                    targetAddr = InetAddress.getLocalHost();
                }
                ip = targetAddr.getHostAddress();
            } catch (UnknownHostException ex) {
                LoggerFactory.getLogger(Server.class)
                        .error("ERROR getting IP for " + host+" : "+ex.toString());
            }
        }
        return ip;
    }
    
    public String getExternalIp() {
        if (externalIp==null || externalIp.isEmpty()) {
            return getIp();
        }
        return externalIp;
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
    
    public String getUrl() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(isSecure()?"https":"http").append("://");
        sb.append(getExternalIp()).append(":").append(getPort());
        sb.append("/").append(getGateway());
        
        return sb.toString();
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
    
    public String printHtmlServer() {
        StringBuilder server = new StringBuilder();
        server.append("<b>host:</b>         ").append(getHostName()).append("<br/>\n");
        server.append("<b>location:</b>     ").append(getLocation()).append("<br/>\n");
        server.append("<b>display:</b>      ").append(getDisplay()).append("<br/>\n");
        server.append("<b>timezone:</b>     ").append(getTimezone()).append("<br/>\n");
        server.append("<b>gateway:</b>      ").append(getGateway()).append("<br/>\n");
        server.append("<b>port:</b>         ").append(getPort()).append("<br/>\n");
        server.append("<b>ip:</b>           ").append(getIp()).append("<br/>\n");
        server.append("<b>external_ip:</b>  ").append(getExternalIp()).append("<br/>\n");
        server.append("<b>central_auth:</b> ").append(centralAuth).append("<br/>\n");
        server.append("<b>secure:</b>       ").append(isSecure()).append("<br/>\n");
        
        return server.toString();
    }
    
}
