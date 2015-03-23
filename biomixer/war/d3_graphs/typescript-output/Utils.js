define(["require", "exports"], function (require, exports) {
    var defaultBioportalUrl = "data.bioontology.org";
    var restURLPrefix = undefined;
    function getBioportalUrl() {
        if (restURLPrefix === undefined) {
            restURLPrefix = purl().param("restURLPrefix");
            if (restURLPrefix === undefined) {
                restURLPrefix = defaultBioportalUrl;
            }
        }
        return restURLPrefix;
    }
    exports.getBioportalUrl = getBioportalUrl;
    function getTime() {
        var now = new Date();
        return now.getHours() + ":" + now.getMinutes() + ':' + now.getSeconds();
    }
    exports.getTime = getTime;
    function endsWith(string, suffix) {
        return string.indexOf(suffix, string.length - suffix.length) !== -1;
    }
    exports.endsWith = endsWith;
    function prepUrlKey(url, includeJsonp) {
        if (includeJsonp === void 0) { includeJsonp = true; }
        var ampersand = "&";
        if (url.indexOf("?") == -1) {
            url = url + "?";
            ampersand = "";
        }
        if (includeJsonp) {
            return url += ampersand + "format=jsonp" + "&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1";
        }
        else {
            return url += ampersand + "apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1";
        }
    }
    exports.prepUrlKey = prepUrlKey;
    function addOrUpdateUrlParameter(url, paramName, value) {
        if (Object.keys(purl(url).param()).length === 0) {
            url += "?";
        }
        if (purl(url).param(paramName) === undefined) {
            url += "&" + paramName + "=" + value;
        }
        else {
            var replaceHead = "";
            if (-1 !== url.indexOf("&" + paramName)) {
                replaceHead = "&" + paramName;
            }
            else {
                replaceHead = paramName;
            }
            url = url.replace(new RegExp(replaceHead + "=[^=&]+(&)*"), replaceHead + "=" + value + "&");
        }
        return url;
    }
    exports.addOrUpdateUrlParameter = addOrUpdateUrlParameter;
    function escapeIdentifierForId(identifier) {
        return identifier.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|\\\/])/g, '__');
    }
    exports.escapeIdentifierForId = escapeIdentifierForId;
    function getClassName(instance) {
        var funcNameRegex = /function (.{1,})\(/;
        var results = (funcNameRegex).exec(instance.constructor.toString());
        var className = "";
        if (results && results.length > 1) {
            className = results[1];
        }
        return className;
    }
    exports.getClassName = getClassName;
    var HasCallbacks = (function () {
        function HasCallbacks() {
            var _this = this, _constructor = this.constructor;
            if (!_constructor.__cb__) {
                _constructor.__cb__ = {};
                for (var m in this) {
                    var fn = this[m];
                    if (typeof fn === 'function' && m.indexOf('cb_') == 0) {
                        _constructor.__cb__[m] = fn;
                    }
                }
            }
            for (var m in _constructor.__cb__) {
                (function (m, fn) {
                    _this[m] = function () {
                        return fn.apply(_this, Array.prototype.slice.call(arguments));
                    };
                })(m, _constructor.__cb__[m]);
            }
        }
        return HasCallbacks;
    })();
});
