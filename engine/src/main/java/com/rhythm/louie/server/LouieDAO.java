/*
 * LouieDAO.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import com.rhythm.louie.ServiceManager;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContext;
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
public class LouieDAO implements LouieClient {
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
    
    @Override
    public String echoTest(String value, Integer sleep) throws Exception {
        String key = RequestContext.getRequest().getSessionKey();
        
        Thread.sleep(sleep);
        
        if (!RequestContext.getRequest().getSessionKey().equals(key)) {
            throw new Exception("Context does not work!!!");
        }
        return value;
    }

    @Override
    public String loopTest(List<String> hosts) throws Exception {
        if (hosts.isEmpty()) {
            return "Done";
        }
        List<String> args = new ArrayList<String>(hosts);
        String host = args.remove(0);
        LouieClient client = new LouieServiceClient(new LouieFacade(
                        LouieConnectionFactory.getConnection(host)));
        return client.loopTest(args);
    }
}
