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


Ext.define('RH.SWAGr', {
    extend: 'Ext.container.Viewport',
    suspendLayout: false,
    initComponent: function(){
        var me = this;
        var hosttypeid;
        me.dateListConfig = {
            getInnerTpl: function() {  //unused? what was i doing with this?
                return '<div class="search-item">' +
                    '{[Ext.Date.format(values.Time, "Ymd")]}' +
                '</div>';
            }
        };
        Ext.apply(this, {
            layout: {
                type: 'border'
            },
            items: [
                {
                    region: 'center',
                    id: 'vp-center',
                    border: true,
                    margins: '5 5 5 5',
                    layout: 'fit',
                    preventHeader: true,
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'top',
                            id: 'primarytoolbar'
                        }
                    ]    
                }
            ]
        });
        Ext.History.init();
        
        Ext.History.on('change',function(token){
            var toolbar = Ext.getCmp('primarytoolbar');
           
            if(token){
                var system = getSystemCookie()
                console.log(system);
                if(system == 'louie'){
                   var service = getServiceCookie();
                   console.log(service);
                }
            }
        });
        
        this.callParent();
    }
});

function getSystemCookie(){
    var cookie = Ext.History.getToken();
    var params = new Array();
    if(cookie){
        params = cookie.split('/');
        return params[0];
    }
    else return undefined;
}

function getServiceCookie(){
    var cookie = Ext.History.getToken();
    var params = new Array();
    if(cookie){
        params = cookie.split('/');
        return params[1];
    }
    else return undefined;
}

Ext.application({
    name: 'RH',
    appFolder: './',
    requires: ['RH.HashSet'],
    controllers: [
                'Graph',
                'PopUp',
                'ToolBar'
            ],
    launch: function() {
        var am_vp = Ext.create('RH.SWAGr', {
            id: 'am-vp'
        });
        var system = getSystemCookie();
        var service = undefined;
        if(system){
            service = getServiceCookie();
        }
        Ext.getCmp('primarytoolbar').fireEvent('startup',system,service);
        
        if(system){
            this.getController('ToolBar').manuallyFireQuery();
        }
        
    }
});

