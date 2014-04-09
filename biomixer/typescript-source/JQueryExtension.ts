///<reference path="headers/require.d.ts" />

///<reference path="headers/jquery.d.ts" />

interface JQuery {
    // Need this, or we need to make a .d.ts file for tipsy.
    // TODO Make a tipsy.d.ts file.
    tipsy(thing: any): any;
}