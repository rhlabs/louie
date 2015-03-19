/* 
 * SingleReqModel.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.model.SingleReqModel', {
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
        {type:'int', name:'minTime', mapping: 'minTime'},
        {type:'int', name:'aveTime', mapping: 'aveTime'},
        {type:'int', name:'maxTime', mapping: 'maxTime'},
        {type:'long', name:'aveBytes', mapping: 'aveBytes'},
        {type:'long', name:'maxBytes', mapping: 'maxBytes'},
        {type:'int', name:'aveRows', mapping: 'aveRows'},
        {type:'int', name:'maxRows', mapping: 'maxRows'},
        {type:'int', name:'fails', mapping: 'fails'},
        {type:'string', name:'location', mapping: 'location'},
        {type:'string', name:'dataType', mapping: 'dataType'},
        {type:'long', name:'value', mapping: 'value'},
        {type:'int', name:'valueType', mapping: 'valueType'},
        {type:'int', name:'systemId', mapping: 'systemId'},
        {type:'int', name:'hostTypeId', mapping: 'hostTypeId'},
        {type:'string', name:'timeString', mapping:'timeString'}
    ], 
    belongsTo: 'RH.model.SingleReqModel'
});