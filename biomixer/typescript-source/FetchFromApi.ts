///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />
// ///<reference path="headers/jquery.jsonp.amd.d.ts" />

///<amd-dependency path="JQueryExtension" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Utils" />
"use strict";

import GraphView = require('./GraphView');
import Utils = require('./Utils');
    
    // TODO Nested classes would be nice, but need more modules. Avoid or use?
    /**
     * Maps nodes to URLs for API calls.
     */
    // TODO We don't need to index these by ndoes, do we?
    // If REST calls are real REST calls, the extra info of the node object adds nothing to this registry.
    // just the API call. Look into whether we actually need the node indexing things.
    interface NodeApiCallRegistry {
        // [node: string]: {[url: string]: restCallStatus };
        // Converting registry to be URL based rather than node based
            [url: string]: RestCallCache ;
    }

        
    export interface ResultData {
        error?;
        status?;
        responseData?;
    }
    
    export enum RestCallStatus {EXCLUDED, ALLOWED, AWAITING, COMPLETED, ERROR, FORBIDDEN}

    class RestCallCache {
        // No need to export, used by Registry only.
        responseData: ResultData = undefined;
        status: RestCallStatus = undefined;
        private callbacksEntered: Array<string> = [];
        
        constructor(status: RestCallStatus, firstCallback: CallbackObject){
            this.status = status;
            this.addCallback(firstCallback);
        }
    
        /**
         * The caller may be updating status only, in which case the callback parameter may be provided with a null value.
         */
        update(newStatus: RestCallStatus, additionalCallback: CallbackObject, responseData: ResultData = undefined){
            // TODO Check validity here...it is checked by caller, the Registry class...
            this.status = newStatus;
            if(responseData !== undefined){
                Cache.updateCacheMemoryUsage(responseData);
                this.addCallback(additionalCallback);
                this.responseData = responseData;
            }
        }
        
        hasCallback(callback: CallbackObject){
            return this.callbacksEntered.indexOf(callback.getCallbackName()) !== -1;
        }
    
        private addCallback(callback: CallbackObject){
            if(null == callback){
                return;
            }
            var callbackName = callback.getCallbackName();
            if(this.callbacksEntered.indexOf(callbackName) == -1){
                this.callbacksEntered.push(callbackName);
            }
        }
    }
    
    // TODO Might want caching to speed up transitions of concept graphs.
    // Don't use the JQuery cache option, which uses browser caching; we only want caching within a page load.
    // Implement by keying returned data by URL (not including any JQuery anti-cache random numbers). 
    export class Cache {
        
        static MB = 1024*1024; // 1000 kilobytes is a MB
        static maxJsonCacheSizeByte = 10 * Cache.MB;
        static currentJsonCacheSizeByte = 0;
        
        //This registry is used to track REST call privileges and status.
        private static restAndCallbackRegistry: NodeApiCallRegistry = { };
        
        static getCachedData(restUrl: string): ResultData {
            if(Cache.restAndCallbackRegistry[restUrl] == undefined || Cache.restAndCallbackRegistry[restUrl].responseData == undefined ){
                return undefined;
            } else {
                return Cache.restAndCallbackRegistry[restUrl].responseData;
            }
        }
        
        /**
         * Track overall size of cached data. If it gets too big, remove some elements.
         * Browsers *tend* to store object properties in the order created, so we will
         * *probably* remove things in order from oldest to newest. It shouldn't matter much.
         */
        static updateCacheMemoryUsage(responseData: ResultData){
            var stringVersion = JSON.stringify(responseData);
            // http://stackoverflow.com/questions/11141136/default-javascript-character-encoding
            var totalByteSize = stringVersion.length * 2; // 2 bytes, Strings are UTF-16, 16 bits
            var toRemove = [];
            
            if(Cache.currentJsonCacheSizeByte + totalByteSize > Cache.maxJsonCacheSizeByte){
                
                // Drop off earliest COMPLETE cache elements. If there are no such, too bad for us; the cache will grow.
                // Would have used grep but these are object properties, not array items.
                $.each(Cache.restAndCallbackRegistry, (uriKey, cacheItem: RestCallCache) => {
                        if(cacheItem.status != RestCallStatus.COMPLETED || cacheItem.responseData === undefined){
                            return true; // next value...
                        }
                        
                        // Drop it like it's hot.
                        var droppedSize = JSON.stringify(cacheItem.responseData).length * 2;
                        Cache.currentJsonCacheSizeByte -= droppedSize;
                        toRemove.push(uriKey);
                    
                        // Jump out if we did enough removal.
                        if(Cache.currentJsonCacheSizeByte + totalByteSize <= Cache.maxJsonCacheSizeByte){
                            return false; // break out
                        }    
                    }
                );
                
                $.each(toRemove, (i, uriKey) => {
                    delete Cache.restAndCallbackRegistry[uriKey];
                });
            }
            
            // Technically we don't add the data until after working on the cache, so this value is the end
            // value from that point, not this point of execution.
            Cache.currentJsonCacheSizeByte += totalByteSize;
        }
        
        /**
         * We can logically make the following transitions:
         * <none> -> ALLOWED
         * <none> -> EXCLUDED
         * ALLOWED -> EXCLUDED
         * ALLOWED -> AWAITING
         * EXCLUDED -> ALLOWED
         * AWAITING -> FORBIDDEN
         * AWAITING -> ERROR
         * AWAITING -> COMPLETED
         * ERROR -> ALLOWED
         * 
         * COMPLETED and FORBIDDEN should never ever be changed.
         */
        private static validRestCallStateTransition(oldStatus: RestCallStatus, newStatus: RestCallStatus, restCallUriFunction: string): boolean{
            var isValid = true;
            switch(oldStatus){
            case RestCallStatus.ALLOWED:
                if(newStatus !== RestCallStatus.AWAITING
                        && newStatus !== RestCallStatus.EXCLUDED){
                    isValid = false;
                }
                break;
            case RestCallStatus.EXCLUDED:
                if(newStatus !== RestCallStatus.ALLOWED){
                    isValid = false;
                }
                break;
            case RestCallStatus.AWAITING:
                if(newStatus !== RestCallStatus.COMPLETED
                        && newStatus !== RestCallStatus.ERROR
                        && newStatus !== RestCallStatus.FORBIDDEN){
                    isValid = false;
                }
                break;
            case RestCallStatus.ERROR:
                if(newStatus !== RestCallStatus.ALLOWED){
                    isValid = false;
                }
                break;
            case RestCallStatus.FORBIDDEN:
                isValid = false;
                break;
            case RestCallStatus.COMPLETED:
                isValid = false;
                break;
            default:
                // This is no value at all.
                if(newStatus !== RestCallStatus.ALLOWED
                        && newStatus !== RestCallStatus.EXCLUDED){
                    isValid = false;
                }
                break;
            }
            
            if(!isValid && oldStatus != newStatus){
                // Note that if any nodes are "pre-loaded", this is a likely and acceptable to happen when they are triggered via other means.
                // This will also happen in the concept graph when multiple relations are followed to the same node.
                // Only use this for debugging purposes.
                console.log("Invalid rest call registry status change requested: "+oldStatus+" to "+newStatus+" for rest call and node: "+restCallUriFunction);
                // console.log(node);
            }
            
            return isValid;
            
            
        }
        
        /**
         * Nodes have REST calls made when they are made visible to the graph, as opposed to when they are added to it.
         * Perhaps our needs will change later, but the logic should be extensible for that.
         * 
         * Nodes will only have their REST calls made when the call in question hasn't been made, and when they are cleared
         * to have their call made.
         * 
         * @param node
         * @param restCallUriFunction
         * @param status    Optional. Defaults to ALLOWED status for the specified REST call.
         */
         public static addUrlToRestCallRegistry(restCallURL: string, callback: CallbackObject, status: RestCallStatus = RestCallStatus.ALLOWED, responseDataToCache: ResultData = undefined ){
            var currentCache = Cache.restAndCallbackRegistry[restCallURL];
            var currentStatus = undefined;
            if(currentCache !== undefined){
                currentStatus = currentCache.status;
            } else {
                // Create a new cache, but we'll abandon it if the new state is invalid. Refactor perhaps
                currentCache = new RestCallCache(status, callback);
            }
            if(currentStatus !== undefined){
            	if(!Cache.validRestCallStateTransition(currentStatus, status, restCallURL)){
            	    return;
            	}
            }
            
            // BUG Technically, this allows undefined status to jump to any value...
            // This currently allows the concept graph to work as is though...
            currentCache.update(status, callback, responseDataToCache);
            Cache.restAndCallbackRegistry[restCallURL] = currentCache;
        }
        
        /**
         * Fetcher uses this to update node privileges. Can we used external to fetcher as well,
         * but if fetches are underway, it is unsafe. Specifically, once permission has been granted,
         * taking it away is not guaranteed to work.
         * 
         * @param node
         * @param restCallUriFunction
         * @param status
         */
        public static updateStatusForUrlInRestCallRegistry(restCallURL, status, responseDataToCache?: ResultData){
            Cache.addUrlToRestCallRegistry(restCallURL, null, status, responseDataToCache);
            
        }
        
        /**
         * Call this prior to asking for a REST call. If it returns true, do the call, otherwise do not.
         * 
         * @param node
         * @param restCallUriFunction
         */
         private static checkUrlInRestCallWhiteList(restCallUriFunction: string, callback: CallbackObject, undefinedOk: boolean = false): boolean{
            var restCache = Cache.restAndCallbackRegistry[restCallUriFunction];
            if(restCache !== undefined){
                // Block multiple usage of callback
                return !restCache.hasCallback(callback);  
            } else if(restCache == undefined || restCache.status == undefined){
                return undefinedOk;
            }else {
                var entry: RestCallStatus = restCache.status;
                return entry === RestCallStatus.ALLOWED
                    || entry === RestCallStatus.ERROR;
            }
        }
    
        public static checkUrlFirstCallOrError(restURL: string, callback: CallbackObject): boolean {
            return Cache.checkUrlInRestCallWhiteList(restURL, callback, true);
        }
    
    }

    // Good example here for when we need additional args as we extend a class:
    // http://blog.pluralsight.com/extending-classes-and-interfaces-using-typescript
    export class CallbackObject {
        // node: any; // leave this pretty loose for now
        graph: GraphView.Graph; // Make more general when refactoring concept graph into this
        url: string;
        
        fetcher: RetryingJsonFetcher; // Gets assigned when fetcher receives callback instance
        
        constructor(
            graph: GraphView.Graph,
            url: string
        ){
                this.graph = graph;
                this.url = Utils.prepUrlKey(url);
        }
        
        getCallbackName(): string{
            var funcNameRegex = /function (.{1,})\(/;
            var results = (funcNameRegex).exec((<any>this).constructor.toString());
            return (results && results.length > 1) ? results[1] : "";;
        }
        
        // Callbacks are problematic because "this" is in dynamic scope in Javascript, not lexical.
        // When the callback is actually called, "this" scopes to somethign other than the class we
        // think "this" refers to.
        // The solution is definign with the fat arrow (=>) or using a solution such as described by
        // Steven Ickman, where callbacks must always be prefixed by "cb_" and his code does magic.
        // see: http://stackoverflow.com/questions/12756423/is-there-an-alias-for-this-in-typescript
        // I added his code in case we want to use it later...but for now fat arrow is the way to go.
        callback: { (dataReceived: any, textStatus: string, jqXHR: any); } // I think this is right...
//        callback: { (dataReceived: any, textStatus: string, xOptions: JQueryJsonp.XOptions); } // Modified for jquery.jsonp lib usage.
//        assignFetcher: { (fetcher: RetryingJsonFetcher); };
    }

    /*
     * This fetcher system allows the success receiver to call it to see if there has been an error that
     * allows for a retry. It is fairly clean on the user side, though it does require checking of
     * return values.
     */
    export class RetryingJsonFetcher {
        callbackObject: CallbackObject;
        previousTriesRemade: number = 0;
        
        constructor(callbackObject: CallbackObject) {
            this.callbackObject = callbackObject;
//            this.callbackObject.assignFetcher(this);
            this.callbackObject.fetcher = this;
        }
        
    private callAgain(){
        var cachedData = Cache.getCachedData(this.callbackObject.url);
        if(cachedData !== undefined){
            // Skip the dispatch of the call, fake the data back into the normal flow.
            this.callbackObject.callback(cachedData, "manually cached", null)
            return;
        }
        
        // http://stackoverflow.com/questions/1641507/detect-browser-support-for-cross-domain-xmlhttprequests
        // var browserSupportsCors = typeof XDomainRequest != "undefined";
//        var browserSupportsCors = 'withCredentials' in new XMLHttpRequest();
//    
//        if(!browserSupportsCors){
        // if CORS isn't available, we cannot receive server status codes off an XHR object,
        // because JSONP requests don't get that back from the browser. So sad.
        
        var outerThis = this;
//        $.getJSON(this.callbackObject.url, null, this.callbackObject.callback);
        $.ajax({
            url: this.callbackObject.url,
            data: null,
            dataType: 'jsonp',
            type: "GET",
            success: function (data, textStatus, jqXHR){
                    outerThis.callbackObject.callback(data, textStatus, jqXHR);
                },
            error: function (jqXHR, textStatus, errorThrown ){
                    outerThis.callbackObject.callback({errors: true, status:errorThrown}, textStatus, jqXHR); 
                },
            }
        );
    
//        // from the jquery.jsonp library, in case we want more options.
//        // Would this offer additional responses with cross domain at all?
//        $.jsonp(
//        <JQueryJsonp.XOptions>{
//            url: this.callbackObject.url,
//            callbackParameter: "callback",
//            data: null,
//            success: function (data, textStatus, xOptions){
//                //console.log("jsonp success");
//                //console.log(arguments);
//                //console.log(jqXHR.status+" and text status "+textStatus);
//                outerThis.callbackObject.callback(data, textStatus, xOptions);
//            },
//            error: function(xOptions, textStatus){
//                // Unless using CORS, there is no way to receive the status code form the browser.
//                //console.log(textStatus); // either 'error' or 'timeout'
//                //console.log(xOptions);
//                var subData = {error: textStatus, status: xOptions};
//                // Still pass back for processing
//                outerThis.callbackObject.callback(subData, textStatus, xOptions);
//            },
//        });
//        } else {
//            var postObject = null;
//            $.ajax({
//                // Make sure we don't ask for JSONP for this; normal JSON instead.
//                url: this.callbackObject.url.replace("format=jsonp", ""),
//                type: "GET", // POST necessary??
//                crossDomain: true,
//                data: postObject,
//                dataType: "json", // not jsonp, note
//                success: function (response){ //(data, textStatus, xOptions){
//                     var resp = JSON.parse(response)
//                    alert(resp.status);
//                    outerThis.callbackObject.callback(response, null, null);
//                },
//                error: function (xhr, textStatus) {
//                     alert("CORS error: "+xhr);
//                     outerThis.callbackObject.callback({errors: true, status: xhr.status}, textStatus, xhr); 
//                },
//                statusCode :{
//                    0: function(){
//                        console.log("Code 0");
//                        },
//                    200: function(){
//                        console.log("Code 200");
//                        },
//                    404: function(){
//                        console.log("Code 200");
//                        },
//                    429: function(xhr){
//                        // Looking to use setTimer in this case, call again after a pause.
//                        console.log("Code 429");
//                        }    
//                }
//            });
//        }

        	// Tried the ajax styler instead, but I still could not catch
        	// errors...poissibly due to making cross site requests.
//$.ajax({
//    url: this.callbackObject.url.replace("mappings","broke"),
//    data: null,
//    dataType: 'jsonp',
//    timeout: 3000,
//    complete: function(xhr, textStatus) {
//        console.log("jsonp complete");
//        console.log(xhr.status+" and text status "+textStatus);
//    },
//    error: function(xhr, textStatus, errorThrown) {
//        console.log('jsonp error');
//        console.log(arguments);
//        console.log(xhr.status+" and text status "+textStatus);
//    },
//    success: (data, textStatus, jqXHR) => {
//        console.log("jsonp success");
//        console.log(arguments);
//        console.log(jqXHR.status+" and text status "+textStatus);
//        this.callbackObject.callback(data, textStatus, jqXHR);
//    },
////    error: (jqXHR, textStatus, errorThrown ) => {
////        this.callbackObject.callback({errors: true, status:errorThrown}, textStatus, jqXHR); 
////    },
////    fail: (jqXHR, textStatus, errorThrown ) => {
////        this.callbackObject.callback({errors: true, status:errorThrown}, textStatus, jqXHR); 
////    },
////    statusCode :{
////        0: function(){
////            },
////        200: function(){
////            },
////    	  429: function(xhr){
////      		// Looking to use setTimer in this case, call again after a pause.
////            }    
////    },
//    complete: function(xhr, textStatus){
//            alert("complete: "+textStatus);
//        }
//  });
            
			
        }


        // TODO Using default value of undefined, but we may want the "resultData?: any" optional param syntax instead, or
        // a default to Null...not sure. Wait til it's working to change that.
        // TODO I think the error codes seen below only worked for the old API. The new one doesn't offer error codes
        // embedded in a response. Worse yet, browsers do not pass received error codes to AJAX callers for cross-site JSONP.
        // That means none of this can function anymore, because no such data is passed on at all.
        /**
         * If the REST calls have been made with the same callback to the same URL before, it will rebuff.
         * If the same REST calls have been made with *different* callbacks within the same page load,
         * it will use a manually cached version of the data.
         */
        public fetch(resultData: ResultData = undefined) : number {
            
            // console.log("retryFetch for "+callbackObject.url);
            // If not error or valid data, call for first time...maybe...
            if(resultData === undefined){
                
                // First things first: we don't allow the same callback to call on the same URL twice.
                // Second, we have caching...but that will be handled a little bit later.
               if(!Cache.checkUrlFirstCallOrError(this.callbackObject.url, this.callbackObject)){
                   // Multiple callbacks may want the same data. If so, let them get it...from the
                   // manually cached responses. Otherwise, prevent callers from abusing the REST services.
                   // Really, multipel calls may be programmer errors, but it happens naturally with the
                   // filtering functionality.
                   // Browser caching can be unreliable, and we don't want to cache between page loads.
                   
                   return null;
                }
                    
                // Make a url and callback entry.
                // If we are recalling on an erorr this should cope with redundant entries.
                Cache.addUrlToRestCallRegistry(this.callbackObject.url, this.callbackObject, RestCallStatus.AWAITING);
                this.callAgain();
                return 0;
            }
                
            // TODO If JqueryJsonp is working out, get this all working off the raw XOptions object.
            if(typeof resultData.error !== "undefined") { // timeout from JQueryJsonp
                if(resultData.status == "404" || resultData.error == "timeout"){
                    // 404 Error should fill in some popup data points, so let through...
                    console.log("Error: "+this.callbackObject.url+" --> Data: "+resultData.error);
                    if(typeof this.callbackObject !== "undefined"){
                        Cache.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
                    }
                    return -1;
                } else if(resultData.status == "403" && resultData.error.indexOf("Forbidden") >= 0){
                    console.log("Forbidden Error, no retry: "
                            +"\nURL: "+this.callbackObject.url
                            +"\nReply: "+resultData.error);
                    Cache.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.FORBIDDEN);
                    return 0;
                } else if(resultData.status == "500" || resultData.status == "403"){
                    if(this.previousTriesRemade < 4){
                        this.previousTriesRemade++;
                        console.log("Retrying: "+this.callbackObject.url);
                        this.callAgain();
                        // update to status unnecessary; still awaiting.
                        return -1;
                    } else {
                        // Error, but we are done retrying.
                        console.log("No retry, Error: "+resultData);
                        Cache.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
                        return null;
                    }
                } else {
                    // Don't retry for other errors
                    console.log("Error: "+this.callbackObject.url+" --> Data: "+resultData.error);
                    Cache.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
                    return 0;
                }
            } else {
                // Success, great!
                Cache.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.COMPLETED, resultData);
                return 1;
            }
        }
    }
