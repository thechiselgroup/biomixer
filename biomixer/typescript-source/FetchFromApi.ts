///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="OntologyGraph" />
"use strict";

import OntologyGraph = require('./OntologyGraph');
    
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
            [url: string]: RestCallStatus ;
    }
    
    export enum RestCallStatus {EXCLUDED, ALLOWED, AWAITING, COMPLETED, ERROR, FORBIDDEN}
    
    export class Registry {
        
        //This registry is used to track REST call privileges and status.
        private static nodeRestCallRegistry: NodeApiCallRegistry = { };
        
        // export static restCallStatus = {EXCLUDED: "excluded", ALLOWED: "allowed", AWAITING: "awaiting", COMPLETED: "completed", ERROR: "error", FORBIDDEN: "forbidden"};
        
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
        private static validRestCallStateTransition(oldStatus: RestCallStatus, newStatus: RestCallStatus,
//          node: any, 
            restCallUriFunction: string): boolean{
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
                // console.log("Invalid rest call registry status change requested: "+oldStatus+" to "+newStatus+" for rest call and node: "+restCallUriFunction);
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
//        public static addNodeToRestCallRegistry(node, restCallUriFunction, status){
         public static addUrlToRestCallRegistry(restCallUriFunction: string, status: RestCallStatus = RestCallStatus.ALLOWED ){
//            if(typeof Registry.nodeRestCallRegistry[node] === "undefined"){
//                Registry.nodeRestCallRegistry[node] = {};
//            } else {
                // var currentStatus = Registry.nodeRestCallRegistry[node][restCallUriFunction];
                var currentStatus = Registry.nodeRestCallRegistry[restCallUriFunction];
                if(typeof currentStatus !== "undefined"){
                    // if(!Registry.validRestCallStateTransition(currentStatus, status, node, restCallUriFunction)){
                    if(!Registry.validRestCallStateTransition(currentStatus, status, restCallUriFunction)){
                        return;
                    }
                }
//            }
            
            // Registry.nodeRestCallRegistry[node][restCallUriFunction] = status;
             Registry.nodeRestCallRegistry[restCallUriFunction] = status;
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
        // public static updateStatusForNodeInRestCallRegistry(node, restCallUriFunction, status){
        public static updateStatusForUrlInRestCallRegistry(restCallUriFunction, status){
            // Registry.addNodeToRestCallRegistry(node, restCallUriFunction, status);
            Registry.addUrlToRestCallRegistry(restCallUriFunction, status);
        }
        
        /**
         * Call this prior to asking for a REST call. If it returns true, do the call, otherwise do not.
         * 
         * @param node
         * @param restCallUriFunction
         */
         // public static checkNodeInRestCallWhiteList(node, restCallUriFunction){
         public static checkUrlInRestCallWhiteList(restCallUriFunction){
            // Used to register nodes against URLs, but I realized I can use the URL straight, no chaser.
//            if(typeof Registry.nodeRestCallRegistry[node] !== "undefined"){
//                return Registry.nodeRestCallRegistry[node][restCallUriFunction] === restCallStatus.ALLOWED;
//            } else {
//                return false;
//            }
              if(typeof Registry.nodeRestCallRegistry[restCallUriFunction] !== "undefined"){
                return Registry.nodeRestCallRegistry[restCallUriFunction] === RestCallStatus.ALLOWED;
            } else {
                return false;
            }
        }
    
    }

    // Good example here for when we need additional args as we extend a class:
    // http://blog.pluralsight.com/extending-classes-and-interfaces-using-typescript
    export interface CallbackObject {
        // node: any; // leave this pretty loose for now
        graph: OntologyGraph.OntologyGraph; // Make more general when refactoring concept graph into this
        fetcher: RetryingJsonFetcher; // Gets assigned when fetcher receives callback instance
        url: string;
        
        // Callbacks are problematic because "this" is in dynamic scope in Javascript, not lexical.
        // When the callback is actually called, "this" scopes to somethign other than the class we
        // think "this" refers to.
        // The solution is definign with the fat arrow (=>) or using a solution such as described by
        // Steven Ickman, where callbacks must always be prefixed by "cb_" and his code does magic.
        // see: http://stackoverflow.com/questions/12756423/is-there-an-alias-for-this-in-typescript
        // I added his code in case we want to use it later...but for now fat arrow is the way to go.
        callback: { (dataReceived: any, textStatus: string, jqXHR: any); } // I think this is right...
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
        
        // TODO Using default value of undefined, but we may want the "resultData?: any" optional param syntax instead, or
        // a default to Null...not sure. Wait til it's working to change that.
        public fetch(resultData: any = undefined) : number {
                // console.log("retryFetch for "+callbackObject.url);
                if(typeof resultData === "undefined"){
                    // If not error, call for first time
                    $.getJSON(this.callbackObject.url, null, this.callbackObject.callback);
//                    if(typeof this.callbackObject.node !== "undefined"){
//                        // I would adore classes to handle this. Cases without nodes would not implement
//                        // registry functionality.
                        Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.AWAITING);
//                    }
                    return 0;
                }
                
                if(typeof resultData.errors !== "undefined") {
                    if(resultData.status == "404"){
                        // 404 Error should fill in some popup data points, so let through...
                        console.log("Error: "+this.callbackObject.url+" --> Data: "+resultData.error);
                        if(typeof this.callbackObject !== "undefined"){
                            Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
                        }
                        return -1;
                    } else if(resultData.status == "403" && resultData.error.indexOf("Forbidden") >= 0){
                        console.log("Forbidden Error, no retry: "
                                +"\nURL: "+this.callbackObject.url
                                +"\nReply: "+resultData.error);
//                        if(typeof this.callbackObject !== "undefined"){
                            Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.FORBIDDEN);
//                        }
                        return 0;
                    } else if(resultData.status == "500" || resultData.status == "403"){
                        if(this.previousTriesRemade < 4){
                            this.previousTriesRemade++;
                            console.log("Retrying: "+this.callbackObject.url);
                            $.getJSON(this.callbackObject.url, null, this.callbackObject.callback);
                            // update to status unnecessary; still awaiting.
                            return -1;
                        } else {
                            // Error, but we are done retrying.
                            console.log("No retry, Error: "+resultData);
//                            if(typeof this.callbackObject.node !== "undefined"){
                                Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
//                            }
                            return null;
                        }
                    } else {
                        // Don't retry for other errors
                        console.log("Error: "+this.callbackObject.url+" --> Data: "+resultData.error);
//                        if(typeof this.callbackObject !== "undefined"){
                            Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.ERROR);
//                        }
                        return 0;
                    }
                } else {
                    // Success, great!
//                    if(typeof this.callbackObject.node !== "undefined"){
                        Registry.updateStatusForUrlInRestCallRegistry(this.callbackObject.url, RestCallStatus.COMPLETED);
//                    }
                    return 1;
                }
        }
    }
