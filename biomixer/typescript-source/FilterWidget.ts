///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./Menu" />

import Utils = require("./Utils");
import Menu = require("./Menu");

export interface IFilterWidget extends AbstractFilterWidget {
    updateFilterUI(): void;
}

export class AbstractFilterWidget {

    private className: string;
    
    filterContainer: JQuery;
    
    constructor(
        public subMenuTitle: string
        ){
    
    }
   
    getClassName(): String {
        if(undefined !== this.className){
            return this.className;
        } else {
            this.className = Utils.getClassName(this);
        }
        
    }
    
    getCheckboxSpanClass(): string {
        return this.getClassName()+"_filterCheckboxSpan";
    }
    
    getCheckboxClass(): string {
        return this.getClassName()+"_filterCheckbox";
    }
    
   addMenuComponents(menuSelector: string): void {
        var outerContainer = $("<div>").attr("id", this.getClassName()+"OuterContainer");
        $(menuSelector).append(outerContainer);
        
        outerContainer.append(
            $("<label>")
                .addClass(Menu.Menu.menuLabelClass).text(this.subMenuTitle));
        
        this.filterContainer = $("<div>").attr("id", this.className+"ScrollContainer")
                .addClass("scroll-div").css("height", 100);
        
        outerContainer.append(this.filterContainer);
    }
    
}