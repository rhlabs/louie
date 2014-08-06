/*
 * LouieDAO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import com.rhythm.louie.ServiceManager;

import java.util.ArrayList;
import java.util.List;

import com.rhythm.louie.process.DAO;

import com.rhythm.pb.command.ArgType;
import com.rhythm.pb.command.PBCommand;
import com.rhythm.pb.command.PBParamType;
import com.rhythm.pb.command.Service;
import com.rhythm.pb.louie.LouieProtos.ArgPB;
import com.rhythm.pb.louie.LouieProtos.CommandPB;
import com.rhythm.pb.louie.LouieProtos.ServicePB;


/**
 * @author cjohnson
 * Created: Oct 27, 2011 11:36:00 AM
 */
@DAO
public class LouieDAO implements LouieService {
    public LouieDAO() {
    }
    
    @Override
    public List<String> getAllServiceNames() throws Exception {
        return new ArrayList<String>(ServiceManager.getServiceNames());
    }
    
    @Override
    public ServicePB getService(String name) throws Exception {
        Service service = ServiceManager.getService(name);
        return convertServiceToPB(service);
    }
    
    @Override
    public List<ServicePB> getAllServices() throws Exception {
        List<ServicePB> services = new ArrayList<ServicePB>();
        for (Service service : ServiceManager.getServices()) {
            services.add(convertServiceToPB(service));
        }
        return services;
    }

    private ServicePB convertServiceToPB(Service service) throws Exception {
        ServicePB.Builder builder = ServicePB.newBuilder()
                .setName(service.getServiceName());
        
        for (PBCommand<?,?> command : service.getCommands()) {
            if (command.isPrivate()) {
                continue;
            }
            CommandPB.Builder commandBuilder = CommandPB.newBuilder()
                    .setName(command.getCommandName())
                    .setDescription(command.getDescription())
                    .setReturntype(command.getReturnType())
                    .setDeprecated(command.isDeprecated())
                    .setReturnList(command.returnList());

            for (PBParamType param : command.getArguments() ) {
                commandBuilder.clearArgs();
                for (ArgType arg : param.getTypes()) {
                    commandBuilder.addArgs(ArgPB.newBuilder()
                            .setName(arg.getName())
                            .setType(arg.getType())
                            .build());
                }
                builder.addCommands(commandBuilder.build());
            }
        }
        return builder.build();
    }
}
