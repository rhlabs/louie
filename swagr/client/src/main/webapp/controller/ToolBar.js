/* 
 * ToolBar.js.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */

Ext.define('RH.controller.ToolBar', {
    extend: 'Ext.app.Controller',
    id: 'toolbarCtl',
    models: [
        'RemovedModel',
        'ChartModel',
        'ServiceModel',
        'LocationModel'
    ],
    stores: [
        'RemovedStore',
        'ChartStore',
        'ServiceStore',
        'LocationStore'
    ],
    refs: [
        {
            ref: 'vpCenter',
            selector: '#vp-center'
        }
    ],
    selObj: new Object(),
    init: function(){
        var me = this;
        var service = me.getServiceStoreStore();
        service.load({
            params: Ext.JSON.encode({
                user: 'none',
                agent: 'Web_SWAGr',
                system: 'swagr',
                method: 'getServices'
            })
        });
        var location = me.getLocationStoreStore();
        location.addListener(
            "load",
            function(store,records,options) {
                var defLoc = records[0].get("location");
                Ext.getCmp('louieLoc').setValue(defLoc);
                me.selObj['location'] = defLoc;
            }
        );
        location.load({
            params: Ext.JSON.encode({
                user: 'none',
                agent: 'Web_SWAGr',
                system: 'swagr',
                method: 'getLocations'
            })
        });
        
        me.control({
            "#vp-center > toolbar": {
                startup: function(system,serv){
                    me.selObj['systemId'] = 1;
                    me.topXorServTotals();
                    me.showTopX();  
                    me.serviceSelection(serv);
                    me.sysType();
                    me.dtRange();
                    me.valueType();
                    me.removeWeekends();
                    me.masterQueryButton();
                }
            }
        });
    },
    
    sysType: function(selected){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        me.selObj['dataType'] = 0;
        tool.add(
            {
                
                xtype: 'combo',
                id: 'louieLoc',
                valueField: 'location',
                emptyText: 'Louie Location',
                displayField: 'location',
                width: 100,
                store: 'LocationStore',
                listeners: {
                    select: function(combo,records){
                        me.selObj['location'] = records[0].get('location');
                    }
                },
                queryMode: 'local'
                
            },
            {
                xtype: 'combobox',
                id: 'dType',
                valueField:'id',
                emptyText: 'Data Type',
                displayField: 'type',
                width: 125,
                value: 0,
                listeners: {
                    select: function(combo, records){
                        me.selObj['dataType'] = records[0].get('id');
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
                        { 'type': "Any DataType", 'id': 0}
                    ]
                }
            }
        ); 
        Ext.create('Ext.tip.ToolTip', {
            target: 'louieLoc',
            html: 'Select a location'
        });
        Ext.create('Ext.tip.ToolTip', {
            target: 'dType',
            html: 'Select a data type to act as a filter'
        });
    },
      
    topXCtrl: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        tool.add(
//            { xtype: 'tbseparator', id: 'firstSep' },
            {
                xtype: 'combo',
                emptyText: 'Top X Control',
                id: 'topXCtrl',
                displayField: 'type',
                valueField: 'id',
                width: 100,
                value: 1,
                listeners: {
                    select: function(combo,records){
                        if(records[0].get('id')==1){
                            var top = Ext.getCmp('topNum')
                            top.enable();
                            me.selObj['topNum'] = top.getValue();
                        }
                        else{
                            Ext.getCmp('topNum').disable();
                            me.selObj['topNum'] = null;
                        }
                    }
                },
                store: {
                    fields: ['type', 'id'],
                    data: [
                        {'type': 'Top X', 'id': 1},
                        {'type': 'Timelog data', 'id': 2},
                    ]
                }
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'topXCtrl',
            html: 'Select either a specific lookup or a Top X style lookup'
        });
    },
    
    serviceSelection: function(service){
        var me = this;
        var value = undefined;
        if(service != undefined){
            value = service;
        }
        else{
            value = 'ALL'
        }
        var tool = Ext.getCmp('primarytoolbar');
        tool.add(
            {
                xtype: 'combo',
                id: 'serviceType',
                valueField: 'service',
                displayField: 'service',
                width: 100,
                value: value,
                listeners: {
                    select: function(combo,records){
                        var top = Ext.getCmp('topNum');
                        var chartType = Ext.getCmp('chartType');
                        if(records[0].get('service') != 'ALL Services'){       //WHY did i build this logic in?
                            top.disable();
//                            chartType.disable();
                            me.selObj['service'] = records[0].get('service');
                        }
                        else{
                            delete me.selObj.service;
                            chartType.enable();
                            top.enable();
                        }
                    }
                },
                store: 'ServiceStore',
                queryMode: 'local'
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'serviceType',
            html: 'Select one or all of the LoUIE services'
        });
    },
    
    topXorServTotals: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        tool.add(
//            { xtype: 'tbseparator', id: 'secSep' },
            {
                xtype: 'combo',
                emptyText: 'Chart Type',
                id: 'chartType',
                displayField: 'type',
                valueField: 'id',
                width: 110,
                value: 1,
                listeners: {
                    select: function(combo,records){
                        if(records[0].get('id')==1){
                            var top = Ext.getCmp('topNum')
                            top.enable();
                            Ext.getCmp('serviceType').enable();
                            me.selObj['topNum'] = top.getValue();
                            me.selObj['serviceTotals'] = false;
                            me.selObj['systemTotals'] = false;
                            me.selObj['deprecated'] = false;
                        }
                        else if (records[0].get('id')==2){
                            Ext.getCmp('serviceType').disable();
                            Ext.getCmp('topNum').disable();
                            me.selObj['topNum'] = null;
                            me.selObj['serviceTotals'] = true;
                            me.selObj['systemTotals'] = false;
                            me.selObj['deprecated'] = false;
                        }
                        else if (records[0].get('id')==3) {
                            Ext.getCmp('serviceType').disable(); 
                            Ext.getCmp('topNum').disable();
                            me.selObj['topNum'] = null;
                            me.selObj['serviceTotals'] = false;
                            me.selObj['systemTotals'] = true;
                            me.selObj['deprecated'] = false;
                        }
                        else {
                            Ext.getCmp('serviceType').enable(); 
                            Ext.getCmp('topNum').disable();
                            me.selObj['topNum'] = null;
                            me.selObj['serviceTotals'] = false;
                            me.selObj['systemTotals'] = false;
                            me.selObj['deprecated'] = true;
                            me.selObj['service'] = Ext.getCmp('serviceType').getValue();
                        }
                    }
                },
                store: {
                    fields: ['type', 'id'],
                    data: [
                        {'type': 'Top X Chart', 'id': 1},
                        {'type': 'Service Totals', 'id': 2},
                        {'type': 'System Totals', 'id': 3},
                        {'type': 'Deprecated Service Calls', 'id': 4}
                    ]
                }
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'chartType',
            width: '500',
            html: 'Select from one of three modes for the chart.<br><b>\"Top X Chart\"</b> will select a number of the most used/largest queries where X is selected by the number selector to the left.<br><b>\"Service Totals\"</b> shows a summation of each service.<br><b>\"System Totals\"</b> will display a grand total per system.'
        });
    },
    
    dtRange: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        var today = new Date();
        var aMonthAgo = new Date();
        aMonthAgo.setDate(today.getDate()-30);
        today = Ext.Date.clearTime(today,false);
        aMonthAgo = Ext.Date.clearTime(aMonthAgo,false);
        me.selObj['startDt'] = parseInt(Ext.Date.format(aMonthAgo,'U'))*1000;
        me.selObj['endDt'] = parseInt(Ext.Date.format(today, 'U'))*1000;
        tool.add(
//            { xtype: 'tbseparator', id: 'sep' },
            {
                xtype: 'datefield',
                id: 'dateStart',
                width: 95,
                value: aMonthAgo,
                tooltip: {text:'Choose a start date'},
                emptyText: 'Start Date',
                minValue: new Date(2010,11,9),
                maxValue: new Date(),
                listeners:{
                    select: function(field,val,opts){
                        me.selObj['startDt'] = parseInt(Ext.Date.format(val,'U'))*1000;
                    }
                }
            },
            {
                xtype: 'datefield',
                id: 'dateEnd',
                width: 95,
                value: today,
                emptyText: 'End Date',    
                minValue: new Date(2010,11,9),
                maxValue: new Date(),
                listeners:{
                    select: function(field,val,opts){
                        me.selObj['endDt'] = parseInt(Ext.Date.format(val,'U'))*1000;
                    }
                }
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'dateStart',
            html: 'Select a starting date for your lookup'
        });
        Ext.create('Ext.tip.ToolTip', {
            target: 'dateEnd',
            html: 'Select an end date for your lookup'
        });
    },
    
    valueType: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        me.selObj['valType'] = 1;
        tool.add(
            {
                xtype: 'combobox',
                id: 'xType',
                valueField: 'id',
                emptyText: 'Top X Type',
                displayField: 'type',
                width: 125,
                value: 1,
                store: {
                    fields: [
                        'type','id'
                    ],
                    data: [
                        { 'type': 'Count Volume', 'id': 1},
                        { 'type': 'Duration', 'id': 3},
                        { 'type': 'Load', 'id': 2}
                    ]
                },
                listeners:{
                    select: function(combo,records){
                        me.selObj['valType'] = records[0].get('id')
                    }
                }
            }  
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'xType',
            html: "Select how the set is calculated.<br>\"Count Volume\" is the number of requests.<br>\"Duration\" is count * average time.<br>\"Load\" is count * average bytes."
        });
    },
    
    showTopX: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        me.selObj['topNum'] = 10;
        tool.add(
            '-',
            {
                xtype: 'numberfield',
                anchor: '100%',
                name: 'topNum',
                id: 'topNum',
                width: 90,
                labelAlign: 'right',
                labelWidth: 40,
                fieldLabel: 'Top X ',
                allowBlank: false,
                allowDecimals: false,
                value: 10,
                maxValue: 20,
                minValue: 1,
                listeners: {
                    change: function(nfield,newVal,oldVal,e){
                        me.selObj['topNum'] = newVal;
                    }
                }
            },
            '',
            '-'
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'topNum',
            html: "Select the number of top unique requests you'd like to see"
        });
    },
    
    masterQueryButton: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        tool.add(
            '',
            '-',
            {
                xtype: 'button',
                id: 'query',
                text: 'DRAW', 
                handler: function(btn){
                    me.getRemovedStoreStore().removeAll();
                    btn.fireEvent('primaryChart',me.selObj);
//                    btn.fireEvent('minimalChart', me.selObj);
                }
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'query',
            dismissDelay: 0,
            html: 'The DRAW button executes a lookup </br>using the parameters to the left </br>and then renders a chart in the panel below'
        });
    },
    
    manuallyFireQuery: function(){
        var me = this;
        me.application.getController('Graph').getChart(me.selObj);
    },
    
    removeWeekends: function(){
        var me = this;
        var tool = Ext.getCmp('primarytoolbar');
        me.selObj['removeWeekends'] = false;
        tool.add(
            '-',
            '',
            {
                xtype: 'checkbox',
                fieldLabel: 'Remove Weekends',
                id: 'removeWeekendsCheck',
                listeners: {
                    change: function(cbox, newVal, oldVal, e){
                        if(newVal == true){
                            me.selObj['removeWeekends'] = true;
                        }
                        else{
                            me.selObj['removeWeekends'] = false;
                        }
                    }
                }
            }
        );
        Ext.create('Ext.tip.ToolTip', {
            target: 'removeWeekendsCheck',
            html: 'When checked, the returned data will not contain weekend dates.'
        });
    }
});