///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />
// ///<reference path="headers/jquery.jsonp.amd.d.ts" />
///<amd-dependency path="JQueryExtension" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="Utils" />
///<amd-dependency path="MouseSpinner" />
"use strict";
define(["require", "exports", './Utils', './MouseSpinner', "JQueryExtension", "GraphView", "Utils", "MouseSpinner"], function (require, exports, Utils, MouseSpinner) {
    (function (RestCallStatus) {
        RestCallStatus[RestCallStatus["EXCLUDED"] = 0] = "EXCLUDED";
        RestCallStatus[RestCallStatus["ALLOWED"] = 1] = "ALLOWED";
        RestCallStatus[RestCallStatus["AWAITING"] = 2] = "AWAITING";
        RestCallStatus[RestCallStatus["COMPLETED"] = 3] = "COMPLETED";
        RestCallStatus[RestCallStatus["ERROR"] = 4] = "ERROR";
        RestCallStatus[RestCallStatus["FORBIDDEN"] = 5] = "FORBIDDEN";
    })(exports.RestCallStatus || (exports.RestCallStatus = {}));
    var RestCallStatus = exports.RestCallStatus;
    var RestCallCache = (function () {
        function RestCallCache() {
            // No need to export, used by Registry only.
            this.responseData = undefined;
            this.status = undefined;
            this.callbackUnservedSet = {};
            this.callbackServedSet = {};
        }
        /**
         * The caller may be updating status only, in which case the callback parameter may be provided with a null value.
         */
        RestCallCache.prototype.updateStatus = function (newStatus, responseData) {
            if (responseData === void 0) { responseData = undefined; }
            // TODO Check validity here...it is checked by caller, the Registry class...
            this.status = newStatus;
            if (responseData !== undefined) {
                CacheRegistry.updateCacheMemoryUsage(responseData);
                this.responseData = responseData;
            }
        };
        /**
         * Return value indicates whether there was a pre-existing match for the callback.
         */
        RestCallCache.prototype.addCallbackToQueue = function (callback) {
            if (undefined !== this.callbackUnservedSet[callback.getCallbackName()]) {
                console.log("Redundant callback registered: " + callback.getCallbackName());
                return false;
            }
            this.callbackUnservedSet[callback.getCallbackName()] = callback;
            return true;
        };
        /**
         * When each callback has been serviced, track it in a separate container.
         */
        RestCallCache.prototype.markAsServed = function (callbackObject) {
            delete this.callbackUnservedSet[callbackObject.getCallbackName()];
            this.callbackServedSet[callbackObject.getCallbackName()] = callbackObject;
        };
        /**
         * We can see if a callback has already been served subsequent to a REST call,
         * whether it was using the cache or not, so that we can avoid makign duplicate
         * calls.
         */
        RestCallCache.prototype.alreadyServed = function (callbackObject) {
            return undefined !== this.callbackServedSet[callbackObject.getCallbackName()];
        };
        /**
         * When we want to loop over unserved callbacks (such as when REST calls return) we can
         * do so here.
         */
        RestCallCache.prototype.getUnservedCallbacks = function () {
            return this.callbackUnservedSet;
        };
        /**
         * Calling this allows redundant calls, which is desirable when we are reloading
         * the visualization somehow, but want to keep all the REST cache we have accumulated.
         */
        RestCallCache.prototype.clearAllCallbackServiceRecords = function () {
            this.callbackUnservedSet = {};
            this.callbackServedSet = {};
        };
        return RestCallCache;
    })();
    exports.RestCallCache = RestCallCache;
    /**
     * Browser caching can be unreliable, and we don't want to cache between page loads.
     * We will manually cache data, while also tracking error status per URL and all callbacks
     * that depend on an outstanding REST call. Those depending on completed REST calls may use
     * cached data and error status.
     */
    var CacheRegistry = (function () {
        function CacheRegistry() {
        }
        CacheRegistry.getCurrentMBStored = function () {
            // console.log((CacheRegistry.currentJsonCacheSizeByte/CacheRegistry.MB)+"");
            return parseFloat("" + (CacheRegistry.currentJsonCacheSizeByte / CacheRegistry.MB)).toFixed(2);
        };
        CacheRegistry.clearAllServiceRecordsKeepCacheData = function () {
            $.each(CacheRegistry.restAndCallbackRegistry, function (uriKey, cacheItem) {
                cacheItem.clearAllCallbackServiceRecords();
            });
        };
        CacheRegistry.getCachedData = function (restUrl) {
            var cachedItem = CacheRegistry.getCachedItem(restUrl);
            if (cachedItem == undefined || cachedItem.responseData == undefined) {
                return undefined;
            }
            else {
                return cachedItem.responseData;
            }
        };
        CacheRegistry.getCachedItem = function (restUrl) {
            var currentCache = CacheRegistry.restAndCallbackRegistry[restUrl];
            if (currentCache === undefined) {
                // Create a new cache, but we'll abandon it if the new state is invalid. Refactor perhaps
                currentCache = new RestCallCache();
                CacheRegistry.restAndCallbackRegistry[restUrl] = currentCache;
            }
            return CacheRegistry.restAndCallbackRegistry[restUrl];
        };
        CacheRegistry.isNotRegisteredInCache = function (restUrl) {
            return undefined === CacheRegistry.restAndCallbackRegistry[restUrl];
        };
        /**
         * Track overall size of cached data. If it gets too big, remove some elements.
         * Browsers *tend* to store object properties in the order created, so we will
         * *probably* remove things in order from oldest to newest. It shouldn't matter much.
         */
        CacheRegistry.updateCacheMemoryUsage = function (responseData) {
            var stringVersion = JSON.stringify(responseData);
            // http://stackoverflow.com/questions/11141136/default-javascript-character-encoding
            var totalByteSize = stringVersion.length * 2; // 2 bytes, Strings are UTF-16, 16 bits
            var toRemove = [];
            if (CacheRegistry.currentJsonCacheSizeByte + totalByteSize > CacheRegistry.maxJsonCacheSizeByte) {
                // Drop off earliest COMPLETE cache elements. If there are no such, too bad for us; the cache will grow.
                // Would have used grep but these are object properties, not array items.
                $.each(CacheRegistry.restAndCallbackRegistry, function (uriKey, cacheItem) {
                    if (cacheItem.status != RestCallStatus.COMPLETED || cacheItem.responseData === undefined) {
                        return true; // next value...
                    }
                    // Drop it like it's hot.
                    var droppedSize = JSON.stringify(cacheItem.responseData).length * 2;
                    CacheRegistry.currentJsonCacheSizeByte -= droppedSize;
                    toRemove.push(uriKey);
                    // Jump out if we did enough removal.
                    if (CacheRegistry.currentJsonCacheSizeByte + totalByteSize <= CacheRegistry.maxJsonCacheSizeByte) {
                        return false; // break out
                    }
                });
                $.each(toRemove, function (i, uriKey) {
                    delete CacheRegistry.restAndCallbackRegistry[uriKey];
                });
            }
            // Technically we don't add the data until after working on the cache, so this value is the end
            // value from that point, not this point of execution.
            CacheRegistry.currentJsonCacheSizeByte += totalByteSize;
        };
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
        CacheRegistry.validRestCallStateTransition = function (oldStatus, newStatus, restCallUriFunction) {
            var isValid = true;
            switch (oldStatus) {
                case RestCallStatus.ALLOWED:
                    if (newStatus !== RestCallStatus.AWAITING
                        && newStatus !== RestCallStatus.EXCLUDED) {
                        isValid = false;
                    }
                    break;
                case RestCallStatus.EXCLUDED:
                    if (newStatus !== RestCallStatus.ALLOWED) {
                        isValid = false;
                    }
                    break;
                case RestCallStatus.AWAITING:
                    if (newStatus !== RestCallStatus.COMPLETED
                        && newStatus !== RestCallStatus.ERROR
                        && newStatus !== RestCallStatus.FORBIDDEN) {
                        isValid = false;
                    }
                    break;
                case RestCallStatus.ERROR:
                    if (newStatus !== RestCallStatus.ALLOWED) {
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
                    if (newStatus !== RestCallStatus.ALLOWED
                        && newStatus !== RestCallStatus.EXCLUDED) {
                        isValid = false;
                    }
                    break;
            }
            if (!isValid && oldStatus != newStatus) {
                // Note that if any nodes are "pre-loaded", this is a likely and acceptable to happen when they are triggered via other means.
                // This will also happen in the concept graph when multiple relations are followed to the same node.
                // Only use this for debugging purposes.
                console.log("Invalid rest call registry status change requested: " + oldStatus + " to " + newStatus + " for rest call and node: " + restCallUriFunction);
            }
            return isValid;
        };
        /**
         * Fetcher uses this to update node privileges. Can we used external to fetcher as well,
         * but if fetches are underway, it is unsafe. Specifically, once permission has been granted,
         * taking it away is not guaranteed to work.
         *
         * @param node
         * @param restCallUriFunction
         * @param status
         */
        CacheRegistry.updateStatusForUrlInRestCallRegistry = function (restCallURL, status, responseDataToCache) {
            var currentCache = CacheRegistry.getCachedItem(restCallURL);
            var currentStatus = currentCache.status;
            if (currentStatus !== undefined) {
                if (!CacheRegistry.validRestCallStateTransition(currentStatus, status, restCallURL)) {
                    return;
                }
            }
            // BUG Technically, this allows undefined status to jump to any value...
            // This currently allows the concept graph to work as is though...
            currentCache.updateStatus(status, responseDataToCache);
            return currentCache;
        };
        CacheRegistry.enqueueCallbackForUrlInRestCallRegistry = function (restCallURL, callback) {
            var currentCache = CacheRegistry.getCachedItem(restCallURL); // restAndCallbackRegistry[restCallURL];
            currentCache.addCallbackToQueue(callback);
            return currentCache;
        };
        /**
         * Call this prior to asking for a REST call. If it returns true, do the call, otherwise do not.
         *
         * @param node
         * @param restCallUriFunction
         */
        CacheRegistry.checkUrlNotBad = function (restURL) {
            var restCache = CacheRegistry.restAndCallbackRegistry[restURL];
            if (restCache == undefined || restCache.status == undefined) {
                return true;
            }
            else {
                var entry = restCache.status;
                return entry === RestCallStatus.ALLOWED
                    || entry === RestCallStatus.ERROR;
            }
        };
        CacheRegistry.MB = 1024 * 1024; // 1000 kilobytes is a MB
        CacheRegistry.maxJsonCacheSizeByte = 20 * CacheRegistry.MB;
        CacheRegistry.currentJsonCacheSizeByte = 0;
        //This registry is used to track REST call privileges and status.
        CacheRegistry.restAndCallbackRegistry = {};
        return CacheRegistry;
    })();
    exports.CacheRegistry = CacheRegistry;
    (function (CallbackVarieties) {
        // Originally added for dealing with aborted expansions. Not used for that.
        // Decided to keep around in case it was useful later.
        CallbackVarieties[CallbackVarieties["nodeSingle"] = 0] = "nodeSingle";
        CallbackVarieties[CallbackVarieties["nodesMultiple"] = 1] = "nodesMultiple";
        CallbackVarieties[CallbackVarieties["links"] = 2] = "links";
        CallbackVarieties[CallbackVarieties["metaData"] = 3] = "metaData";
        CallbackVarieties[CallbackVarieties["fullOntologyMapping"] = 4] = "fullOntologyMapping";
    })(exports.CallbackVarieties || (exports.CallbackVarieties = {}));
    var CallbackVarieties = exports.CallbackVarieties;
    // Good example here for when we need additional args as we extend a class:
    // http://blog.pluralsight.com/extending-classes-and-interfaces-using-typescript
    var CallbackObject = (function () {
        function CallbackObject(url, uniqueContextId, callbackVariety) {
            this.url = url;
            this.uniqueContextId = uniqueContextId;
            this.callbackName = this.computeCallbackName();
            this.callbackVariety = callbackVariety;
        }
        CallbackObject.prototype.getCallbackName = function () {
            return this.callbackName;
        };
        CallbackObject.prototype.computeCallbackName = function () {
            var funcNameRegex = /function (.{1,})\(/;
            var results = (funcNameRegex).exec(this.constructor.toString());
            var className = "";
            if (results && results.length > 1) {
                className = results[1];
            }
            className += "::" + this.uniqueContextId;
            return className;
        };
        return CallbackObject;
    })();
    exports.CallbackObject = CallbackObject;
    /*
     * This fetcher system allows the success receiver to call it to see if there has been an error that
     * allows for a retry. It is fairly clean on the user side, though it does require checking of
     * return values.
     */
    var RetryingJsonFetcher = (function () {
        function RetryingJsonFetcher(restUrl) {
            this.restUrl = restUrl;
            this.previousTriesRemade = 0;
        }
        RetryingJsonFetcher.prototype.callAgain = function () {
            // http://stackoverflow.com/questions/1641507/detect-browser-support-for-cross-domain-xmlhttprequests
            // var browserSupportsCors = typeof XDomainRequest != "undefined";
            var browserSupportsCors = 'withCredentials' in new XMLHttpRequest();
            var dataType = "json";
            var type = "GET"; // Later with batch calls, might use POST sometimes
            var data = null;
            var urlString = Utils.prepUrlKey(this.restUrl, RetryingJsonFetcher.userapikey, false);
            if (!browserSupportsCors) {
                // if CORS isn't available, we cannot receive server status codes off an XHR object,
                // because JSONP requests don't get that back from the browser. So sad.
                // Cannot use POST or CORS here, must use GET and jsonp
                console.log("Browser not CORS compatible");
                // TODO be sure to check this beforehand if we attempt to do batch CORS requests
                dataType = "jsonp";
                type = "GET";
            }
            if (type === "POST") {
                // TODO Can refactor. I think that if the data is provided as POST needs it for a GET call instead
                // that th eajax call will encode it into the GET url properly.
                var url = purl(urlString);
                data = url.attr("query");
                // Decompose GET URL and recompose into POST URL and data.
                var urlString = this.restUrl.replace("?" + data, "");
            }
            var outerThis = this;
            $.ajax({
                url: urlString,
                data: data,
                dataType: dataType,
                type: type,
                crossDomain: true,
                success: function (data, textStatus, jqXHR) {
                    var errorOrRetry = outerThis.processResponse(data);
                    // These error handlers are from the older API with the embedded error codes.
                    if (0 == errorOrRetry) {
                        return;
                    }
                    else if (-1 == errorOrRetry) {
                        // have an error. Done?
                        return;
                    }
                    // We need the ability to fulfill multiple requests. Any callbacks registered with this
                    // URL will be fulfilled when we have a success.
                    var cacheItem = CacheRegistry.getCachedItem(outerThis.restUrl);
                    var queue = cacheItem.getUnservedCallbacks();
                    for (var i in queue) {
                        var callbackObj = queue[i];
                        // Callback trigger doesn't ever receive or handle errors...after all, how could it?
                        // We can therefore safely run through them all, dispatch them, and remove them.
                        cacheItem.markAsServed(callbackObj);
                        MouseSpinner.MouseSpinner.haltSpinner(callbackObj.getCallbackName());
                        callbackObj.callback(data, textStatus, jqXHR);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    var errorOrRetry = outerThis.processResponse(jqXHR);
                    // These error handlers are from the older API with the embedded error codes.
                    if (-1 == errorOrRetry) {
                        // retrying, do nothing.
                        return;
                    }
                    else if (0 == errorOrRetry) {
                    }
                    else if (1 == errorOrRetry) {
                        // Success? Why are we in the error handler if this happens?
                        // I do not expect this to ever occur.
                        console.log("Success found in error handler:");
                        console.log(jqXHR);
                    }
                    console.log("Error Code: " + jqXHR.status + " (" + errorThrown + "; " + textStatus + ")");
                    var cacheItem = CacheRegistry.getCachedItem(outerThis.restUrl);
                    var queue = cacheItem.getUnservedCallbacks();
                    for (var i in queue) {
                        var callbackObj = queue[i];
                        // Callback trigger doesn't ever receive or handle errors...after all, how could it?
                        // We can therefore safely run through them all, dispatch them, and remove them.
                        cacheItem.markAsServed(callbackObj);
                        MouseSpinner.MouseSpinner.haltSpinner(callbackObj.getCallbackName());
                        callbackObj.callback({ errors: true, status: errorThrown }, textStatus, jqXHR);
                    }
                },
                complete: function (jqXHR, textStatus) {
                    if (jqXHR.status === 200) {
                    }
                    else {
                        console.log("Code " + jqXHR.status);
                    }
                },
            });
        };
        // TODO Using default value of undefined, but we may want the "resultData?: any" optional param syntax instead, or
        // a default to Null...not sure. Wait til it's working to change that.
        // TODO I think the error codes seen below only worked for the old API. The new one doesn't offer error codes
        // embedded in a response. Worse yet, browsers do not pass received error codes to AJAX callers for cross-site JSONP.
        // That means none of this can function anymore, because no such data is passed on at all.
        /**
         * If the REST calls have been made with the same callback to the same URL before, it will rebuff.
         * If the same REST calls have been made with *different* callbacks within the same page load,
         * it will use a manually cached version of the data.
         *
         * -1: retrying (first attempt or after any error that allows retry)
         *  0: error with no retry advisable
         *  1: success
         */
        RetryingJsonFetcher.prototype.fetch = function (callbackObject, giveUserBusyIndicator) {
            if (giveUserBusyIndicator) {
                // Start up the busy spinner right away
                MouseSpinner.MouseSpinner.applyMouseSpinner(callbackObject.getCallbackName());
            }
            var cacheItem = CacheRegistry.getCachedItem(this.restUrl);
            if (cacheItem.alreadyServed(callbackObject)) {
            }
            var cachedData = cacheItem.responseData;
            if (cachedData !== undefined) {
                // Skip the dispatch of the call, fake the data back into the normal flow.
                // Multiple callbacks may want the same data. If so, let them get it...from the
                // manually cached responses. Otherwise, prevent callers from abusing the REST services.
                // Really, multiple calls may be programmer errors, but it happens naturally with the
                // filtering functionality.
                // Browser caching can be unreliable, and we don't want to cache between page loads.
                //console.log("Used cache ("+CacheRegistry.getCurrentMBStored()+"MB) for callback named "+callbackObject.getCallbackName());
                MouseSpinner.MouseSpinner.haltSpinner(callbackObject.getCallbackName());
                callbackObject.callback(cachedData, "manually cached", null);
                return 1;
            }
            if (!CacheRegistry.checkUrlNotBad(this.restUrl)) {
                // Unusable URLs should not be re-visited
                MouseSpinner.MouseSpinner.haltSpinner(callbackObject.getCallbackName());
                // TODO Do I want to cache the error maybe? Pass that in via the cache system?
                // callbackObject.callback(cachedData, "manually cached", null);
                return 0;
            }
            // Make a url and callback entry.
            // If we are recalling on an erorr this should cope with redundant entries.
            // Cache.addUrlToRestCallRegistry(this.callbackObject.url, this.callbackObject, RestCallStatus.AWAITING);
            var cacheItem = CacheRegistry.enqueueCallbackForUrlInRestCallRegistry(this.restUrl, callbackObject);
            if (cacheItem.status === undefined) {
                // Only set to awaiting and trigger call if this is truly a new REST call.
                // console.log("Not using cache for "+callbackObject.getCallbackName());
                CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.AWAITING);
                this.callAgain();
            }
            else if (cacheItem.status !== RestCallStatus.AWAITING) {
                console.log("Non-cached REST, not AWAITING, but attempted fetch: " + callbackObject.getCallbackName());
            }
            return -1;
        };
        /**
         * The errors dealt with here are from days of yore when Bioportal embedded errors in JSON,
         * and therefore Javascript could receive and process such errors.
         * This needs revision, though we will likely use CORS, so do that at the same time.
         *
         * 0 fail due to error, 1 success, -1 retrying.
         */
        RetryingJsonFetcher.prototype.processResponse = function (resultData) {
            // TODO If JqueryJsonp is working out, get this all working off the raw XOptions object.
            if (typeof resultData.error !== "undefined") {
                if (resultData.status == "404" || resultData.error == "timeout") {
                    // 404 Error should fill in some popup data points, so let through...
                    console.log("Error: " + this.restUrl + " --> Data: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.ERROR);
                    return 0;
                }
                else if (resultData.status == "403") {
                    console.log("Forbidden Error, no retry: "
                        + "\nURL: " + this.restUrl
                        + "\nReply: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.FORBIDDEN);
                    return 0;
                }
                else if (resultData.status == "500") {
                    if (this.previousTriesRemade < 3) {
                        this.previousTriesRemade++;
                        console.log("Retrying: " + this.restUrl);
                        this.callAgain();
                        // update to status unnecessary; still awaiting.
                        return -1;
                    }
                    else {
                        // Error, but we are done retrying.
                        console.log("No retry, Error: ");
                        console.log(resultData);
                        CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.ERROR);
                        return 0;
                    }
                }
                else {
                    // Don't retry for other errors
                    console.log("Error: " + this.restUrl + " --> Data: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.ERROR);
                    return 0;
                }
            }
            else {
                // Success, great!
                CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, RestCallStatus.COMPLETED, resultData);
                return 1;
            }
        };
        RetryingJsonFetcher.userapikey = "";
        return RetryingJsonFetcher;
    })();
    exports.RetryingJsonFetcher = RetryingJsonFetcher;
});
