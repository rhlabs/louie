/*
 * ExternalProperties.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
public class ExternalProperties {
    
    private static final String PROP_GIT_VERSION = "git_version";
    private static final String PROP_COMPILE_DATE = "compile_date";
    
    private static final String BUILD_VERSION = "Build-Version";
    private static final String BUILD_TIME = "Build-Time";
    
    
    private static final String FILE_PATH = "external_properties";
    
    private ExternalProperties() {
        processExternalProperties();
    }
    
    public static ExternalProperties getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ExternalProperties INSTANCE = new ExternalProperties();
    } 
    
    private String gitVersion = "Unknown";
    private String compileDate = "Unknown";
    private String version = "Unknown";
    private Map<String,String> gitVersions = new HashMap<String,String>();
    private Map<String,String> compileDates = new HashMap<String,String>();
    
    
    private void processExternalProperties_OLD(){
        Properties props = loadProperties();
        for (String key : props.stringPropertyNames()) { 
            String value = props.getProperty(key);
            
            if(key.equals(PROP_GIT_VERSION)) {
                this.gitVersion = value;
            } else if (key.equals(PROP_COMPILE_DATE)) {
                this.compileDate = value;
            }
        }
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        InputStream in = null;
        try {
            URL resourceURL = getClass().getResource(FILE_PATH);
            File file = null;
            if (resourceURL!=null) {
                 file = new File(resourceURL.toURI());
            }
            if (file==null || !file.exists()) {
                LoggerFactory.getLogger(ExternalProperties.class).warn("external_properties file not found");
                return props;
            }
            in = new FileInputStream(file);
            props.load(in);
        } catch (IOException ex) {
            LoggerFactory.getLogger(ExternalProperties.class).warn(null, ex);
        } catch (URISyntaxException ex) {
            LoggerFactory.getLogger(ExternalProperties.class).warn(null, ex);
        } finally {
            if (in!=null)  {
                try {
                    in.close();
                } catch (IOException ex) {
                    LoggerFactory.getLogger(ExternalProperties.class).warn(null, ex);
                }
            }
        }
        return props;
    }

    private void processExternalProperties() {
        String tmpVersion = getClass().getPackage().getImplementationVersion();
        if (tmpVersion!=null) {
            version = tmpVersion;
        }

        try {
            Enumeration<URL> resources = Constants.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                InputStream input = null;
                try {
                    input = resources.nextElement().openStream();
                    Manifest manifest = new Manifest(input);
                    Attributes attr = manifest.getMainAttributes();
                    String impl = attr.getValue("Implementation-Title");
                    if (impl != null) {
                        String buildVersion = attr.getValue(BUILD_VERSION);
                        if (buildVersion != null) {
                            gitVersions.put(impl,buildVersion);
                        }
                        String buildTime = attr.getValue(BUILD_TIME);
                        if (buildTime != null) {
                            compileDates.put(impl,buildTime);
                        }
                    }
                } finally {
                    if (input!=null) input.close();
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ExternalProperties.class).error("Error searching manifests", e);
        }
    }
    
    public Map<String,String> getGitVersionMap() {
        return gitVersions;
    }
    
    public Map<String,String> getCompileDateMap() {
        return compileDates;
    }
    
    @Deprecated
    public String getGitVersion() {
        return gitVersion;
    }

    @Deprecated
    public String getCompileDate() {
        return compileDate;
    }
    
    public String getVersion() {
        return version;
    }
    
}
