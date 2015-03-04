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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sfong
 */
public class TaskScheduler {
    
    private ScheduledExecutorService scheduler;
    
    private final List<ScheduledFuture<?>> futures = new ArrayList<>();
    
    private final int poolSize;
    private String jndi = null;
    
    private TaskScheduler() {
        poolSize = TaskSchedulerProperties.getThreadPoolSize();
        jndi = TaskSchedulerProperties.getJndiKey();
    }
        
    public static TaskScheduler getInstance() {
        return TaskSchedulerHolder.INSTANCE;
    }

    private static class TaskSchedulerHolder {
        private static final TaskScheduler INSTANCE = new TaskScheduler();
    }
    
    private synchronized ScheduledExecutorService getScheduler() {
        if (scheduler == null) {
            if (jndi != null) {
                InitialContext ctx;
                try {
                    ctx = new InitialContext();
                    scheduler = (ManagedScheduledExecutorService) ctx.lookup(jndi);
                    LoggerFactory.getLogger(TaskScheduler.class)
                            .info("Loaded TaskScheduler from JNDI resource.");
                    return scheduler;
                } catch (NamingException ex) {
                    LoggerFactory.getLogger(TaskScheduler.class)
                            .error("Failed to fetch TaskScheduler JNDI resource.");
                }
            } 
            //load w/ no jndi config, or on failure of jndi lookup
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("louie-taskscheduler-%d").build();
            scheduler = Executors.newScheduledThreadPool(poolSize,threadFactory);
        }
        return scheduler;
    }
    
    
    synchronized public void shutdown() {
        if (scheduler != null) {
            if (jndi == null) {
                scheduler.shutdownNow();
            } else {
                for (ScheduledFuture<?> future : futures) {
                    future.cancel(true);
                }
            }
        }
    }
    
    public List<ScheduledFuture<?>> getFutures() {
//        for (ScheduledFuture<?> future : futures){
//        }
        return futures;
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
        getScheduler().execute(command);
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
        ScheduledFuture<?> future = getScheduler().schedule(command, delay, unit);
        futures.add(future);
        return future;
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
        ScheduledFuture<?> future = getScheduler().scheduleWithFixedDelay(command, initialDelay, delay, unit);
        futures.add(future);
        return future;
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
        ScheduledFuture<?> future = getScheduler().scheduleAtFixedRate(command, initialDelay, period, unit);
        futures.add(future);
        return future;
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
        ScheduledFuture<?> future = getScheduler().scheduleWithFixedDelay(command, initialDelay, delay, unit);
        futures.add(future);
        return future;
    }
}
