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


Ext.define('RH.model.MinimalChartModel', {
    extend: 'Ext.data.Model', 
    fields: [{type:'int', name:'id', mapping: 'queryId'}],
    hasMany: [
        {model: 'RH.model.FCMF', name: 'chartFields', mapping: 'chartFields', associationKey: 'chartFields'},
        {model: 'RH.model.FormattedChartModelData', name: 'data', mapping: 'data', associationKey: 'data'}
    ]
});

Ext.define('RH.model.FCMF', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'string', name:'type', mapping: 'type'}, //replaced with chartmodel's fields, doesnt appear to be that
        {type:'string', name:'name', mapping: 'name'}
    ],
    belongsTo: 'RH.model.MinimalChartModel'
});

Ext.define('RH.model.FormattedChartModelData', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'string', name:'location', mapping: 'location'},
    ], 
    hasMany: [
        {model: 'RH.model.FormattedChartModelDataPoints', name: 'dataPoints', mapping: 'dataPoints', associationKey: 'dataPoints'}
    ],
    belongsTo: 'RH.model.FormattedChartModel'
});


Ext.define('RH.model.FormattedChartModelDataPoints', {
    extend: 'Ext.data.Model',
    fields: [
        {type:'string', name:'fieldName', mapping: 'fieldName', defaultValue: undefined},
        {type:'int', name:'value', mapping: 'value', defaultValue: undefined}
    ],
    belongsTo: 'RH.model.FormattedChartModelData'
});