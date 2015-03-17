///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="UndoRedo/BreadcrumbTrail" />
///<amd-dependency path="UndoRedo/UndoRedoManager" />

import BreadcrumbTrail = require("./BreadcrumbTrail");
import UndoRedoManager = require("./UndoRedoManager");

/**
 * This class wraps the spatially wide breadcrumb trail into a forward and back button pair, with drop down menus.
 */
export class BackForwardBreadcrumbButtons extends BreadcrumbTrail.BreadcrumbTrail {
    static undoButtonText = "Undo Node Expansion";
    static redoButtonText = "Redo Node Expansion";
    static undoListButtonText = "Click to see list of undo options";
    static redoListButtonText = "Click to see list of redo options";
    
    static undoButtonIconClass = "action-undo-icon";
    static redoButtonIconClass = "action-redo-icon";
    static undoListButtonIconClass = "caret-bottom-icon";
    
    static undoRedoButtonClass = "undo_redo_button";
    static undoRedoListButtonClass = "undo_redo_list_button";
    
    static disabledButtonClass = "undo_redo_button_disabled";
    
    static undoButtonId = "undo_button";
    static redoButtonId = "redo_button";
    static undoListButtonId = "undo_list_button";
    static redoListButtonId = "redo_list_button";
    static undoListCrumbContainerId = "undo_list_crumb_container";
    static redoListCrumbContainerId = "redo_list_crumb_container";
    
    static verticalCrumbDivClass = "vertical_stack_bread_crumb_div";
    static undoredoVerticalListContainer = "vertical_stack_bread_crumb_container"
    
    constructor(){
        super();
    }
    
    initGui(){
        // The undo and redo button do what they sound like. The undo and redo list are drop down lists
        // of the available undo and redo checkpoints. Openign the list and clicking one of these will also
        // undo or redo the command.
        
        var undoButton = $("<div>").attr("id", BackForwardBreadcrumbButtons.undoButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoButtonClass)
                .addClass(BackForwardBreadcrumbButtons.undoButtonIconClass)
                .attr("title", BackForwardBreadcrumbButtons.undoButtonText)
            ;
        var redoButton = $("<div>").attr("id", BackForwardBreadcrumbButtons.redoButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoButtonClass)
                .addClass(BackForwardBreadcrumbButtons.redoButtonIconClass)
                .attr("title", BackForwardBreadcrumbButtons.redoButtonText)
            ;
        
        var undoDropDownButton = $("<div>").addClass(BackForwardBreadcrumbButtons.undoListButtonIconClass)
            .attr("title", BackForwardBreadcrumbButtons.undoListButtonText)
            .addClass(BackForwardBreadcrumbButtons.crumbTextClass);
        var undoList = $("<div>").attr("id", BackForwardBreadcrumbButtons.undoListButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoListButtonClass)
            .append(undoDropDownButton)
            .append(
                $("<div>").attr("id", BackForwardBreadcrumbButtons.undoListCrumbContainerId).addClass(BackForwardBreadcrumbButtons.undoredoVerticalListContainer)
            );
        var redoDropDownButton = $("<div>").addClass(BackForwardBreadcrumbButtons.undoListButtonIconClass)
            .attr("title", BackForwardBreadcrumbButtons.redoListButtonText)
            .addClass(BackForwardBreadcrumbButtons.crumbTextClass);
        var redoList = $("<div>").attr("id", BackForwardBreadcrumbButtons.redoListButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoListButtonClass)
            .append(redoDropDownButton)
            .append(
                $("<div>").attr("id", BackForwardBreadcrumbButtons.redoListCrumbContainerId).addClass(BackForwardBreadcrumbButtons.undoredoVerticalListContainer)
            );
        
        undoButton.click(()=>{this.undoRedoModel.undoOneStep();});
        redoButton.click(()=>{this.undoRedoModel.redoOneStep();});
        
        undoDropDownButton.click(this.dropdownClickLambda("#"+BackForwardBreadcrumbButtons.undoListCrumbContainerId));
        redoDropDownButton.click(this.dropdownClickLambda("#"+BackForwardBreadcrumbButtons.redoListCrumbContainerId));
        
        $("#"+BackForwardBreadcrumbButtons.breadcrumbMenuId)
            .append(undoList)
            .append(undoButton)
            .append($("<div>").addClass("undo_redo_spacer"))
            .append(redoButton)
            .append(redoList)    
        ;
        
    }
    
    private dropdownClickLambda(dropdownSelector: string){
        $(document).click(
            function(event: JQueryEventObject){
                $(dropdownSelector).slideUp();
            }
        );
        return function(event: JQueryEventObject){
            event.stopPropagation();
            $(dropdownSelector).slideToggle();
        }
    }
    
    addCrumbElement(command: UndoRedoManager.ICommand){
        var finalCrumb = this.getFinalCrumb();
        
        var newCrumb = new BreadcrumbTrail.Breadcrumb(command, this)
        // Make it
        var newCrumbElement = $("<div>")
            .attr("id", this.generateCrumbElementId(command))
            .addClass(BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName)
            .addClass(BackForwardBreadcrumbButtons.verticalCrumbDivClass)
            .click(newCrumb.breadcrumbClickedLambda(newCrumb))
            .hover(newCrumb.breadcrumbHoveredLambda(newCrumb), newCrumb.breadcrumbUnhoveredLambda(newCrumb))
            ;
        var crumbName = $("<p>").text(command.getDisplayName()).addClass(BackForwardBreadcrumbButtons.crumbTextClass);
        newCrumbElement.append(crumbName);
        command.addNameUpdateListener(
            this.generateCrumbElementId(command),
            ()=>{
             crumbName.text(command.getDisplayName()); }
        );
        
        // Use it
        // Note that whenever we add a crumb, it is modifying redo, and thus is always going on the
        // top of the undo stack. For the UI, this means we only have things in redo when they have
        // been undone.
        if(null === finalCrumb){
            // No prev breadcrumb? Use label as parent.
            var crumbElementPredecessor = $("#"+BackForwardBreadcrumbButtons.undoListButtonId);
            crumbElementPredecessor.append(newCrumbElement);
        } else {
            // Use preceding crumb as sibling. This is always in the undo stack, so we put them before,
            // not after, the sibling.
            var crumbElementPredecessor = this.selectCrumbElement(finalCrumb.getCommand());
            crumbElementPredecessor.before(newCrumbElement);
        }
        
        // Sort it
        this.trailOfCrumbs.push(command.getUniqueId());
        // Store it
        this.trailMap[command.getUniqueId()] = newCrumb;
    }
    
    removeCrumbElement(command: UndoRedoManager.ICommand){
        // Note that for the back and forward undo lists, we only ever
        // remove from the undo list (when creating a new command head for the undo list)
        // Remove the crumb's element from the GUI
        this.selectCrumbElement(command).remove();
        
        // Clean up three containers
        var popped: string = this.trailOfCrumbs.pop();
        if(popped !== command.getUniqueId()){
            console.log("Sequence problem in breadcrumbs: popped element does not match expected.");
        }
        var crumbElement = this.selectCrumbElement(this.trailMap[command.getUniqueId()].command);
        var isActiveCrumb = crumbElement.hasClass(BackForwardBreadcrumbButtons.activeCrumbClassName);
        delete this.trailMap[command.getUniqueId()];
        
        // Activate next crumb if this popped one was indeed the active one.
        if(isActiveCrumb){
            this.updateActiveCommand(this.getNthCrumb(this.trailOfCrumbs.length).command);
        }
    }
    
    updateActiveCommand(activeCommand: UndoRedoManager.ICommand): void{
        // For the back and forward undo lists, the active crumb is the top one in
        // the undo list. All commands that are later in the command sequence get
        // shunted to the redo list.
        super.updateActiveCommand(activeCommand);
        var activeCommandIndex = this.undoRedoModel.getCommandIndex(activeCommand);

        var undoContainer = $("#"+BackForwardBreadcrumbButtons.undoListCrumbContainerId);
        var redoContainer = $("#"+BackForwardBreadcrumbButtons.redoListCrumbContainerId);
        
        var undoEmpty = true;
        var redoEmpty = true;
        // Going backwards through commands, we can push them onto redo, and shift them onto
        // undo (push at head, shift at tail).
        for(var i = this.trailOfCrumbs.length - 1; i >= 0; i--){
            var crumb = this.selectCrumbElement(this.trailMap[this.trailOfCrumbs[i]].command);
            if(i <= activeCommandIndex){
                crumb.detach();
                undoContainer.append(crumb);
                undoEmpty = false;
            } else {
                crumb.detach();
                redoContainer.prepend(crumb);
                crumb.removeClass(BreadcrumbTrail.BreadcrumbTrail.fadedCrumbClassName);
                redoEmpty = false;
            }
        }
        
        // Update the GUI to reflect availability of undo and redo
        if(undoContainer.children("."+BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName).length <= 1){
            // Minimum one element to undo...we list the initial state, but it cannot be undone.
            $("#"+BackForwardBreadcrumbButtons.undoButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            $("#"+BackForwardBreadcrumbButtons.undoListButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
        } else {
            $("#"+BackForwardBreadcrumbButtons.undoButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            $("#"+BackForwardBreadcrumbButtons.undoListButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
        }
        if(redoContainer.children("."+BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName).length < 1){
            $("#"+BackForwardBreadcrumbButtons.redoButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            $("#"+BackForwardBreadcrumbButtons.redoListButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
        } else {
            $("#"+BackForwardBreadcrumbButtons.redoButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            $("#"+BackForwardBreadcrumbButtons.redoListButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
        }
    }

}