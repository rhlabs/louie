/*
 * PBRequest.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.request;

import com.rhythm.louie.request.data.DataType;
import com.rhythm.louie.request.data.Param;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rhythm.louie.generator.TypeUtils;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.RoutePB;
import com.rhythm.pb.RequestProtos.RoutePathPB;
import com.rhythm.pb.RequestProtos.SessionKey;

import com.rhythm.louie.service.command.PBParamType;

/**
 * @author cjohnson
 * Created: Jun 14, 2011 5:49:18 PM
 */
public class RequestContext {
    private final RequestHeaderPB header;
    private final RequestPB request;
    private final DataType dataType;
    private final RequestProperties props;

    private final List<Param> params;
    
    private String userAgent;
    
    private final PBParamType type;
    
    private IdentityPB identity;
    private SessionKey sessionKey = null;
    private RoutePB route;
    private boolean enableRouteUser = true;
    
    private List<RoutePathPB> destinations;
    
    public RequestContext(RequestHeaderPB header, RequestPB request,
            DataType dataType, RequestProperties props) {

        this.header = header;
        this.request = request;
        this.dataType = dataType;
        this.props = props;
        this.params = new ArrayList<>();
        
        List<String> convertedTypes = new ArrayList<>(request.getTypeCount());
        for (String param : request.getTypeList()) {
            if (param.startsWith("rh.pb")) {
                param = TypeUtils.legacyConvert(param);
            }
            convertedTypes.add(param);
        }
        
        type = PBParamType.typeForNames(convertedTypes);
        destinations = null;
    }
    
    public void enableRouteUser(boolean enable) {
        enableRouteUser = enable;
    }
    
    public boolean isRouteUserEnabled() {
        return enableRouteUser;
    }
    
    /**
     * Returns the effective user for the request.  This user may be different
     * than the actual user making the request, in the case of routing
     * @return 
     */
    @SuppressWarnings("deprecation")
    public String getWho() {
        if (getRequest().hasRouteUser() && !getRequest().getRouteUser().isEmpty()) {
            return getRequest().getRouteUser();
        } else {
            return getRequester();
        }
    }
    
    @SuppressWarnings("deprecation")
    public String getRequester() {
        String who;
        if (identity!=null) {
            who = identity.getUser();
        } else {
            who = getHeader().getUser();
        }
        
        if (who == null || who.isEmpty()) {
            who = "unknown";
        }
        
        return who;
    }
    
    public String getProgram() {
        String program;
        if (identity!=null) {
            program = identity.getProgram();
            if (identity.hasProgramVersion() && !identity.getProgramVersion().isEmpty()) {
                program+="-"+identity.getProgramVersion();
            }
        } else {
            program = getUserAgent();
        }
        if (program!=null) {
            program = program.replaceAll("\\||\\/","-");
        }
        return program;
    }
    
    public String getLanguage() {
        String language;
        if (identity!=null) {
            language = identity.getLanguage();
            if (identity.hasLanguageVersion() && !identity.getLanguageVersion().isEmpty()) {
                language+="/"+identity.getLanguageVersion();
            }
        } else {
            language = getDataType().toString();
        }
        if (language!=null) {
            language = language.replaceAll("\\|","/");
        }
        return language;
    }
    
    public String getModule() {
        String module = getProgram();
        String useragent = getUserAgent();
        if (useragent!=null && !useragent.isEmpty() && !useragent.equals(module)) {
            module +="/"+useragent;
        }
        if (module==null) {
            module = "Unknown";
        }
        return module;
    }
    
    public IdentityPB getIdentity() {
        return identity;
    }
    
    public void setIdentity(IdentityPB identity) {
        this.identity = identity;
    }
    
    public void setSessionKey(SessionKey key) {
        this.sessionKey = key;
    }
    
    public String getSessionKey() {
        if (header.hasKey()) {
            SessionKey key = header.getKey();
            if (key.hasKey()) {
                return key.getKey();
            }
        } else if (sessionKey != null) {
            return sessionKey.getKey();
        }
        return "";
    }
    
    public RequestHeaderPB getHeader() {
        return header;
    }

    public RequestPB getRequest() {
        return request;
    }
    
    public List<Param> getParams() {
        return params;
    }
    
    public void addParam(Param param) {
        params.add(param);
    }

    @SuppressWarnings("deprecation")
    public void readPBParams(InputStream input) throws Exception {
        int paramCount = 1;
        if (request.hasParamCount() && request.getParamCount()>0) {
            paramCount = request.getParamCount();
        }
        for (int d = 0; d < paramCount; d++) {
            addParam(Param.readPBParam(input, request.getTypeCount()));
        }
    }
    
    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        String ua = userAgent;
        if (ua==null) {
            if (header.hasAgent() && !header.getAgent().isEmpty()) {
                ua = header.getAgent();
            }
        }
        if (ua!=null) {
            ua = ua.replaceAll("\\||\\/","-");
        }
        return ua;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the dataType
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * @return the types
     */
    public PBParamType getType() {
        return type;
    }
    
    /**
     * @return the localPort
     */
    public RequestProperties getRequestProperties() {
        return props;
    }
    
    /**
     * @return the route
     */
    public RoutePB getRoute() {
        return route;
    }
    
    /**
     * @param route the route of the current Request
     */
    public void setRoute(RoutePB route) {
        this.route=route;
    }

    /**
     * Adds a RoutePath to the list of visited destinations
     * 
     * @param routes
     */
    synchronized public void addDestinationRoutes(List<RoutePathPB> routes) {
        if (destinations == null) {
            destinations = new ArrayList<>();
        }
        destinations.addAll(routes);
    }
    
    /**
     * List of Routes that this request visited
     * @return 
     */
    synchronized public List<RoutePathPB> getDesinationRoutes() {
        if (destinations == null) {
            return Collections.emptyList();
        }
        return destinations;
    }
}
