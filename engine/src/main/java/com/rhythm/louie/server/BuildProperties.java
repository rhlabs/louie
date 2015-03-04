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
package com.rhythm.louie.server;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
public class BuildProperties implements Comparable<BuildProperties> {
    private static final String BUILD_VERSION = "Build-Version";
    private static final String BUILD_TIME = "Build-Time";
    private static final String IMP_TITLE = "Implementation-Title";
    private static final String IMP_VERSION = "Implementation-Version";
    
    private static final Map<String,BuildProperties> props = new ConcurrentHashMap<>();
    private static boolean scanned = false;
    private static List<BuildProperties> sorted = null;
    private static List<BuildProperties> serviceSorted = null;
    
    private final String name;
    private String version = "Unknown";
    private String buildVersion = "";
    private String buildTime = "";
    
    private BuildProperties(String name) {
        this.name = name;
    }
    
    public static synchronized Collection<BuildProperties> getAllBuildProperties() {
        if (!scanned) {
            processExternalProperties();
        }
        if (sorted==null) {
            sorted = new ArrayList<>(props.values());
            Collections.sort(sorted);
        }
        return sorted;
    }
    
    public static synchronized Collection<BuildProperties> getServiceBuildProperties() {
        if (!scanned) {
            processExternalProperties();
        }
        if (serviceSorted==null) {
            serviceSorted = new ArrayList<>();
            for (BuildProperties build : props.values()) {
                if (build.isServiceBuild()) {
                    serviceSorted.add(build);
                }
            }
            Collections.sort(serviceSorted);
        }
        return serviceSorted;
    }
    
    synchronized private static void processExternalProperties() {
        if (scanned) {
            return;
        }
        scanned = true;
        try {
            Enumeration<URL> resources = LocalConstants.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try (InputStream input = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(input);
                    Attributes attr = manifest.getMainAttributes();
                    String impl = attr.getValue(IMP_TITLE);
                    if (impl != null) {
                        BuildProperties prop = new BuildProperties(impl);
                        props.put(impl, prop);
                        
                        String version = attr.getValue(IMP_VERSION);
                        if (version != null) {
                            prop.version = version;
                        }
                        
                        String buildVersion = attr.getValue(BUILD_VERSION);
                        if (buildVersion != null) {
                            prop.buildVersion = buildVersion;
                        }
                        
                        String buildTime = attr.getValue(BUILD_TIME);
                        if (buildTime != null) {
                            prop.buildTime = buildTime;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(BuildProperties.class).error("Error searching manifests", e);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getBuildVersion() {
        return buildVersion;
    }

    public String getBuildTime() {
        return buildTime;
    }
    
    public String getVersion() {
        return version;
    }

    public String getBuildString() {
        if (buildTime.isEmpty() && buildVersion.isEmpty()) {
            return "";
        }
        return "("+buildVersion+
                (!buildTime.isEmpty() && !buildVersion.isEmpty()?" ":"")
                +buildTime+")";
    }
    
    public boolean isServiceBuild() {
        // Need a better way to determine this at some point
        return !buildTime.isEmpty();
    }
    
    @Override
    public int compareTo(BuildProperties o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
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
        final BuildProperties other = (BuildProperties) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
}
