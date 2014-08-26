/*
 * PBCompilerMojo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Configurable execution of the Protocol Buffer compiler.
 * @author eyasukoc
 * @goal pbcompiler
 * @phase process-resources
 * 
 */

public class PBCompilerMojo extends AbstractMojo{
    /**
      * The current project representation.
      * @parameter default-value="${project}"
      * @required
      * @read-only
      */
    private MavenProject project;
    /**
     * The base directory for the project
     * @parameter
     * property="basedirectory"
     * default-value="${basedir}"
     */
    private String basedirectory;
    /**
     * The base build directory for the project
     * @parameter
     * property="builddirectory"
     * default-value="${project.build.directory}"
     */
    private String builddirectory;
    /**
     * The Google Protocol Buffer compiler
     * @parameter
     * property="compiler"
     * default-value="/opt/randh/protobuf-2.4.0a/bin/protoc"
     */
    private String compiler;
    /**
     * The Google Protocol Buffer lib (include) folder
     * @parameter
     * property="compilerlibs"
     * default-value="/opt/randh/protobuf-2.4.0a/include"
     */
    private String compilerlibs;
    /**
     * The source directory within which we look for .proto files (relative to basedir)
     * @parameter
     * property="protosrc"
     * default-value="src/main/resources"
     */
    private String protosrc;
    /**
     * Java generated PB output folder (relative to basedir)
     * @parameter
     * property="javaPbdir"
     * default-value="target/generated-sources/protobuf-java"
     */
    private String javaPbdir;
    /**
     * Python generated PB output folder (relative to basedir)
     * @parameter
     * property="pythonPbdir"
     * default-value="target/generated-sources/protobuf-python"
     */
    private String pythonPbdir;
    /**
     * C++ generated PB output folder (relative to basedir)
     * @parameter
     * property="cppPbdir"
     * default-value="target/generated-sources/protobuf-cpp"
     */
    private String cppPbdir;
    /**
     * Flag to enable or disable generation of Python PBs (default is false)
     * @parameter
     * property="pythongen"
     */
    private boolean pythongen;
    /**
     * Flag to enable or disable generation of C++ PBs (default is false)
     * @parameter
     * property="cppgen"
     */
    private boolean cppgen;
    
    // A list of compilers in possible linux locations
    private final String[] protocompiler = {"/usr/bin/protoc","/usr/sbin/protoc"};
    // A list of compiler libs in possible linux locations
    private final String[] protolibs = {"/usr/include"};
    
    @Override
    public void execute() throws MojoExecutionException {
        //adjust directories according to basedir
        javaPbdir = basedirectory+"/"+javaPbdir;
        pythonPbdir = (pythongen) ? basedirectory+"/"+pythonPbdir : "" ;
        cppPbdir = (cppgen) ? basedirectory+"/"+cppPbdir : "";

        //do mkdirs
        getLog().debug("Creating directories");
        
        File jf = new File(javaPbdir);
        if (!jf.exists()) {
            jf.mkdirs();
        }
        if (pythongen) new File(pythonPbdir).mkdirs();    
        if (cppgen) new File(cppPbdir).mkdirs();    
        
        List<String> args = new ArrayList<String>();

        //Check existence of specified compiler and libraries ( GUESS A BUNCH )
        File compilerF = new File(compiler);
        int i = 0;
        while (!compilerF.exists() && i < protocompiler.length) {
            compilerF = new File(protocompiler[i]);
            i++;
        }
        i = 0;
        File libdir = new File(compilerlibs+"/google");
        String libmarker = compilerlibs;
        while (!libdir.exists() && i < protolibs.length) {
            libdir = new File(protolibs[i]+"/google");
            libmarker = protolibs[i];
            i++;
        }
         
        getLog().info("Compiling proto files with STANDARD compiler");

        args.add(compilerF.toString());                                         //configurable protoc
        args.add("--proto_path="+basedirectory+"/"+protosrc);
        args.add("--proto_path="+libmarker);                                    //configurable libs
        String archivePath = builddirectory+"/maven-shared-archive-resources";
        File sharedArchive = new File(archivePath);
        if (sharedArchive.exists()) {
            args.add("--proto_path="+archivePath);
        }
        args.add("--java_out="+javaPbdir);
        if (pythongen) args.add("--python_out="+pythonPbdir);
        if (cppgen) args.add("--cpp_out="+cppPbdir);

        //Find the proto files 
        File dir = new File(basedirectory+"/"+protosrc);
        IOFileFilter filter = new WildcardFileFilter("**.proto");
        Iterator<File> files = FileUtils.iterateFiles(dir, filter, TrueFileFilter.INSTANCE);
        while (files.hasNext()) {
            args.add(files.next().toString());
        }

        // exec
        BufferedReader input = null;
        try {
            getLog().info(args.toString());
            String line;
            Process p = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
            
            //ERROR HANDLING
            if (p.getErrorStream() != null) {
                try {
                    input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = input.readLine()) != null) {
                        getLog().error(line);
                    }
                } finally {
                    if (input != null) input.close();
                }
            }
            //OUTPUT HANDLING
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                getLog().info(line);
            }
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.toString());
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ex) {
                throw new MojoExecutionException(ex.toString());
            }
        }
        
        //Add the gen'ed java dir back into maven resources
        getLog().debug("Injecting the Java PB dir into the compile time source root");
        project.addCompileSourceRoot(javaPbdir);
        
    }
    
}
