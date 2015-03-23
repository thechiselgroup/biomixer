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
            this.responseData = undefined;
            this.status = undefined;
            this.callbackUnservedSet = {};
            this.callbackServedSet = {};
        }
        RestCallCache.prototype.updateStatus = function (newStatus, responseData) {
            if (responseData === void 0) { responseData = undefined; }
            this.status = newStatus;
            if (responseData !== undefined) {
                CacheRegistry.updateCacheMemoryUsage(responseData);
                this.responseData = responseData;
            }
        };
        RestCallCache.prototype.addCallbackToQueue = function (callback) {
            if (undefined !== this.callbackUnservedSet[callback.getCallbackName()]) {
                console.log("Redundant callback registered: " + callback.getCallbackName());
                return false;
            }
            this.callbackUnservedSet[callback.getCallbackName()] = callback;
            return true;
        };
        RestCallCache.prototype.markAsServed = function (callbackObject) {
            delete this.callbackUnservedSet[callbackObject.getCallbackName()];
            this.callbackServedSet[callbackObject.getCallbackName()] = callbackObject;
        };
        RestCallCache.prototype.alreadyServed = function (callbackObject) {
            return undefined !== this.callbackServedSet[callbackObject.getCallbackName()];
        };
        RestCallCache.prototype.getUnservedCallbacks = function () {
            return this.callbackUnservedSet;
        };
        RestCallCache.prototype.clearAllCallbackServiceRecords = function () {
            this.callbackUnservedSet = {};
            this.callbackServedSet = {};
        };
        return RestCallCache;
    })();
    exports.RestCallCache = RestCallCache;
    var CacheRegistry = (function () {
        function CacheRegistry() {
        }
        CacheRegistry.getCurrentMBStored = function () {
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
                currentCache = new RestCallCache();
                CacheRegistry.restAndCallbackRegistry[restUrl] = currentCache;
            }
            return CacheRegistry.restAndCallbackRegistry[restUrl];
        };
        CacheRegistry.isNotRegisteredInCache = function (restUrl) {
            return undefined === CacheRegistry.restAndCallbackRegistry[restUrl];
        };
        CacheRegistry.updateCacheMemoryUsage = function (responseData) {
            var stringVersion = JSON.stringify(responseData);
            var totalByteSize = stringVersion.length * 2;
            var toRemove = [];
            if (CacheRegistry.currentJsonCacheSizeByte + totalByteSize > CacheRegistry.maxJsonCacheSizeByte) {
                $.each(CacheRegistry.restAndCallbackRegistry, function (uriKey, cacheItem) {
                    if (cacheItem.status != 3 /* COMPLETED */ || cacheItem.responseData === undefined) {
                        return true;
                    }
                    var droppedSize = JSON.stringify(cacheItem.responseData).length * 2;
                    CacheRegistry.currentJsonCacheSizeByte -= droppedSize;
                    toRemove.push(uriKey);
                    if (CacheRegistry.currentJsonCacheSizeByte + totalByteSize <= CacheRegistry.maxJsonCacheSizeByte) {
                        return false;
                    }
                });
                $.each(toRemove, function (i, uriKey) {
                    delete CacheRegistry.restAndCallbackRegistry[uriKey];
                });
            }
            CacheRegistry.currentJsonCacheSizeByte += totalByteSize;
        };
        CacheRegistry.validRestCallStateTransition = function (oldStatus, newStatus, restCallUriFunction) {
            var isValid = true;
            switch (oldStatus) {
                case 1 /* ALLOWED */:
                    if (newStatus !== 2 /* AWAITING */ && newStatus !== 0 /* EXCLUDED */) {
                        isValid = false;
                    }
                    break;
                case 0 /* EXCLUDED */:
                    if (newStatus !== 1 /* ALLOWED */) {
                        isValid = false;
                    }
                    break;
                case 2 /* AWAITING */:
                    if (newStatus !== 3 /* COMPLETED */ && newStatus !== 4 /* ERROR */ && newStatus !== 5 /* FORBIDDEN */) {
                        isValid = false;
                    }
                    break;
                case 4 /* ERROR */:
                    if (newStatus !== 1 /* ALLOWED */) {
                        isValid = false;
                    }
                    break;
                case 5 /* FORBIDDEN */:
                    isValid = false;
                    break;
                case 3 /* COMPLETED */:
                    isValid = false;
                    break;
                default:
                    if (newStatus !== 1 /* ALLOWED */ && newStatus !== 0 /* EXCLUDED */) {
                        isValid = false;
                    }
                    break;
            }
            if (!isValid && oldStatus != newStatus) {
                console.log("Invalid rest call registry status change requested: " + oldStatus + " to " + newStatus + " for rest call and node: " + restCallUriFunction);
            }
            return isValid;
        };
        CacheRegistry.updateStatusForUrlInRestCallRegistry = function (restCallURL, status, responseDataToCache) {
            var currentCache = CacheRegistry.getCachedItem(restCallURL);
            var currentStatus = currentCache.status;
            if (currentStatus !== undefined) {
                if (!CacheRegistry.validRestCallStateTransition(currentStatus, status, restCallURL)) {
                    return;
                }
            }
            currentCache.updateStatus(status, responseDataToCache);
            return currentCache;
        };
        CacheRegistry.enqueueCallbackForUrlInRestCallRegistry = function (restCallURL, callback) {
            var currentCache = CacheRegistry.getCachedItem(restCallURL);
            currentCache.addCallbackToQueue(callback);
            return currentCache;
        };
        CacheRegistry.checkUrlNotBad = function (restURL) {
            var restCache = CacheRegistry.restAndCallbackRegistry[restURL];
            if (restCache == undefined || restCache.status == undefined) {
                return true;
            }
            else {
                var entry = restCache.status;
                return entry === 1 /* ALLOWED */ || entry === 4 /* ERROR */;
            }
        };
        CacheRegistry.MB = 1024 * 1024;
        CacheRegistry.maxJsonCacheSizeByte = 20 * CacheRegistry.MB;
        CacheRegistry.currentJsonCacheSizeByte = 0;
        CacheRegistry.restAndCallbackRegistry = {};
        return CacheRegistry;
    })();
    exports.CacheRegistry = CacheRegistry;
    (function (CallbackVarieties) {
        CallbackVarieties[CallbackVarieties["nodeSingle"] = 0] = "nodeSingle";
        CallbackVarieties[CallbackVarieties["nodesMultiple"] = 1] = "nodesMultiple";
        CallbackVarieties[CallbackVarieties["links"] = 2] = "links";
        CallbackVarieties[CallbackVarieties["metaData"] = 3] = "metaData";
        CallbackVarieties[CallbackVarieties["fullOntologyMapping"] = 4] = "fullOntologyMapping";
    })(exports.CallbackVarieties || (exports.CallbackVarieties = {}));
    var CallbackVarieties = exports.CallbackVarieties;
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
    var RetryingJsonFetcher = (function () {
        function RetryingJsonFetcher(restUrl) {
            this.restUrl = restUrl;
            this.previousTriesRemade = 0;
        }
        RetryingJsonFetcher.prototype.callAgain = function () {
            var browserSupportsCors = 'withCredentials' in new XMLHttpRequest();
            var dataType = "json";
            var type = "GET";
            var data = null;
            var urlString = Utils.prepUrlKey(this.restUrl, false);
            if (!browserSupportsCors) {
                console.log("Browser not CORS compatible");
                dataType = "jsonp";
                type = "GET";
            }
            if (type === "POST") {
                var url = purl(urlString);
                data = url.attr("query");
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
                    if (0 == errorOrRetry) {
                        return;
                    }
                    else if (-1 == errorOrRetry) {
                        return;
                    }
                    var cacheItem = CacheRegistry.getCachedItem(outerThis.restUrl);
                    var queue = cacheItem.getUnservedCallbacks();
                    for (var i in queue) {
                        var callbackObj = queue[i];
                        cacheItem.markAsServed(callbackObj);
                        MouseSpinner.MouseSpinner.haltSpinner(callbackObj.getCallbackName());
                        callbackObj.callback(data, textStatus, jqXHR);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    var errorOrRetry = outerThis.processResponse(jqXHR);
                    if (-1 == errorOrRetry) {
                        return;
                    }
                    else if (0 == errorOrRetry) {
                    }
                    else if (1 == errorOrRetry) {
                        console.log("Success found in error handler:");
                        console.log(jqXHR);
                    }
                    console.log("Error Code: " + jqXHR.status + " (" + errorThrown + "; " + textStatus + ")");
                    var cacheItem = CacheRegistry.getCachedItem(outerThis.restUrl);
                    var queue = cacheItem.getUnservedCallbacks();
                    for (var i in queue) {
                        var callbackObj = queue[i];
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
        RetryingJsonFetcher.prototype.fetch = function (callbackObject, giveUserBusyIndicator) {
            if (giveUserBusyIndicator) {
                MouseSpinner.MouseSpinner.applyMouseSpinner(callbackObject.getCallbackName());
            }
            var cacheItem = CacheRegistry.getCachedItem(this.restUrl);
            if (cacheItem.alreadyServed(callbackObject)) {
            }
            var cachedData = cacheItem.responseData;
            if (cachedData !== undefined) {
                MouseSpinner.MouseSpinner.haltSpinner(callbackObject.getCallbackName());
                callbackObject.callback(cachedData, "manually cached", null);
                return 1;
            }
            if (!CacheRegistry.checkUrlNotBad(this.restUrl)) {
                MouseSpinner.MouseSpinner.haltSpinner(callbackObject.getCallbackName());
                return 0;
            }
            var cacheItem = CacheRegistry.enqueueCallbackForUrlInRestCallRegistry(this.restUrl, callbackObject);
            if (cacheItem.status === undefined) {
                CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 2 /* AWAITING */);
                this.callAgain();
            }
            else if (cacheItem.status !== 2 /* AWAITING */) {
                console.log("Non-cached REST, not AWAITING, but attempted fetch: " + callbackObject.getCallbackName());
            }
            return -1;
        };
        RetryingJsonFetcher.prototype.processResponse = function (resultData) {
            if (typeof resultData.error !== "undefined") {
                if (resultData.status == "404" || resultData.error == "timeout") {
                    console.log("Error: " + this.restUrl + " --> Data: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 4 /* ERROR */);
                    return 0;
                }
                else if (resultData.status == "403") {
                    console.log("Forbidden Error, no retry: " + "\nURL: " + this.restUrl + "\nReply: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 5 /* FORBIDDEN */);
                    return 0;
                }
                else if (resultData.status == "500") {
                    if (this.previousTriesRemade < 3) {
                        this.previousTriesRemade++;
                        console.log("Retrying: " + this.restUrl);
                        this.callAgain();
                        return -1;
                    }
                    else {
                        console.log("No retry, Error: ");
                        console.log(resultData);
                        CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 4 /* ERROR */);
                        return 0;
                    }
                }
                else {
                    console.log("Error: " + this.restUrl + " --> Data: " + resultData.error);
                    CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 4 /* ERROR */);
                    return 0;
                }
            }
            else {
                CacheRegistry.updateStatusForUrlInRestCallRegistry(this.restUrl, 3 /* COMPLETED */, resultData);
                return 1;
            }
        };
        return RetryingJsonFetcher;
    })();
    exports.RetryingJsonFetcher = RetryingJsonFetcher;
});
