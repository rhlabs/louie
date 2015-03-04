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
package com.rhythm.louie.generator;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class Generator {
    
    public static void generate(ServiceInfo info) throws Exception {
        String header = info.getInputFile();
        if (!info.createFactory()) {
            header+=" (NO FACTORY)";
        }
        System.out.println(header);
        
        processTemplate(info,"Client.vm",info.getBaseName()+"Client");
        processTemplate(info,"ServiceClient.vm",info.getBaseName()+"ServiceClient");
        processTemplate(info,"ServiceHandler.vm",info.getBaseName()+"ServiceHandler");
        processTemplate(info,"Delegate.vm",info.getBaseName()+"DelegateAdaptor");
        processTemplate(info,"RemoteService.vm",info.getBaseName()+"RemoteService");
        processTemplate(info,"LocalClient.vm",info.getBaseName()+"LocalClient");
        
        if (info.createFactory()) {
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
            vc.put("imports",ProcessorUtils.getImports(info, imports));
            vc.put("className", className);
            vc.put("baseName", info.getBaseName());
            Template vt = ve.getTemplate("templates/"+template);

            JavaFileObject jfo = info.getProcessingEnv().getFiler().
                createSourceFile(info.getPackageName()+"."+className,info.getTypeElement().getEnclosingElement());

            try (Writer writer = jfo.openWriter()) {
                vt.merge(vc, writer);
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger(Generator.class)
                        .error("Error setting MaxTimeout", ex);
        }
    }
}
