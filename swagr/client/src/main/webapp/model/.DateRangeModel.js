/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('AM.model.DateRangeModel', {
    extend: 'Ext.data.Model',
    fields: [{type: 'date', dateFormat: 'Ymd', name: 'Time'}]
});