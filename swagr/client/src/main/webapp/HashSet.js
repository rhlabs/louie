/* 
 * HashSet.js
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
Ext.define('RH.HashSet', {

    constructor: function() {
        this._arr = new Array();
    },

    add: function(e) {
        var arr = this._arr;
        var i = arr.indexOf(e);
        if (i == -1) arr.push(e);
    },

    get: function(i) {
        return this._arr[i];
    },

    size: function(i) {
        return this._arr.length;
    },

    remove: function(e) {
        var arr =this._arr;
        var i = arr.indexOf(e);
        if (i != -1) arr.splice(i, 1);
    },

    toString: function() {
        return this._arr.join(',');
    },
    
    toArray: function() {
        return this._arr;
    }
    
});