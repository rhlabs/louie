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
import java.util.*;

import com.google.protobuf.Descriptors;

/**
 *
 * @author eyasukoc
 */
public class JSMethodInfo extends MethodInfo{
//ya know, for javascript.
    
    //probably still need the pbtypeMap to denote built ins 
    //could be hashset instead? i know what package will be pre-pended to the PB name. it could be a single static field or something so that it's still configurable.
    protected static final Map<String, String> pbtypeMap;
    static {
        pbtypeMap = new HashMap<>();
        pbtypeMap.put("StringPB", "str");
        pbtypeMap.put("StringListPB", "list");
        pbtypeMap.put("DatePB", "int");
        pbtypeMap.put("DateListPB", "list");
        pbtypeMap.put("DateTimePB", "int");
        pbtypeMap.put("DateTimeListPB", "list");
        pbtypeMap.put("BoolPB", "bool");
        pbtypeMap.put("BoolListPB", "list");
        pbtypeMap.put("IntPB", "int");
        pbtypeMap.put("IntListPB", "list");
        pbtypeMap.put("UIntPB", "int");
        pbtypeMap.put("UIntListPB", "list");
        pbtypeMap.put("LongPB", "long");
        pbtypeMap.put("LongListPB", "list");
        pbtypeMap.put("ULongPB", "long");
        pbtypeMap.put("ULongListPB", "list");
        pbtypeMap.put("FloatPB", "float");
        pbtypeMap.put("FloatListPB", "list");
        pbtypeMap.put("DoublePB", "float");
        pbtypeMap.put("DoubleListPB", "list");
    }
    
    public JSMethodInfo(Method method) throws Exception {
        super(method);
    }
    
    //I think i can get them from the loadPBType logic, it seems to be handling shit via the .proto name?
    public List<ProtoInfo> getProtos() throws NoSuchMethodException, IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException, Exception {
        //so get the return type, and the parameters. done
        // i really hate that there is redundancy between ProtoInfo and how the data is retrieved later. 
        //just create it this way for now, optimize and refactor later.
        
        //get inputs via the info in method.getParameterTypes()
        //break apart similar to how it's done in getImports
        Map<String, ProtoInfo> protos = new HashMap<>();
        for (Class<?> c : method.getParameterTypes()) {
            ProtoInfo pInfo = loadProtoInfo(c);
            String protoKey = pInfo.getProtoFile();
            if (protos.containsKey(protoKey)) {
                protos.get(protoKey).merge(pInfo);
            } else {
                protos.put(protoKey, pInfo);
            }
        }
        
//        System.out.println("js getProtos return type: " + getBaseReturnType());
        String returnClass = getBaseReturnType();
        StringBuilder bldr = new StringBuilder(returnClass);
        int lastDotIndex = returnClass.lastIndexOf(".");
        bldr.replace(lastDotIndex, lastDotIndex + 1, "$" );
        ProtoInfo pInfo = loadProto(bldr.toString());
        String protoKey = pInfo.getProtoFile();
        if (protos.containsKey(protoKey)) {
            protos.get(protoKey).merge(pInfo);
        } else {
            protos.put(protoKey, pInfo);
        }
        
        //get outputs (Ideally get filename, and full package-included PB name)
        return new ArrayList<>(protos.values());
    }
    
    @Deprecated //in favor of getProtos returning the ProtoInfo objects
    public List<String> getPBs() {
        return null;
    }
    
    // So, collection of pbs and proto files can probably be done simultaneously in all cases
    // need to locate return type, which is already mostly there.
    // then need to figure out how to grab the param types? The python auto-generator does not deal in this, because Python will handle the type stuff behind the scene, so only the param name is visible w/in the auto py clients
    // perhaps the base methodinfo? method.getParameters. then ParamInfo.getPbType i think...
    
    // distribution of responsibility:
    // jsmethodinfo supplies lists of strings, via getProtos and getPBs, each of which are internally managed as sets?
    // jsmethodinfo should also track or be able to indicate whether a service requires the base datatypes.js for core datatypes (for inclusion into require.js part as well)
    //   i think this would look like a dedicated getBasePBs which returns another list, and a non-empty list also indicates that the datatypes.js is necessary.
    

    //Critically, the arguments are managed separately via the core methodinfo and it's construction of ParamInfo Objects.
    // Tentatively I think I should modify or extend ParamInfo to lookup the proto file name and store it as well?
    // Requires loading class, then getting declared method on it etc.
    // To accomplish w/out tarnishing MethodInfo, I could override getParameters() here and recreate the Params list w/ the class loading and all that. That's heavy and redundant though. 
    // JUST MODIFY MethodInfo to DO THIS at construction time OH FUCKING WELL. 
    // This also means I can do away with ProtoInfo and just use the ParamInfo and store the proto file name in there? That sucks too and goes against the object hierarchy i was intending.
    // ParamInfo can track the proto file it came from, and I can reconstruct the shit here in the JSMethodInfo prior to handing off to JSServiceInfo (or JSServiceInfo can do it) but the point is that it can be rebuilt.
    
    //FILE SHUFFLING MACHINERY.... 
    // We would be generating per-service files.
    // More static things to be brought in include:
    // louieclient.js
    // datatypes.js
    // all required protos
    // Everything would have to be tracked in an assembly again, similar to other languages.
    
   
    public String getReturnType() throws Exception{
        String classname = getBaseReturnType();
        StringBuilder bldr = new StringBuilder(classname);
        int lastDotIndex = classname.lastIndexOf(".");
        bldr.replace(lastDotIndex, lastDotIndex + 1, "$" );
        classname = bldr.toString();
//        System.out.println("return type class name: " + classname);
        String retType = loadPBType(classname);
//        System.out.println("JSMethodInfo getReturnType returns: " + retType);
        return retType;
//        return loadPBType(classname);
    }
        
    public String getType(String pbType) {
        if (!pbtypeMap.containsKey(pbType)) {
            return "";
        }
        String retType = pbtypeMap.get(pbType);
//        System.out.println("JSMethodInfo getType returns: " + retType);
        return retType;
//        return pbtypeMap.get(pbType);
    }
        
    private ProtoInfo loadProto(String classname) throws Exception {
        //this method is actually breaking apart 
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> c = cl.loadClass(classname);
        
        return loadProtoInfo(c);
    }
    
    private ProtoInfo loadProtoInfo(Class<?> c) throws NoSuchMethodException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String pbName = c.getSimpleName(); //this is the basic PB name, which becomes the field name, as well as appended to the package for loading from builder
        Method staticMethod = c.getDeclaredMethod("getDescriptor");
        Descriptors.Descriptor desc = (Descriptors.Descriptor) staticMethod.invoke(null, (Object[]) null);
        String packageName = desc.getFullName();
        String file = desc.getFile().getName();
        ProtoInfo pInfo = new ProtoInfo(file, pbName, packageName);
        return pInfo;
    }
    
    private String loadPBType(String classname) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> c = cl.loadClass(classname);
        Method staticMethod = c.getDeclaredMethod("getDescriptor");
        Descriptors.Descriptor desc = (Descriptors.Descriptor) staticMethod.invoke(null, (Object[]) null);
        String file = desc.getFile().getName();
//        System.out.println("descriptors.getFile().getName() returns " + file); //this could be a better proto file storage structure (just copy from base proto directory, preserving package names for clarity? as opposed to jamming all protos in one dir
        file = file.replaceAll("\\.proto", "_pb2");
        file = file.replaceAll("\\/", ".");
//        System.out.println("JSMethodInfo loadPBType returns: " + file);
        return file;
    }
    
    public String getImportType(Class<?> param) throws Exception {
        
        String type = param.toString();
        type = type.replace("class ", ""); 
        String retType = loadPBType(type);
//        System.out.println("JSMethodInfo getReturnType returns: " + retType);
        return retType;
//        return loadPBType(type);
    }
    
    public Set<String> getImports() throws Exception {
        //this method returns both the pb as well as the required file it came from....
        Set<String> imports = new LinkedHashSet<>();
        for (ParamInfo param : params) {
            if (isLouieDataType(param.getPbType())) {
                imports.add("from " + getImportType(param.getParam()) + " import " + param.getPbType());
            }
        }
        
        imports.add("from " + getReturnType() + " import " + getPbReturnType());  // could hack apart getBaseReturnType() i think.
        return imports;
    }
    
    public String getParamNameString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ParamInfo param : params) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param.getName());
        }
//        System.out.println("JSMethodInfo getParamNameString() returns: " + sb.toString());
        return sb.toString();
    }
    
}
