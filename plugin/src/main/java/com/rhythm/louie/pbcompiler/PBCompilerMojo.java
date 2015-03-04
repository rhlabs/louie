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
package com.rhythm.louie.pbcompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

import com.google.common.base.Joiner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Configurable execution of the Protocol Buffer compiler for Java only
 * @author eyasukoc
 * 
 */
@Mojo(name="pbcompile", defaultPhase=LifecyclePhase.PROCESS_RESOURCES)
public class PBCompilerMojo extends AbstractMojo{
    /**
      * The current project representation.
      */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;
    
    /**
     * The base directory for the project
     */
    @Parameter(defaultValue="${basedir}")
    private String basedirectory;
    
    /**
     * The base build directory for the project
     */
    @Parameter(defaultValue="${project.build.directory}")
    private String builddirectory;
    
    /**
     * The Google Protocol Buffer compiler
     */
    @Parameter(defaultValue="protoc")
    private String compiler;
    
    /**
     * The Google Protocol Buffer lib (include) folder
     */
    @Parameter(name = "compiler-include")
    private String[] compilerInclude;
    
    /**
     * The source directory within which we look for .proto files (relative to basedir)
     */
    @Parameter(defaultValue="src/main/resources")
    private String protosrc;
    
    /**
     * Java generated PB output folder (relative to basedir)
     */
    @Parameter(defaultValue="target/generated-sources/java")
    private String javadir;
    
    /**
     * Python generated PB output folder (relative to basedir)
     */
    @Parameter(defaultValue="target/generated-sources/python")
    private String pythondir;
    
    /**
     * C++ generated PB output folder (relative to basedir)
     */
    @Parameter(defaultValue="target/generated-sources/cpp")
    private String cppdir;
    
    /**
     * Flag to enable or disable generation of Java PBs (default is false)
     */
    @Parameter
    private boolean javagen;
    
    /**
     * Flag to enable or disable generation of C++ PBs (default is false)
     */
    @Parameter
    private boolean cppgen;
    
    /**
     * Flag to enable or disable generation of py PBs (default is false)
     */
    @Parameter
    private boolean pygen;
    
    // A list of compiler libs in possible linux locations
    private static final String[] includeDirs = {"/usr/include","/usr/local/include"};
    
    private static final String mavenSharedDir = "maven-shared-archive-resources";
    
    @Override
    public void execute() throws MojoExecutionException {
        if (!cppgen && !pygen && !javagen) {
            getLog().warn("PB Compiler had nothing to run!");
            return;
        }
        
        //adjust directories according to basedir, and create if necessary
        if (javagen) {
            javadir = basedirectory+"/"+javadir;
            makeDir(javadir);
        }
        if (pygen) {
            pythondir = basedirectory+"/"+pythondir;
            makeDir(pythondir);
        }
        if (cppgen) {
            cppdir = basedirectory+"/"+cppdir;
            makeDir(cppdir);
        }  

        List<String> args = new ArrayList<>();
        args.add(compiler);                                         
        args.add("--proto_path="+basedirectory+"/"+protosrc);
        
        if (compilerInclude!=null && compilerInclude.length>0) {
            for (String dir : compilerInclude) {
                args.add("--proto_path="+dir);   
            }
        } else {
            // Try to find the google include in some known include dirs
            for (String dir : includeDirs) {
                File libdir = new File(dir+"/google");
                if (libdir.exists()) {
                    args.add("--proto_path="+dir);   
                }
            }
        }

        String archivePath = builddirectory+"/"+mavenSharedDir;
        File sharedArchive = new File(archivePath);
        if (sharedArchive.exists()) {
            args.add("--proto_path="+archivePath);
        }
        if (javagen) args.add("--java_out="+javadir);
        if (pygen) args.add("--python_out="+pythondir);
        if (cppgen) args.add("--cpp_out="+cppdir);

        //Find the proto files 
        File dir = new File(basedirectory+"/"+protosrc);
        IOFileFilter filter = new WildcardFileFilter("**.proto");
        Iterator<File> files = FileUtils.iterateFiles(dir, filter, TrueFileFilter.INSTANCE);
        while (files.hasNext()) {
            args.add(files.next().toString());
        }
        
        try {
            execProtoCompile(args);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.toString());
        }
        
        if (pygen) {
            //Generate __init__ files throughout tree where necessary
            Path pybase = Paths.get(pythondir);
            PlacePyInit pf = new PlacePyInit();
            pf.start = pybase;
            try {
                Files.walkFileTree(pybase, pf);
            } catch (IOException ex) {
                getLog().error(ex.toString());
            }
        }
        
        if (javagen) {
            //Add the gen'ed java dir back into maven resources
            getLog().debug("Injecting the Java PB dir into the compile time source root");
            project.addCompileSourceRoot(javadir);
        }
        
    }
    
    private void execProtoCompile(List<String> args) throws IOException {
        getLog().info(Joiner.on(" ").join(args));
        Process p = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        
        try (InputStreamReader errorIn = new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8);
                BufferedReader error = new BufferedReader(errorIn);
                InputStreamReader inputIn = new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader input = new BufferedReader(inputIn)) {
        
            String line;
            while ((line = error.readLine()) != null) {
                getLog().error(line);
            }
            while ((line = input.readLine()) != null) {
                getLog().info(line);
            }
        }
    }

    private void makeDir(String path) throws MojoExecutionException {
        File f = new File(path);
        if (!f.exists() && !f.mkdirs()) {
            throw new MojoExecutionException("Error creating directory path: "+path);
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
                            Files.write(init, initTemplate.getBytes(StandardCharsets.UTF_8));
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
