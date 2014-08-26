/*
 * MethodInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Executes com.rhythm.louie.serviceclient.Generator.main()
 * @author eyasukoc
 * @goal generator
 * @requiresDependencyResolution test
 */
public class GeneratorMojo extends AbstractMojo{
    /**
      * The current project representation.
      * @parameter
      * default-value="${project}"
      * @readonly
      * @required
      */
    private MavenProject project;
    /**
     * Hostname
     * @parameter
     * property="hostname"
     */
    private String hostname = "";
    /**
     * Application server gateway
     * @parameter
     * property="gateway"
     */
    private String gateway = "";
    
    /**
     * A list of package prefixes to look for
     * @parameter
     */
    private List<String> prefixes;
    
    /**
     * A list of complete PACKAGES to blacklist (not just some prefix)
     * @parameter
     */
    private List<String> blacklist;
    
    /**
     * A list of fully qualified CLASSES to whitelist
     * @parameter
     */
    private List<String> whitelist;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing LoUIE Service Client Generator");
        List<URL> urls = new ArrayList<URL>();
        try {
            String outputDir = project.getBuild().getOutputDirectory();
            List<Artifact> artifacts = project.getCompileArtifacts();
            urls.add(new File(outputDir).toURI().toURL());
//            System.out.println(outputDir);
            for (Artifact a : artifacts) {
//                System.out.println(a.getFile().getAbsolutePath());
                urls.add(a.getFile().toURI().toURL());
            }
           
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        
        if (blacklist == null) blacklist = new ArrayList<String>();
        blacklist.add("com.rhythm.louie");
        blacklist.add("com.rhythm.louie.servlet");
        blacklist.add("com.rhythm.louie.auth");
        blacklist.add("com.rhythm.louie.client.connection");
        blacklist.add("com.rhythm.louie.connection");
        blacklist.add("com.rhythm.louie.email");
        blacklist.add("com.rhythm.louie.exception");
        blacklist.add("com.rhythm.louie.jdbc");
        blacklist.add("com.rhythm.louie.jdbc.query");
        blacklist.add("com.rhythm.louie.jms");
        blacklist.add("com.rhythm.louie.log");
        blacklist.add("com.rhythm.louie.request");
        blacklist.add("com.rhythm.louie.server");
        blacklist.add("com.rhythm.louie.servlets");
        blacklist.add("com.rhythm.louie.stream");
        blacklist.add("com.rhythm.louie.testservice");
        blacklist.add("com.rhythm.louie.topology");
        blacklist.add("com.rhythm.louie.pb");
        blacklist.add("com.rhythm.louie.pb.command");
        blacklist.add("com.rhythm.louie.pb.data");
        
        if (whitelist == null) whitelist = new ArrayList<String>();
        whitelist.add("com.rhythm.louie.auth.AuthServiceHandler");
        whitelist.add("com.rhythm.louie.testservice.TestServiceHandler");
        whitelist.add("com.rhythm.louie.server.LouieServiceHandler");
        
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(cl);
        com.rhythm.louie.plugins.Generator.exec(hostname, gateway, prefixes, whitelist, blacklist);
        
    }
    
}
