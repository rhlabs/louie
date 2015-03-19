/*
 * SwagrStatsPK.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.swagr;

import com.rhythm.pb.swagr.SwagrProtos.SwagrOptionsPB;

/**
 *
 * @author eyasukoc
 */
public class SwagrStatsPK {
    
    private final int topNum;
    private final String location;
    private final int dataType;
    private final int systemId;
    private final int timeShift;
    private final int valType;
    private final int id;
    private final Boolean locComposition;
    private final Boolean dTypeComposition;
    private final Boolean serviceTotals;
    private final int hostTypeId;
    private final long startDt;
    private final long endDt;
    private final Boolean movingAvg;
    private final int ridOrQid;
    private final Boolean removeWeekends;
    private final String service;
    
    
    public SwagrStatsPK(int topNum, String location, int dataType, int systemId,
            int timeShift, int valType, int id, Boolean locComposition, Boolean dTypeComposition,
            Boolean serviceTotals, int hostTypeId, long startDt, long endDt, Boolean movingAvg,
            int ridOrQid, Boolean removeWeekends, String service) {
        this.topNum = topNum;
        this.location = location;
        this.dataType = dataType;
        this.systemId = systemId;
        this.timeShift = timeShift;
        this.valType = valType;
        this.id = id;
        this.dTypeComposition = dTypeComposition;
        this.locComposition = locComposition;
        this.serviceTotals = serviceTotals;
        this.hostTypeId = hostTypeId;
        this.startDt = startDt;
        this.endDt = endDt;
        this.movingAvg = movingAvg;
        this.ridOrQid = ridOrQid;
        this.removeWeekends = removeWeekends;
        this.service = service;
    }
    
    public SwagrStatsPK(SwagrOptionsPB opts){
        this.topNum = opts.getTopNum();
        this.location = opts.getLocation();
        this.dataType = opts.getDataType();
        this.systemId = opts.getSystemId();
        this.timeShift = opts.getTimeShift();
        this.valType = opts.getValType();
        this.id = opts.getId();
        this.dTypeComposition = opts.getDTypeComposition();
        this.locComposition = opts.getLocComposition();
        this.serviceTotals = opts.getServiceTotals();
        this.hostTypeId = opts.getHostTypeId();
        this.startDt = opts.getStartDt();
        this.endDt = opts.getEndDt();
        this.movingAvg = opts.getMovingAvg();
        this.ridOrQid = opts.getRidOrQid();
        this.removeWeekends = opts.getRemoveWeekends();
        this.service = opts.getService();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SwagrStatsPK other = (SwagrStatsPK) obj;
        if (this.topNum != other.topNum) {
            return false;
        }
        if ((this.location == null) ? (other.location != null) : !this.location.equals(other.location)) {
            return false;
        }
        if (this.dataType != other.dataType) {
            return false;
        }
        if (this.systemId != other.systemId) {
            return false;
        }
        if (this.timeShift != other.timeShift) {
            return false;
        }
        if (this.valType != other.valType) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (this.locComposition != other.locComposition && (this.locComposition == null || !this.locComposition.equals(other.locComposition))) {
            return false;
        }
        if (this.dTypeComposition != other.dTypeComposition && (this.dTypeComposition == null || !this.dTypeComposition.equals(other.dTypeComposition))) {
            return false;
        }
        if (this.serviceTotals != other.serviceTotals && (this.serviceTotals == null || !this.serviceTotals.equals(other.serviceTotals))) {
            return false;
        }
        if (this.hostTypeId != other.hostTypeId) {
            return false;
        }
        if (this.startDt != other.startDt) {
            return false;
        }
        if (this.endDt != other.endDt) {
            return false;
        }
        if (this.movingAvg != other.movingAvg && (this.movingAvg == null || !this.movingAvg.equals(other.movingAvg))) {
            return false;
        }
        if (this.ridOrQid != other.ridOrQid) {
            return false;
        }
        if (this.removeWeekends != other.removeWeekends && (this.removeWeekends == null || !this.removeWeekends.equals(other.removeWeekends))) {
            return false;
        }
        if ((this.service == null) ? (other.service != null) : !this.service.equals(other.service)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.topNum;
        hash = 89 * hash + (this.location != null ? this.location.hashCode() : 0);
        hash = 89 * hash + this.dataType;
        hash = 89 * hash + this.systemId;
        hash = 89 * hash + this.timeShift;
        hash = 89 * hash + this.valType;
        hash = 89 * hash + this.id;
        hash = 89 * hash + (this.locComposition != null ? this.locComposition.hashCode() : 0);
        hash = 89 * hash + (this.dTypeComposition != null ? this.dTypeComposition.hashCode() : 0);
        hash = 89 * hash + (this.serviceTotals != null ? this.serviceTotals.hashCode() : 0);
        hash = 89 * hash + this.hostTypeId;
        hash = 89 * hash + (int) (this.startDt ^ (this.startDt >>> 32));
        hash = 89 * hash + (int) (this.endDt ^ (this.endDt >>> 32));
        hash = 89 * hash + (this.movingAvg != null ? this.movingAvg.hashCode() : 0);
        hash = 89 * hash + this.ridOrQid;
        hash = 89 * hash + (this.removeWeekends != null ? this.removeWeekends.hashCode() : 0);
        hash = 89 * hash + (this.service != null ? this.service.hashCode() : 0);
        return hash;
    }

}
