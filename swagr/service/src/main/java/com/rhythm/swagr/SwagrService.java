/*
 * SwagrService.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.swagr;

import java.util.List;

import com.rhythm.louie.Grouping;
import com.rhythm.louie.Service;

import com.rhythm.pb.swagr.SwagrProtos.SwagrBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrChartBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrComboBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrLocationPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrOptionsPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrServicePB;

/**
 *
 * @author eyasukoc
 */
@Service
public interface SwagrService {
    
    static final String CHARTS = "Charts";
    
    /**
     * Returns a minimum set of system activity data
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    SwagrChartBPB getSystemActivityChartFormatted(SwagrOptionsPB req) throws Exception;
    
    /**
     * Returns a filtered set of system activity data
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    List<SwagrBPB> getSystemActivity(SwagrOptionsPB req) throws Exception;
    
    /**
     * Get a list of all available LoUIE services
     * 
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    List<SwagrServicePB> getServices() throws Exception;
    
    /**
     * Get all request types belonging to a specific LoUIE service
     * 
     * @param service
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    SwagrServicePB getRequestTypes(String service) throws Exception;
    
    /**
     * Get both a ChartBPB and a list of SwagrBPBs
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    SwagrComboBPB getSystemActivityCombo(SwagrOptionsPB req) throws Exception;
     
    /**
     * Get all deprecated calls for a given service
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    @Grouping(group = CHARTS)
    List<SwagrBPB> getDeprecatedActivity(SwagrOptionsPB req) throws Exception;
    
    /**
     * Get data for a specific request
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    List<SwagrBPB> getSingleRequest(SwagrOptionsPB req) throws Exception;
    
    /**
     * Get the service totals for a system
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    List<SwagrBPB> getServiceTotals(SwagrOptionsPB req) throws Exception;
    
    /**
     * Get the overall sum for a system
     * 
     * @param req
     * @return
     * @throws Exception 
     */
    List<SwagrBPB> getSystemTotals(SwagrOptionsPB req) throws Exception;
    
    /**
     * Get a list of valid server locations 
     * 
     * @return
     * @throws Exception 
     */
    List<SwagrLocationPB> getLocations() throws Exception;
    
}
