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
package com.rhythm.louie.pb;

import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author cjohnson
 * Created: Aug 18, 2011 1:59:19 PM
 */
public class PBParam {
    public static final PBParam EMPTY = new PBParam(0);
    
    private final ArrayList<Message> arguments;
    
    private PBParam(int size) {
        arguments = new ArrayList<>(size);
    }
    private PBParam(List<Message> messages) {
        arguments = new ArrayList<>(messages);
    }
    private PBParam(Message message) {
        arguments = new ArrayList<>(1);
        arguments.add(message);
    }
    
    public static PBParam createParam(List<Message> messages) {
        return new PBParam(messages);
    }
    
    public static PBParam createParam(Message... messages) {
        return new PBParam(Arrays.asList(messages));
    }
    
    public static PBParam singleParam(Message message) {
        return new PBParam(message);
    }
    
    public int count() {
        return arguments.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PBParam other = (PBParam) obj;
        if (this.arguments != other.arguments && (this.arguments == null || !this.arguments.equals(other.arguments))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.arguments != null ? this.arguments.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return Joiner.on(",").join(arguments);
    }
    
    public List<Message> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
}
