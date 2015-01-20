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
package com.rhythm.louie.generator;

import java.util.*;


/**
 *
 * @author cjohnson
 */
public class ProcessorUtils {
    public static final Set<String> IMPORT_EXCLUDES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                "com.rhythm.pb.command.Service"
                )));
    public static final Set<String> IMPORT_INCLUDES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                "java.util.List"
                )));
    
    public static Set<String> getImports(ServiceInfo info, Collection<String> imports) {
        Set<String> empty = Collections.emptySet();
        return getImports(info,imports,empty);
    }
    
    public static Set<String> getImports(ServiceInfo info, Collection<String> imports, Collection<String> excludes) {
        Set<String> doneImports = new HashSet<String>();
        doneImports.addAll(IMPORT_EXCLUDES);
        doneImports.addAll(excludes);
        
        Set<String> todoImports = new TreeSet<String>();
        todoImports.addAll(IMPORT_INCLUDES);
        todoImports.addAll(info.getImports());
        todoImports.addAll(imports);
        
        todoImports.removeAll(doneImports);
        
        return todoImports;
    }
    
    public static String extractDescriptionFromJavadoc(String javadoc) {
        StringBuilder desc = new StringBuilder();
        for (String line : javadoc.split("\n")) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("@")) {
                if (desc.length() != 0) {
                    desc.append("\n");
                }
                desc.append(line);
            }
        }
        return desc.toString();
    }
}
