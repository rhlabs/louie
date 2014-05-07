/*
 * PollingService.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public abstract class PollingService {
    private final Logger LOGGER = LoggerFactory.getLogger(PollingService.class);
    
    private static final String POLL = "poll";
    private static final String POLL_INTERVAL = "pollInterval";
    
    private ScheduledExecutorService scheduler;
    
    protected PollingService(ServiceProperties props, int defaultPollInterval) {
        try {
            boolean enable = props.getCustomProperty(POLL,"false").equals("true");
            int pollInterval = defaultPollInterval;
            try {
                String s = props.getCustomProperty(POLL_INTERVAL,"");
                if (!s.equals("")) {
                    pollInterval = Integer.parseInt(s);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Error parsing pollInterval: '{}'",
                        props.getCustomProperty(POLL_INTERVAL,""));
            }
            
            if (enable) {
                beginPolling(pollInterval);
                LOGGER.info("{} Polling Enabled ({})",props.getName(),pollInterval);
            } else {
                LOGGER.info("{} Polling DISABLED!",props.getName());
            }
        } catch (Exception ex) {
            LOGGER.error("Error initializing polling service", ex);
        }
    }

    synchronized public void shutdown() throws Exception {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    synchronized private void beginPolling(int pollInterval) throws Exception {
        scheduler = initializeScheduler(pollInterval);
    }
    
    abstract protected ScheduledExecutorService initializeScheduler(int pollInterval) throws Exception;
}

