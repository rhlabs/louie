/*
 * LouieDAO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.info;

import com.rhythm.louie.server.ServiceManager;

import java.util.ArrayList;
import java.util.List;

import com.rhythm.louie.DAO;
import com.rhythm.louie.server.Server;

import com.rhythm.louie.service.command.ArgType;
import com.rhythm.louie.service.command.PBCommand;
import com.rhythm.louie.service.command.PBParamType;
import com.rhythm.louie.service.Service;

import com.rhythm.louie.info.InfoProtos.*;
import com.rhythm.louie.server.ServiceProperties;

/**
 * @author cjohnson
 * Created: Oct 27, 2011 11:36:00 AM
 */
@DAO
public class InfoDAO implements InfoService {
    public InfoDAO() {
    }
    
    @Override
    public List<String> getAllServiceNames() throws Exception {
        return new ArrayList<>(ServiceManager.getServiceNames());
    }
    
    @Override
    public ServicePB getService(String name) throws Exception {
        Service service = ServiceManager.getService(name);
        return convertServiceToPB(service);
    }
    
    @Override
    public List<ServicePB> getAllServices() throws Exception {
        List<ServicePB> services = new ArrayList<>();
        for (Service service : ServiceManager.getServices()) {
            services.add(convertServiceToPB(service));
        }
        return services;
    }

    private ServicePB convertServiceToPB(Service service) throws Exception {
        if (service == null) {
            return null;
        }
        
        ServicePB.Builder builder = ServicePB.newBuilder()
                .setName(service.getServiceName())
                .setReserved(service.isReserved());
        
        for (PBCommand command : service.getCommands()) {
            if (command.isInternal()) {
                continue;
            }
            MethodPB.Builder method = MethodPB.newBuilder()
                    .setName(command.getCommandName())
                    .setDescription(command.getDescription())
                    .setReturntype(command.getReturnType())
                    .setDeprecated(command.isDeprecated())
                    .setReturnList(command.returnList());

            for (PBParamType param : command.getArguments() ) {
                method.clearParams();
                for (ArgType arg : param.getTypes()) {
                    method.addParamsBuilder()
                            .setName(arg.getName())
                            .setType(arg.getType());
                }
                builder.addMethods(method.build());
            }
        }
        return builder.build();
    }

    @Override
    public List<String> getServerLocations() throws Exception {
        return Server.getServerLocations();
    }

    @Override
    public List<ServerPB> getServers() throws Exception {
        return Server.allServerPbs();
    }
}
