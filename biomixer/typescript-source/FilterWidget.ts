///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./Menu" />

import Utils = require("./Utils");
import Menu = require("./Menu");

export class FilterWidget {

    
    filterContainer: JQuery;
    
    public className: string;
    
    public subMenuTitle: string;
    
    constructor(){
        this.className =  Utils.getClassName(this);
    }
    
    addMenuComponents(menuSelector: string){
        this.filterContainer = $("<div>").attr("id", this.className+"Container").addClass("scroll-div").css("height", 100);
        $(menuSelector).append(this.filterContainer);
        
        this.filterContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text(this.subMenuTitle));
    }
  
}