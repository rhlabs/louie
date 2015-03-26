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

import java.util.*;

/**
 * An informational object for tracking used PBs from w/in a proto file. 
 * The ProtoInfo object is inteded strictly for use by the javascript machinery, 
 * where it is desirable to see all the pbs used by any needed proto file.
 * @author eyasukoc
 */
public class ProtoInfo {
    
    private final Set<PBInfo> pbs;
    private final String protoFile; //rh/pb/swagr/swagr.proto
    private final String protoName; //swagr
    private final String pkg; //rh.pb.swagr
    
    public ProtoInfo(String protoFile, String pb, String pkg) {
        pbs = new HashSet<>();
        pbs.add(new PBInfo(pb,pkg));
        this.protoFile = protoFile;
        this.pkg = pkg;
        this.protoName = shortenProto(protoFile);
    }
    
    public void merge(ProtoInfo newInfo) {
        pbs.addAll(newInfo.getPBs());
    }
    
    private String shortenProto(String protoFile) {
        //strip <pkg>.<protoname>.proto to <protoname>
        StringBuilder bldr = new StringBuilder(protoFile);
        int dotIndex = bldr.lastIndexOf(".");
        bldr = bldr.replace(dotIndex, bldr.length(), "");
        dotIndex = bldr.lastIndexOf("/");
        bldr = bldr.replace(0, dotIndex+1, "");
        return bldr.toString();
    }
    
    public void addPB(String pb, String pkg) {
        pbs.add(new PBInfo(pb, pkg));
    }
    
    public List<PBInfo> getPBs() {
        return new ArrayList<>(pbs);
    }
    
    public String getName() {
        return protoName;
    }
    
    public String getProtoFile() {
        return protoFile;
    }
    
    public class PBInfo {
        
        private final String name;
        private final String fullPackageName;
        
        public PBInfo(String shortName, String pkgName) {
            this.name = shortName;
            this.fullPackageName = pkgName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getFullName() {
            return fullPackageName;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + Objects.hashCode(this.fullPackageName);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PBInfo other = (PBInfo) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.fullPackageName, other.fullPackageName)) {
                return false;
            }
            return true;
        }
        
    }
    
}
