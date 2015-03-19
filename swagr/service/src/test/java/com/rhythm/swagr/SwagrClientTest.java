/*
 * SwagrClientTest.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.swagr;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnectionFactory;

import com.rhythm.pb.swagr.SwagrProtos;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author eyasukoc
 */
public class SwagrClientTest {
    
    private static SwagrClient client;
    public SwagrClientTest() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = SwagrClientFactory.getClient(
                LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity()));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testGetSystemActivity_TopXQ() throws Exception {
        System.out.println("getSystemActivity_TopXQ");
        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
        opts.setTopNum(2);
        opts.setHostTypeId(3);
        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
        //System.out.println(result);
        assertTrue(!result.isEmpty()); 
    }
    
//    @Test
//    public void testGetSystemActivity_TopXSys() throws Exception {
//        System.out.println("getSystemActivity_TopXSys");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setTopNum(10);
//        //opts.setLocation("ALL");
//        //opts.setDataType(3);
//        opts.setSystemId(1);
//        //opts.setId(17);
//        opts.setValType(2);
//        //opts.setLocComposition(false);
//        //opts.setDTypeComposition(false);
//        //opts.setTimeShift(0);
//        DateMidnight enddt = new DateMidnight();
//        DateMidnight startdt = enddt.minusDays(4);
//        opts.setStartDt(startdt.getMillis());
//        opts.setEndDt(enddt.getMillis());
//        opts.setTimeShift(1);
//        //opts.setLocComposition(true);
//        //opts.setDTypeComposition(true);
//        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
//        System.out.println(result);
//        assertTrue(!result.isEmpty()); 
//    }
    
//    @Test
//    public void testGetFormattedChartThing() throws Exception {
//        System.out.println("getSystemActivity_TopXSys");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setTopNum(10);
//        opts.setLocation("MAIN");
//        opts.setDataType(0);
//        opts.setSystemId(1);
//        //opts.setId(17);
//        opts.setValType(1);
//        //opts.setLocComposition(false);
//        //opts.setDTypeComposition(false);
//        opts.setTimeShift(0);
//        DateMidnight enddt = new DateMidnight();
//        DateMidnight startdt = enddt.minusDays(30);
//        opts.setStartDt(startdt.getMillis());
//        opts.setEndDt(enddt.getMillis());
//        opts.setRemoveWeekends(true);
//        //opts.setLocComposition(true);
//        //opts.setDTypeComposition(true);
//        SwagrChartBPB result = client.getSystemActivityChartFormatted(opts.build());
//        System.out.println(result);
//        assertNotNull(result); 
//    }
    
    @Test
    public void testGetDeprecatedCalls() throws Exception {
        System.out.println("getDeprecatedCalls");
        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
        opts.setService("ALL");
        opts.setSystemId(1);
        List<SwagrProtos.SwagrBPB> result = client.getDeprecatedActivity(opts.build());
        System.out.println(result);
        assertTrue(!result.isEmpty());
    }
    
//    @Test
//    public void testGetSystemActivity_MovingAvg() throws Exception {
//        System.out.println("getSystemActivity_MovingAvg");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setId(17);
//        opts.setRidOrQid(1);//this is wrong but the program doesn't care, it just wants it to be set. lame
//        opts.setMovingAvg(true);
//        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
//        //System.out.println(result);
//        assertTrue(!result.isEmpty()); 
//    }
//    
//    @Test
//    public void testGetSystemActivity_ServiceTotals() throws Exception{
//        System.out.println("getSystemActivity_ServiceTotals");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setServiceTotals(true);
//        opts.setLocation("MAIN");
//        opts.setDataType(0);
//        opts.setValType(1);
//        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
//        //System.out.println(result);
//        assertTrue(!result.isEmpty());
//    }
//    
//    @Test
//    public void testGetSystemActivity_SingleReq() throws Exception{
//        System.out.println("getSystemActivity_SingleReq");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setId(17);
//        opts.setRidOrQid(0);
//        //opts.setMovingAvg(true);
//        opts.setValType(1);
//        opts.setTimeShift(1);
//        //opts.setLocComposition(true);
//        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
//        //System.out.println(result);
//        assertTrue(!result.isEmpty());
//    }
//    
//    @Test
//    public void testGetSystemActivity_Timelog() throws Exception{
//        System.out.println("getSystemActivity_Timelog");
//        SwagrProtos.SwagrOptionsPB.Builder opts = SwagrProtos.SwagrOptionsPB.newBuilder();
//        opts.setHostTypeId(3);
//        opts.setValType(1);
//        List<SwagrProtos.SwagrBPB> result = client.getSystemActivity(opts.build());
//        System.out.println(result); 
//        assertTrue(!result.isEmpty());
//    }
}
