///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />

import Utils = require("./Utils");

export class FilterWidget {

    public className: string;
    
    constructor(){
        this.className =  Utils.getClassName(this);
    }
  
}