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
package com.rhythm.swagr;

import java.sql.Date;
import java.sql.*;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.rhythm.louie.DAO;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.info.InfoProtos.*;
import com.rhythm.louie.info.InfoProtos.MethodPB;
import com.rhythm.louie.info.InfoProtos.ParamPB;
import com.rhythm.louie.info.InfoProtos.ServicePB;
import com.rhythm.louie.jdbc.JdbcFactory;
import com.rhythm.louie.jdbc.JdbcService;
import com.rhythm.louie.services.info.InfoClient;
import com.rhythm.louie.services.info.InfoClientFactory;

import com.rhythm.pb.swagr.SwagrProtos.SwagrBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrChartBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrComboBPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrLocationPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrOptionsPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrRequestPB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrServicePB;
import com.rhythm.pb.swagr.SwagrProtos.SwagrStatPB;

import com.rhythm.swagr.datasource.DrjJdbc;
import com.rhythm.swagr.datasource.SwagrJdbc;

/**
 *
 * @author eyasukoc
 */
@DAO
public class SwagrDAO implements SwagrService {
    
    private final JdbcFactory swagrJdbcFac = SwagrJdbc.getFactory();
    private final JdbcFactory drjJdbcFac = DrjJdbc.getFactory();
    
    public SwagrDAO() {}

//    private static final String TIMELOG_MINUTE = "SELECT "
//            + "UNIX_TIMESTAMP(CONCAT(logdate,' ',logtime)),count,ave,fails "
//            + "FROM timelog WHERE unix_timestamp(concat(logdate,' ',logtime)) "
//            + "BETWEEN ? AND ? AND hosttypeid = ?";
    private static final String TIMELOG_DAY = "SELECT UNIX_TIMESTAMP(logdate),"
            + "count,ave,fails FROM timelog_day WHERE logdate "
            + "BETWEEN ? AND ? AND hosttypeid = ?";
//    private static final String TIMELOG_HOUR = "SELECT "
//            + "UNIX_TIMESTAMP(CONCAT(logdate,' ',logtime)),count,ave,fails "
//            + "FROM timelog_hour WHERE "
//            + "concat(logdate,' ',logtime) BETWEEN ? AND ? AND "
//            + "hosttypeid = ?";
    
    private static final String SWAGR_STATS_DAILY_PREFIX = "SELECT s.rid,"
            + "UNIX_TIMESTAMP(s.logdate),s.count,s.min_time,s.ave_time,"
            + "s.max_time,s.ave_bytes,s.max_bytes,s.ave_rows,s.max_rows,"
            + "s.fails,s.location,d.name FROM stats_daily s, data_type d ";
    
    private static final String DRJ_STATS_DAILY_PREFIX = "SELECT qid,"
            + "UNIX_TIMESTAMP(dt),hosttypeid,count,min,max,ave,fails,"
            + "ave_rows,ave_bytes FROM stats_daily ";
    
    private static final String SWAGR_TOP_SYS_DISTINCT = 
            SWAGR_STATS_DAILY_PREFIX 
            + "WHERE d.id=s.data_type AND s.logdate BETWEEN ? AND ? AND "
            + "s.location LIKE ? AND s.rid IN "
            + "(SELECT DISTINCT rid FROM top_stats_system WHERE dt "
            + "BETWEEN ? AND ? AND rank <= ? AND location = ? AND systemid = ? "
            + "AND type = ?) GROUP BY s.rid,s.logdate,s.location,d.name"; 
    
    private static final String SWAGR_SERVICES = 
            SWAGR_STATS_DAILY_PREFIX
            + "WHERE d.id=s.data_type AND s.logdate BETWEEN ? AND ? AND "
            + "s.location LIKE ? AND s.data_type LIKE ? AND s.rid IN "
            + "(SELECT id FROM requests WHERE service = ?)";
    
    private static final String SWAGR_SERVICES_DEPRECATED = 
            SWAGR_STATS_DAILY_PREFIX
            + "WHERE d.id=s.data_type AND s.logdate BETWEEN ? AND ? AND "
            + "s.location LIKE ? AND s.data_type LIKE ? AND s.rid IN "
            + "("; 
    
    private static final String S_TOP_DATA_DISTINCT = 
            SWAGR_STATS_DAILY_PREFIX
            + "WHERE d.id=s.data_type AND s.logdate BETWEEN ? AND ? AND "
            + "s.data_type LIKE ? AND location LIKE ? AND s.rid IN "
            + "(SELECT DISTINCT rid FROM top_stats_data WHERE dt "
            + "BETWEEN ? AND ? AND rank <= ? AND location = ? AND systemid = ? "
            + "AND data_type LIKE ? AND type = ?) "
            + "GROUP BY s.rid,s.logdate,s.location";
    
    private static final String SINGLE_REQ = 
            SWAGR_STATS_DAILY_PREFIX
            + "WHERE s.data_type=d.id AND logdate BETWEEN ? AND ? AND "
            + "s.rid = ? AND s.location LIKE ? AND s.data_type LIKE ?";
    
    private static final String SINGLE_QUERY = 
            DRJ_STATS_DAILY_PREFIX
            + "WHERE dt BETWEEN ? AND ? AND rid = ?";
    
    private static final String DRJ_TOP_STATS = 
            DRJ_STATS_DAILY_PREFIX
            + "WHERE hosttypeid = ? AND dt BETWEEN ?"
            + " AND ? AND qid IN (SELECT DISTINCT qid FROM "
            + "top_stats_daily WHERE dt BETWEEN ? AND ?"
            + " AND rank <=? and hosttypeid=?) ORDER BY dt";   
    
    private static final String SWAGR_STRINGS_PREFIX = "SELECT r.id,"
            + "r.service,r.function,r.args,s.name FROM requests r, system s ";
    
    private static final String SWAGR_SERVICE_STRINGS_SYS = 
            SWAGR_STRINGS_PREFIX
            + "WHERE r.id IN (SELECT id FROM requests WHERE service = ?)";
    
    private static final String SWAGR_SINGLE_STRINGS_SYS = 
            SWAGR_STRINGS_PREFIX
            + "WHERE r.id = ?";
    
    private static final String SWAGR_STRINGS_SYS = 
            SWAGR_STRINGS_PREFIX
            + "WHERE s.id=r.systemid "
            + "AND r.id IN (SELECT DISTINCT rid FROM top_stats_system WHERE dt "
            + "BETWEEN ? AND ? AND rank<=? AND location=? AND systemid = ?)";    

    private static final String SWAGR_STRINGS_DATA =  
            SWAGR_STRINGS_PREFIX
            + "WHERE s.id=r.systemid "
            + "AND r.id IN (SELECT DISTINCT rid FROM top_stats_data WHERE dt "
            + "BETWEEN ? AND ? AND rank<=? AND location=? AND systemid = ? "
            + "AND data_type = ?)"; 
    
    private static final String DRJ_STRINGS_PREFIX = "SELECT q.typeid,q.id,q.qtarget,"
            + "q.qtable,q.qwhere,qid.type FROM query AS q ";
    
    private static final String DRJ_SINGLE_STRING = 
            DRJ_STRINGS_PREFIX
            + "WHERE q.id = ?";
    
    private static final String DRJ_STRINGS = 
            DRJ_STRINGS_PREFIX
            + "INNER JOIN "
            + "query_type AS qid ON q.typeid=qid.id WHERE q.id IN "
            + "(SELECT DISTINCT qid FROM top_stats_daily WHERE dt BETWEEN "
            + "? AND ? AND rank<=? AND "
            + "hosttypeid=?)"; 
    
    private static final String SERVICE_TOTALS_SUFFIX = "WHERE s.logdate BETWEEN ? AND ? AND s.data_type "
            + "LIKE ? AND s.location LIKE ? AND  s.rid=r.id AND r.systemid = ? "
            + "GROUP BY r.service,s.location,s.data_type,s.logdate";
    
    private static final String SERVICE_TOTALS_COUNT = "SELECT r.service,s.data_type,"
            + "s.location,UNIX_TIMESTAMP(s.logdate),SUM(s.count) FROM stats_daily s, "
            + "requests r " + SERVICE_TOTALS_SUFFIX;
    
    private static final String SERVICE_TOTALS_LOAD = "SELECT r.service,s.data_type,"
            + "s.location,UNIX_TIMESTAMP(s.logdate),SUM(s.count*s.ave_bytes) FROM stats_daily s, "
            + "requests r " + SERVICE_TOTALS_SUFFIX;
    private static final String SERVICE_TOTALS_DURATION = "SELECT r.service,s.data_type,"
            + "s.location,UNIX_TIMESTAMP(s.logdate),SUM(s.count*s.ave_time) FROM stats_daily s, "
            + "requests r " + SERVICE_TOTALS_SUFFIX;
    
    private static final String SYSTEM_TOTALS_SUFFIX = " WHERE s.logdate BETWEEN ? AND ? AND s.data_type "
            + "LIKE ? AND s.location LIKE ? AND s.rid=r.id AND r.systemid = ? GROUP BY s.location,s.data_type,s.logdate";
    
    private static final String SYSTEM_TOTALS_COUNT = "SELECT s.data_type,s.location,"
            + "UNIX_TIMESTAMP(s.logdate),SUM(s.count) FROM stats_daily s, requests r"
            + SYSTEM_TOTALS_SUFFIX;
    
    private static final String SYSTEM_TOTALS_LOAD = "SELECT s.data_type,s.location,"
            + "UNIX_TIMESTAMP(s.logdate),SUM(s.count*s.ave_bytes) FROM stats_daily s, requests r"
            + SYSTEM_TOTALS_SUFFIX;
    
    private static final String SYSTEM_TOTALS_DURATION = "SELECT s.data_type,s.location,"
            + "UNIX_TIMESTAMP(s.logdate),SUM(s.count*s.ave_time) FROM stats_daily s, requests r"
            + SYSTEM_TOTALS_SUFFIX;
    
    private static final String MOVING_AVG = "SELECT m.rid,UNIX_TIMESTAMP(m.dt),"
            + "m.value,d.name,m.location FROM moving_avgs m, data_type d WHERE "
            + "d.id=m.data_type AND m.dt BETWEEN ? AND ? AND m.location LIKE ? "
            + "AND m.data_type LIKE ? AND m.rid = ? AND m.type = ?";
    
    private static final String SERVICES = "SELECT DISTINCT service,systemid FROM requests";
    
    private static final String SERVICE_REQUEST_TYPES = "SELECT id,function,args FROM requests WHERE service = ?";
    
    private static final String LOCATIONS = "SELECT DISTINCT location FROM host WHERE active = 1";
    
    @Override
    public List<SwagrServicePB> getServices() throws Exception{
        return serviceList();
    }
    
    @Override
    public List<SwagrLocationPB> getLocations() throws Exception {
        List<SwagrLocationPB> locations = new ArrayList<>();
        try (JdbcService jdbc = swagrJdbcFac.newService(LOCATIONS)) {
            ResultSet rs = jdbc.executeQuery();
            while (rs.next()) {
                locations.add(SwagrLocationPB.newBuilder().setLocation(rs.getString(1)).build());
            }
        }
        locations.add(SwagrLocationPB.newBuilder().setLocation("ALL").build());
        return locations;
    }
     
    @Override
    public List<SwagrBPB> getDeprecatedActivity(SwagrOptionsPB req) throws Exception {
        if (!req.hasService()) {
            throw new Exception("Must include a service name to find deprecated calls");
        } 
        return buildDeprecatedCalls(req).getIdList();
    }
    
    @Override
    public List<SwagrBPB> getSingleRequest(SwagrOptionsPB req) throws Exception {
        if(!req.hasRidOrQid())
            throw new Exception("Missing Parameter: If id is set, "
                    + "ridOrQid field must be set");
        return buildSingleReq(req).getIdList();
    }

    @Override
    public List<SwagrBPB> getServiceTotals(SwagrOptionsPB req) throws Exception {
        //location, datatype, systemid, timeshift, valuetype, startdt, enddt, removeweekends
        if((req.hasStartDt() && !req.hasEndDt()) || (req.hasEndDt() && !req.hasStartDt()))
            throw new Exception("Missing Parameter: If included, "
                    + "both startDt and endDt must be set.");
        SwagrStructure struct = buildServiceTotals(req);
        return struct.getStringList();
    }

    @Override
    public List<SwagrBPB> getSystemTotals(SwagrOptionsPB req) throws Exception {
        if((req.hasStartDt() && !req.hasEndDt()) || (req.hasEndDt() && !req.hasStartDt()))
            throw new Exception("Missing Parameter: If included, "
                    + "both startDt and endDt must be set.");
        SwagrStructure struct = buildSystemTotals(req);
        return struct.getStringList();
    }
    
    @Override
    public List<SwagrBPB> getSystemActivity(SwagrOptionsPB req) 
            throws Exception {
        
        if(req.hasService()){
            return buildAllForService(req).getIdList();
        }
        else if(req.hasTopNum()){ 
            if(req.hasId() || req.getMovingAvg() || req.getServiceTotals() || req.getSystemTotals())
                conflictParamException();
            if(req.hasSystemId() && req.hasHostTypeId())
                conflictParamException();
            return buildTopX(req).getIdList();
        }
        
        else if(req.hasHostTypeId()){
            if(req.hasId()) 
                conflictParamException();
            SwagrStructure struct = buildTimelog(req);
            return struct.getIdList();
        }
        
        else
            throw new Exception("Malformed Request: Your OptionsPB is missing "
                    + "at least one necessary field.");
    }
    
    @Override
    public SwagrComboBPB getSystemActivityCombo(SwagrOptionsPB req) throws Exception {
        SwagrComboBPB.Builder combo = SwagrComboBPB.newBuilder();
        
        SwagrStructure struct;
        
        if(req.hasService()){
            struct = buildAllForService(req);
            combo.addAllStats(struct.getIdList());
        }
        else if(req.hasTopNum()){ 
            if(req.hasId() || req.getMovingAvg() || req.getServiceTotals())
                conflictParamException();
            if(req.hasSystemId() && req.hasHostTypeId())
                conflictParamException();
            struct = buildTopX(req);
            combo.addAllStats(struct.getIdList());
        }
        
        else if(req.hasId()){ 
            if(req.hasTopNum() || req.hasHostTypeId() || req.getServiceTotals())
                conflictParamException();
            if(!req.hasRidOrQid())
                throw new Exception("Missing Parameter: If id is set, "
                        + "ridOrQid field must be set");
            struct = buildSingleReq(req);
            combo.addAllStats(struct.getIdList());
        }
        
        else if(req.getServiceTotals()){
            if(req.hasTopNum() || req.hasId() || req.getMovingAvg())
                conflictParamException();
            if((req.hasStartDt() && !req.hasEndDt()) || (req.hasEndDt() && !req.hasStartDt()))
                throw new Exception("Missing Parameter: If included, "
                        + "both startDt and endDt must be set.");
            struct = buildServiceTotals(req);
            combo.addAllStats(struct.getStringList());
        }
        
        else if(req.hasHostTypeId()){
            if(req.hasId()) 
                conflictParamException();
            struct = buildTimelog(req);
            combo.addAllStats(struct.getIdList());
        }
        
        else
            throw new Exception("Malformed Request: Your OptionsPB is missing "
                    + "at least one necessary field.");
        
        combo.setChart(struct.getFormattedChart());
        return combo.build();
    }
    
    private void conflictParamException() throws Exception{ 
        throw new Exception("Conflicting Parameters: You have set at least "
                + "one pair of conflicting fields.");
    }
    
    private void evaluateModifyingFlags(SwagrOptionsPB req, SwagrStructure struct) {
        if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
            struct.localizeIdToLA();
        if("ALL".equals(req.getLocation()) && req.getDataType()==0){
            if(req.getLocComposition() && !req.getDTypeComposition())
                struct.collapseIdDataTypesOnly();
            else if(!req.getLocComposition() && req.getDTypeComposition())
                struct.collapseIdLocationsOnly();
            else if (!req.getLocComposition() && !req.getDTypeComposition())
                struct.collapseIdBoth();
        }
        else if ("ALL".equals(req.getLocation())){
            if(!req.getLocComposition())
                struct.collapseIdLocationsOnly();
        }
        else if (0 == req.getDataType()){
            if(!req.getDTypeComposition())
                struct.collapseIdDataTypesOnly();
        }
        if(req.getRemoveWeekends())
            struct.removeWeekEnds();
    }
    
    private void evaluateStringModifyingFlags(SwagrOptionsPB req, SwagrStructure struct) {
        if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
            struct.localizeStringToLA();
        if("ALL".equals(req.getLocation()) && req.getDataType()==0){
            if(req.getLocComposition() && !req.getDTypeComposition())
                struct.collapseStringDataTypesOnly();
            else if(!req.getLocComposition() && req.getDTypeComposition())
                struct.collapseStringLocationsOnly();
            else
                struct.collapseStringBoth();
        }
        else if ("ALL".equals(req.getLocation())){
            if(!req.getLocComposition())
                struct.collapseStringLocationsOnly();
        }
        else if (0 == req.getDataType()){
            if(!req.getDTypeComposition())
                struct.collapseStringDataTypesOnly();
        }
        if(req.getRemoveWeekends())
                struct.removeWeekEnds();
    }
    
    private SwagrStructure buildAllForService(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct;
        struct = service(req);
        evaluateModifyingFlags(req,struct);
        struct.computeIdValues(req.getValType());
        return struct;
    }
    
    private SwagrStructure buildDeprecatedCalls(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct;
        struct = deprecatedService(req);
        evaluateModifyingFlags(req, struct);
        struct.computeIdValues(req.getValType());
        return struct;
    }
    
    private SwagrStructure buildServiceTotals(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct;
        struct = serviceTotals(req);
        evaluateStringModifyingFlags(req, struct);
//        if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
//            struct.localizeStringToLA();
//        if("ALL".equals(req.getLocation()) && req.getDataType()==0){
//            if(req.getLocComposition() && !req.getDTypeComposition())
//                struct.collapseStringDataTypesOnly();
//            else if(!req.getLocComposition() && req.getDTypeComposition())
//                struct.collapseStringLocationsOnly();
//            else
//                struct.collapseStringBoth();
//        }
//        else if ("ALL".equals(req.getLocation())){
//            if(!req.getLocComposition())
//                struct.collapseStringLocationsOnly();
//        }
//        else if (0 == req.getDataType()){
//            if(!req.getDTypeComposition())
//                struct.collapseStringDataTypesOnly();
//        }
//        if(req.getRemoveWeekends())
//                struct.removeWeekEnds();
        return struct;
    }
    
    private SwagrStructure buildSystemTotals(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct;
        struct = systemTotals(req);
        evaluateStringModifyingFlags(req, struct);
//        if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
//            struct.localizeStringToLA();
//        if("ALL".equals(req.getLocation()) && req.getDataType()==0){
//            if(req.getLocComposition() && !req.getDTypeComposition())
//                struct.collapseStringDataTypesOnly();
//            else if(!req.getLocComposition() && req.getDTypeComposition())
//                struct.collapseStringLocationsOnly();
//            else
//                struct.collapseStringBoth();
//        }
//        else if ("ALL".equals(req.getLocation())){
//            if(!req.getLocComposition())
//                struct.collapseStringLocationsOnly();
//        }
//        else if (0 == req.getDataType()){
//            if(!req.getDTypeComposition())
//                struct.collapseStringDataTypesOnly();
//        }
//        if(req.getRemoveWeekends())
//            struct.removeWeekEnds();
        return struct;
    }
    
    private SwagrStructure buildTopX(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct;
        if(req.hasSystemId()){ //sys request block\
            struct = topXSys(req);
            evaluateModifyingFlags(req, struct);
            struct.computeIdValues(req.getValType());
//            if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
//                struct.localizeIdToLA();
//            if("ALL".equals(req.getLocation()) && req.getDataType()==0){
//                if(req.getLocComposition() && !req.getDTypeComposition())
//                    struct.collapseIdDataTypesOnly();
//                else if(!req.getLocComposition() && req.getDTypeComposition())
//                    struct.collapseIdLocationsOnly();
//                else if (!req.getLocComposition() && !req.getDTypeComposition())
//                    struct.collapseIdBoth();
//            }
//            else if ("ALL".equals(req.getLocation())){
//                if(!req.getLocComposition())
//                    struct.collapseIdLocationsOnly();
//            }
//            else if (0 == req.getDataType()){
//                if(!req.getDTypeComposition())
//                    struct.collapseIdDataTypesOnly();
//            }
//            struct.computeIdValues(req.getValType());
//            if(req.getRemoveWeekends())
//                struct.removeWeekEnds();
            return struct;
        }
        else if(req.hasHostTypeId()){ //sys query block
            struct = topXQ(req);
            struct.computeIdValues(req.getValType());
            if(req.getRemoveWeekends())
                struct.removeWeekEnds();
            return struct;
        }
        else
            throw new Exception("Missing Parameter: Top X request requires "
                    + "systemId or hostTypeId.");
    }
    
    private SwagrStructure buildSingleReq(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct = new SwagrStructure();
        if(req.getMovingAvg() || req.getRidOrQid() == 0){
            if(req.getMovingAvg())
                struct = movingAvg(req);
            else
                struct = singleReq(req);
            evaluateModifyingFlags(req, struct);
//            if(req.getTimeShift() == 1 && "ALL".equals(req.getLocation()))
//                struct.localizeIdToLA();
//            if("ALL".equals(req.getLocation()) && req.getDataType()==0){
//                if(req.getLocComposition() && !req.getDTypeComposition())
//                    struct.collapseIdDataTypesOnly();
//                else if(!req.getLocComposition() && req.getDTypeComposition())
//                    struct.collapseIdLocationsOnly();
//                else
//                    struct.collapseIdBoth();
//            }
//            else if ("ALL".equals(req.getLocation())){
//                if(!req.getLocComposition())
//                    struct.collapseIdLocationsOnly();
//            }
//            else if (0 == req.getDataType()){
//                if(!req.getDTypeComposition())
//                    struct.collapseIdDataTypesOnly();
//            }
        }
        else if(req.getRidOrQid() == 1)
            struct = singleQuery(req);
        if(!req.getMovingAvg())
            struct.computeIdValues(req.getValType());
//        if(req.getRemoveWeekends())
//                struct.removeWeekEnds();
        return struct;
    }
    
    private SwagrStructure buildTimelog(SwagrOptionsPB req) throws Exception{
        SwagrStructure struct;
        struct = timelog(req);
        struct.computeIdValues(req.getValType());
        if(req.getRemoveWeekends())
            struct.removeWeekEnds();
        return struct;
    }
    
    private List<SwagrServicePB> serviceList() throws Exception{
        try(JdbcService jdbc = swagrJdbcFac.newService(SERVICES)) {
            ResultSet rs = jdbc.executeQuery();
            return serviceListResSet(rs);
        }
    }
    
    @Override
    public SwagrServicePB getRequestTypes(String service) throws Exception {
        SwagrServicePB.Builder servicePB = SwagrServicePB.newBuilder();
        servicePB.setService(service);
        
        try (JdbcService jdbc = swagrJdbcFac.newService(SERVICE_REQUEST_TYPES)) {
            jdbc.getPreparedStatement().setString(1, service);
            ResultSet rs = jdbc.executeQuery();
            while (rs.next()) {
                servicePB.addRequests(
                        SwagrRequestPB.newBuilder()
                        .setId(rs.getInt(1))
                        .setFunction(rs.getString(2))
                        .setArgs(rs.getString(3))
                        .build());
            }
        }
        return servicePB.build();
    }
    
    
    private SwagrStructure timelog(SwagrOptionsPB req) throws Exception{
        Date start;
        Date end;
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        String request = TIMELOG_DAY;
        if(req.hasStartDt() && req.hasEndDt()){
        //    if((req.getStartDt()-req.getEndDt())<=604800)
        //        request = TIMELOG_HOUR;
            start = new Date(req.getStartDt());
            end = new Date(req.getEndDt());
        }
        else{
            start = new Date(startdt.getMillis());
            end = new Date(enddt.getMillis());
        }
            
        try (JdbcService jdbc = drjJdbcFac.newService(request)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,start);
            ps.setDate(2,end);
            ps.setInt(3,req.getHostTypeId());
            ResultSet rs = jdbc.executeQuery();
            return timelogResSet(rs,req.getHostTypeId());
        }
    }
    
    private SwagrStructure systemTotals(SwagrOptionsPB req) throws Exception{
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        String dtype = "%";
        String loc = "%";
        String systemTotals = SYSTEM_TOTALS_COUNT;
        
        if(req.hasDataType())
            if(req.getDataType() != 0)
                dtype = Integer.toString(req.getDataType());
        if(req.hasLocation())
            if(!"ALL".equals(req.getLocation()))
                loc = req.getLocation();
        
        int valType = req.getValType();
        if(valType == 2)
            systemTotals = SYSTEM_TOTALS_LOAD;
        else if (valType == 3)
            systemTotals = SYSTEM_TOTALS_DURATION;
        
        try(JdbcService jdbc = swagrJdbcFac.newService(systemTotals)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1, req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2, req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setString(3, dtype);
            ps.setString(4, loc);
            ps.setInt(5, req.getSystemId());
            ResultSet rs = jdbc.executeQuery();
            return systemTotalsResultSet(rs, req.getSystemId());
        }
    }
    
    private SwagrStructure serviceTotals(SwagrOptionsPB req) throws Exception{
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        String dtype = "%";
        String loc = "%";
        String serviceQuery = SERVICE_TOTALS_COUNT;
        
        if(req.hasDataType())
            if(req.getDataType() != 0)
                dtype = Integer.toString(req.getDataType());
        if(req.hasLocation())
            if(!"ALL".equals(req.getLocation()))
                loc = req.getLocation();
        
        int valType = req.getValType();
        if(valType == 2)
            serviceQuery = SERVICE_TOTALS_LOAD;
        else if (valType == 3)
            serviceQuery = SERVICE_TOTALS_DURATION;
        
        try(JdbcService jdbc = swagrJdbcFac.newService(serviceQuery)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1, req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2, req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setString(3, dtype);
            ps.setString(4, loc);
            ps.setInt(5, req.getSystemId());
            ResultSet rs = jdbc.executeQuery();
            return serviceResultSet(rs);
        }
    }
    
    private SwagrStructure singleReq(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        String tempLoc;
        String tempDType;
        if("ALL".equals(req.getLocation()))
            tempLoc = "%";
        else
            tempLoc = req.getLocation();
        if(req.getDataType() == 0)
            tempDType = "%";
        else
            tempDType = Integer.toString(req.getDataType());
        
        try(JdbcService jdbc = swagrJdbcFac.newService(SINGLE_REQ)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setInt(3,req.getId());
            ps.setString(4,tempLoc);
            ps.setString(5,tempDType);
            ResultSet rs = jdbc.executeQuery();
            sysStatsDailyResSet(rs,struct);
        }
        
        try(JdbcService jdbc = swagrJdbcFac.newService(SWAGR_SINGLE_STRINGS_SYS)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setInt(1,req.getId());
            ResultSet rs = jdbc.executeQuery();
            reqStrings(rs,struct);
            return struct;
        }
    }
    
    private SwagrStructure singleQuery(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        
        try(JdbcService jdbc = swagrJdbcFac.newService(SINGLE_QUERY)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setInt(3,req.getId());
            ResultSet rs = jdbc.executeQuery();
            queryStatsDailyResSet(rs,struct);
        }
        
        try(JdbcService jdbc = swagrJdbcFac.newService(DRJ_SINGLE_STRING)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setInt(1,req.getId());
            ResultSet rs = jdbc.executeQuery();
            qStrings(rs,struct);
            return struct;
        }
    }
    
    private SwagrStructure movingAvg(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct;
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        
        String tempLoc;
        if("ALL".equals(req.getLocation()))
            tempLoc = "%";
        else
            tempLoc = req.getLocation();
        
        String tempDType;
        if(req.getDataType() == 0)
            tempDType = "%";
        else
            tempDType = Integer.toString(req.getDataType());
        
        //where start,end,loc,dtype,rid,valtype
        try(JdbcService jdbc = swagrJdbcFac.newService(MOVING_AVG)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setString(3,tempLoc);
            ps.setString(4,tempDType);
            ps.setInt(5,req.getId());
            ps.setInt(6,req.getValType());
            ResultSet rs = jdbc.executeQuery();
            struct = movingAvgResSet(rs,req.getValType());
        }
        
        try(JdbcService jdbc = swagrJdbcFac.newService(SWAGR_SINGLE_STRINGS_SYS)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setInt(1,req.getId());
            ResultSet rs = jdbc.executeQuery();
            reqStrings(rs,struct);
            return struct;
        }
        
    }
    
    private SwagrStructure topXSys(SwagrOptionsPB req) throws Exception { 
        SwagrStructure struct = new SwagrStructure();
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        String temploc;
        if("ALL".equals(req.getLocation()))
            temploc = "%";
        else
            temploc = req.getLocation();
        if(req.getDataType() == 0){
            
            //start,end,loc,start,end,topnum,loc,sysid,type
            try (JdbcService jdbc = swagrJdbcFac.newService(SWAGR_TOP_SYS_DISTINCT)) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setString(3,temploc);
                ps.setDate(4,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(5,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setInt(6,req.getTopNum());
                ps.setString(7,req.getLocation());
                ps.setInt(8,req.getSystemId());
                ps.setInt(9,req.getValType());
                ResultSet rs = jdbc.executeQuery();
                sysStatsDailyResSet(rs,struct);
                jdbc.close();
            }
        }
        else{
            //start,end,dtype,loc,start,end,topnum,loc,sysid,dtype,valtype
            try (JdbcService jdbc = swagrJdbcFac.newService(S_TOP_DATA_DISTINCT)) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setInt(3, req.getDataType());
                ps.setString(4,temploc);
                ps.setDate(5,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(6,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setInt(7,req.getTopNum());
                ps.setString(8,req.getLocation()); 
                ps.setInt(9,req.getSystemId());
                ps.setInt(10,req.getDataType());
                ps.setInt(11,req.getValType());
                ResultSet rs = jdbc.executeQuery();
                sysStatsDailyResSet(rs,struct);
                jdbc.close();            
            }
        }     
        
        if(req.getDataType()==0){
            try(JdbcService jdbc = swagrJdbcFac.newService(SWAGR_STRINGS_SYS)) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setInt(3,req.getTopNum());
                ps.setString(4,req.getLocation());
                ps.setInt(5,req.getSystemId());
                ResultSet rs = jdbc.executeQuery();
                reqStrings(rs,struct);
                return struct;
            }
        }
        else{
            try(JdbcService jdbc = swagrJdbcFac.newService(SWAGR_STRINGS_DATA)) {
                PreparedStatement ps = jdbc.getPreparedStatement();
                ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
                ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
                ps.setInt(3,req.getTopNum());
                ps.setString(4,req.getLocation());
                ps.setInt(5,req.getSystemId());
                ps.setInt(6,req.getDataType());
                ResultSet rs = jdbc.executeQuery();
                reqStrings(rs,struct);
                return struct;
            } 
        }
    }
    
    private SwagrStructure topXQ(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        
        try (JdbcService jdbc = drjJdbcFac.newService(DRJ_TOP_STATS)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setInt(1,req.getHostTypeId());
            ps.setDate(2,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(3,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setDate(4,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(5,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setInt(6,req.getTopNum());
            ps.setInt(7,req.getHostTypeId());
            ResultSet rs = jdbc.executeQuery();
            queryStatsDailyResSet(rs,struct);
        }
        
        try (JdbcService jdbc = drjJdbcFac.newService(DRJ_STRINGS)) {
            PreparedStatement ps2 = jdbc.getPreparedStatement();
            ps2.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps2.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps2.setInt(3,req.getTopNum());
            ps2.setInt(4,req.getHostTypeId());
            ResultSet rs2 = jdbc.executeQuery();
            qStrings(rs2,struct);
            return struct;
        }
    }
    
    private SwagrStructure service(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        
        //start,end,location,service
        try (JdbcService jdbc = swagrJdbcFac.newService(SWAGR_SERVICES)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setString(3,req.hasLocation() ? req.getLocation() : "%");
            ps.setString(4,req.getDataType() == 0 ? "%" : Integer.toString(req.getDataType()));
            ps.setString(5,req.getService());
            ResultSet rs = jdbc.executeQuery();
            sysStatsDailyResSet(rs, struct);
        }
        
        try (JdbcService jdbc = swagrJdbcFac.newService(SWAGR_SERVICE_STRINGS_SYS)) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setString(1,req.getService());
            ResultSet rs = jdbc.executeQuery();
            reqStrings(rs, struct);
            return struct;
        }
    }
    
    private class RequestKey {
        private final String service,function,args;
        public RequestKey(String service, String function, String args) {
            this.service = service;
            this.function = function;
            this.args = args;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.service != null ? this.service.hashCode() : 0);
            hash = 59 * hash + (this.function != null ? this.function.hashCode() : 0);
            hash = 59 * hash + (this.args != null ? this.args.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RequestKey other = (RequestKey) obj;
            if ((this.service == null) ? (other.service != null) : !this.service.equals(other.service)) {
                return false;
            }
            if ((this.function == null) ? (other.function != null) : !this.function.equals(other.function)) {
                return false;
            }
            if ((this.args == null) ? (other.args != null) : !this.args.equals(other.args)) {
                return false;
            }
            return true;
        }
    }

    private static final String SELECT_SYSTEM_REQUESTS =
          "SELECT id,service,function,args FROM requests WHERE systemid=?";  
    
    private SwagrStructure deprecatedService(SwagrOptionsPB req) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        DateTime enddt = new DateTime();
        DateTime startdt = enddt.minusDays(30);
        
        Map<RequestKey,Integer> requestIDMap = new HashMap<>();

        try (JdbcService jdbc = swagrJdbcFac.newService(SELECT_SYSTEM_REQUESTS)) {
            jdbc.getPreparedStatement().setInt(1, req.getSystemId());
            ResultSet rs = jdbc.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                requestIDMap.put(new RequestKey(rs.getString(2),rs.getString(3),rs.getString(4)),id);
            }
        }
        
        List<Integer> depReqIds = new ArrayList<>();
        
        InfoClient infoClient = InfoClientFactory.getClient(LouieConnectionFactory.getSharedConnection());
        
        List<ServicePB> services;
        if (req.getService().equals("ALL"))  {
            services = infoClient.getAllServices();
        } else {
            services = Collections.singletonList(infoClient.getService(req.getService()));
        }
            
        for (ServicePB serviceData : services) {
            for (MethodPB method : serviceData.getMethodsList()) {
                if (method.getDeprecated()) {
                    StringBuilder paramList = new StringBuilder();
                    for (ParamPB param : method.getParamsList()) {
                        paramList.append(param.getType()).append(",");
                    }
                    if (paramList.length() != 0) {
                        paramList.deleteCharAt(paramList.length() - 1);
                    }
                    RequestKey rkey = new RequestKey(serviceData.getName(), method.getName(), paramList.toString());
                    Integer rid = requestIDMap.get(rkey);
                    if (rid != null) {
                        depReqIds.add(rid);
                    }
                }
            }
        }

        if (depReqIds.isEmpty()){
            return struct;
        }
        StringBuilder depQuery = new StringBuilder();
        for (int i = 0; i<depReqIds.size(); i++) {
            depQuery.append("?");
            if (i<depReqIds.size()-1) {
                depQuery.append(",");
            }
        }
        depQuery.append(")");
        
        try (JdbcService jdbc = swagrJdbcFac.newService(SWAGR_SERVICES_DEPRECATED + depQuery.toString())) {
            PreparedStatement ps = jdbc.getPreparedStatement();
            ps.setDate(1,req.hasStartDt() ? new Date(req.getStartDt()) : new Date(startdt.getMillis()));
            ps.setDate(2,req.hasEndDt() ? new Date(req.getEndDt()) : new Date(enddt.getMillis()));
            ps.setString(3,req.hasLocation() ? req.getLocation() : "%");
            ps.setString(4,req.getDataType() == 0 ? "%" : Integer.toString(req.getDataType()));
            int i = 5;
            for (int id : depReqIds) {
                ps.setInt(i++, id);
            }
            ResultSet rs = jdbc.executeQuery();
            sysStatsDailyResSet(rs, struct);
        }
        try (JdbcService jdbc = swagrJdbcFac.newService(SWAGR_STRINGS_PREFIX 
                    + " WHERE r.id IN (" 
                    + depQuery.toString())) { 
            PreparedStatement ps = jdbc.getPreparedStatement();
            
            int i = 1;
            for (int id : depReqIds) {
                ps.setInt(i++, id);
            }
            ResultSet rs = jdbc.executeQuery();
            reqStrings(rs, struct);
            return struct;
        }
    }
    
    private SwagrStructure timelogResSet(ResultSet rs, int id) throws Exception{
        SwagrStructure struct = new SwagrStructure();
        while(rs.next()){
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setCount(rs.getInt(i++));
            builder.setAveTime(rs.getInt(i++));
            builder.setFails(rs.getInt(i++));
            builder.setId(id);
            struct.addId(builder);
        }
        return struct;
    }
    
    private SwagrStructure movingAvgResSet(ResultSet rs, 
            int valType) throws Exception{
        SwagrStructure struct = new SwagrStructure();
        while(rs.next()){
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            builder.setId(rs.getInt(i++));
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setValue(rs.getLong(i++));
            builder.setDataType(rs.getString(i++));
            builder.setLocation(rs.getString(i++));
            builder.setValueType(valType);
            struct.addId(builder);
        }
        return struct;
    }
    
    private SwagrStructure serviceResultSet(ResultSet rs) 
            throws Exception{
        SwagrStructure struct = new SwagrStructure();
        while(rs.next()){
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            builder.setIdString(rs.getString(i++));
            builder.setDataType(rs.getString(i++));
            builder.setLocation(rs.getString(i++));
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setValue(rs.getLong(i++));
            struct.addString(builder);
        }
        return struct;
    }
    
    private SwagrStructure systemTotalsResultSet(ResultSet rs, int systemID) throws Exception {
        SwagrStructure struct = new SwagrStructure();
        String serviceName = "LoUIE";
        if (systemID == 2){
            serviceName = "JobTracker";
        }
        while(rs.next()){
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            builder.setIdString(serviceName);                                   
            builder.setDataType(rs.getString(i++));
            builder.setLocation(rs.getString(i++));
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setValue(rs.getLong(i++));
            struct.addString(builder);
        }
        return struct;
    }
    
    //qid,dt,hosttype,count,min,max,ave,fails,averows,avebytes
    private void queryStatsDailyResSet(ResultSet rs, 
            SwagrStructure struct) throws Exception{  
        while(rs.next()){ 
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            builder.setId(rs.getInt(i++));
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setHostTypeId(rs.getInt(i++));
            builder.setCount(rs.getInt(i++));
            builder.setMinTime(rs.getInt(i++));
            builder.setMaxTime(rs.getInt(i++));
            builder.setAveTime(rs.getInt(i++));
            builder.setFails(rs.getInt(i++));
            builder.setAveRows(rs.getInt(i++));
            builder.setAveBytes(rs.getLong(i++));
            struct.addId(builder);
        }
        rs.close();
    }
    
    private void qStrings(ResultSet rs, 
            SwagrStructure struct) throws Exception{
        while(rs.next()){
            int genid = rs.getInt(1);
            int qid = rs.getInt(2);
            String target = rs.getString(3);
            String table = rs.getString(4);
            String where = rs.getString(5);
            String type = rs.getString(6); 
            StringBuilder query = new StringBuilder();
            if(genid==1)
                query.append("select ").append(target).append(" from ").append(table).append(" where ").append(where);
            else if (genid==2)
                query.append("insert into ").append(table).append(" ").append(target).append(" ").append(where);
            else if(genid==3)
                query.append("update ").append(table).append(" set ").append(target).append(" where ").append(where);
            else if(genid==4)
                query.append("delete from ").append(table).append(" where ").append(where);
            else if (genid==5)
                query.append("execute procedure ").append(target).append(" where ").append(where);
            else if (genid==6)
                query.append("execute function ").append(target).append(" where ").append(where);
            else
                query.append("query was malformed or misread from logs");
            struct.updateIdString(query.toString(),qid);
        }
        rs.close();
    }
    
    //rid,time,count,mint,avet,maxt,aveb,maxb,aver,maxr,fails,loc,dtypename
    private void sysStatsDailyResSet(ResultSet rs, 
            SwagrStructure struct) throws Exception{  
        while(rs.next()){ 
            int i = 1;
            SwagrStatPB.Builder builder = SwagrStatPB.newBuilder();
            builder.setId(rs.getInt(i++));
            long time = rs.getLong(i++);
            builder.setTime(time);
            builder.setTimeString(utcToString(time));
            builder.setCount(rs.getInt(i++));
            builder.setMinTime(rs.getInt(i++));
            builder.setAveTime(rs.getInt(i++));
            builder.setMaxTime(rs.getInt(i++));
            builder.setAveBytes(rs.getLong(i++));
            builder.setMaxBytes(rs.getLong(i++));
            builder.setAveRows(rs.getInt(i++));
            builder.setMaxRows(rs.getInt(i++));
            builder.setFails(rs.getInt(i++));
            builder.setLocation(rs.getString(i++));
            builder.setDataType(rs.getString(i++));
            struct.addId(builder);
        }
        rs.close();
    }
    
    private List<SwagrServicePB> serviceListResSet(ResultSet rs) throws Exception{
        List<SwagrServicePB> servList = new ArrayList<>();
        SwagrServicePB.Builder all = SwagrServicePB.newBuilder();
        all.setService("ALL");
        all.setSystemid(1);
        servList.add(all.build());
        while(rs.next()){
            SwagrServicePB.Builder serv = SwagrServicePB.newBuilder();
            serv.setService(rs.getString(1));
            serv.setSystemid(rs.getInt(2));
            servList.add(serv.build());
        }
        return servList;
    }

    private void reqStrings(ResultSet rs, SwagrStructure struct) 
            throws Exception{
        while(rs.next()){
            StringBuilder str = new StringBuilder();
            int rid = rs.getInt(1);
            str.append(rs.getString(2)).append(":");
            str.append(rs.getString(3)).append("(");
            str.append(rs.getString(4)).append(") on system ");
            str.append(rs.getString(5));
            struct.updateIdString(str.toString(), rid);
        }
        rs.close();
    }
    
    private String utcToString(long dt){
        DateTime jodadt = new DateTime(dt*1000);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        return fmt.print(jodadt);
    }

    @Override
    public SwagrChartBPB getSystemActivityChartFormatted(SwagrOptionsPB req) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    


}
