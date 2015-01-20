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
    
    private static String email = null;
    private static long duration = -1L;
    private static int monitorPollCycle = -1;
    private static int summaryHour = -1;
    
    public static void processProperties(Element alerts) {
        for (Element child : alerts.getChildren()) {
            String propName = child.getName();
            String value = child.getTextTrim();
            switch (propName) {
                case EMAIL: email = value;
                    break;
                case REQUEST_DURATION: duration = Long.parseLong(value);
                    break;
                case MONITOR_CYCLE: monitorPollCycle = Integer.parseInt(value);
                    break;
                case SUMMARY_HOUR: summaryHour = Integer.parseInt(value);
                    break;
                default: LoggerFactory.getLogger(LouieProperties.class)
                            .warn("Unexpected alert property  {}",propName);
                    break;
            }    
        }
    }

    public static String getEmail() {
        return email;
    }

    public static long getDuration() {
        return duration;
    }

    public static int getMonitorPollCycle() {
        return monitorPollCycle;
    }

    public static int getSummaryHour() {
        return summaryHour;
    }
    
}
