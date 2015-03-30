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
    
    static menuExpanderButtonIconClass = "menuExpanderUtilityButton";
    static resetCheckboxesButtonIconClass = "resetCheckboxesButton";
    static deleteNodesButtonIconClass = "deleteNodesButton";
    static gapperButtonIconClass = "menuExpanderButtonGapper";
    static resetCheckboxesButtonText = "Re-check all of these checkboxes";
    static deleteNodesButtonText = "Exclude all unchecked and dimmed nodes from graph";
    
    constructor(
        public subMenuTitle: string
        ){
    
    }
    
    modifyClassName(newClassName: string){
        this.className = newClassName;
    }
   
    getClassName(): String {
        if(undefined == this.className){
            this.className = Utils.getClassName(this);
        }
        return this.className;
        
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
    
    addResetAndDeleteButtonsToMenuComponents(resetHandler, deleteHandler){
        var menuHeaderContainer = $("#"+ this.getClassName()+"OuterContainer");
        menuHeaderContainer.children(".menuLabel").first()
        .before(
            $("<div>").attr("id", this.getClassName()+"ButtonGapper")
            .addClass(AbstractFilterWidget.menuExpanderButtonIconClass)
            .addClass(AbstractFilterWidget.gapperButtonIconClass)
        )
        .before(
            $("<div>").attr("id", this.getClassName()+"CheckboxDeleteButton")
            .click(deleteHandler)
            .addClass(AbstractFilterWidget.menuExpanderButtonIconClass)
            .addClass(AbstractFilterWidget.deleteNodesButtonIconClass)
            .attr("title", AbstractFilterWidget.deleteNodesButtonText)
            // .tipsy() // or is title sufficient?
        )
        .before(
            $("<label>").attr("id", this.getClassName()+"CheckboxResetButton")
            .click(resetHandler)
            .addClass(AbstractFilterWidget.menuExpanderButtonIconClass)
            .addClass(AbstractFilterWidget.resetCheckboxesButtonIconClass)
            .attr("title", AbstractFilterWidget.resetCheckboxesButtonText)
            // .tipsy() // or is title sufficient?
            )
        ;
    }
    
}