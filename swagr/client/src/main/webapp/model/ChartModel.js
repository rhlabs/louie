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

