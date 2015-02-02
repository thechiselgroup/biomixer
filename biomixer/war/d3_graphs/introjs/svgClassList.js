/*
 * classList.js: Cross-browser full element.classList implementation.
 * 2011-06-15
 *
 * By Eli Grey, http://eligrey.com
 * Public Domain.
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 */

/*global self, document, DOMException */

/*! @source http://purl.eligrey.com/github/classList.js/blob/master/classList.js*/

/*
 * 2011-12-02 Modified by Brian Birtles (Mozilla Japan) to add classList to SVG
 * elements for browsers that don't support it (e.g. WebKit)
 */

if (typeof document !== "undefined" &&
    !("classList" in document.createElement("a") &&
      "classList" in document.createElementNS("http://www.w3.org/2000/svg", "g")
     )
   )
{

(function (view) {

"use strict";

var
      svgNamespace = "http://www.w3.org/2000/svg"
    , classListProp = "classList"
    , protoProp = "prototype"
    , objCtr = Object
    , strTrim = String[protoProp].trim || function () {
        return this.replace(/^\s+|\s+$/g, "");
    }
    , arrIndexOf = Array[protoProp].indexOf || function (item) {
        var
              i = 0
            , len = this.length
        ;
        for (; i < len; i++) {
            if (i in this && this[i] === item) {
                return i;
            }
        }
        return -1;
    }
    // Vendors: please allow content code to instantiate DOMExceptions
    , DOMEx = function (type, message) {
        this.name = type;
        this.code = DOMException[type];
        this.message = message;
    }
    , checkTokenAndGetIndex = function (classList, token) {
        if (token === "") {
            throw new DOMEx(
                  "SYNTAX_ERR"
                , "An invalid or illegal string was specified"
            );
        }
        if (/\s/.test(token)) {
            throw new DOMEx(
                  "INVALID_CHARACTER_ERR"
                , "String contains an invalid character"
            );
        }
        return arrIndexOf.call(classList, token);
    }
    , ClassList = function (elem) {
        var
              className = typeof elem.className.baseVal !== "undefined"
                        ? elem.className.baseVal : elem.className
            , trimmedClasses = strTrim.call(className)
            , classes = trimmedClasses ? trimmedClasses.split(/\s+/) : []
            , i = 0
            , len = classes.length
        ;
        for (; i < len; i++) {
            this.push(classes[i]);
        }
        this._updateClassName = function () {
            typeof elem.className.baseVal !== "undefined"
              ? elem.className.baseVal = this.toString()
              : elem.className = this.toString();
        };
    }
    , classListProto = ClassList[protoProp] = []
    , classListGetter = function () {
        return new ClassList(this);
    }
    , addClassList = function (elemCtr) {
      if (objCtr.defineProperty) {
          var classListPropDesc = {
                get: classListGetter
              , enumerable: true
              , configurable: true
          };
          try {
              objCtr.defineProperty(elemCtr[protoProp], classListProp, classListPropDesc);
          } catch (ex) { // IE 8 doesn't support enumerable:true
              if (ex.number === -0x7FF5EC54) {
                  classListPropDesc.enumerable = false;
                  objCtr.defineProperty(elemCtr[protoProp], classListProp, classListPropDesc);
              }
          }
      } else if (objCtr[protoProp].__defineGetter__) {
          elemCtrl[protoProp].__defineGetter__(classListProp, classListGetter);
      }
    }
    , addClassListToDoc = function (doc, view) {
      if (!("classList" in doc.createElement("a"))) {
          addClassList(view.HTMLElement || view.Element);
      }
      if (!("classList" in doc.createElementNS(svgNamespace, "g"))) {
          addClassList(view.SVGElement);
      }
    }
    , addClassListToObj = function (object) {
      if (object.contentDocument &&
          object.contentDocument.readyState == "complete") {
        var win = object.contentWindow || object.contentDocument.defaultView;
        addClassListToDoc(object.contentDocument, win);
      }
      // else we just wait for the load event handler to call this later
    }
    , addClassListToAllObjs = function () {
      var objects = document.getElementsByTagName('object');
      for (var i = 0; i < objects.length; ++i) {
        addClassListToObj(objects[i]);
      }
    }
    , initialize = function () {
      addClassListToAllObjs();
      document.addEventListener('DOMNodeInserted', onNodeInserted, true);
    }
    , onNodeInserted = function(e) {
      addClassListToObj(e.srcElement);
      // Catch-all for IE
      e.srcElement.addEventListener("load", addClassListToAllObjs, true);
    }
;
// Most DOMException implementations don't allow calling DOMException's toString()
// on non-DOMExceptions. Error's toString() is sufficient here.
DOMEx[protoProp] = Error[protoProp];
classListProto.item = function (i) {
    return this[i] || null;
};
classListProto.contains = function (token) {
    token += "";
    return checkTokenAndGetIndex(this, token) !== -1;
};
classListProto.add = function (token) {
    token += "";
    if (checkTokenAndGetIndex(this, token) === -1) {
        this.push(token);
        this._updateClassName();
    }
};
classListProto.remove = function (token) {
    token += "";
    var index = checkTokenAndGetIndex(this, token);
    if (index !== -1) {
        this.splice(index, 1);
        this._updateClassName();
    }
};
classListProto.toggle = function (token) {
    token += "";
    if (checkTokenAndGetIndex(this, token) === -1) {
        this.add(token);
    } else {
        this.remove(token);
    }
};
classListProto.toString = function () {
    return this.join(" ");
};

addClassListToDoc(document, view);

if (document.readyState != 'complete') {
  document.addEventListener('DOMContentLoaded', initialize, true);
  // When we get the DOMContentLoaded event in IE the object might still not
  // have a contentDocument.
  //
  // However, I can't find any event supported by IE9 that will tell us when the
  // contentDocument is available *AND* which fires before the window load event
  // (when other scripts might be wanting to interact with the document).
  //
  // However, in IE9 event handlers seem to be called in the order they are
  // registered so we just register our own window.load handler and, if this
  // file has been included before other scripts that depend on it, we should
  // have time to add the classList before those other scripts get called.
  window.addEventListener('load', addClassListToAllObjs, true);
} else {
  initialize();
}

}(self));

}
