/* 
 * ServiceModel.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.model.ServiceModel', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'int', name:'systemid', mapping: 'systemid'},
        {type:'string', name:'service', mapping: 'service'}
    ]
});