/* 
 * PopUp.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.controller.PopUp', {
    extend: 'Ext.app.Controller',
    masterPopUpState: new Object(),
    models: [
        'RemovedModel',
        'ChartModel',
        'StackChartModel',
        'SingleReqModel',
        'MovingAvgModel'
    ],
    stores: [
        'RemovedStore',
        'ChartStore',
        'StackChartStore',
        'SingleReqStore',
        'MovingAvgStore'
    ],
    
    suppWindow: function(seriesId, dTypeSel, locSel, valueType){
        var me = this;
        //var query = Ext.getStore('dataStore');
        var query = me.getChartStoreStore();
        var cur = query.findRecord('id',seriesId, 0,false,false,true)
        var idstr = cur.get('idString');
        var str = '<tb>&nbsp; ID: ' + seriesId  + '<br/>&nbsp; ' + idstr;
        var tb = Ext.create('Ext.toolbar.Toolbar');
        tb.add(
            {
                text: 'Location',
                id: 'locTog',
                tooltip: {text:'View By Location'},
                enableToggle: true,
                listeners: {
                    click: function(btn){
                        var state = me.masterPopUpState;
                        state['selected'] = 'location';
                        
                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var valType = Ext.getCmp('valType').getValue();
                        if((state.lgloc != loc) || (state.lgdType != dtype) || (state.lgshift != shifted) || (state.lvalueType != valType)){
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,'location',valType);
                        }
                        else{
                            Ext.getCmp('compPanel2').hide();
                            Ext.getCmp('compPanel1').show();
                            Ext.getCmp('compPanel3').hide();
                        }
                        Ext.getCmp('dTypeTog').toggle(false);
                        Ext.getCmp('mvgAvgTog').toggle(false);
                        this.toggle(true);
                    }
                },
                pressed: true
            },
            {
                text: 'Data Type', 
                id: 'dTypeTog',
                tooltip: {text:'View By Data Type'},
                enableToggle: true,
                listeners: {
                    click: function(btn){
                        var state = me.masterPopUpState;
                        state['selected'] = 'dtype';

                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var valType = Ext.getCmp('valType').getValue();
                        if((state.dgloc != loc) || (state.dgdType != dtype) || (state.dgshift != shifted) || (state.dvalueType != valType)){
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,'dType',valType);
                        }
                        else{
                            Ext.getCmp('compPanel1').hide();
                            Ext.getCmp('compPanel2').show();
                            Ext.getCmp('compPanel3').hide();
                        }
                        Ext.getCmp('locTog').toggle(false);
                        Ext.getCmp('mvgAvgTog').toggle(false);
                        this.toggle(true);
                    }
                }
            },
            {
                text: 'Moving Avg', 
                id: 'mvgAvgTog',
                tooltip: {text:'View with Moving Avg'},
                enableToggle: true,
                listeners: {
                    click: function(btn){
                        var state = me.masterPopUpState;
                        state['selected'] = 'mvgAvg';

                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var valType = Ext.getCmp('valType').getValue();
                        if((state.mgloc != loc) || (state.mgdType != dtype) || (state.mgshift != shifted) || (state.mvalueType != valType)){
                            me.fetchMvgAvg(loc,dtype,shifted,seriesId,valType);
                        }
                        else{
                            Ext.getCmp('compPanel1').hide();
                            Ext.getCmp('compPanel2').hide();
                            Ext.getCmp('compPanel3').show();
                        }
                        Ext.getCmp('locTog').toggle(false);
                        Ext.getCmp('dTypeTog').toggle(false);
                        this.toggle(true);
                    }
                }
            },
            '-',
            {
                xtype: 'combo',
                id: 'locOverView',
                valueField: 'location',
                emptyText: 'Location',
                displayField: 'location',
                width: 50,
                value: 'ALL',
                listeners: {
                    select: function(combo,records){
                        var loc = records[0].get('location');
                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var toggle = me.masterPopUpState.selected;
                        var valType = Ext.getCmp('valType').getValue();
                        if(toggle == 'mvgAvg'){
                            me.fetchMvgAvg(loc,dtype,shifted,seriesId,valType);
                        }
                        else{
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,toggle,valType);
                        }
                    }
                },
                store: 'LocationStore',
                queryMode: 'local'
            },
            {
                xtype: 'combobox',
                id: 'dTypeOverView',
                valueField:'id',
                emptyText: 'Data Type',
                displayField: 'type',
                width: 110,
                value: 0,
                listeners: {
                    select: function(combo,records){
                        var dtype = records[0].get('id');
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var toggle = me.masterPopUpState.selected;
                        var valType = Ext.getCmp('valType').getValue();
                        if(toggle == 'mvgAvg'){
                            me.fetchMvgAvg(loc,dtype,shifted,seriesId,valType);
                        }
                        else{
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,toggle,valType);
                        }
                    }
                },
                store: {
                    fields: [
                        'type','id'
                    ],
                    data: [
                        { 'type': 'JSON', 'id': 2},
                        { 'type': 'Python' , 'id': 3},
                        { 'type': 'Objective C' , 'id': 4},
                        { 'type': 'Java' , 'id': 5},
                        { 'type': 'Perl' , 'id': 6},
                        { 'type': 'C' , 'id': 7},
                        { 'type': 'LoUIE', 'id': 8},
                        { 'type': 'unknown', 'id': 1},
                        { 'type': "All Data Types", 'id': 0}
                    ]
                }
            },
            {
                xtype: 'combobox',
                id: 'timeShiftPopUp',
                valueField: 'id',
                emptyText: 'Time Shift',
                displayField: 'type',
                width: 120,
                value: 0,
                listeners: {
                    select: function(combo,records){
                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = records[0].get('id');
                        var toggle = me.masterPopUpState.selected;
                        var valType = Ext.getCmp('valType').getValue();
                        if(toggle == 'mvgAvg'){
                            me.fetchMvgAvg(loc,dtype,shifted,seriesId,valType);
                        }
                        else{
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,toggle,valType);
                        }
                    }
                },
                store: {
                    fields: [
                        'type','id'
                    ],
                    data: [
                        { 'type': 'Daylight Matched', 'id': 0},
                        { 'type': 'LA Localized', 'id': 1}
                    ]
                }
            },
            {
                xtype: 'combobox',
                id: 'valType',
                valueField: 'id',
                emptyText: 'Volume Type',
                displayField: 'type',
                width: 85,
                value: valueType,
                listeners: {
                    select: function(combo,records){
                        var dtype = Ext.getCmp('dTypeOverView').getValue();
                        var loc = Ext.getCmp('locOverView').getValue();
                        var shifted = Ext.getCmp('timeShiftPopUp').getValue();
                        var toggle = me.masterPopUpState.selected;
                        var valType = records[0].get('id');
                        if(toggle == 'mvgAvg'){
                            me.fetchMvgAvg(loc,dtype,shifted,seriesId,valType);
                        }
                        else{
                            me.fetchSingleReq(loc,dtype,shifted,seriesId,toggle,valType);
                        }
                    }
                },
                store: {
                    fields: [
                        'type','id'
                    ],
                    data: [
                        { 'type': 'Count Vol', 'id': 1},
                        { 'type': 'Duration', 'id': 3},
                        { 'type': 'Load', 'id': 2}
                    ]
                }
            }
        )

        Ext.create('Ext.Window', {
            title: 'Series ' + seriesId,
            id: 'popup',
            width: 600,
            height: 400,
            x: 50,
            y: 50,
            plain: true,
            layout: {
                type: 'border'
            },
            headerPosition: 'bottom',
            items: [
                {
                    region: 'north',
                    title: 'Data Overview',
                    collapsible: true,
                    floatable: false,
                    padding: 0,
                    border: 0,
                    items:[
                        {
                            xtype: 'panel',
                            padding: 0,
                            border: 0,
                            height: 35,
                            html: str
                        },
                        tb
                    ]
                },
                {
                    region: 'center',
                    xtype: 'panel',
                    id: 'compCenter',
                    layout: 'fit',
                    padding: 0,
                    border: 0,
                    preventHeader: true,
                    listeners: {
                        afterrender: function(center){
                            this.mask = new Ext.LoadMask(center, {msg:"Please wait..."});
                        }
                    },
                    items: [{
                        xtype: 'panel',
                        id: 'compPanel1',
                        padding: 0,
                        border: 0,
                        layout: 'fit'
                    },
                    {
                        xtype: 'panel',
                        id: 'compPanel2',
                        layout: 'fit',
                        padding: 0,
                        border: 0,
                        hidden: true
                    },
                    {
                        xtype: 'panel',
                        id: 'compPanel3',
                        layout: 'fit',
                        padding: 0,
                        border: 0,
                        hidden: true
                    }
                    ]
                },
                {border: false}
            ]
        }).show();
        me.masterPopUpState = new Object();
        Ext.getCmp('locTog').fireEvent('click');
    },
    
    fetchMvgAvg: function(loc,dType,shifted,id,valueType){ //split into two requests now
        var me = this;
        var panelOne = Ext.getCmp('compPanel1');
        var panelTwo = Ext.getCmp('compPanel2');
        var panelThree = Ext.getCmp('compPanel3');
        var centerPan = Ext.getCmp('compCenter');
        centerPan.mask.show();
        
        var startdt = parseInt(Ext.Date.format(Ext.getCmp('dateStart').getValue(),'U'))*1000;
        var enddt = parseInt(Ext.Date.format(Ext.getCmp('dateEnd').getValue(),'U'))*1000;
        
        var argObj = {};
        argObj['id'] = parseInt(id);
        argObj['location'] = loc;
        argObj['dataType'] = dType;
        argObj['timeShift'] = shifted;
        argObj['valType'] = valueType;
        argObj['ridOrQid'] = 0;
        argObj['movingAvg'] = true;
        argObj['startDt'] = startdt
        argObj['endDt'] = enddt
        argObj['removeWeekends'] = Ext.getCmp('removeWeekendsCheck').getValue();
        
        me.getMovingAvgStoreStore().load({
            params: Ext.JSON.encode({
                user: 'none',
                agent: 'Web_SWAGr',
                system: 'swagr',
                version: 2,
                method: 'getSingleRequest',
                params: [{
                    type: 'rh.pb.swagr.SwagrOptionsPB',
                    value: argObj
                }]
            }),
            callback: function(){
                argObj['movingAvg'] = false;
                me.getSingleReqStoreStore().load({
                params: Ext.JSON.encode({
                    user: 'none',
                    agent: 'Web_SWAGr',
                    system: 'swagr',
                    version: 2,
                    method: 'getSingleRequest',
                    params: [{
                        type: 'rh.pb.swagr.SwagrOptionsPB',
                        value: argObj
                    }]
                }),
                callback: function(){
                    var obj = me.mvgAvgChartRen();
                    me.masterPopUpState['mgloc'] = loc;
                    me.masterPopUpState['mgdType'] = dType;
                    me.masterPopUpState['mgshift'] = shifted;
                    me.masterPopUpState['mvalueType'] = valueType;
                    panelTwo.hide();
                    panelOne.hide();
                    panelThree.removeAll();
                    panelThree.add(obj);
                    panelThree.show();
                    centerPan.mask.hide();
                }
        });
            }
        });
        
    },

    fetchSingleReq: function(loc,dType,shifted,id,chartType,valueType){
        var me = this;
        var panelOne = Ext.getCmp('compPanel1');
        var panelTwo = Ext.getCmp('compPanel2');
        var panelThree = Ext.getCmp('compPanel3');
        var centerPan = Ext.getCmp('compCenter');
        var startdt = parseInt(Ext.Date.format(Ext.getCmp('dateStart').getValue(),'U'))*1000;
        var enddt = parseInt(Ext.Date.format(Ext.getCmp('dateEnd').getValue(),'U'))*1000;
        
        var args = {};
        args['id'] = parseInt(id);
        args['location'] = loc;
        args['dataType'] = dType;
        args['timeShift'] = shifted;
        args['valType'] = valueType;
        args['ridOrQid'] = 0;
        args['startDt'] = startdt
        args['endDt'] = enddt
        args['removeWeekends'] = Ext.getCmp('removeWeekendsCheck').getValue();
  
        centerPan.mask.show();
        if(chartType == 'location'){
            args['locComposition'] = true
        }
        else{
            args['dTypeComposition'] = true
        }
        me.getStackChartStoreStore().load({
            params: Ext.JSON.encode({
                user: 'none',
                agent: 'Web_SWAGr',
                system: 'swagr',
                version: 2,
                method: 'getSingleRequest',
                params: [{
                    type: 'rh.pb.swagr.SwagrOptionsPB',
                    value: args
                }]
            }),
            callback: function(){
                var obj = me.areaChartRen(chartType);
                panelThree.hide();
                if(chartType == 'location'){
                    me.masterPopUpState['lgloc'] = loc;
                    me.masterPopUpState['lgdType'] = dType;
                    me.masterPopUpState['lgshift'] = shifted;
                    me.masterPopUpState['lvalueType'] = valueType;
                    panelTwo.hide();
                    panelOne.removeAll();
                    panelOne.add(obj);
                    panelOne.show();
                }
                else{
                    me.masterPopUpState['dgloc'] = loc;
                    me.masterPopUpState['dgdType'] = dType;
                    me.masterPopUpState['dgshift'] = shifted;
                    me.masterPopUpState['dvalueType'] = valueType;
                    panelOne.hide();
                    panelTwo.removeAll();
                    panelTwo.add(obj);
                    panelTwo.show();
                }
                centerPan.mask.hide();
            }
        });
    },
    
    mvgAvgChartRen: function(){
        //works under the assumption that there will only be one series per avg and reg stores
        var me = this;
        var regChart = me.getSingleReqStoreStore();
        var avgChart = me.getMovingAvgStoreStore(); 
        if (regChart.last() == undefined || avgChart.last() == undefined){
            Ext.Msg.alert('Error', 'Lookup returned without all necessary data.');
            return;
        }
        var leftAxesLabel;
        if (avgChart.getAt(0).get('valueType') == 1)
            leftAxesLabel = 'COUNT VOLUME';
        else if (avgChart.getAt(0).get('valueType') ==2)
            leftAxesLabel = 'LOAD VOLUME';
        else
            leftAxesLabel = 'DURATION';
        var dates = new Ext.util.MixedCollection();
        var modelfields = new Array();
        var axesfields = new Array();
        var vals = new Array();
        
        var reg = regChart.getAt(0);
        var avg = avgChart.getAt(0);
        
        modelfields.push({type: 'int', name: 'reg'});
        modelfields.push({type: 'int', name: 'avg'});
        modelfields.push({ type: 'date',  name: 'Time'});
        axesfields.push('reg');
        axesfields.push('avg');
        
        
        var stats = reg.statsStore;
        stats.each(function(item){
            var time = item.get('time');
            var temp = new Object(); 
            temp['reg'] = item.get('value');
            dates.add(time,temp);
        });
        var stats2 = avg.statsStore;
        stats2.each(function(item){
           var time = item.get('time');
            if(dates.containsKey(time)){
                var temp = new Object();
                temp = dates.get(time);
                temp['avg'] = item.get('value');
                dates.replace(time,temp);
            }
            else{
                var temp = new Object(); 
                temp['avg'] = item.get('value');
                dates.add(time,temp);
            } 
        });
        
        dates.eachKey(function(key,item){
            var tmpdt = new Object();
            tmpdt = item;
            tmpdt['Time'] = key;
            vals.push(tmpdt);
        }); 
        vals.sort(function(a, b) {
            if(a.Time > b.Time) return 1;
            if(a.Time < b.Time) return -1;
        });
        
        var obj = Ext.create('Ext.chart.Chart', {
            store: Ext.create('Ext.data.Store',{
                fields: modelfields,
                data : vals
            }),
            legend: {
                position: 'bottom'
            },
            axes: [{
                type: 'Numeric',
                grid: true,
                position: 'left',
                fields: axesfields,
                title: leftAxesLabel,                 
                minimum: 0,
                adjustMinimumByMajorUnit: 0
            }, {
                type: 'Time',
                dateFormat: 'n/j/y',
                position: 'bottom',
                fields: ['Time'],
                grid: true,
                label: {
                    font: '11px Arial',
                    rotate: {
                        degrees: 315
                    }
                }
            }],
            series: [
                {
                    type: 'line',
                    fill: true,
                    highlight: false,
                    smooth: false,
                    axis: ['left','bottom'],
                    selectionTolerance: 10,
                    xField: 'Time',
                    yField: 'reg',
                    style: {
                        'stroke-width': 1
                    },
                    showMarkers: false
                },
                {
                    type: 'line',
                    highlight: false,
                    smooth: false,
                    axis: ['left','bottom'],
                    selectionTolerance: 10,
                    xField: 'Time',
                    yField: 'avg',
                    style: {
                        'stroke-width': 4
                    },
                    showMarkers: false
                } 
            ]
        });
        
        return obj;
    },
  
    areaChartRen: function(stackType){
        var me = this;
        var chartData = me.getStackChartStoreStore(); 
        if (chartData.last() == undefined){
            Ext.Msg.alert('No Data', 'Lookup returned with no data.');
            return;
        }
        var leftAxesLabel = chartData.leftAxesLabel;
        if (chartData.getAt(0).get('valueType') == 1)
            leftAxesLabel = 'COUNT VOLUME';
        else if (chartData.getAt(0).get('valueType') ==2)
            leftAxesLabel = 'LOAD VOLUME';
        else
            leftAxesLabel = 'DURATION';
        var dates = new Ext.util.MixedCollection();
        var modelHash = Ext.create('RH.HashSet');
        var axesHash = Ext.create('RH.HashSet');
        var vals = new Array();
        
        chartData.each(function(rec){
            var stats = rec.statsStore;
            stats.each(function(item){
                if(stackType == 'location'){
                    id = item.get('location').toString();
                }
                else{
                    id = item.get('dataType').toString();
                }
                
                axesHash.add(id.toString());
                var time = item.get('time');
                if(dates.containsKey(time)){
                    var temp = new Object();
                    temp = dates.get(time);
                    temp[id] = item.get('value');
                    dates.replace(time,temp);
                }
                else{
                    var temp = new Object(); 
                    temp[id] = item.get('value');
                    dates.add(time,temp);
                }
            });
        });
        for(var i=0; i<axesHash.size(); i++){
           modelHash.add({type: 'int', name: axesHash.get(i).toString()}); 
        }
        modelHash.add({ type: 'date',  name: 'Time'});;
        dates.eachKey(function(key,item){
            var tmpdt = new Object();
            tmpdt = item;
            tmpdt['Time'] = key;
            vals.push(tmpdt);
        }); 
        vals.sort(function(a, b) {
            if(a.Time > b.Time) return 1;
            if(a.Time < b.Time) return -1;
        });
        var obj = Ext.create('Ext.chart.Chart', {
            renderTo: Ext.getBody(),
            style: 'background:#fff',
            store: Ext.create('Ext.data.Store',{
                fields: modelHash.toArray(),
                data: vals
            }),
            legend: {
                position: 'bottom'
            },
            axes: [{
                type: 'Numeric',
                grid: true,
                position: 'left',
                fields: axesHash.toArray(),
                title: leftAxesLabel,                 
                minimum: 0,
                adjustMinimumByMajorUnit: 0
            }, {
                type: 'Time',
                dateFormat: 'n/j/y',
                position: 'bottom',
                fields: ['Time'],
                grid: true,
                label: {
                    font: '11px Arial',
                    rotate: {
                        degrees: 315
                    }
                }
            }],
            series: [{
                type: 'area',
                highlight: true,
                axis: 'left',
                xField: 'Time',
                yField: axesHash.toArray(),
                style: {
                    opacity: 0.93
                }
            }]
        });
        return obj;
    }
    



});