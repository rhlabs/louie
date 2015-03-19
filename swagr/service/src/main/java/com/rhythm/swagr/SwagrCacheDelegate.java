/*
 * SwagrCacheDelegate.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.swagr;

import com.google.common.cache.CacheBuilderSpec;

import com.rhythm.louie.cache.CacheManager;
import com.rhythm.louie.cache.GuavaCache;
import com.rhythm.louie.CacheDelegate;

import com.rhythm.pb.swagr.SwagrProtos.SwagrBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrChartBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrComboBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrOptionsPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrServicePB;

import java.util.List;

import com.rhythm.louie.cache.SingletonCache;

import com.rhythm.pb.swagr.SwagrProtos.SwagrLocationPB;

/**
 *
 * @author eyasukoc
 */
@CacheDelegate
public class SwagrCacheDelegate extends SwagrDelegateAdaptor {
    private final CacheManager cacheManager;
    
    private final GuavaCache<SwagrStatsPK, List<SwagrBPB>> STAT_CACHE;
    private final GuavaCache<SwagrStatsPK, SwagrChartBPB> CHART_FORMAT_STAT_CACHE;
    private final SingletonCache<List<SwagrLocationPB>> LOCATION_CACHE;
    private final SingletonCache<List<SwagrServicePB>> SERVICE_CACHE;
    
    public SwagrCacheDelegate() {
        cacheManager = CacheManager.createCacheManager("swagr");
        CacheBuilderSpec spec = CacheBuilderSpec.parse("expireAfterAccess=12h,maximumSize=1000");
        STAT_CACHE = cacheManager.guavaCache("SwagrFullRequestCache", spec);
        CHART_FORMAT_STAT_CACHE = cacheManager.guavaCache("SwagrFormattedRequestCache",spec);
        LOCATION_CACHE = cacheManager.singletonCache("SwagrLocationCache");
        SERVICE_CACHE = cacheManager.singletonCache("SwagrServiceCache");
    }
    
    @Override
    public List<SwagrBPB> getSystemActivity(SwagrOptionsPB req) throws Exception {
        SwagrStatsPK currReq = new SwagrStatsPK(req);
        List<SwagrBPB> statsList = STAT_CACHE.get(currReq);
        if(statsList == null){
            statsList = super.getSystemActivity(req);
            STAT_CACHE.put(currReq, statsList);
        }
        return statsList;
    }

    @Override
    public List<SwagrServicePB> getServices() throws Exception {
        List<SwagrServicePB> services = SERVICE_CACHE.get();
        if (services == null) {
            services = super.getServices();
            SERVICE_CACHE.set(services);
        }
        return services;
    }

    @Override
    public SwagrChartBPB getSystemActivityChartFormatted(SwagrOptionsPB req) throws Exception {
        SwagrStatsPK currReq = new SwagrStatsPK(req);
        SwagrChartBPB chartBPB = CHART_FORMAT_STAT_CACHE.get(currReq);
        if(chartBPB == null){ 
            SwagrComboBPB combo = super.getSystemActivityCombo(req);
            STAT_CACHE.put(currReq, combo.getStatsList());
            chartBPB = combo.getChart();
            CHART_FORMAT_STAT_CACHE.put(currReq, chartBPB);
        }
        return chartBPB;
    }

    @Override
    public SwagrComboBPB getSystemActivityCombo(SwagrOptionsPB req) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<SwagrLocationPB> getLocations() throws Exception {
        List<SwagrLocationPB> locations = LOCATION_CACHE.get();
        if (LOCATION_CACHE.get() == null) {
            locations = super.getLocations();
            LOCATION_CACHE.set(locations);
        }
        return locations;
    }
}
