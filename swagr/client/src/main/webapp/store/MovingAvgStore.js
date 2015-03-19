/* 
 * MovingAvgStore.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.store.MovingAvgStore', {
    requires: ['Ext.data.Store'],
    extend: 'Ext.data.Store',
    model: 'RH.model.MovingAvgModel',
    autoLoad: false,
    proxy: {
        actionMethods: { read: 'POST'},
        headers: {'Content-Type': 'application/json; charset=utf-8' },
        type: 'ajax',
        noCache: false,
        url: '/swagrserver/json'
    }
});
