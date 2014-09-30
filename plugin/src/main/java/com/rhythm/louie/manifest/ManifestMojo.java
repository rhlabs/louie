/*
 * ManifestMojo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.project.MavenProject;

/**
 * Update the manifest with the Build Version and Time
 * 
 * @author cjohnson
 */
@Mojo(name = "manifest", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ManifestMojo extends AbstractMojo {
    /**
      * The current project representation.
      */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The format of the timestamp to store
     */
    @Parameter(property="manifest.dateformat", defaultValue = "MMM dd, yyyy HH:mm:ss z", required = true)
    private String dateformat;
    
    /**
     * The process to run to retrieve the version string
     */
    @Parameter(property="manifest.versionexec", defaultValue = "git describe --long --always")
    private String versionexec;
    
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Gathering Build Properties");
        
        // store the current time                       
        String currentBuildTime = new SimpleDateFormat(dateformat).format(new Date());
        project.getProperties().put("build.time", currentBuildTime); 
        
        // retrieve the version through the exec
        String versionText = null;
        try {
            versionText = retrieveVersion();
        } catch (IOException e) {
            getLog().error(e.toString());
        }
        
        if (versionText != null && !versionText.isEmpty()) {
            project.getProperties().put("build.version", versionText);
        }
    }
    
    private String retrieveVersion() throws IOException {
        getLog().info("Retrieving version through: "+versionexec);
        
        Process p = Runtime.getRuntime().exec(versionexec);
        
        try (BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))
            ) {
            String line;
            
            //ERROR HANDLING
            StringBuilder errorText = new StringBuilder();
            while ((line = error.readLine()) != null) {
                if (errorText.length()!=0) {
                    errorText.append(" ");
                }
                errorText.append(line);
            }
            if (errorText.length()!=0) {
                getLog().warn(errorText);
            }
            
            //INPUT HANDLING
            StringBuilder version = new StringBuilder();
            while ((line = input.readLine()) != null) {
                if (version.length()!=0) {
                    version.append(" ");
                }
                version.append(line);
            }
            return version.toString();
        }
    }
}
