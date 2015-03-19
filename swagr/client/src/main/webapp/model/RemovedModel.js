/* 
 * RemovedModel.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */


Ext.define('RH.model.RemovedModel', {
   extend: 'Ext.data.Model',
   idProperty: 'id',
   fields: [
       {type: 'int', name: 'id'},
       {type: 'string', name: 'idString'}
   ]
});