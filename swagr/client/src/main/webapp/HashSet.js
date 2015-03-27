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