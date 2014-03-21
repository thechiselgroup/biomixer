///<reference path="headers/require.d.ts" />

export function getTime(){
    var now = new Date();
    return now.getHours()+":"+now.getMinutes()+':'+now.getSeconds();
}

/**
 * I prefer my approach, seen in this code base, of passing the instance into a closure lambda
 * function that returns the actual callback, with 'outerThis' enclosed. The typed parameter helps.
  * 
 * Need fat arrow definition rather than regular type, so that we can get lexical scoping of
 * "this" to refer to the class instance (a lamda by use of closure, I think) rather than whatever Javascript binds "this" to
 * when the callback is executed.
 * As a further complication, when we make anonymous functions within this method, our references to "this" get re-scoped
 * again. In order to cope with those, we need to use fat arrow => again!
 * If we are passing such a function to D3, and need "this" to refer to the element that D3 is operating
 * on, then we stay with function() syntax, and make a new variable pointing to the object-instance "this",
 * and allow the function() to closure onto that variable. Did you catch all that?
 * 
 * We could try using this class to get this for free. It might be dangerous or useless though. Useless
 * because when we expect a method to be used as a callback, we might need both the external and internal "this",
 * and that is only achievable by aliasing the class instance "this" in a wrapper around the callback method,
 * and leaving "this" to mean the dynamically scoped entity.
 * 
 * From Steven Ickman (http://stackoverflow.com/questions/12756423/is-there-an-alias-for-this-in-typescript)
 * 
 * Example for HasCallback usage:
 * class Foo extends HasCallbacks  {
 *     private label = 'test';
 * 
 *     constructor() {
 *         super();
 * 
 *     }
 * 
 *     public cb_Bar() {
 *         alert(this.label);
 *     }
 * }
 * var x = new Foo();
 * x.cb_Bar.call({}); // Will refer to Foo instance when "this" is used.
 */
class HasCallbacks {
    constructor() {
        var _this = this, _constructor = (<any>this).constructor;
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
}

/* 
 * 
 */