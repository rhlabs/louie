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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.protobuf.Descriptors;

/**
 *
 * @author sfong
 */
public class ParamInfo {

    private final Class<?> param;
    private final String name;
    private final String pkg;

    public ParamInfo(Class<?> param, String name) throws NoSuchMethodException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.param = param;
        this.name = name;
        Method staticMethod = param.getDeclaredMethod("getDescriptor");
        Descriptors.Descriptor desc = (Descriptors.Descriptor) staticMethod.invoke(null, (Object[]) null);
        pkg = desc.getFullName();
    }

    public Class<?> getParam() {
        return param;
    }
    
    public String getName() {
        return name;
    }

    public String getPbType() {
        return param.getSimpleName();
    }
    
    public String getFullName() {
        return pkg;
    }
}
