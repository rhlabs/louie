/* 
 * Navigation.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */

Ext.define('RH.Navigation', {
    override: 'Ext.chart.Chart', 
    //constructor: function() {
    //    console.log('zoom init');
    //    this.originalStore = this.store;
    //},

    setZoom: function(zoomConfig) {
        var me = this,
            axes = me.axes,
            axesItems = axes.items,
            i, ln, axis,
            bbox = me.chartBBox,
            xScale = 1 / bbox.width,
            yScale = 1 / bbox.height,
            zoomer = {
                x : zoomConfig.x * xScale,
                y : zoomConfig.y * yScale,
                width : zoomConfig.width * xScale,
                height : zoomConfig.height * yScale
            },
            ends, from, to;
        console.log('OVERRIDDEN');
        console.log(axes);
        console.log(zoomer);
        for (i = 0, ln = axesItems.length; i < ln; i++) {
            
            axis = axesItems[i];
            console.log(axis.getRange());
            console.log(axis);
            ends = axis.calcEnds();
            console.log(ends);
            if (axis.position == 'bottom' || axis.position == 'top') {
                from = (ends.to - ends.from) * zoomer.x + ends.from;
                to = (ends.to - ends.from) * zoomer.width + from;
                axis.minimum = from;
                axis.maximum = to;
                //console.log(from);
                //console.log(to);
            } else{
                console.log(ends.to);
                console.log(ends.from);
                console.log(1-zoomer.y);
                console.log(ends.from);
                to = (ends.to - ends.from) * (1 - zoomer.y) + ends.from;
                from = to - (ends.to - ends.from) * zoomer.height;
                axis.minimum = from;
                axis.maximum = to;
                
                //console.log(from);
                //console.log(to);
            }
        }
        
        me.redraw(false);
    },
    
    oldZoom: function(zoomConfig) { //pretty much the same, just calcends is fucked
        var me = this,
            axes = me.axes,
            bbox = me.chartBBox,
            xScale = 1 / bbox.width,
            yScale = 1 / bbox.height,
            zoomer = {
                x : zoomConfig.x * xScale,
                y : zoomConfig.y * yScale,
                width : zoomConfig.width * xScale,
                height : zoomConfig.height * yScale
            };
        axes.each(function(axis) {
            var ends = axis.calcEnds();
            if (axis.position == 'bottom' || axis.position == 'top') {
                var from = (ends.to - ends.from) * zoomer.x + ends.from,
                    to = (ends.to - ends.from) * zoomer.width + from;
                axis.minimum = from;
                axis.maximum = to;
            } else {
                var to = (ends.to - ends.from) * (1 - zoomer.y) + ends.from,
                    from = to - (ends.to - ends.from) * zoomer.height;
                axis.minimum = from;
                axis.maximum = to;
            }
        });
        me.redraw(false);
    },

    restoreZoom: function() {
        if (this.originalStore) {
            this.store = this.substore = this.originalStore;
            this.redraw(true);
        }
    } 
})
