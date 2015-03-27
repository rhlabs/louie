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
package com.rhythm.louie.request.data;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * @author cjohnson
 * Created: Mar 2, 2011 11:02:26 AM
 */

public class Param {
    public static final Param EMPTY;
    static {
        List<Data> emptyData = Collections.emptyList();
        EMPTY = new Param(emptyData);
    }
    
    private final List<Data> args;
    private final Object[] parsed;

    private Param(final List<Data> data) {
        this.args = data;
        parsed = new Object[data.size()];
    }
    
    public List<Data> getArgs() {
        return Collections.unmodifiableList(args);
    }
    
    public Data getArg(int i) {
        return args.get(i);
    }
    
    public Object getParsedArg(int i) {
        return parsed[i];
    }
    
    public List<Object> getParsedArgs() {
        return Collections.unmodifiableList(Arrays.asList(parsed));
    }
    
    @Override
    public String toString() {
        return Joiner.on(",").join(args);
    }
    
    public final <A> A parseData(DataParser<A> parser,int index) throws Exception {
        A a = parser.parseData(args.get(index));
        parsed[index] = a;
        return a;
    }

    public static Param readPBParam(InputStream input, int argcount) throws Exception {
        if (argcount==0) {
            List<Data> args = Collections.emptyList();
            return new Param(args);
         } else if (argcount == 1) {
            return new Param(Collections.singletonList(Data.readPBData(input)));
        } else {
            List<Data> args = new ArrayList<>(argcount);
            for (int a = 0; a < argcount; a++) {
                args.add(Data.readPBData(input));
            }
            return new Param(args);
        }
    }
    
    public static Param buildJsonParam(List<String> jsonArgs) {
        List<Data> args = new ArrayList<>(jsonArgs.size());
        for (String arg : jsonArgs) {
            args.add(Data.newJsonData(arg));
        }
        return new Param(args);
    }
}