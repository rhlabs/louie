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
package com.rhythm.louie.info;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Joiner;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.server.ServiceProperties;
import com.rhythm.louie.service.command.ArgType;
import com.rhythm.louie.service.command.PBCommand;
import com.rhythm.louie.service.command.PBParamType;

/**
 *
 * @author cjohnson
 */
public class InfoUtils {

    public static void writeTemplateResponse(HttpServletRequest request, HttpServletResponse response,String template,Map<String,Object> properties) {
        response.setContentType("text/html;charset=UTF-8");
        try {
            Properties props = new Properties();
            URL url = ServicesServlet.class.getClassLoader().getResource("config/velocity.properties");
            props.load(url.openStream());

            VelocityEngine ve = new VelocityEngine(props);
            ve.init();

            VelocityContext vc = new VelocityContext();
            for (Map.Entry<String,Object> property : properties.entrySet()) {
                vc.put(property.getKey(), property.getValue());
            }
            vc.put("utils", InfoUtils.class);
            
            vc.put("contextPath", request.getContextPath());

            Template vt = ve.getTemplate("templates/"+template);
            vt.merge(vc, response.getWriter());
        } catch (IOException ex) {
            LoggerFactory.getLogger(ServicesServlet.class.getName()).error("Error generating info template", ex);
        }
    }
    
    public static String methodSignature(String serviceName, PBCommand command) {
        ServiceProperties serviceProps = ServiceProperties.getServiceProperties(serviceName);

        StringBuilder methodHTML = new StringBuilder();

        if (command.isUpdate()) {
            methodHTML.append("<span class='glyphicon glyphicon-pencil' title='Updating'></span> ");
        }
        if (command.isStreaming()) {
            methodHTML.append("<span class='glyphicon glyphicon-flash' title='Streaming'></span> ");
        }

        if (command.returnList()) {
            methodHTML.append("List&lt;");
        }
        methodHTML.append(protoLink(command.getReturnType().trim()));
        if (command.returnList()) {
            methodHTML.append("&gt;");
        }

        methodHTML.append(" <span class='call-name");
        if (command.isUpdate() && serviceProps.isReadOnly()) {
            methodHTML.append(" call-disabled");
        }
        if (command.isDeprecated() || command.getClass().isAnnotationPresent(Deprecated.class)) {
            methodHTML.append(" call-deprecated");
        }
        methodHTML.append("'>");
        methodHTML.append(command.getCommandName());
        methodHTML.append("</span>");

        for (PBParamType param : command.getArguments()) {
            methodHTML.append("(");
            int count = 0;
            for (ArgType arg : param.getTypes()) {
                if (count > 0) {
                    methodHTML.append(", ");
                }
                methodHTML.append(protoLink(arg.toString()));
                if (arg.getName().isEmpty()) {
                    methodHTML.append(" x").append(count);
                } else {
                    methodHTML.append(" ").append(arg.getName());
                }

                count++;
            }
            methodHTML.append(")");
        }

        return methodHTML.toString();
    }

    public static String protoLink(String pb) {
        return "<a href='proto?pb=" + pb + "' title='" + pb + "'>" + pb.replaceAll(".*\\.", "") + "</a>";
    }

    public static String servicePropertyString(String serviceName) {
        ServiceProperties props = ServiceProperties.getServiceProperties(serviceName);
        List<String> propLabels = new ArrayList<>();
        if (props.isReserved()) {
            propLabels.add("Reserved");
        }
        if (props.isReadOnly()) {
            propLabels.add("Read Only");
        }
        if (!props.isCachingOn()) {
            propLabels.add("Caching OFF");
        }
        return Joiner.on(" / ").join(propLabels);
    }
}
