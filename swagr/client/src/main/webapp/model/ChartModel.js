/* 
 * ChartModel.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.model.ChartModel', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'int', name:'id', mapping: 'id'},
        {type:'string', name:'idString', mapping: 'idString'},
        {type:'string', name:'suppString', mapping: 'suppString'},
        {type:'int', name: 'valueType', mapping: 'valueType'},
    ],
    hasMany: {model: 'RH.model.PB', name: 'stats', mapping: 'stats', associationKey: 'stats'}
});

Ext.define('RH.model.PB', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'date', dateFormat:'U', name:'time', mapping: 'time'},
        {type:'int', name:'count', mapping: 'count'},
        {type:'int', name:'minTime', mapping: 'minTime', defaultValue: undefined},
        {type:'int', name:'aveTime', mapping: 'aveTime', defaultValue: undefined},
        {type:'int', name:'maxTime', mapping: 'maxTime', defaultValue: undefined},
        {type:'long', name:'aveBytes', mapping: 'aveBytes', defaultValue: undefined},
        {type:'long', name:'maxBytes', mapping: 'maxBytes', defaultValue: undefined},
        {type:'int', name:'aveRows', mapping: 'aveRows', defaultValue: undefined},
        {type:'int', name:'maxRows', mapping: 'maxRows', defaultValue: undefined},
        {type:'int', name:'fails', mapping: 'fails', defaultValue: undefined},
        {type:'string', name:'location', mapping: 'location', defaultValue: undefined},
        {type:'string', name:'dataType', mapping: 'dataType', defaultValue: undefined},
        {type:'long', name:'value', mapping: 'value'},
        {type:'int', name:'valueType', mapping: 'valueType'},
        {type:'int', name:'systemId', mapping: 'systemId', defaultValue: undefined},
        {type:'int', name:'hostTypeId', mapping: 'hostTypeId', defaultValue: undefined},
        {type:'string', name:'timeString', mapping:'timeString'}
    ], 
    belongsTo: 'RH.model.ChartModel'
});

