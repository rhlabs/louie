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

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;


/**
 *
 * @author eyasukoc
 */
public class MemoryPoller implements Runnable {

    MemoryMXBean memoryBean;
    boolean thresholdPassed = false;
    double threshold;

    public MemoryPoller(MemoryMXBean bean, double threshold) {
        memoryBean = bean;
        this.threshold = threshold;
    }

    @Override
    public void run() {
        MemoryUsage usage = memoryBean.getHeapMemoryUsage();
        long max = usage.getMax();
        long used = usage.getUsed();
        if (used >= (threshold*max)) {
            if (!thresholdPassed) {
                thresholdPassed = true;
                MemoryAlertManager.generateReport("Heap memory usage threshold exceeded: " 
                        + readableFileSize(used) + " used of " 
                        + readableFileSize(max) + " max.");
            }
        } else {
            thresholdPassed = false;
        }
    }
    
    private String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
