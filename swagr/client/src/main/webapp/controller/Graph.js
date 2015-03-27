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

Ext.define('RH.controller.Graph', {
    extend: 'Ext.app.Controller',
    id: 'graphCtl',
    models: [
        'RemovedModel',
        'ChartModel',
//        'MinimalChartModel'
    ],
    stores: [
        'RemovedStore',
        'ChartStore',
//        'MinimalChartStore'
    ],
    refs: [
        {
            ref: 'lineChart',
            selector: '#vp-center > lgraph'  
        },
        {
            ref: 'chart',
            selector: '#vp-center > dynamicChart'
        },
        {
            ref: 'dataGrid',
            selector: '#vp-east > dgrid'
        },
        {
            ref: 'vpCenter',
            selector: '#vp-center'
        },
        {
            ref: 'vpEast',
            selector: '#vp-east'
        },
    ],
    masterSer: new Array(),
    masterChart: new Object(), 
    masterValueType: 1,
    init: function(){
        var me = this;
        Ext.getBody().on('contextmenu', function(e, t) {    
            if (e.button === 2) {
                e.stopEvent();
                var xy = Ext.getCmp('dynamicChart').getPositionEl();
                var offsetX = xy.getX();
                var offsetY = xy.getY();
                var x = e.getX() - offsetX;
                var y = e.getY() - offsetY;
                var ser = Ext.getCmp('dynamicChart').series.items;
                var series = new Array();
                Ext.each(ser, function(item){
                   var found = item.getItemForPoint(x,y); 
                   if(found != null){
                       series.push(found);
                   }
                });
                if(series.length != 0){
                    me.newContext(series, e.getX(), e.getY(),x,y);
                }
            }
        });

        me.control({
          "#vp-center": {
              
              afterrender: function(center){
                me.mask = new Ext.LoadMask(center, {msg:"Please wait..."});
              }
          },
          "#vp-center > toolbar > button":{
              primaryChart: function(options){
                  me.masterValueType = options['valType'];
                  if (options['deprecated']){
                      me.getChart(options, 'getDeprecatedActivity');
                  } else if (options['systemTotals']) {
                      me.getChart(options, 'getSystemTotals');
                  } else if (options['serviceTotals']) {
                      me.getChart(options, 'getServiceTotals');
                  } else {
                    me.getChart(options, 'getSystemActivity');
                  }
              }
//              minimalChart: function(options){
//                  me.getMinimalChart(options);
//              }
           }
        });
    },
    
    getChart: function(options, methodString){
        var me = this;
        me.mask.show();
        args = [{
                type: 'rh.pb.swagr.SwagrOptionsPB',
                value: options
        }]

        me.getChartStoreStore().load({
            params: Ext.JSON.encode({
                user: 'none',
                agent: 'Web_SWAGr',
                system: 'swagr',
                version: '2',
                method: methodString,
                params: args
            }),
            callback: function(){
                    me.mask.hide();
                    var panel = me.getVpCenter();
                    panel.removeAll();
//                    console.log(me.getChartStoreStore());
                    me.pbChartRen();
            }
        });
    },

    
//    getMinimalChart: function(options){
//        var me = this;
//        
//        args = [{
//                type: 'rh.pb.swagr.SwagrOptionsPB',
//                value: options
//        }]
//
//        me.getMinimalChartStoreStore().load({
//            params: Ext.JSON.encode({
//                user: 'none',
//                agent: 'Web_SWAGr',
//                system: 'swagr',
//                version: '2',
//                method: 'getSystemActivityChartFormatted',
//                params: args
//            }),
//            callback: function(){
//                    me.mask.hide();
//                    var panel = me.getVpCenter();
//                    panel.removeAll();
//                    me.simpleChartRen();
//                    //then somehow add in everything for getChart func above, but without the me.pbChartRen();
//            }
//        });
//    },
    
    removeSeries: function(chart, seriesId){
        var surface = chart.surface;
        for(var serieKey = 0; serieKey < chart.series.keys.length; serieKey++){
            if(chart.series.keys[serieKey] == seriesId){                
                for(var groupKey = 0; groupKey < surface.groups.keys.length; groupKey++){
                    if(surface.groups.keys[groupKey].search(seriesId) == 0){
                        surface.groups.items[groupKey].destroy();
                    }
                }                    
                var serie = chart.series.items[serieKey];
                chart.series.remove(serie);
            }
        }
        chart.redraw();
    },
    
    removedMenu: function(){
        var me = this;
        Ext.getCmp('primarytoolbar').add({
            xtype: 'combobox',
            id: 'removedSer',
            valueField: 'id',
            emptyText: 'Removed Lines',
            displayField: 'idString',
            width: 200,
            queryMode: 'local',
            store: 'RemovedStore',
            listeners: {
                collapse: function(field){
                    var val = field.getValue();
                    if (val != null && val != undefined){
                        var store = me.getRemovedStoreStore();
                        var record = me.getRemovedStoreStore().getById(val);
                        //console.log(record);
                        field.clearValue();
                        store.remove(record);
                        me.addSeries(Ext.getCmp('dynamicChart'),val);
                    }
                } 
            }
        });
    },

    addSeries: function(chart, selected){
        var me = this;
        var add = new Array();
        Ext.each(masterSer, function(ser){
            if(ser.seriesId == selected){
                add.push(ser)
            }
        });
        chart.series.addAll(add);
        chart.redraw();
    },

    newContext: function(series, x, y, altX, altY){
        var me = this;
        var menu = Ext.create('Ext.menu.Menu', {
            margin: '0 0 10 0',
            items: [
            ]
        });
        if(series.length>1){
            menu.add({
                icon: 'black_delete_icon.png',
                text: "Remove ALL listed items",
                listeners: {
                    click: function(menu, item, e){
                        me.mask.show();
                        var ret = Ext.getCmp('removedSer');
                        if(ret == undefined){
                            me.removedMenu();
                        }
                        var chart = Ext.getCmp('dynamicChart');
                        for(var i=0; i<series.length; i++){
                            var id = series[i].series.seriesId;
                            var serString = chartStore.findRecord('id',id, 0,false,false,true);
                            if(serString == undefined){
                                serString = id;
                            }
                            else{
                                serString = serString.get('idString');
                            }
                            me.getRemovedStoreStore().add({id: id, idString: serString});
                            me.removeSeries(chart,id);
                        }
                        me.mask.hide();
                    }
                }
            });
        }
        var chartStore = me.getChartStoreStore();
        for(var i = 0; i<series.length;i++){
            var id = series[i].series.seriesId
            var serString = chartStore.findRecord('id',id, 0,false,false,true);
            if(serString == undefined){
                break; //can't allow a view more data on this because there is no specific ID number
            }
            else{
                serString = serString.get('idString');
            }
            if(Ext.getCmp('louieLoc') == undefined){
                break;
            }
            menu.add(
                {
                    icon: 'magnifying-glass.png',
                    text: "View More Data for: " + serString,
                    listeners: {
                        click: function(menu,item,e){
                            var locSel = Ext.getCmp('louieLoc').getValue();
                            var dTypeSel = Ext.getCmp('dType').getValue();
                            var varTypeSel = Ext.getCmp('xType').getValue();
                            if(!Ext.getCmp('popup')){
                                var ctl = me.getController('RH.controller.PopUp');
                                ctl.suppWindow(id,dTypeSel,locSel,varTypeSel);
                            }
                            else{
                                Ext.Msg.show({
                                    title: 'Replace Existing PopUp',
                                    msg: 'Do you want to replace the existing popup with data for Series '+serString+'?',
                                    buttons: Ext.Msg.YESNO,
                                    fn: function(btnId){
                                        if(btnId == 'yes'){
                                            Ext.getCmp('popup').close();
                                            var ctl = me.getController('RH.controller.PopUp');
                                            ctl.suppWindow(id,dTypeSel,locSel,varTypeSel);
                                        }
                                        else{
                                            this.close();
                                        }
                                    }
                                });
                            }
                        }
                    }                  
                }
            )
        }
        for(var i = 0; i<series.length;i++){
            var id = series[i].series.seriesId
            var serString = chartStore.findRecord('id',id, 0,false,false,true);
            if(serString == undefined){
                serString = id;
            }
            else{
                serString = serString.get('idString');
            }
            menu.add({
                icon: 'black_delete_icon.png',
                text: "Remove: " + serString,
                listeners: {
                    click: function(menu, item, e){
                        me.mask.show();
                        me.getRemovedStoreStore().add({id: id},{idString: serString});
                        var ret = Ext.getCmp('removedSer');
                        if(ret == undefined){
                            me.removedMenu();
                        }
                        var chart = Ext.getCmp('dynamicChart');
                        me.removeSeries(chart,id);
                        me.mask.hide();
                    }
                }
            });
        }        
        menu.showAt(x,y);
    },
    
    pbChartRen: function(){
        var me = this;
        var panel = me.getVpCenter();
        var chartData = me.getChartStoreStore(); 
        if (chartData.last() == undefined){
            panel.removeAll();
            Ext.Msg.alert('No Data', 'Lookup returned with no data.');
            return;
        }
        var dates = new Ext.util.MixedCollection();
        var leftAxesLabel;
        if (me.masterValueType == 1)
            leftAxesLabel = 'COUNT VOLUME';
        else if (me.masterValueType == 2)
            leftAxesLabel = 'LOAD VOLUME';
        else
            leftAxesLabel = 'DURATION';
        var modelfields = new Array();
        var axesfields = new Array();
        var seriesArray = new Array();
        var vals = new Array();
        chartData.each(function(rec){
            var id;
            if(rec.get('id') != undefined){
                id = rec.get('id').toString();
                modelfields.push({type: 'int', name: id})
            }
            else{
                id = rec.get('idString').toString();
                modelfields.push({type: 'int', name: id}); //seems like it should be type string
            }
            axesfields.push(id);
            var stats = rec.statsStore;
            if(stats!=undefined){
                stats.each(function(item){
                    var time = item.get('timeString');
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
            }
            var ser = {
                type: 'line',
                seriesId: id,
                fill: true,
                highlight: false,
                smooth: false,
                axis: ['bottom'], //was left,bottom but unnecessary b/c of strings as time
                selectionTolerance: 5,
                xField: 'Time',
                yField: id,
                tips: { 
                    width: 'auto',
                    trackMouse: true,
                    dismissDelay: 0,
                    
                    border: false,
                    shadow: false,
                    frame: false,
                    header: false,
                    margin: 0,
                    padding: 0,
                    isPanel: false,
                    bodyStyle: 'background:transparent;',
                    anchorToTarget: false,
                    anchor: 'top',

                    renderer: function(record, item) {
                        var dt = record.get('Time');
                        var id = item.series.yField;
                        var chstr = me.getChartStoreStore();
                        var cur = chstr.findRecord('id',id, 0,false,false,true);
                        if (cur == undefined){
                            cur = chstr.findRecord('idString',id,0,false,false,true);
                        }
                        var str = cur.get('idString');
                        var curStat = cur.statsStore.findRecord('timeString',dt);
                        var val = curStat.get('value');
                        var value = Ext.util.Format.number(val,'0,000');
                        var count = Ext.util.Format.number(curStat.get('count'),'0,000');
//                        this.setTitle(str);
                        this.update('<b>' + str + '</b><br/>\
                        ID: ' + id + '<br/>\
                        Value: ' + value + '<br />\
                        Date: ' +  dt + '<br />\
                        count: ' + count + '<br />\
                        min. Time: ' + Ext.util.Format.number(curStat.get('minTime'),'0,000') + '<br />\
                        avg. Time: '  + Ext.util.Format.number(curStat.get('aveTime'),'0,000') + '<br />\
                        max Time: ' + Ext.util.Format.number(curStat.get('maxTime'),'0,000') + '<br />\
                        avg. Bytes: ' + Ext.util.Format.number(curStat.get('aveBytes'),'0,000') + '<br />\
                        max Bytes: ' + Ext.util.Format.number(curStat.get('maxBytes'),'0,000') + '<br />\
                        avg. Rows: ' + Ext.util.Format.number(curStat.get('aveRows'),'0,000') + '<br />\
                        max Rows: ' + Ext.util.Format.number(curStat.get('maxRows'),'0,000')+ '<br />\
                        fails: ' + Ext.util.Format.number(curStat.get('fails'),'0,000'));

                    }
                },
                showMarkers: false
            }
            seriesArray.push(ser);  
        });
        modelfields.push({ type: 'string',  name: 'Time'});
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
        masterSer = seriesArray;
        var obj = Ext.create('Ext.chart.Chart',{
            xtype: 'chart',
            id: 'dynamicChart',
            shadow: false,
//            legend: true,
            mask: 'horizontal',
            listeners: {
                select: {
                    fn: function(self, selection) {
                        self.setZoom(selection);
                        self.mask.hide();
                    }
                }
            },
            store: Ext.create('Ext.data.Store',{
                id: 'dynStore',
                fields: modelfields,
                data : vals
            }),
            axes: [{
                type: 'Numeric',
                position: 'left',
                fields: axesfields,
                title: leftAxesLabel,
                label: {
                    font: '12px Arial',
                    renderer: Ext.util.Format.numberRenderer('0,000')
                },
                grid: true
            },{
                type: 'Category',
                position: 'bottom',
                fields: 'Time',
                grid: true,
                label: {
                    font: '12px Arial',
                    rotate: {
                        degrees: 315
                    }
                }
            }],
            series: seriesArray
        });
        
        panel.removeAll();
        panel.add(obj);
    },
    
    simpleChartRen: function(){
        /*
         * 
         * var store = Ext.create('Ext.data.JsonStore', {
                fields: ['name', 'data1', 'data2', 'data3', 'data4', 'data5'],
                data: [
                    { 'name': 'metric one',   'data1': 10, 'data2': 12, 'data3': 14, 'data4': 8,  'data5': 13 },
                    { 'name': 'metric two',   'data1': 7,  'data2': 8,  'data3': 16, 'data4': 10, 'data5': 3  },
                    { 'name': 'metric three', 'data1': 5,  'data2': 2,  'data3': 14, 'data4': 12, 'data5': 7  },
                    { 'name': 'metric four',  'data1': 2,  'data2': 14, 'data3': 6,  'data4': 1,  'data5': 23 },
                    { 'name': 'metric five',  'data1': 4,  'data2': 4,  'data3': 36, 'data4': 13, 'data5': 33 }
                ]
            });
        
         */
        var me = this;
        var chartBundle = me.getMinimalChartStoreStore();
//        console.log(chartBundle);
        var chartFields = chartBundle.get('chartFields');
        var chartData = chartBundle.get('data');
        var chartSeries = chartBundle.get('queryId'); //an array
        var formattedChartFields = new Array();
        var formattedData = new Array();
        var seriesArray = new Array();
        var axesfields = new Array();
        
        if (masterValueType == 1)
            leftAxesLabel = 'COUNT VOLUME';
        else if (masterValueType == 2)
            leftAxesLabel = 'LOAD VOLUME';
        else
            leftAxesLabel = 'DURATION';
        
        chartFields.each(function(rec){
            formattedChartFields.push({type: rec.get('type'), name: rec.get('name')}); //modelfields.push({type: 'int', name: id})
        });
        data.each(function(rec){
            var perDate = new Object();
            perDate['Time'] = rec.get('xAxisParam');
            rec.get('dataPoints').each(function(innerRec){
                perDate[innerRec.get('fieldName')] = innerRec.get('value');
            });
            formattedData.push(perDate);//push formatted object made from each data index
        });
        chartSeries.each(function(id){
            axesFields.push(id);
            var ser = {
                type: 'line',
                seriesId: id,
                fill: true,
                highlight: false,
                smooth: false,
                axis: ['bottom'], //was left,bottom but unnecessary b/c of strings as time
                selectionTolerance: 10,
                xField: 'Time',
                yField: id,
                showMarkers: false
            }
            seriesArray.push(ser);
        });
        
        var obj = Ext.create('Ext.chart.Chart',{
            xtype: 'chart',
            id: 'dynamicChart',
            shadow: false,
//            legend: true,
            mask: 'horizontal',
            listeners: {
                select: {
                    fn: function(self, selection) {
                        self.setZoom(selection);
                        self.mask.hide();
                    }
                }
            },
            store: Ext.create('Ext.data.Store',{
                id: 'dynStore',
                fields: formattedChartFields,
                data : formattedData
            }),
            axes: [{
                type: 'Numeric',
                position: 'left',
                fields: axesfields,
                title: leftAxesLabel,
                label: {
                    font: '12px Arial',
                    renderer: Ext.util.Format.numberRenderer('0,000')
                },
                grid: true
            },{
                type: 'Category',
                position: 'bottom',
                fields: 'Time',
                grid: true,
                label: {
                    font: '12px Arial',
                    rotate: {
                        degrees: 315
                    }
                }
            }],
            series: seriesArray
        });
        var panel = me.getVpCenter();
        panel.removeAll();
        panel.add(obj);
    }
});
