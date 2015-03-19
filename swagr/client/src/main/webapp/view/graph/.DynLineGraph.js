/*
 * store and model are already populated, via controller so here i must parse the fields 'name' part into an array, excluding 'Time'
 * then use this array to fill out the 'fields' part of the numeric axis (time is hard coded because it's consistent)
 * then use this array to create all of the series (just yfield, x is hard coded again)
 * Possibly need to use it again to create the tool tips, but it can be done without. may not matter in implementation since series is in a loop anyway? 
 *     this would also be where i would attach the tool tips to data in an alternate store connected to my controller (possibly for the rich tips etc).
 *     
 * Hiding data would require copying fields and data into a temp store, then manually trimming out the undesired qids and feeding data to this view again.
 *         
 *     var regex = eval('/^(' +  selected.join('|') + ')$/');
 *     
 *     
 *     NOT FUNCTIONING, DUE TO IMPROPER INITIALIZATION
 */

Ext.define('RH.view.graph.DynLineGraph', {
    initComponent: function(chartData){
        var obj = new Object();
        //list of dyn areas: store.model.fields, store.data, axes, series
        //we may want to make axes time steps dynamic, maybe get from metadata?
        var modelfields = [];
        var axesfields = [];
        dates = new Array();
        seriesArray = new Array();

        for (var i in chartData.data[0]){
        dates[chartData.data[0][i].dt] = '';
        }

        for (var id in chartData.data) {
        modelfields.push({ type: 'int', name: '\'' + id + '\''});

            axesfields.push('\'' + id + '\'')

            for (var data in chartData.data[id]){
                dates[chartData.data[id][data]] = '\'' + id + '\': ' + chartData.data[id][data].val + ', ';
            }


            var ser = {
                    type: 'line',
                    highlight: {
                        size: 7,
                        radius: 7
                    },
                    smooth: true,
                    axis: ['left','bottom'],
                    xField: 'Time',
                    yField: '\'' + id + '\'',
                    tips: { 
                        trackMouse: true,
                        width: 500,
                        layout: 'fit',
                        dismissDelay: 0,
                        renderer: function(storeItem, item) {
                            var dt = storeItem.get('Time');
                            var qid = item.series.yField;
                            var query = Ext.getStore('dataStore');
                            var rec = query.findRecord('qid',qid);
                            var count = storeItem.get(qid);
                            var cnt = Ext.util.Format.number(count,'0,000');
                            obj.setTitle('ID: ' + qid + '<br/>Cnt: ' + cnt + '<br />' +  Ext.Date.format(dt,'M j, Y') + '<br/>Name: ' + rec.get('query'))                 }
                    },
                    showMarkers: false,
                    markerConfig: {
                        type: 'cross',
                        size: 4,
                        radius: 4,
                        'stroke-width': 0
                    }
            }
            seriesArray.push(ser);
        }
       
        modelfields.push({ type: 'date', dateFormat: 'U', name: 'Time'});

        var vals = [];
        for (var dt in dates){
            var tmpdt = [];
            for (var perdt in dates[dt]){
                tmpdt.push(perdt);
            }
            tmpdt.push('\'Time\':' + dt);
            vals.push(tmpdt);
        };
        console.log(modelfields);
        console.log(axesfields);
        console.log(seriesArray);
        console.log(vals);

    },
    extend: 'Ext.chart.Chart',
    alias: 'widget.dynlgraph',
    itemId: 'DynLineGraph',
    
    layout: 'fit',
    theme: 'Category1',
    store: Ext.create('Ext.data.Store',{
        id: 'dynStore',
        model: Ext.define('DynChart', {
            id: 'dynModel',
            extend: 'Ext.data.Model',
            fields: modelfields
        }),

        data: vals
    }),
    axes: [{
        type: 'Numeric',
        position: 'left',
        //dynamic crapola
        fields: axesfields,
        title: 'Volume',
        minorTickSteps: 1,
        grid: true
    },{
        type: 'Time',
        dateFormat: 'm/d/Y',
        position: 'bottom',
        fields: 'Time',
        grid: true,
        step: [Ext.Date.DAY, 1], //want obj dynamic!
        label: {
            rotate: {
                degrees: 90
            }
        },
        title: 'Log Date'
    }],
    series: seriesArray
});    