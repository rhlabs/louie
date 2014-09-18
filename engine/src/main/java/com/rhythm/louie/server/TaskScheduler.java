/*
 * TaskScheduler.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sfong
 */
public class TaskScheduler {
    private final Logger LOGGER = LoggerFactory.getLogger(TaskScheduler.class);
    
    private static final String POLL = "poll";
    
    private static final int THREAD_POOL_SIZE = 8;
    
    private final ScheduledExecutorService scheduler;
    
    private TaskScheduler() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("louie-taskscheduler-%d").build();
        scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE,threadFactory);
    }
        
    public static TaskScheduler getInstance() {
        return TaskSchedulerHolder.INSTANCE;
    }

    private static class TaskSchedulerHolder {
        private static final TaskScheduler INSTANCE = new TaskScheduler();
    }
    
    synchronized public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
    
    private boolean isEnabled(ServiceProperties props) {
        return props.getCustomProperty(POLL, "false").equals("true"); 
    }
    
    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the <tt>Executor</tt> implementation.
     *
     * @param command the runnable task
     * 
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable) 
     */
    public void execute(Runnable command) {
        scheduler.execute(command);
    }
    
     /**
     * Creates and executes a one-shot action that becomes enabled
     * after the given delay.
     *
     * @param command the task to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of
     *         the task and whose <tt>get()</tt> method will return
     *         <tt>null</tt> upon completion
     *
     * @see java.util.concurrent.ScheduledExecutorService#schedule(
     * java.lang.Runnable, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }
    
     /**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the
     * given delay between the termination of one execution and the
     * commencement of the next.  If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.
     *
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one
     * execution and the commencement of the next
     * @param unit the time unit of the initialDelay and delay parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose <tt>get()</tt> method will throw an
     *         exception upon cancellation
     *
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(
     * java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    
    /**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the given
     * period; that is executions will commence after
     * <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
     * <tt>initialDelay + 2 * period</tt>, and so on.
     * If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.  If any execution of this task
     * takes longer than its period, then subsequent executions
     * may start late, but will not concurrently execute.
     * 
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions
     * @param unit the time unit of the initialDelay and period parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose <tt>get()</tt> method will throw an
     *         exception upon cancellation
     *
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(
     * java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
    
    /**
     * Allows overriding of the delay via the serviceProperties
     * 
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the
     * given delay between the termination of one execution and the
     * commencement of the next.  If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.
     *
     * @param serviceProperties override properties from the service config
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one
     * execution and the commencement of the next
     * @param unit the time unit of the initialDelay and delay parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose <tt>get()</tt> method will throw an
     *         exception upon cancellation
     *
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(
     * java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(ServiceProperties serviceProperties, Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (!isEnabled(serviceProperties)) {
            LOGGER.info("{} Polling DISABLED!", serviceProperties.getName());
            return null;
        }
        LOGGER.info("{} Polling Enabled ({})", serviceProperties.getName(), delay);
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
