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
package com.rhythm.louie.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom2.Element;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eyasukoc
 */
public class AlertProperties {

    
    private static final String EMAIL = "email";
    private static final String REQUEST_DURATION = "request_duration";
    private static final String MONITOR_CYCLE = "monitor_cycle";
    private static final String SUMMARY_HOUR = "summary_hour";
    private static final String PERCENTAGE = "percentage";
    
    public static final String REQUEST = "request";
    public static final String MEMORY = "memory";
    
    private String email = null;
    private long duration = -1L;
    private int monitorPollCycle = -1;
    private int summaryHour = -1;
    private double percentThreshold = 1.0;

    private static final Map<String, AlertProperties> properties = new ConcurrentHashMap<>();
    
    public static AlertProperties getProperties(String alert) {
        return properties.get(alert);
    }
    
    public static void processProperties(Element alerts) {
        for (Element child : alerts.getChildren()) {
            String alert = child.getName();
            AlertProperties prop = new AlertProperties();
            properties.put(alert,prop);
            for (Element grandchild : child.getChildren()) {
                String propName = grandchild.getName();
                String value = grandchild.getTextTrim();
                switch (propName) {
                    case EMAIL: prop.setEmail(value);
                        break;
                    case REQUEST_DURATION: prop.setDuration(Long.parseLong(value));
                        break;
                    case MONITOR_CYCLE: prop.setMonitorPollCycle(Integer.parseInt(value));
                        break;
                    case SUMMARY_HOUR: prop.setSummaryHour(Integer.parseInt(value));
                        break;
                    case PERCENTAGE: int capture = Integer.parseInt(value);
                        double adjusted = (double) capture/100;
                        prop.setPercentThreshold(adjusted);
                        break;
                    default: LoggerFactory.getLogger(LouieProperties.class)
                                .warn("Unexpected alert property  {}",propName);
                        break;
                }    
            }
        }
    }

    private void setPercentThreshold(double percentTheshold) {
        this.percentThreshold = percentTheshold;
    }
    
    private void setEmail(String email) {
        this.email = email;
    }
    
    private void setDuration(long duration) {
        this.duration = duration;
    }
    
    private void setMonitorPollCycle(int period) {
        this.monitorPollCycle = period;
    }
    
    private void setSummaryHour(int hour) {
        this.summaryHour = hour;
    }
    
    public double getPercentThreshold() {
        return percentThreshold;
    }
    
    public String getEmail() {
        return email;
    }

    public long getDuration() {
        return duration;
    }

    public int getMonitorPollCycle() {
        return monitorPollCycle;
    }

    public int getSummaryHour() {
        return summaryHour;
    }
    
}
