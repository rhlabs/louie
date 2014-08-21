/*
 * Identity.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.connection;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import com.rhythm.louie.LocalConstants;

import com.rhythm.pb.RequestProtos.IdentityPB;

/**
 * @author cjohnson
 * Created: Mar 14, 2011 3:37:25 PM
 */
public class Identity {
    private static final String JAVA = "Java";
    private static final String JAVA_VERSION = System.getProperty("java.version","");
    private static final String OS = System.getProperty("os.name","Unknown");
    private static final String OS_VERSION = System.getProperty("os.version","");
            
    private static final String MACHINE;
    private static final String IP;
    private static final String PROCESSID = ManagementFactory.getRuntimeMXBean().getName();
    static {
        String machine;
        try {
            machine = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            machine = "Unknown";
        }
        MACHINE = machine;
        
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e) {
            ip = "Unknown";
        }
        IP = ip;
    }

    private static IdentityPB id = createIdentity("Unknown");
    
    public static IdentityPB getIdentity() {
        return id;
    }
    
    public static void registerIdentity(IdentityPB id) {
        Identity.id = id;
    }
    
    public static void registerIdentity(String program) {
        Identity.id = createIdentity(program);
    }
    
    public static void registerIdentity(String user,String program) {
        Identity.id = createIdentity(user,program);
    }
    
    public static void registerJUnitIdentity() {
        Identity.id = createJUnitIdentity();
    }
    
    public static void registerLouieIdentity() {
        Identity.id = createLouieIdentity();
    }
    
    private static IdentityPB.Builder defaultIdentity() {
        return IdentityPB.newBuilder()
                .setLanguage(JAVA)
                .setLanguageVersion(JAVA_VERSION)
                .setUser(System.getProperty("user.name"))
                .setIp(IP)
                .setMachine(MACHINE)
                .setOs(OS)
                .setOsVersion(OS_VERSION)
                .setProcessId(PROCESSID);
    }
    
    public static IdentityPB createIdentity(String program) {
        return defaultIdentity()
                .setProgram(program)
                .build();
    }
    
    public static IdentityPB createIdentity(String user,String program) {
        return defaultIdentity()
                .setProgram(program)
                .setUser(user)
                .build();
    }
    
    public static IdentityPB createJUnitIdentity() {
        return createIdentity("JUnit");
    }
    
    public static IdentityPB createLouieIdentity() {
        return defaultIdentity()
                .setLanguage("LoUIE")
                .setLanguageVersion("")
                .setProgram("LoUIE/"+LocalConstants.HOST)
                .setUser("LoUIE")
                .build();
    }
}
