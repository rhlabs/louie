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
package com.rhythm.louie.clientgen;

import java.lang.reflect.Method;
import java.util.*;

import com.rhythm.louie.generator.ProcessorUtils;
import com.rhythm.louie.process.ServiceCall;

/**
 *
 * @author sfong
 */
public class MethodInfo implements Comparable<MethodInfo> {
    protected final Method method;
    protected final List<ParamInfo> params = new ArrayList<ParamInfo>();
    protected final ServiceCall serviceCall;
    private final String description;
    
    protected static final Map<String, String> pbargMap;
    static {
        pbargMap = new HashMap<String, String>();
        pbargMap.put("StringPB", "value");
        pbargMap.put("StringListPB", "values");
        pbargMap.put("DatePB", "time");
        pbargMap.put("DateListPB", "times");
        pbargMap.put("DateTimePB", "time");
        pbargMap.put("DateTimeListPB", "times");
        pbargMap.put("BoolPB", "value");
        pbargMap.put("BoolListPB", "values");
        pbargMap.put("IntPB", "value");
        pbargMap.put("IntListPB", "values");
        pbargMap.put("UIntPB", "value");
        pbargMap.put("UIntListPB", "values");
        pbargMap.put("LongPB", "value");
        pbargMap.put("LongListPB", "values");
        pbargMap.put("ULongPB", "value");
        pbargMap.put("ULongListPB", "values");
        pbargMap.put("FloatPB", "value");
        pbargMap.put("FloatListPB", "values");
        pbargMap.put("DoublePB", "value");
        pbargMap.put("DoubleListPB", "values");
    }

    public MethodInfo(Method method) {
        this.method = method;
        serviceCall = method.getAnnotation(ServiceCall.class);
        int i = 0;
        for (Class<?> c : method.getParameterTypes()) {
            params.add(new ParamInfo(c, serviceCall.args()[i]));
            i++;
        }
        description = ProcessorUtils.extractDescriptionFromJavadoc(serviceCall.javadoc());
    }
    
    public boolean isDeprecated() throws Exception {
        return method.getAnnotation(Deprecated.class) != null;
    }
    
    public boolean returnsList() {
        return method.getGenericReturnType().toString().contains("List<");
    }

    public List<ParamInfo> getParameters() {
        return params;
    }

    public String getBaseReturnType() {
        String baseReturnType = method.getGenericReturnType().toString();
        baseReturnType = baseReturnType.replaceFirst("class ", "");
        baseReturnType = baseReturnType.replaceFirst("(?:java\\.util\\.)?List<(.*)>","$1");
        baseReturnType = baseReturnType.replaceAll("\\$", ".");
        return baseReturnType;
    }
    
    public String getPbReturnType() {
        String name = getBaseReturnType();
        String[] split = name.split("\\.");
        if (split.length == 0) {
            return name;
        }
        return split[split.length - 1];
    }

    public String getPbArg(String pbName) {
        if (!pbargMap.containsKey(pbName)) {
            return "";
        }
        return pbargMap.get(pbName);
    }

    public boolean isLouieDataType(String type) {
        return pbargMap.containsKey(type);
    }
    
    public String getName() {
        return method.getName();
    }
    
    public boolean hasParams() {
        return !params.isEmpty();
    }
        
    public String getDescription() {
        return description;
    }
    
    public String getJavadoc() {
        return serviceCall.javadoc();
    }

    @Override
    public int compareTo(MethodInfo o) {
        return method.getName().compareTo(o.method.getName());
    }
}
