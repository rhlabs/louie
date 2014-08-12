/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rhythm.louie.plugins;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Executes com.rhythm.louie.servieclient.Generator.main()
 * @author eyasukoc
 */
@Mojo(name = "generator", defaultPhase = LifecyclePhase.DEPLOY)
public class GeneratorMojo extends AbstractMojo{
    
    /**
     * Hostname
     */
    @Parameter(property = "generator.hostname")
    private String hostname = "";
    /**
     * Application server gateway
     */
    @Parameter(property = "generator.gateway")
    private String gateway = "";
    /**
     * a prefix?
     */
    @Parameter(property = "generator.prefix")
    private String prefix = "";
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing LoUIE Service Client Generator");
        List<String> args = new ArrayList<String>();
        if (!hostname.isEmpty()) {
            args.add(hostname);
            if (!gateway.isEmpty()) {
                args.add(gateway);
                if (!prefix.isEmpty()) { //have to nest these due to the way Generator is expecting arguments
                    args.add(prefix);
                }
            }
        }
        com.rhythm.louie.plugins.Generator.main(args.toArray(new String[args.size()]));
    }
    
}
