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

import java.lang.management.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


import org.joda.time.*;
import org.joda.time.format.*;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.request.ProtoProcessor;

import com.rhythm.pb.RequestProtos.RequestPB;

/**
 *
 * @author eyasukoc
 */
public class MemoryAlertManager {
    
    private static final DateTimeFormatter dtFmt = DateTimeFormat.forPattern("MM/dd/yyy HH:mm:ss");;
    private static final PeriodFormatter timeFmt = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix(" hour", " hours")
            .appendSeparator(" ")
            .appendMinutes()
            .appendSuffix(" minute", " minutes")
            .appendSeparator(" ")
            .appendSeconds()
            .appendSuffix(" second", " seconds")
            .toFormatter();
  
    protected static void generateReport(String message) {
        
        List<RequestPB> requests = ProtoProcessor.getActiveRequests();
        StringBuilder report = new StringBuilder();
        report.append("Memory Usage Threshold has been surpassed.\n");
        report.append("Note that due to the typical speed of execution, ");
        report.append("some stack traces may not represent the activity of the listed Request.\n");
        report.append("The notification included this message: ").append(message).append("\n");
        report.append("Currently running requests:\n");
        report.append("\n-------------------------\n");
        for (RequestPB req : requests) {
            report.append("ID:         ").append(req.getId()).append("\n");
            report.append("service:    ").append(req.getService()).append("\n");
            report.append("method:     ").append(req.getMethod()).append("\n");
            report.append("thread ID:  ").append(req.getThreadId()).append("\n");
            DateTime start = new DateTime(req.getStartTime());
            report.append("Start time: ").append(dtFmt.print(start)).append("\n");
            report.append("Duration:   ");
            report.append(timeFmt.print(new Interval(start, new Instant()).toPeriod())).append("\n");
            report.append("\nStack trace:\n");
            report.append(ThreadInspector.INSTANCE.dumpStack(req.getThreadId(), 10));
            report.append("\n-------------------------\n");
            report.append("\n");
        }
        String email = AlertProperties.getProperties(AlertProperties.MEMORY).getEmail();
        String subject = "Memory usage threshold exceeded on "+Server.LOCAL.getHostName();
        try {
            EmailService.getInstance().sendMail(email, email, subject, report.toString());
        } catch (Exception ex) {
            LoggerFactory.getLogger(MemoryAlertManager.class).warn("Failed to send Memory Usage email: {}",ex.toString());
        }
        LoggerFactory.getLogger(MemoryAlertManager.class).error(report.toString());
    }

    public static void initializeMonitors() {
        AlertProperties props = AlertProperties.getProperties(AlertProperties.MEMORY);
        if (props != null) {
            MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
            MemoryPoller mp = new MemoryPoller(mbean, props.getPercentThreshold());
            int cycle = props.getMonitorPollCycle();
            TaskScheduler.getInstance().scheduleAtFixedRate(mp, cycle, cycle, TimeUnit.SECONDS);
            LoggerFactory.getLogger(MemoryAlertManager.class).info("Memory Monitor enabled with cycle of " 
                    + cycle + " seconds and a threshold of " + props.getPercentThreshold());
        } else {
            LoggerFactory.getLogger(MemoryAlertManager.class).info("No Memory Monitor configured!");
        }
    }

}
