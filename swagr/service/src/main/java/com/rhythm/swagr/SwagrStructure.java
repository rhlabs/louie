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

import com.rhythm.pb.swagr.SwagrProtos.*;
import java.util.*;
import org.joda.time.DateTime;
/**
 *
 * @author eyasukoc
 */
public class SwagrStructure {
    
    private Map<Integer,Map<Long,List<SwagrStatPB.Builder>>> idMap = //rid : map<date:list<SwagrStat>> so per rid, everything on a day (WHY IS THE END THING A LIST?) seriously
            new HashMap<Integer,Map<Long,List<SwagrStatPB.Builder>>>();
    
    private Map<String,Map<Long,List<SwagrStatPB.Builder>>> stringMap = 
            new HashMap<String,Map<Long,List<SwagrStatPB.Builder>>>();

    private Map<Long,String> dateMap = new HashMap<Long,String>(); //to make building the chart formatted stuff easier
    
    public SwagrStructure(){};
    
    public void addId(SwagrStatPB.Builder stat){
        int id = stat.getId();
        long dt = stat.getTime();
        dateMap.put(dt,stat.getTimeString()); //hacky
        Map<Long,List<SwagrStatPB.Builder>> dtMap= idMap.get(id); 
        if(dtMap!=null){
            List<SwagrStatPB.Builder> statList = dtMap.get(dt);
            if(statList!=null)
                statList.add(stat);
            else{
                List<SwagrStatPB.Builder> list = 
                        new ArrayList<SwagrStatPB.Builder>();
                list.add(stat);
                dtMap.put(dt,list);
            }
        }
        else{
            List<SwagrStatPB.Builder> newList = 
                    new ArrayList<SwagrStatPB.Builder>();
            newList.add(stat);
            Map<Long,List<SwagrStatPB.Builder>> newDtMap = 
                    new HashMap<Long,List<SwagrStatPB.Builder>>();
            newDtMap.put(dt,newList);
            idMap.put(id,newDtMap); 
        }
    }
    
    public void addString(SwagrStatPB.Builder stat){
        String id = stat.getIdString();
        long dt = stat.getTime();
        dateMap.put(dt,stat.getTimeString()); //hacky
        Map<Long,List<SwagrStatPB.Builder>> dtMap= stringMap.get(id); 
        if(dtMap!=null){
            List<SwagrStatPB.Builder> statList = dtMap.get(dt);
            if(statList!=null)
                statList.add(stat);
            else{
                List<SwagrStatPB.Builder> list = 
                        new ArrayList<SwagrStatPB.Builder>();
                list.add(stat);
                dtMap.put(dt,list);
            }
        }
        else{
            List<SwagrStatPB.Builder> newList = 
                    new ArrayList<SwagrStatPB.Builder>();
            newList.add(stat);
            Map<Long,List<SwagrStatPB.Builder>> newDtMap = 
                    new HashMap<Long,List<SwagrStatPB.Builder>>();
            stringMap.put(id,newDtMap); 
        }
    }
    
    public void updateIdString(String idStr, int id){
        Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(id);
        if(dtMap!=null){
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                for(SwagrStatPB.Builder b : pbList){
                    b.setIdString(idStr);
                }
            }
        }
    }
    
    public void updateIdString(String idStr, String id){
        Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(id);
        if(dtMap!=null){
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                for(SwagrStatPB.Builder b : pbList){
                    b.setIdString(idStr);
                }
            }
        }
    }
    
    public void computeIdValues(int valType){
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            computeInternals(valType,dtMap);
        }
    }
    
    @Deprecated
    public void computeStringValues(int valType){
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            computeInternals(valType,dtMap);
        }
    }
    
    private void computeInternals(int valType, //what a shit name
            Map<Long,List<SwagrStatPB.Builder>> dtMap){
        for(long dtKey : dtMap.keySet()){
            List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
            for(SwagrStatPB.Builder b : pbList){
                if(valType == 2){
                    if(b.getAveBytes()<1)
                        b.setValue(b.getCount());
                    else
                        b.setValue(b.getCount()*b.getAveBytes());
                    b.setValueType(2);
                }
                else if(valType == 3){
                    if(b.getAveTime()<1)
                        b.setValue(b.getCount());
                    else
                        b.setValue(b.getCount()*b.getAveTime());
                    b.setValueType(3);
                }
                else{
                    b.setValue(b.getCount());
                    b.setValueType(1);
                }
            }
        }
    }
    
    public void localizeIdToLA(){ 
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            localizeInternals(dtMap);
        }
    }
    
    public void localizeStringToLA(){ 
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            localizeInternals(dtMap);
        }
    }
    
    private void localizeInternals(Map<Long,List<SwagrStatPB.Builder>> dtMap){
        for(long dtKey : dtMap.keySet()){
            List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
            for(SwagrStatPB.Builder b : pbList){
                String loc = b.getLocation();
                String dtype = b.getDataType();
                if("HYD".equals(loc)|| "KL".equals(loc) || "MUM".equals(loc) || "CAVE".equals(loc)){
                    List<SwagrStatPB.Builder> nxtList = dtMap.get(b.getTime()+86400); 
                    if(nxtList != null){ 
                        for(SwagrStatPB.Builder bTwo : nxtList){
                            if(bTwo.getLocation().equals(loc) && bTwo.getDataType().equals(dtype)){
                                double perCurr = 1;
                                double perNext = 1;
                                if("HYD".equals(loc) || "MUM".equals(loc)){
                                    perCurr = .48;
                                    perNext = .52;
                                }
                                if("KL".equals(loc) || "CAVE".equals(loc)){
                                    perCurr = .38;
                                    perNext = .62;
                                }
                                b.setCount((int)Math.round((perCurr*b.getCount())+(perNext*bTwo.getCount())));
                                b.setMinTime((int)Math.round((perCurr*b.getMinTime())+(perNext*bTwo.getMinTime())));
                                b.setAveTime((int)Math.round((perCurr*b.getAveTime())+(perNext*bTwo.getAveTime())));
                                b.setMaxTime((int)Math.round((perCurr*b.getMaxTime())+(perNext*bTwo.getMaxTime())));
                                b.setAveRows((int)Math.round((perCurr*b.getAveRows())+(perNext*bTwo.getAveRows())));
                                b.setMaxRows((int)Math.round((perCurr*b.getMaxRows())+(perNext*bTwo.getMaxRows())));
                                b.setAveBytes((long)Math.round((perCurr*b.getAveBytes())+(perNext*bTwo.getAveBytes())));
                                b.setMaxBytes((long)Math.round((perCurr*b.getMaxBytes())+(perNext*bTwo.getMaxBytes())));
                                b.setFails((int)Math.round((perCurr*b.getFails())+(perNext*bTwo.getFails())));
                                if(b.hasValue()){
                                    int adjValue = (int)Math.round((perCurr*b.getValue())+(perNext*bTwo.getValue()));
                                    b.setValue(adjValue);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void collapseIdDataTypesOnly(){ 
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            collapseDataTypesInternals(dtMap);
        }
    }
    
    public void collapseStringDataTypesOnly(){ 
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            collapseDataTypesInternals(dtMap);
        }
    }
    
    private void collapseDataTypesInternals(
            Map<Long,List<SwagrStatPB.Builder>> dtMap){
        for(long dtKey : dtMap.keySet()){
            List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
            Map<String,SwagrStatPB.Builder> allMap = 
                    new HashMap<String,SwagrStatPB.Builder>();
            for(SwagrStatPB.Builder b : pbList){
                SwagrStatPB.Builder tempAll = allMap.get(b.getLocation());
                b.setDataType("ALL");
                if(tempAll!=null)
                    allMap.put(b.getLocation(),combineBuilders(b,tempAll));
                else
                    allMap.put(b.getLocation(),b);
            }
            pbList.clear();
            pbList.addAll(allMap.values());
        }
    }
    
    public void collapseIdLocationsOnly(){
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            collapseLocsInternals(dtMap);
        }
    }
    
    public void collapseStringLocationsOnly(){
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            collapseLocsInternals(dtMap);
        }
    }
    
    private void collapseLocsInternals(Map<Long,List<SwagrStatPB.Builder>> dtMap){
        for(long dtKey : dtMap.keySet()){
            List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
            Map<String,SwagrStatPB.Builder> allMap = 
                    new HashMap<String,SwagrStatPB.Builder>();
            for(SwagrStatPB.Builder b : pbList){
                SwagrStatPB.Builder tempAll = allMap.get(b.getDataType());
                b.setLocation("ALL");
                if(tempAll!=null)
                    allMap.put(b.getDataType(),combineBuilders(b,tempAll));
                else
                    allMap.put(b.getDataType(),b);
            }
            pbList.clear(); 
            pbList.addAll(allMap.values());
        }
    }
    
    public void collapseIdBoth(){
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                SwagrStatPB.Builder all = SwagrStatPB.newBuilder();
                all.setLocation("ALL");
                all.setDataType("ALL");
                all.setId(idKey);
                all.setTime(dtKey);
                for(SwagrStatPB.Builder b : pbList){
                    all.setTimeString(b.getTimeString());
                    if(b.hasIdString())
                        all.setIdString(b.getIdString());
                    combineBuilders(b,all);
                }
                pbList.clear(); 
                pbList.add(all);
            }
        }
    }
    
    public void collapseStringBoth(){
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                SwagrStatPB.Builder all = SwagrStatPB.newBuilder();
                all.setLocation("ALL");
                all.setDataType("ALL");
                all.setIdString(idKey);
                all.setTime(dtKey);
                for(SwagrStatPB.Builder b : pbList){
                    all.setTimeString(b.getTimeString());
                    if(b.hasIdString())
                        all.setIdString(b.getIdString());
                    combineBuilders(b,all);
                }
                pbList.clear(); 
                pbList.add(all);
            }
        }
    }

    public SwagrChartBPB getFormattedChart(){
        SwagrChartBPB.Builder chart = SwagrChartBPB.newBuilder();
        SwagrChartFieldPB.Builder dateField = SwagrChartFieldPB.newBuilder();
        dateField.setName("Time");
        dateField.setType("string");
        chart.addChartFields(dateField.build());
        //iterate through the maps highest level to get a list of ids, this is for the FieldPB
        //during that iteration, get keys to the next level's map: this is all of the dates. \
        //add these to a treeset (or whatever is order guaranteed and can be sorted by date)
        //we need order to be guaranteed in every list.
        //then, for each date, for each id, iterate through the final list of statPB and extract the value field into the data field
        Set<Integer> idSet = new HashSet<Integer>(idMap.keySet());
        chart.addAllQueryId(idSet);
        for(int id : idSet){
            SwagrChartFieldPB.Builder field = SwagrChartFieldPB.newBuilder();
            field.setName(Integer.toString(id));
            field.setType("int");
            chart.addChartFields(field.build());
        }
        for(long dt : dateMap.keySet()){
            SwagrChartDataBPB.Builder data = SwagrChartDataBPB.newBuilder();
            for(int id : idSet){
                if (idMap.get(id).containsKey(dt)){
                    data.setXAxisParam(dateMap.get(dt));
                    SwagrStatPB.Builder valStat = idMap.get(id).get(dt).get(0);
                    SwagrChartDataPB.Builder value = SwagrChartDataPB.newBuilder();
                    value.setFieldName(Integer.toString(id));
                    value.setValue(valStat.getValue());
                    data.addDataPoints(value.build());
                }
            }
            chart.addData(data.build()); //works but will still return an empty data object for weekend days that were skipped or that's the way it looks anylames.
        }
        
        return chart.build();
    }
    
    public List<SwagrBPB> getIdList(){
        List<SwagrBPB> idList = new ArrayList<SwagrBPB>();
        for(int idKey : idMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
            SwagrBPB.Builder built = SwagrBPB.newBuilder();
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                SwagrStatPB.Builder sample = pbList.get(0);
                built.setId(sample.getId());
                built.setIdString(sample.getIdString());
//                built.setValueType(sample.getValueType()==0 ? 1 : sample.getValueType());
//                System.out.println("pbList length: " + pbList.size());
                for(SwagrStatPB.Builder b : pbList){ //this for loop is always one iteration, what the crap.
                    b.clearId();
                    b.clearIdString();
                    b.clearValueType();
                    built.addStats(b.build());
                } 
            }
            idList.add(built.build());
        }
        return idList;
    }
    
    public List<SwagrBPB> getStringList(){
        List<SwagrBPB> idList = new ArrayList<SwagrBPB>();
        for(String idKey : stringMap.keySet()){
            Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
            SwagrBPB.Builder built = SwagrBPB.newBuilder();
            for(long dtKey : dtMap.keySet()){
                List<SwagrStatPB.Builder> pbList = dtMap.get(dtKey);
                SwagrStatPB.Builder sample = pbList.get(0);
                built.setIdString(sample.getIdString());
//                built.setValueType(sample.getValueType()==0 ? 1 : sample.getValueType());
                for(SwagrStatPB.Builder b : pbList){
                    b.clearId();
                    b.clearIdString();
                    b.clearValueType();
                    built.addStats(b.build());
                } 
            }
            idList.add(built.build());
        }
        return idList;
    }
    
    public void removeWeekEnds(){ 
        List<Long> toBeRemoved = new ArrayList<Long>(); 
        if(!idMap.isEmpty()){
            for(int idKey : idMap.keySet()){
                Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
                for(long dtKey : dtMap.keySet()){
                    DateTime dt = new DateTime(dtKey*1000);
                    if(dt.getDayOfWeek() == 6 || dt.getDayOfWeek() == 7)
                        toBeRemoved.add(dtKey);
                }
                for(long rmKey : toBeRemoved){
                    dtMap.remove(rmKey);
                }
            }
        }
        else{
            for(String idKey : stringMap.keySet()){
                Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
                for(long dtKey : dtMap.keySet()){
                    DateTime dt = new DateTime(dtKey*1000);
                    if(dt.getDayOfWeek() == 6 || dt.getDayOfWeek() == 7)
                       toBeRemoved.add(dtKey); 
                }
                for(long rmKey : toBeRemoved){
                    dtMap.remove(rmKey);
                }
            }
        }
    }
    
    public void compressWeeks(){ 
        /*
         * Days 1-7 need to be average together into Day 1 and then Days 2-7 need to be dropped from corresponding date map
         * dtmaps are hashmaps so i won't know the order. for days 2-7, i need to recompute monday and shove current data into it, issue there is that successive averages are not the same as one total average.
         * Instead of iterating through the datemap i could just calculate every monday within the given date range?
         * Then, iterate through mondays, calculating every other day of the week and accessing it, then adding it to a toBeRemoved list. 
         */
        List<Long> toBeRemoved = new ArrayList<Long>(); 
        if(!idMap.isEmpty()){
            for(int idKey : idMap.keySet()){
                Map<Long,List<SwagrStatPB.Builder>> dtMap = idMap.get(idKey);
                for(long dtKey : dtMap.keySet()){
                    DateTime dt = new DateTime(dtKey*1000);
                    int dayOfWk = dt.getDayOfWeek();
                    if( dayOfWk == 2 || dayOfWk == 3 || dayOfWk == 4 || dayOfWk == 5 || dayOfWk == 6 || dayOfWk == 7)
                        toBeRemoved.add(dtKey);
//                    dtMap.get(dtKey);
                }
                for(long rmKey : toBeRemoved){
                    dtMap.remove(rmKey);
                }
            }
        }
        else{
            for(String idKey : stringMap.keySet()){
                Map<Long,List<SwagrStatPB.Builder>> dtMap = stringMap.get(idKey);
                for(long dtKey : dtMap.keySet()){
                    DateTime dt = new DateTime(dtKey*1000);
                    if(dt.getDayOfWeek() == 6 || dt.getDayOfWeek() == 7)
                       toBeRemoved.add(dtKey); 
                }
                for(long rmKey : toBeRemoved){
                    dtMap.remove(rmKey);
                }
            }
        }
    }
    
    private SwagrStatPB.Builder combineBuilders(SwagrStatPB.Builder one, 
            SwagrStatPB.Builder two){
        if(one.hasCount())
            two.setCount(one.getCount() + two.getCount());
        if(one.hasMinTime())
            two.setMinTime(Math.min(one.getMinTime(),two.getMinTime()));
        if(one.hasAveTime())
            two.setAveTime((one.getAveTime()+two.getAveTime())/2);
        if(one.hasMaxTime())
            two.setMaxTime(Math.max(one.getMaxTime(),two.getMaxTime()));
        if(one.hasAveBytes())
            two.setAveBytes((one.getAveBytes() + two.getAveBytes())/2);
        if(one.hasMaxBytes())
            two.setMaxBytes(Math.max(one.getMaxBytes(),two.getMaxBytes()));
        if(one.hasAveBytes())
            two.setAveRows((one.getAveRows() + two.getAveRows())/2);
        if(one.hasMaxRows())
            two.setMaxRows(Math.max(one.getMaxRows(),two.getMaxRows()));
        if(one.hasFails())
            two.setFails(one.getFails() + two.getFails());
        if(one.hasValue()){
            two.setValue(one.getValue() + two.getValue());
            two.setValueType(one.getValueType());
        }
        if(one.hasSystemId())
            two.setSystemId(one.getSystemId());
        else if (one.hasHostTypeId())
            two.setHostTypeId(one.getHostTypeId());
        
        return two;
    }
}
