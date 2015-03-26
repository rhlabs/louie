/* 
 * LouieClient.js
 * 
 * Copyright (c) 2015 Rhythm & Hues Studios. All rights reserved.
 */

define(['ProtoBuf','ByteBuffer'],function (ProtoBuf,ByteBuffer) { 

    var LouieClient = function(host,port,gateway) {
        console.log("LouieClient constructed");    
        this.host = host;
        this.port = typeof port !== "undefined" ? port : "8080"; //to be replaced in ECMA 6 I guess?
        this.gateway = typeof gateway !== "undefined" ? gateway : "louie";
        this.key = "undefined";
    };
    
    var requestProto = ProtoBuf.loadProtoFile("proto/request.proto");
    LouieClient.prototype.ReqHeaderPB = requestProto.build("louie.RequestHeaderPB");
    LouieClient.prototype.IdentityPB = requestProto.build("louie.IdentityPB");
    LouieClient.prototype.SessionKey = requestProto.build("louie.SessionKey");
    LouieClient.prototype.ResHeaderPB = requestProto.build("louie.ResponseHeaderPB");
    LouieClient.prototype.ResponsePB = requestProto.build("louie.ResponsePB");
    LouieClient.prototype.ReqPB = requestProto.build("louie.RequestPB");

    /**
     * 
     * @param {type} service The Service name
     * @param {type} method The method name
     * @param {type} paramNames A list of parameter String names
     * @param {type} params An array of the encoded paramaters 
     * @param {type} decoder The Builder associated with the response type.
     * @param {type} callback The Callback method called after response is received
     * @returns {undefined} 
     */
    LouieClient.prototype.request = function(service, method, paramNames, params, decoder, callback) {
        var self = this;
        var request = new XMLHttpRequest();
        
    //    var url = this.host + ":" + this.port + "/" + this.gateway + "/pb";       
        var url = "/" + this.gateway + "/pb"; //Working in CORS later? maybe?

        request.onload = function () { 
            var status = request.status;
            if (status !== 200) {
                if (status === 407) { //authentication failure
                    self.key = "undefined";
                } 
                console.log("Request failed, received: " + status.toString());
            }
            var arrayBuffer = request.response;
            if (arrayBuffer) {
                
                var bb = ByteBuffer.wrap(arrayBuffer);
                var resHeader = self.ResHeaderPB.decodeDelimited(bb);
                if (resHeader.key) { 
                    self.key = resHeader.key.key;
                    console.log(self.key); 
                }

                var results = new Array();
                for (var i = 0; i < resHeader.count; i++) {
                    var response = self.ResponsePB.decodeDelimited(bb); 
                    if (response.error) {
                        console.log(response.error.description); // Handle it your own way!
                    }
                    for (var j = 0; j < response.count; j++) {
                        results.push(decoder.decodeDelimited(bb));
                    }
                }
                callback(results);
            } else {
                console.log("Whoops. Didn't get anything back...?");
            }
        };

        var headerPB;
        if (this.key === 'undefined') {
            headerPB = new self.ReqHeaderPB({
                "count" : 1,
                "identity" : new self.IdentityPB({
                "user" : "unknown",
                "language" : "javascript",
                "program" : document.documentURI,
                "path" : "internet"
                })
            });
        } else {
            headerPB = new self.ReqHeaderPB({
                "count" : 1,
                "key" :new self.SessionKey({
                    "key" : this.key
                })
            });
        }
        var requestPB = new self.ReqPB({
            "id" : 1,
            "service" : service,
            "method" : method,
            "param_count" : 1,
            "type" : paramNames
        });

        var hbuffer = headerPB.encodeDelimited();
        var rbuffer = requestPB.encodeDelimited();
        var buffArray = [hbuffer.toArrayBuffer(), rbuffer.toArrayBuffer()];
        var finalArray = buffArray.concat(params);
        var buffer = ByteBuffer.concat(finalArray);

        request.open("POST",url,true);
        request.setRequestHeader("Content-type", "application/x-protobuf");
        request.responseType = "arraybuffer"; //REQUIRED

        request.send(buffer.toArrayBuffer());

    };
    
    return LouieClient;
});


// CONCEPTS, NOTES:


// should be a generic utilty to issue HTTP requests and receive a PB response?
// unclear at this point what the interaction with the ProtoBuf.js stuff will be like. 
// I think it may be preferable to just return the binary data directly from the request method and let the auto-gen clients 
// use some other module or handle the decoding themselves. That way this is strictly a comm tool and the role separation is cleaner IMO.
// 
// 
// Format it similar to the python client.

// IDEA: The service clients should pre-parse all of their requisite shit, and then hand in a list of param names, as well as the prepared (encoded i think) object, as two separate args to request()
    
    //so are the params pre-encoded and concatenated? Guess we'll just have to try some shit... First try concatenating the list of ByteBuffers, then later we can try concatenating the arraybuffers
    

 //
    //i somehow encode the header, the request, and the params and then concatenate that into an ArrayBuffer. Not sure if I can encode first, then concatenate, then get array buffer, or if i have to concat after getting arraybuffer?
    // I THINK I can concatenate two arraybuffers, if I"m unable to concatenate the encoded messages first? (can't find doc on encode()) 
    
    //RequestHeaderPB, RequestPB
    //what"s the hold up??? file management i guess?? where to put shit and how to make sure i can get at it later???
    // I think we should use RequireJS??? wtfffff
    //so require supplies all of our proto files somehow... 
    // then that allows me to structure all of my js in folders.
    
    // because any .proto files can be versioned, a copying mechanism should be part of the build process to avoid versioning issues later on. Then this all should be jammed into another assembly. Hell yeah. 
    // when auto-gen obj defs happen, that"s when the .proto files are copied i guess?
    // everything finally gets packaged into a set of folders.
    // Ideally, a client can just load the single "service" obj def they need and new it, then start running. 
    // While we"re at it, we might as well use the command line thingy to auto-gen the matching JSON definitions? Because why not?
    //    If we have the JSON defs, and it"s simple, then the service obj defs should be able to return the decoded JS obj or a corresponding JSON obj.
    
    