///<reference path="headers/require.d.ts" />

///<reference path="headers/jquery.d.ts" />

interface JQuery {
    // Need this, or we need to make a .d.ts file for tipsy.
    // TODO Make a tipsy.d.ts file.
    tipsy(thing: any): any;
}

interface JQuery {
	// spectrum colorpicker
    spectrum(thing: any, thing2?: any, thing3?: any): any;
}

interface JQueryStylesheet extends JQuery{
	// jquery-stylesheet plugin
    rules(): Array<any>;
}

interface JQueryStatic {
	// jquery-stylesheet plugin
    stylesheet(selector: any): JQueryStylesheet;
    stylesheet(selector: any, styles: any): JQueryStylesheet;
}