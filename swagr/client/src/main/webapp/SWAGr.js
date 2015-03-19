/*
 * SWAGr.js : application + viewport
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
//                    items: { 
//                        xtype: 'image',
////                        html: '<center><img src="./Swagr.bmp"></center>',
//                        src: 'Swagr.bmp',
//                        id: 'swagrLogo',
//                        maxWidth: 1000,
//                        style:{
////                            align: 'center'
//                        },
//                        region: 'south'
//                    },
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

