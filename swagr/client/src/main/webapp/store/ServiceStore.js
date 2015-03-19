/* 
 * ServiceStore.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */

Ext.define('RH.store.ServiceStore', {
    requires: ['Ext.data.Store'],
    extend: 'Ext.data.Store',
    model: 'RH.model.ServiceModel',
    autoLoad: false,
    proxy: {
        actionMethods: { read: 'POST'},
        headers: {'Content-Type': 'application/json; charset=utf-8' },
        type: 'ajax',
        noCache: false,
        url: '/swagrserver/json'
    },
    sorters: [{
        property: 'service'
    }]
});
