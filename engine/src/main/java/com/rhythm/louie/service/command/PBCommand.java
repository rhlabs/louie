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
package com.rhythm.louie.service.command;

import java.util.List;

import com.rhythm.louie.request.RequestContext;
import com.rhythm.louie.request.data.Result;

/**
 * @author cjohnson
 * Created: Mar 17, 2011 1:53:01 PM
 */
public interface PBCommand {
    public List<PBParamType> getArguments();
    public String getReturnType();
    public String getCommandName();
    public Result execute(RequestContext request) throws Exception;
    public String getDescription();
    public boolean isDeprecated();
    public boolean returnList();
    public boolean isUpdate();
    public String getGroup();
    public int getGroupOrder();
    public boolean isInternal();
    public boolean isStreaming();
    public boolean adminAccess();
    public boolean restrictedAccess();
}
