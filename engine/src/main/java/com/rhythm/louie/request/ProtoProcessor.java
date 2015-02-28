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
package com.rhythm.louie.request;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.louie.email.EmailService;
import com.rhythm.louie.request.data.DataType;
import com.rhythm.louie.request.data.Result;
import com.rhythm.louie.server.*;
import com.rhythm.louie.services.auth.*;

import com.rhythm.pb.RequestProtos.ErrorPB;
import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.RequestHeaderPB;
import com.rhythm.pb.RequestProtos.RequestPB;
import com.rhythm.pb.RequestProtos.ResponseHeaderPB;
import com.rhythm.pb.RequestProtos.ResponsePB;
import com.rhythm.pb.RequestProtos.RoutePB;
import com.rhythm.pb.RequestProtos.SessionKey;

/**
 *
 * @author cjohnson
 */
public class ProtoProcessor implements ProtoProcess {
    private final Logger LOGGER = LoggerFactory.getLogger(ProtoProcessor.class);
    private final Pattern userCN;
    private final boolean secured;
    private static final Map<Long, RequestContext> currentRequestMap = new ConcurrentHashMap<>();
    
    public ProtoProcessor() {
        secured = Server.LOCAL.isSecure();
        userCN = Pattern.compile(".*CN=([\\w\\s]+),*.*");
        AlertProperties prop = AlertProperties.getProperties(AlertProperties.REQUEST);
        if (prop != null) {
            int cycle = prop.getMonitorPollCycle();
            TaskScheduler.getInstance().scheduleWithFixedDelay(new RequestMonitor(prop), cycle, cycle, TimeUnit.SECONDS);
            LOGGER.info("Request Monitor started");
        } else {
            LOGGER.info("No Request Monitor configured");
        }
    }
    
    @Override
    public List<Result> processRequest(InputStream input, OutputStream output, RequestProperties props) throws UnauthorizedSessionException, IOException, Exception {
        long start = System.nanoTime();

        List<Result> results = new ArrayList<>();
        
        RequestHeaderPB header = RequestHeaderPB.parseDelimitedFrom(input);
        if (header.getCount()>1) {
            throw new Exception("Batching Requests is not supported!");
        }
        
        IdentityPB identity = null;
        SessionKey sessionKey = null;
        if (header.hasKey()) {
            SessionStat session = AuthUtils.accessSession(header.getKey());
            identity = session.getIdentity();
        } else { //initial request, we will handle creating and returning a key
            if (header.hasIdentity()) { //to make backwards compatible
                identity = header.getIdentity();
                if (secured) {
                    Matcher match;
                    try {
                        match = userCN.matcher(props.getRemoteUser());
                    } catch (NullPointerException ex) {
                        LOGGER.error("IMPROPERLY CONFIGURED DEPLOYMENT. This instance is configured to be secure, "
                                + "but the container has not provided authorization/validation. Please check your web.xml");
                        throw new Exception("Improperly configured secure server. Please contact your server admin.");
                    }
                    if (match.matches()) {
                        if (identity.getUser().equalsIgnoreCase(match.group(1))) {
                            sessionKey = AuthUtils.createKey(identity);
                        }
                    }
                } else {
                    sessionKey = AuthUtils.createKey(identity);
                }
            }
            if (sessionKey == null && secured) {
                throw new UnauthenticatedException("Unable to create a Session Key, likely due to authentication failure");
            }
        }
        
        ResponseHeaderPB.Builder responseHeader = ResponseHeaderPB.newBuilder();
        responseHeader.setCount(header.getCount());
        if (sessionKey != null) {
            responseHeader.setKey(sessionKey);
        }
        responseHeader.build().writeDelimitedTo(output);
        
        for (int r = 0; r < header.getCount(); r++) {
            RequestPB request = RequestPB.parseDelimitedFrom(input);
            if (request == null) {
                throw new Exception("Improper Request format! Reached EOF prematurely! @ProtoProcessor.processRequest()");
            }
            if (request.hasRouteUser() && identity == null) {
                throw new Exception("User Route Permission Denied!");
            }
            
            RequestContext requestContext = null;
            Result result = null;

            RoutePB localRoute = props.createRoute(request.getService());
            for (RoutePB route : request.getRouteList()) {
                if (route.equals(localRoute)) {
                    throw new Exception("Route Loop Detected! "+route.getHostIp()+"/"+route.getGateway()+" visited twice!");
                }
            }
            
            try {
                requestContext = new RequestContext(header, request, DataType.PB, props);
                if (header.hasIdentity()) {
                    requestContext.setSessionKey(sessionKey);
                }
                requestContext.setIdentity(identity);
                requestContext.readPBParams(input);
                requestContext.setRoute(localRoute);
                requestContext.setThreadId(Thread.currentThread().getId());
                currentRequestMap.put(requestContext.getThreadID(), requestContext);
                result = RequestHandler.processSingleRequest(requestContext);
                result.setExecTime((System.nanoTime() - start) / 1000000);
                handleResult(requestContext, result, output);
            } catch (Exception e) {
                String errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                LOGGER.error("ProtoProcessor caught error: "+errorMessage,e);
                if (result != null) {
                    result.addError(e);
                } else {
                    result = Result.errorResult(e);
                }
            } finally {
                long end = System.nanoTime();
                if (requestContext == null) {
                    LOGGER.error("Unknown Error, Request is null");
                } else {
                    if (result == null) {
                        result = Result.errorResult(null);
                    }
                    result.setDuration((end - start) / 1000000);
                    try {
                        RequestHandler.logRequest(requestContext, result);
                    } catch (Exception le) {
                        LOGGER.error("Error Logging: {}", le.getMessage());
                    }
                }
                currentRequestMap.remove(Thread.currentThread().getId());
                start = end;
                results.add(result);
            }
        }
        return results;
    }

    private void handleResult(RequestContext requestContext,Result result,OutputStream output) throws Exception {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        ResponsePB.Builder responseBuilder = ResponsePB.newBuilder();
        responseBuilder.setId(requestContext.getRequest().getId());
        
        if (result.isError()) {
            ErrorPB.Builder error = ErrorPB.newBuilder();
            error.setCode(500);
            Exception ex = result.getException();
            if (ex!=null) {
                error.setType(ex.getClass().getSimpleName());
                if (ex.getMessage()!=null) {
                    error.setDescription(ex.getMessage());
                }
            } else {
                error.setType("Unknown Exception");
            }
            responseBuilder.setError(error);
        }
        
        responseBuilder.addRouteBuilder()
                .setRoute(requestContext.getRoute())
                .addAllPath(requestContext.getDesinationRoutes());
        
        if (result.getMessages().isEmpty()) {
            responseBuilder.setCount(0);
            ResponsePB response = responseBuilder.build();
            codedOutput.writeRawVarint32(response.getSerializedSize());
            response.writeTo(codedOutput);
        } else {
            responseBuilder.setCount(result.getMessages().size());
            boolean first = true;
            long totalSize = 0;
            for (Object oMessage : result.getMessages()) {
                Message message = (Message) oMessage;
                if (first) {
                    responseBuilder.setType(message.getDescriptorForType().getFullName());

                    ResponsePB response = responseBuilder.build();
                    codedOutput.writeRawVarint32(response.getSerializedSize());
                    response.writeTo(codedOutput);
                    first = false;
                }
                int serializedSize = message.getSerializedSize();
                codedOutput.writeRawVarint32(serializedSize);
                message.writeTo(codedOutput);
                if (result.isStreaming()) {
                    codedOutput.flush();
                    output.flush();
                }
                totalSize+=serializedSize;
            }
            result.setSize(totalSize);
        }
        codedOutput.flush();
        output.flush();
    }
    
    public static List<RequestPB> getActiveRequests() {
        List<RequestPB> reqs = new ArrayList<>();
        for (RequestContext ctx : currentRequestMap.values()) {
            reqs.add(ctx.getRequestThreadContext());
        }
        return reqs;
    }
    
    private List<RequestContext> getLongRunningRequests(long msDuration) {
        List<RequestContext> longrunning = new ArrayList<>();
        long currentTime = System.nanoTime()/1000000;
        for (RequestContext ctx : currentRequestMap.values()) {
            if (currentTime - ctx.getCreateInstant() > msDuration) {
                longrunning.add(ctx);
            }
        }
        return longrunning;
    }
    
    private class RequestMonitor implements Runnable {
        
        private Set<Long> trackedThreads = new HashSet<>();
        private final DateTimeFormatter fmt;
        private long duration = 120000L;
        private final int summaryHour;
        private final String email;
        private int hour;
        
        public RequestMonitor(AlertProperties prop) {
            fmt = DateTimeFormat.forPattern("MM/dd/yyy HH:mm:ss");
            duration = prop.getDuration();
            summaryHour = prop.getSummaryHour();
            email = prop.getEmail();
            hour = -1;
        }
        private final PeriodFormatter timeFmt = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix(" hour", " hours")
            .appendSeparator(" ")
            .appendMinutes()
            .appendSuffix(" minute", " minutes")
            .appendSeparator(" ")
            .appendSeconds()
            .appendSuffix(" second", " seconds")
            .toFormatter();
        
        @Override
        public void run() {
            //hour threshold passing logic
            boolean genSummary = false;
            int currentHour = new DateTime(System.currentTimeMillis()).getHourOfDay();
            if (currentHour == summaryHour && currentHour != hour) {
                genSummary = true;
            }
            hour = currentHour;
            
            List<RequestContext> requests = getLongRunningRequests(duration);
            Server local = Server.LOCAL;
            String subject = local.getHostName() +" ("+ local.getIp() +"/"+ local.getGateway() 
                    +") ["+ local.getName() +"] Louie Request Monitor";
            if (!requests.isEmpty()) {
                Set<Long> foundThreads = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                for (RequestContext ctx : requests) {
                    if (!trackedThreads.contains(ctx.getThreadID()) || genSummary) { //warn only once per request or for summary
                        sb.append("Thread ID:  ").append(ctx.getThreadID()).append("\n");
                        sb.append("SessionKey: ").append(ctx.getSessionKey()).append("\n");
                        sb.append("User:       ").append(ctx.getWho()).append("\n");
                        sb.append("IP:         ").append(ctx.getRequestProperties().getRemoteAddress()).append("\n");
                        sb.append("Module:     ").append(ctx.getModule()).append("\n");
                        sb.append("Language:   ").append(ctx.getLanguage()).append("\n");
                        DateTime create = new DateTime(ctx.getCreateTime());
                        sb.append("Start time: ").append(fmt.print(create)).append("\n");
                        sb.append("Duration:   ");
                        sb.append(timeFmt.print(new Interval(create, new Instant()).toPeriod())).append("\n");
                        sb.append("Request:    ");
                        
                        sb.append(ctx.getRequest().getService()).append(":");
                        sb.append(ctx.getRequest().getMethod()).append("(");
                        if (ctx.getRequest().getTypeCount() > 0) {
                            Joiner.on(",").appendTo(sb, ctx.getRequest().getTypeList());
                        }
                        sb.append(")");
                        if (!ctx.getParams().isEmpty()) {
                            sb.append(" - ");
                            sb.append("(");
                            RequestHandler.appendListString(sb,ctx.getParams());
                            sb.append(")");
                        }
                        sb.append("\n").append("Stacktrace: \n");
                        sb.append(ThreadInspector.INSTANCE.dumpStack(ctx.getThreadID(), 15));
                        sb.append("\n\n");
                    }
                    foundThreads.add(ctx.getThreadID());
                }
                
                Set<Long> cleared = new HashSet<>(trackedThreads);
                cleared.removeAll(foundThreads);
                if (!cleared.isEmpty()) {
                    sb.append("Cleared thread IDs:\t");
                    for (long l : cleared) {
                        sb.append(l).append("\t");
                    }
                }
                
                trackedThreads = foundThreads;
                
                if (sb.length() > 0) {
                    LOGGER.info("Request Monitor Update:\n{}",sb.toString());
                    try {
                        
                        EmailService.getInstance().sendMail(email, email, subject, sb.toString());
                    } catch (Exception ex) {
                        LOGGER.error(ex.toString());
                    }
                }
                
            } else {
                if (!trackedThreads.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cleared thread IDs:\t");
                    for (long l : trackedThreads) {
                        sb.append(l).append("\t");
                    }
                    trackedThreads.clear();
                    
                    LOGGER.info("Request Monitor Update:\n{}",sb.toString());
                    try {
                        EmailService.getInstance().sendMail(email, email, subject, sb.toString());
                        // TODO drive addresses via properties
                    } catch (Exception ex) {
                        LOGGER.error(ex.toString());
                    }
                }
            }
        }

    }
}
