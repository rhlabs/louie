/*
 * Generator.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.generator;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * @author cjohnson
 */
public class Generator {
    
    public static void generate(ServiceInfo info) throws Exception {
        System.out.print(info.getInputFile());
        if (!info.getServiceFacade().factory()) {
            System.out.print(" (NO FACTORY)");
        }
        System.out.println();
        
        processTemplate(info,"Client.vm",info.getBaseName()+"Client");
        processTemplate(info,"Facade.vm",info.getBaseName()+"Facade");
        processTemplate(info,"ServiceClient.vm",info.getBaseName()+"ServiceClient");
        processTemplate(info,"ServiceHandler.vm",info.getBaseName()+"ServiceHandler");
        processTemplate(info,"Delegate.vm",info.getBaseName()+"DelegateAdaptor");
        
        if (info.getServiceFacade()!=null && info.getServiceFacade().factory()) {
            processTemplate(info,"ServiceFactory.vm",info.getBaseName()+"ServiceFactory");
        }
        processTemplate(info,"ClientFactory.vm",info.getBaseName()+"ClientFactory");
    }

    public static void processTemplate(ServiceInfo info, String template, String className) {
        Collection<String> imports = Collections.emptySet();
        processTemplate(info,imports,template,className);
    }
    
    public static void processTemplate(ServiceInfo info, Collection<String> imports,
            String template, String className) {
        try {
            Properties props = new Properties();
            URL url = Generator.class.getClassLoader().getResource("config/velocity.properties");
            props.load(url.openStream());

            VelocityEngine ve = new VelocityEngine(props);
            ve.init();

            VelocityContext vc = new VelocityContext();

            vc.put("info", info);
            vc.put("imports",Utils.getImports(info, imports));
            vc.put("className", className);
            vc.put("baseName", info.getBaseName());
            Template vt = ve.getTemplate("templates/"+template);

            JavaFileObject jfo =  info.getProcessingEnv().getFiler().
                createSourceFile(info.getPackageName()+"."+className,info.getTypeElement().getEnclosingElement());

            Writer writer = jfo.openWriter();
            vt.merge(vc, writer);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
