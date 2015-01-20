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
package com.rhythm.louie.service;

import java.util.Collection;

import com.rhythm.louie.jms.MessageHandler;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.service.command.PBCommand;

/**
 *
 * @author chris
 */
public interface Service {
    static final String PK_CHECK_ERROR = "Invalid request Parameters!";

    Result executeCommand(RequestContext req) throws Exception;

    Collection<PBCommand> getCommands();

    String getServiceName();
    
    Class<?> getServiceInterface();
    
    void initialize() throws Exception;
    
    void shutdown() throws Exception;
    
    MessageHandler getMessageHandler();
    
    boolean isReserved();
    
}
