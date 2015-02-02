/// <reference path="../jquery/introjs.d.ts"/>

declare module IntroJS {

//export interface XOptions {
//        // Quick and dirty. If we want better typing, inspect in debugger and code.
//        callback: string;
//        callbackParameter: string;
//        data;
//        url: string;
//        success: {(data, textStatus, jqXHR)};
//        error: {(xOptions: XOptions, textStatus: string)};
//        abort: {()};
//        always: {()};
//        done: {(): any};
//        fail: {(): any};
//        pipe: {(): any};
//        progress: {(): any};
//        promise: { (a): any };
//        state: { (): any};
//        then: {(): any};
//    }
//}
}

interface JQueryStatic {
    introJs(): any;
    forwardevents(): void;
}

interface JQuery {
    introJs(): any;
    forwardevents(): void;
}

//declare module "jquery" {
//    export = $;
//}
//declare var jQuery: JQueryStatic;
//declare var $: JQueryStatic;