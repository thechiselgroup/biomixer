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
    
   addMenuComponents(menuSelector: string, defaultHideContainer: boolean): void {
        // This container holds the checkbox widgets
        var containers = Menu.Menu.slideToggleHeaderContainer(this.getClassName()+"OuterContainer", this.className+"ScrollContainer", this.subMenuTitle, defaultHideContainer);
        var outerContainer = containers.outer;
        this.filterContainer = containers.inner;
        this.filterContainer.addClass("scroll-div").css("height", 100);
        
        // This container is the encapsulating one for this entire widget item.
        $(menuSelector).append(outerContainer);
    }
    
}