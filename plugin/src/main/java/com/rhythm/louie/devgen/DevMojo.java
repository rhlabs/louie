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
package com.rhythm.louie.devgen;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Right now it just bundles alternate configuration files, but this mojo should be expanded to
 * cover other developer specific build actions
 * @author eyasukoc
 */
@Mojo(name="dev-prep", defaultPhase=LifecyclePhase.PREPARE_PACKAGE)
public class DevMojo extends AbstractMojo{
    /**
      * The current project representation.
      */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;
    
    /**
     * Your custom config file path (relative to base directory of the current project).
     */
    @Parameter
    private String configpath = null;
   
    private static final String OUTPUT_DIR = "target/classes/louie.xml";
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (configpath == null) {
            throw new MojoExecutionException("You must specify an alternate config path via <configpath>");
        }
        
        getLog().info("Setting alternate config path inside embedded louie.xml");
        
        if (configpath.startsWith("/")) configpath = configpath.substring(1);
        
        File projectbase = project.getBasedir();
        
        StringBuilder louiexml = new StringBuilder();
        louiexml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        louiexml.append("<louie xmlns:xsi= \"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://louiehost:8080/xsd/louie_config.xsd\">\n");
        louiexml.append("    <config_path>").append(projectbase.toString()).append("/").append(configpath).append("</config_path>\n");
        louiexml.append("</louie>\n");
        
        File f = new File(projectbase, OUTPUT_DIR);
        
        try (FileOutputStream out = new FileOutputStream(f);
                OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                BufferedWriter buf = new BufferedWriter(w);
                Writer writer = new PrintWriter(buf, false)) {
            writer.append(louiexml.toString());
        } catch (IOException ex) {
            getLog().error("Failed to write the dev config file");
        }
    }
    
}
