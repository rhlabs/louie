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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 *
 * @author eyasukoc
 */
public enum ThreadInspector {
 
    INSTANCE;
    
    ThreadMXBean threadBean;
    
    ThreadInspector() {
        threadBean = ManagementFactory.getThreadMXBean();
    }
    
    public long[] findDeadlockedThreads() {
        return threadBean.findDeadlockedThreads();
    }
    
    public long[] findMonitorDeadlockedThreads() {
        return threadBean.findMonitorDeadlockedThreads();
    }
    
    public ThreadInfo getThreadInfo(long id) {
        return threadBean.getThreadInfo(id);
    }
    
    public ThreadInfo getThreadInfo(long id, int maxDepth) {
        return threadBean.getThreadInfo(id, maxDepth);
    }
    
    public String dumpStack(long id, int maxDepth) {
        ThreadInfo info= threadBean.getThreadInfo(id,maxDepth);
        StackTraceElement[] trace = info.getStackTrace();
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement elem : trace) {
           builder.append(elem).append("\n");
        }
        return builder.toString();
    }
    
}
