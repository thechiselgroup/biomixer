///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="UndoRedo/BreadcrumbTrail" />
///<amd-dependency path="UndoRedo/BackForwardBreadcrumbButtons" />


import BreadcrumbTrail = require("./BreadcrumbTrail");
import BackForwardBreadcrumbButtons = require("./BackForwardBreadcrumbButtons");


/**
 * An undo model with a breadcrumb view composited into it.
 */
export class UndoRedoManager {
    
    public crumblez: BreadcrumbTrail.BreadcrumbTrail;
    private trail = new Array<ICommand>();
    private currentTrailIndex: number = -1;
    
    constructor(
        initGui: boolean,
        useBackForwardButtons: boolean
        ){
        if(useBackForwardButtons){
            this.crumblez = new BackForwardBreadcrumbButtons.BackForwardBreadcrumbButtons();
        } else {
            this.crumblez = new BreadcrumbTrail.BreadcrumbTrail();
        }
        this.crumblez.undoRedoModel = this;
        
        if(initGui){
            this.initGui();
        }
    }
    
    initGui(){
        this.crumblez.initGui();
    }
    
    /**
     * When a new command is executed, add it here. This will truncate any undone command, and they
     * will no longer be available for redoing.
     * 
     * NB There is no way to *only* remove commands without adding a new one. If there's a use case (limiting
     * memory use for example), go ahead and add that functionality.
     */
    addCommand(newCommand: ICommand): void{
        // console.log("Adding to trail at: "+(this.currentTrailIndex + 1));
        // console.log("Remove from trail: "+(this.trail.length-1 - this.currentTrailIndex));
        // If we have 10 items, and the current item is 7 (index 0, so 8th item), then when we splice, we want to
        // insert at 8 (after 7, so at 7+1), and we want to remove from the array 2 items (index 8 and 9), which is
        // 10 - 1 - 7
        var removed = this.trail.splice(this.currentTrailIndex + 1, (this.trail.length-1 - this.currentTrailIndex), newCommand);
        // console.log(this.trail[this.trail.length-1].getDisplayName());
        this.currentTrailIndex = this.trail.length - 1;
        // TODO Should we bother deleting the removed commands?
        // That is with the delete keyword?
        if(undefined !== this.crumblez){
            // Get that command to store the current layout, before changing things.
            var active = this.crumblez.getActiveCrumb();
            if( undefined !== active){
                active.getCommand().snapshotLayout(true);
            }
            this.crumblez.updateView(this.trail, newCommand);
        }
    }
    
    /**
     * Useful for when the text of a crumb is not fixed at creation time.
     */
    updateUI(existingCommand: ICommand){
        this.crumblez.updateElementText(existingCommand);
    }
    
    /**
     * Go back to another crumb. Really a convenience method, since
     * we will know whether it is a redo or undo internally.
     */
    undoOneStep(): void{
        var index = Math.max(0, this.currentTrailIndex - 1);
        this.changeCurrentTrailPosition(this.trail[index]);
    }
    
    redoOneStep(): void{
        var index = Math.min(this.currentTrailIndex + 1, this.trail.length - 1);
        this.changeCurrentTrailPosition(this.trail[index]);
    }
    
    changeCurrentTrailPosition(command: ICommand): void{
        if(null == command){
            // No change.
            return;
        }
        
        // Current active? Do nothing.
        var commandIndex = this.getCommandIndex(command);
        if(commandIndex === this.currentTrailIndex){
            return;
        }
        
        var oldIndex = this.currentTrailIndex;
        this.currentTrailIndex = commandIndex;
        
        // No need to call the full refresh method, we haven't
        // changed the undo stack, just the active position.
        if(undefined !== this.crumblez){
            this.crumblez.updateActiveCommand(command);
        }
        
        
        var increment;
        var undo;
        var stopAtIndex;
        var startAtIndex;
        if(commandIndex < oldIndex){
            increment = -1;
            undo = true; //undoing
            startAtIndex = oldIndex; // From current downward, undo
            stopAtIndex = commandIndex; // DOn't undo our target
        } else {
            increment = +1;
            undo = false; //redoing
            startAtIndex = oldIndex + 1; // From next after current, redo
            stopAtIndex = commandIndex + 1; // Redo the target one, not past
        }
        for(var i = startAtIndex; i !== stopAtIndex; i += increment){
            var anotherCommand = this.trail[i];
            if(undo){
                // In case the snapshot is at the head of the undo stack,
                // temporarily save its layout in case we come back to it.
                anotherCommand.snapshotLayout(false);
                anotherCommand.executeUndo();
            } else {
                anotherCommand.executeRedo();
            }
        }
        
        // Apply the layout we got a snapshot for in addCommand(), when the command was created.
        this.crumblez.getActiveCrumb().getCommand().applyLayout();
        
        // TODO Filters may need some refreshing, but the design separates the undo/redo in model from filters in view.
        // Fix if the inconsistency is a problem. Filters could be moved to be model oriented, but I have
        // them more as controller/views, due to the fact they only change the view. It might be tricky
        // to refactor to facilitate refreshing those on redo/undo!
        // When we do this, we also need to refresh filters. So, let's tell the graph model
        // that things have been undone, and it can carry out any additional cleanup as necessary.
        // this.graphView.afterUndoRedoPerformed();
    }
    
    getCommandIndex(command: ICommand): number{
        return this.trail.indexOf(command);
    }
    
    /**
     * Although it trades modularity for encapsulation, I need to allow another class to access breadcrumb history to
     * inspect for whether a given node was expanded in our current state. Deletions and expansions
     * both weigh in on the decision.
     */
    getCrumbHistory(){
        return this.trail.slice(0, this.currentTrailIndex+1);
    }
    
}

// Unused for now.
// We need it, but I am defering the refactoring necessary to hook it up.
export interface UndoRedoListener {
    afterUndoRedoPerformed();
}

export interface NodeInteraction extends String {
    gorgonzola; // strengthen duck typing
}
            
/**
 * Commands for use in the undo/redo system.
 * Actions that are added to the breadcrumb trail implement this.
 * 
 * I expect ICommand to be used for expansion sets, and layout triggers.
 * The preceding exact layout positions and graph constituents should be stored
 * explicitly for the undo of a command. This allows manual node placement to be
 * returned to on undo, but only to the manual placement immediately preceding
 * the event that triggered the command creation.
 */
export interface ICommand {
    
    getUniqueId(): string;
    
    getDisplayName(): string;
    
    /**
     * Add or remove elements as appropriate. Must be reversable.
     * Must also take care of applying layout snapshot.
     */
    executeRedo(): void;
    
    /**
     * Add or remove elements as appropriate. Must be reversable.
     * Must also take care of applying layout snapshot.
     */
    executeUndo(): void;
   
    /**
     * Render the state of this command so that the user can see what the results
     * of clicking it would be. Has to compute the undo/redo stack up to this
     * point, so this could be a moderately expensive operation if we are not caching
     * the entire state in some fashion.
     * 
     * TODO Perhaps we should indeed cache the state somehow...is there a swift way of
     * capturing a scaled or full scale image of an SVG rendering? How do we avoid the
     * various highlighting and whatnot that might be occurring due to mouse interactions?
     * Perhaps that's ok. We can close menus as necessary too. Automating this and
     * not *actually* using the ICommand data would be the best way, since the preview
     * cannot be interacted with anyway; an image would completely suffice. 
     * 
     *  Very advanced functionality. Might not be implemented.
     */
    preview(): void;
    
    /**
     * This tells the caller what happens to the provided node id, when
     * moving in the forward (redo) direction. If the ICommand does not
     * interact with the node, it will return null.
     */
    nodeInteraction(nodeId: string): NodeInteraction;
    
    // How do I actually want to do this? This seems incorrect...
    // Maybe takeLayoutSnapshot(layouProvider: LayoutProvider.LayoutProvider) ???
//    getLayoutProvider(): LayoutProvider.LayoutProvider;
    
    snapshotLayout(finalSnapshot: boolean);
    
    applyLayout();
    
    /**
     * Used to determine if the command's execution was interrupted.
     * This is the case if expansions are stopped because there are too many
     * nodes in the graph, likely determined by prompting the user.
     */
    commandCutShort(setToTrue?: boolean): boolean;
    
    
    areCommandNodesCurrentlyLoaded(): boolean;
    
    numberOfCommandNodesCurrentlyLoaded(): number;
    
    /**
     * Gives the number of nodes there would be if all were loaded,
     * excluding any permanently failed callbacks.
     */
    numberOfNodesInCommand(): number;
}

