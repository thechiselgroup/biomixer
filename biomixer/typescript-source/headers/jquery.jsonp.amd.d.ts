
declare module JQueryJsonp {

export interface XOptions {
        // Quick and dirty. If we want better typing, inspect in debugger and code.
        callback: string;
        callbackParameter: string;
        data;
        url: string;
        success: {(data, textStatus, jqXHR)};
        error: {(xOptions: XOptions, textStatus: string)};
        abort: {()};
        always: {()};
        done: {(): any};
        fail: {(): any};
        pipe: {(): any};
        progress: {(): any};
        promise: { (a): any };
        state: { (): any};
        then: {(): any};
    }
}

interface JQueryStatic {
    jsonp(thing: JQueryJsonp.XOptions): any;
}
