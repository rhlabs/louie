/*
 * NonJavaPBCompilerMojo.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.pbcompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Configurable execution of the Protocol Buffer compiler optionally generates 
 * Python and C++
 * @author eyasukoc
 * @goal extpbcompile
 * @phase package
 * 
 */

public class ExtPBCompilerMojo extends AbstractMojo{
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
     * default-value="/usr/bin/protoc"
     */
    private String compiler;
    /**
     * The Google Protocol Buffer lib (include) folder
     * @parameter
     * property="compilerlibs"
     * default-value="/usr/include"
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
     * Python generated PB output folder (relative to basedir)
     * @parameter
     * property="pythondir"
     * default-value="target/generated-sources/python"
     */
    private String pythondir;
    /**
     * C++ generated PB output folder (relative to basedir)
     * @parameter
     * property="cppdir"
     * default-value="target/generated-sources/cpp"
     */
    private String cppdir;
    /**
     * Flag to enable or disable generation of C++ PBs (default is false)
     * @parameter
     * property="cppgen"
     */
    private boolean cppgen;
    /**
     * Flag to enable or disable generation of py PBs (default is false)
     * @parameter
     * property="pygen"
     */
    private boolean pygen;
    
    // A list of compilers in possible linux locations
    private final String[] protocompiler = {"/usr/bin/protoc","/usr/sbin/protoc"};
    // A list of compiler libs in possible linux locations
    private final String[] protolibs = {"/usr/include"};
    
    private final String mavenSharedDir = "maven-shared-archive-resources";
    
    @Override
    public void execute() throws MojoExecutionException {
        if (!cppgen && !pygen) {
            getLog().warn("PB Compiler had nothing to run!");
            return;
        }
        
        //do mkdirs
        getLog().debug("Creating directories");
        
        if (pygen) {
            pythondir = basedirectory+"/"+pythondir ;
            File pyf = new File(pythondir);
            if (!pyf.exists()) {
                pyf.mkdirs();
            }
        }
        if (cppgen) {
            cppdir = basedirectory+"/"+cppdir;
            File cf = new File(cppdir);
            if (!cf.exists()) {
                cf.mkdirs();
            }
        }    
        
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

        args.add(compilerF.toString());                                         //configurable protoc
        args.add("--proto_path="+basedirectory+"/"+protosrc);
        args.add("--proto_path="+libmarker);                                    //configurable libs
        String archivePath = builddirectory+"/"+mavenSharedDir;
        File sharedArchive = new File(archivePath);
        if (sharedArchive.exists()) {
            args.add("--proto_path="+archivePath);
        }
        if (pygen) args.add("--python_out="+pythondir);
        if (cppgen) args.add("--cpp_out="+cppdir);

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
        
        if (pygen) {
            Path pybase = Paths.get(pythondir);
            PlacePyInit pf = new PlacePyInit();
            pf.start = pybase;
            try {
                Files.walkFileTree(pybase, pf);
            } catch (IOException ex) {
                getLog().error(ex.toString());
            }
        }
        
    }
   
    /**
     * A file visitor (java 7) that will generate __init__ files in relevant
     * py directories that it finds.
     */
    public static class PlacePyInit extends SimpleFileVisitor<Path> {
        public Path start = null;
        String initTemplate = "__version__ = '${project.version}'";
        @Override

        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (!dir.equals(start)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    boolean containsinit = false;
                    for (Path file: stream) {
                        if (file.endsWith("__init__.py")) {
                            containsinit = true;
                        }
                    }
                    if (!containsinit) {
                        Path init = Paths.get(dir.toString(), "__init__.py");
                        try {
                            Files.write(init, initTemplate.getBytes());
                        } catch (FileAlreadyExistsException ex) {
                        } catch (IOException ex) {
                            System.out.println("Failed to create __init__.py in " + dir.toString());
                        }
                    }
                } catch (IOException | DirectoryIteratorException x) {
                    System.err.println(x);
                }
            }
            return CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }
    
}
