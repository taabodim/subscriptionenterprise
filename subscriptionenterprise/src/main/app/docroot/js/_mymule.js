dojo.require("dojox.cometd");
dojo.require("dojox.cometd.timestamp");
dojo.require("dojox.uuid.generateRandomUuid");

//ugly patch to work IE

var mule = {

    _replyToChannels : new Array(),

    uri: '/ajax/cometd',

    /* set this to 0.0.0.0 for all local adapters or 127.0.0.1 for loopback */
    localAdapter: 'localhost',

    _init: function()
    {
        var loc = new String(document.location);
        loc = loc.replace("localhost", mule.localAdapter);
        loc = loc.substring(0, loc.indexOf("/", 8)) + mule.uri;
//        if(console){console.debug("initing now: " + loc)};
        dojox.cometd.init(loc);

        // handle ajax failures
        if (mule._meta)
        {
            dojo.unsubscribe(mule._meta, null, null);
        }
    },


    _dispose: function()
    {
//    	if(console){console.debug("disposing now")};

        for (c in mule._replyToChannels)
        {
            mule.unsubscribe(c, null);
        }

        if (mule._meta)
        {
            dojo.unsubscribe(mule._meta);
        }
        mule._meta = null;
//        if(console){console.debug("exit disposing now")};
        dojox.cometd.disconnect();
    },

    subscribe: function(channel, callback)
    {
//    	if(console){console.debug("subscribe:" + channel + ", " + callback)};
        dojox.cometd.subscribe(channel, mule, callback);
    },

    unsubscribe: function(channel, callback)
    {
//    	if(console){console.debug("unsubscribe:" + channel + ", " + callback)};
        dojox.cometd.unsubscribe(channel, mule, callback);
    },

    publish: function(channel, data)
    {
//    	if(console){console.debug("publish:" + channel + ", " + data)};
        dojox.cometd.publish(channel, data);
    },

    rpc: function(channel, data, callback)
    {
        var replyTo = channel + "#" + dojox.cometd.clientId;
        if(console){console.debug("RPC:" + channel + ", " + data)};
        if(console){console.debug("RPC: setting replyTo: " + replyTo)};
        var message = new Object();
        message.data = data;
        message.replyTo = replyTo;
       // alert(mule._replyToChannels.toString()+" "+mule._replyToChannels.toString().indexOf(replyTo)); 
        if(mule._replyToChannels.toString().indexOf(replyTo) == -1)
        {
        	if(console){console.debug("Mule RPC: creating subscription for client: " + replyTo)};
            dojox.cometd.subscribe(replyTo, mule, callback);
            mule._replyToChannels[mule._replyToChannels.length] = replyTo;
            if(console){console.debug("Mule RPC: subscriptions are: " + mule._replyToChannels.toString())};
        }
        var messageJson = dojo.toJson(message);
        if(console){console.debug("message is: " + messageJson)};
        dojox.cometd.publish(channel, message);
        if(console){console.debug("channel is: " + channel)};
    }

};

dojo.addOnLoad(mule, "_init");
dojo.addOnUnload(mule, "_dispose");


