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

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import com.google.common.collect.ImmutableSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.*;
import com.rhythm.louie.process.ServiceCall;

/**
 * Executes the Louie client generator
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
     * javascript client package name
     */
    @Parameter
    private String javascriptpackage = "";
    
    /**
     * output directory for generated python
     */
    @Parameter(defaultValue="target/generated-sources/python")
    private String pythondir = "";
    
    /**
     * output directory for generated javascript
     */
    @Parameter(defaultValue="target/generated-sources/javascript")
    private String jsdir = "";
    
    /**
     * An optional list of ServiceHandlers to generate Clients for.
     * **Does not replace anything that is auto-detected**
     */
    @Parameter
    private List<String> servicehandlers;
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing Louie Service Client Generator");
        final List<URL> urls = new ArrayList<>();
        try {
            String outputDir = project.getBuild().getOutputDirectory();
            List<Artifact> artifacts = project.getCompileArtifacts();
            urls.add(new File(outputDir).toURI().toURL());
            for (Artifact a : artifacts) {
                urls.add(a.getFile().toURI().toURL());
            }
        } catch (MalformedURLException ex) {
            LoggerFactory.getLogger(GeneratorMojo.class).error("Error loading artifact urls", ex);
        }
        
        AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run()  {
                ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
                Thread.currentThread().setContextClassLoader(cl);
                return cl;
            }
        });
        
        //sanitize python output dir
        if (pythondir.startsWith("/")) pythondir = pythondir.substring(1);
        if (!pythondir.endsWith("/")) pythondir = pythondir + "/";
        
        //sanitize python output dir
        if (jsdir.startsWith("/")) jsdir = jsdir.substring(1);
        if (!jsdir.endsWith("/")) jsdir = jsdir + "/";
        
        List<Class<?>> services = loadClasses(servicehandlers);
        
        process(hostname, gateway, pythonpackage, services);
    }
    
    //////////////////////////// FROM GENERATOR ////////////////////////////////
    
    private static final String JS_TEMPLATE = "templates/javascript/ServiceClient.vm";
    private static final String JS_CLIENT = "client.js";
    
    private static final String PYTHON_TEMPLATE = "templates/python/ServiceClient.vm";
    private static final String PYTHON_CLIENT_MODULE = "client.py";
    
    protected static final Set<String> LOUIE_SERVICES = ImmutableSet.of("auth","info","test","jmstest");
    
    public void process(String host, String gateway, String pypackage, List<Class<?>> services) {
        
        // sanitize pypath
        pypackage = pypackage.replaceAll("\\.", "\\/");
        if (pypackage.startsWith("/")) pypackage = pypackage.substring(1);
        if (!pypackage.endsWith("/")) pypackage = pypackage + "/";

        for (Class<?> service : services) {
            List<MethodInfo> pythonMethods = new ArrayList<>();
            List<JSMethodInfo> jsMethods = new ArrayList<>();
//            Set<String> protos = new HashSet<>();       //there's no reason to extract the protos or pbs here, it can be done by ServiceInfo itself, as is needed. SO just adjust JSMethodInfo to collect that stuff. And change MethodInfo to include it.
//            Set<String> pbs = new HashSet<>();
            try {
                for (Method method : service.getMethods()) {
                    if (!Modifier.isStatic(method.getModifiers())
                                && Modifier.isPublic(method.getModifiers())
                                && method.isAnnotationPresent(ServiceCall.class)
                                && !method.isAnnotationPresent(Internal.class)
                                && !method.isAnnotationPresent(Disabled.class)) {
                        pythonMethods.add(new PythonMethodInfo(method));
                        JSMethodInfo jsInfo = new JSMethodInfo(method);
//                        protos.addAll(jsInfo.getProtos());
//                        pbs.addAll(jsInfo.getPBs()); //this actually might be better for python anyway? tricky though because it's different for each language
                        jsMethods.add(jsInfo);
                        //each new JSMethodInfo should also be used to grab the proto file, as well as all pbs, into two hashsets
                        //then we carry around the two sets in the ServiceInfo
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(GeneratorMojo.class).error("Error Processing Service Methods", e);
            }
            
            // Python
            try {
                Collections.sort(pythonMethods, new Comparator<MethodInfo>() {
                    @Override
                    public int compare(MethodInfo o1, MethodInfo o2) {
                        return o1.getName().compareTo(o2.method.getName());
                    }
                });
                ServiceInfo info = new ServiceInfo(service, host, gateway, pythonMethods);
                generatePython(info, pypackage, project.getBasedir().toString(),pythondir);
            } catch (Exception e) {
                LoggerFactory.getLogger(GeneratorMojo.class).error("Error Generating Python Clients", e);
            }
            
            try {
                //jsMethods or something else needs to have collected the summation of the imports.... also the proto file name somehow?
//                Collections.sort(jsMethods);
                JSServiceInfo info = new JSServiceInfo(service, host, gateway, jsMethods);
                generateJavascript(info, pypackage, project.getBasedir().toString(),jsdir); //swap pypackage for something else?
            } catch (Exception e) {
                LoggerFactory.getLogger(GeneratorMojo.class).error("Error Generating Javascript Clients", e);
            }
        }
    }
    
    protected List<Class<?>> loadClasses(List<String> serviceHandlers) {
        List<Class<?>> services = new ArrayList<>();
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        if (serviceHandlers != null) {
            for (String serviceHandler : serviceHandlers) {
                try {
                    Class<?> service = cl.loadClass(serviceHandler);
                    services.add(service);
                } catch (ClassNotFoundException ex) {
                    getLog().error("Failed to load class: " +serviceHandler
                            + " from ServiceHandler list argument: "+ex.toString());
                }
            }
        }
        
        Enumeration<URL> serviceClasses;
        try {
            serviceClasses = cl.getResources(ServiceProcessor.SERVICE_HANDLER_FILE);
        } catch (IOException ex) {
            getLog().error("Failed to fetch ServiceHandler prop files: "+ex.toString());
            return services;
        }
        
        while (serviceClasses.hasMoreElements()) {
            URL serviceClass = serviceClasses.nextElement();
    
            try (InputStreamReader in = new InputStreamReader(serviceClass.openStream(), StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(in)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        Class<?> service = cl.loadClass(line);
                        services.add(service);
                    } catch (ClassNotFoundException ex) {
                        getLog().error("Failed to load a class from ServiceHandler prop file: "+ex.toString());
                    }
                }
            } catch (IOException ex) {
                getLog().error("Failed to parse a ServiceHandler prop file: "+ex.toString());
            }
        }
        return services;
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
            Files.write(file,initTemplate.getBytes(StandardCharsets.UTF_8));
        } catch (FileAlreadyExistsException ex) {
        } catch (IOException ex) {
            System.out.println("Failed to create __init__.py file in " + output);
        }
    }
    
    protected void generateJavascript(JSServiceInfo info, String jspath, String basedir, String jsdir) throws Exception {
        //mostly incorrect still.....
        
        printServiceInfo(info);
        String serviceName = info.getServiceName().toLowerCase();
        
        String pypackage = jspath.replaceAll("\\/","\\."); //lame that i'm converting back
        if (pypackage.length() == 1) pypackage = "";
        
        StringBuilder output = new StringBuilder();
        output.append(basedir).append("/");
        output.append(jsdir);
        //special case louie internal services
        if (LOUIE_SERVICES.contains(serviceName)) {
            output.append("louie/");
            pypackage = "louie.";
        } else {
            output.append(jspath);
        }
        output.append(serviceName).append("/");
        output.append(serviceName);
        output.append(JS_CLIENT); //client.js
        processTemplate(info, JS_TEMPLATE, output.toString(), pypackage);
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
        if (!f.exists() && !f.getParentFile().mkdirs()) {
            throw new Exception("Error creating directory path: "+f.getPath());
        }

        try (FileOutputStream out = new FileOutputStream(f);
                OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                BufferedWriter buf = new BufferedWriter(w);
                Writer writer = new PrintWriter(buf, false)) {
            vt.merge(vc, writer);
        }
    }
    
}
