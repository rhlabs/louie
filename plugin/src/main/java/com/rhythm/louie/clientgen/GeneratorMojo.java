/*
 * MethodInfo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.clientgen;

import com.rhythm.louie.Classes;
import com.rhythm.louie.Disabled;
import com.rhythm.louie.Internal;
import com.rhythm.louie.process.ServiceCall;
import com.rhythm.louie.process.ServiceHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

/**
 * Executes the LoUIE client generator
 * @author eyasukoc
 */
@Mojo(name="generate", requiresDependencyResolution = ResolutionScope.TEST)
public class GeneratorMojo extends AbstractMojo{
    /**
      * The current project representation.
      */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;
    
    /**
     * Hostname
     */
    @Parameter
    private String hostname = "";
    
    /**
     * Application server gateway
     */
    @Parameter
    private String gateway = "";
    
    /**
     * python client package name
     */
    @Parameter
    private String pythonpackage = "";
    
    /**
     * output directory for generated python
     */
    @Parameter(defaultValue="target/generated-sources/python")
    private String pythondir = "";
    
    /**
     * A list of package prefixes to look for
     */
    @Parameter
    private List<String> prefixes;
    
    /**
     * A list of complete PACKAGES to blacklist (not just some prefix)
     */
    @Parameter
    private List<String> blacklist;
    
    /**
     * A list of fully qualified CLASSES to whitelist
     */
    @Parameter
    private List<String> whitelist;
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing LoUIE Service Client Generator");
        List<URL> urls = new ArrayList<>();
        try {
            String outputDir = project.getBuild().getOutputDirectory();
            List<Artifact> artifacts = project.getCompileArtifacts();
            urls.add(new File(outputDir).toURI().toURL());
            for (Artifact a : artifacts) {
                urls.add(a.getFile().toURI().toURL());
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        
        // TODO exclude all of com.rhythm.louie once we rename other rh based services
        if (blacklist == null) blacklist = new ArrayList<>();
        blacklist.add("com.rhythm.louie");
        blacklist.add("com.rhythm.louie.servlet");
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
        blacklist.add("com.rhythm.louie.topology");
        blacklist.add("com.rhythm.louie.pb");
        blacklist.add("com.rhythm.louie.pb.command");
        blacklist.add("com.rhythm.louie.pb.data");
        
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(cl);
        
        //sanitize python output dir
        if (pythondir.startsWith("/")) pythondir = pythondir.substring(1);
        if (!pythondir.endsWith("/")) pythondir = pythondir + "/";
        
        exec(hostname, gateway, prefixes, whitelist, blacklist, pythonpackage);
        
    }
    
    //////////////////////////// FROM GENERATOR ////////////////////////////////
    
    private static final String PYTHON_TEMPLATE = "templates/python/ServiceClient.vm";
    private static final String PYTHON_CLIENT_MODULE = "client.py";
    
    protected static final Set<String> LOUIE_SERVICES = ImmutableSet.of("auth","info","test","jmstest");
    
    public void exec(String host, String gateway, List<String> prefix, List<String> whitelist, List<String> blacklist, String pypackage) {
        
        List<Class<?>> services;
        
        // sanitize pypath
        pypackage = pypackage.replaceAll("\\.", "\\/");
        if (pypackage.startsWith("/")) pypackage = pypackage.substring(1);
        if (!pypackage.endsWith("/")) pypackage = pypackage + "/";
        
        try {
            services = Classes.getAnnotatedSpecialized(prefix, ServiceHandler.class, whitelist, blacklist);
        } catch (IOException e) {
            LoggerFactory.getLogger(GeneratorMojo.class).error("Error Finding Service Handlers", e);
            return;
        }
        for (Class<?> service : services) {
            List<MethodInfo> pythonMethods = new ArrayList<>();
            try {
                for (Method method : service.getMethods()) {
                    if (!Modifier.isStatic(method.getModifiers())
                                && Modifier.isPublic(method.getModifiers())
                                && method.isAnnotationPresent(ServiceCall.class)
                                && !method.isAnnotationPresent(Internal.class)
                                && !method.isAnnotationPresent(Disabled.class)) {
                        pythonMethods.add(new PythonMethodInfo(method));
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(GeneratorMojo.class).error("Error Processing Service Methods", e);
            }
            
            // Python
            try {
                Collections.sort(pythonMethods);
                ServiceInfo info = new ServiceInfo(service, host, gateway, pythonMethods);
                generatePython(info, pypackage, project.getBasedir().toString(),pythondir);
            } catch (Exception e) {
                LoggerFactory.getLogger(GeneratorMojo.class).error("Error Generating Python Clients", e);
            }
        }
    }
    
    protected void printServiceInfo(ServiceInfo info) {
        getLog().info(info.getInputFile()+ " "+info.getServiceName());
    }
    
    String initTemplate = "__version__ = '${project.version}'";
    protected void generatePython(ServiceInfo info, String pypath, String basedir, String pydir) throws Exception {
        printServiceInfo(info);
        String serviceName = info.getServiceName().toLowerCase();
        
        String pypackage = pypath.replaceAll("\\/","\\."); //lame that i'm converting back
        if (pypackage.length() == 1) pypackage = "";
        
        StringBuilder output = new StringBuilder();
        output.append(basedir).append("/");
        output.append(pydir);
        //special case louie internal services
        if (LOUIE_SERVICES.contains(serviceName)) {
            output.append("louie/");
            pypackage = "louie.";
        } else {
            output.append(pypath);
        }
        output.append(serviceName).append("/");
        Path file = Paths.get(output.toString(), "__init__.py");
        output.append(PYTHON_CLIENT_MODULE);
        processTemplate(info, PYTHON_TEMPLATE, output.toString(), pypackage);
        
        //generate some __init__ files
        try {
            Files.write(file,initTemplate.getBytes());
        } catch (FileAlreadyExistsException ex) {
        } catch (IOException ex) {
            System.out.println("Failed to create __init__.py file in " + output);
        }
    }
    
    protected void processTemplate(ServiceInfo info, String template, String output, String pkg) throws Exception {
        Properties props = new Properties();
        URL url = GeneratorMojo.class.getClassLoader().getResource("config/velocity.properties");
        try (InputStream is = url.openStream()) {
            props.load(is);
        }

        VelocityEngine ve = new VelocityEngine(props);
        ve.init();

        VelocityContext vc = new VelocityContext();
        vc.put("info", info);
        vc.put("baseName", info.getBaseName());
        vc.put("serviceName", info.getServiceName());
        vc.put("package", pkg);

        Template vt = ve.getTemplate(template);
        File f = new File(output);
        f.getParentFile().mkdirs();
        try (Writer writer = new PrintWriter(f)) {
            vt.merge(vc, writer);
        }
    }
    
}
