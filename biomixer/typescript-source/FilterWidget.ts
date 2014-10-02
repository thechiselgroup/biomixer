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
        // This container is the encapsulating one for this entire widget item.
        var outerContainer = $("<div>").attr("id", this.getClassName()+"OuterContainer");
        $(menuSelector).append(outerContainer);
        
        // This container holds the checkbox widgets 
        this.filterContainer = $("<div>").attr("id", this.className+"ScrollContainer")
                .addClass("scroll-div").css("height", 100);
       
        if(defaultHideContainer){
            this.filterContainer.css("display", "none");
        }
        
        // This only indicates collapsability and status
        var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass).text("+");
        
        // The label labels the section,a dn acts as a huge collapse button
        var label = $("<label>").addClass(Menu.Menu.menuLabelClass).text(this.subMenuTitle);
        label.click(
            ()=>{
                $(this.filterContainer).slideToggle('slow',
                    ()=>{labelExpanderIcon.text( $(this.filterContainer).css("display") === "none" ? "+" : "-"); }
                );
                
            }
        );
        
        outerContainer.append(labelExpanderIcon);
        outerContainer.append(label);
        
        // We don't know the default necessarily, so set the text here.
        labelExpanderIcon.text( $(this.filterContainer).css("display") === "none" ? "+" : "-"); 
        
        outerContainer.append(this.filterContainer);
    }
    
}