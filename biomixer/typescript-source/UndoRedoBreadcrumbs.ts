///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

/**
 * An undo model with a breadcrumb view composited into it.
 */
export class UndoRedoManager {
    
    public crumblez: BreadcrumbTrail;
    private trail = new Array<ICommand>();
    private currentTrailIndex: number = 0;
    
    constructor(
        ){
        this.crumblez = new BreadcrumbTrail();
        this.crumblez.undoRedoModel = this;
    }
    
    /**
     * When a new command is executed, add it here. This will truncate any undone command, and they
     * will no longer be available for redoing.
     * 
     * NB There is no way to *only* remove commands without adding a new one. If there's a use case (limiting
     * memory use for example), go ahead and add that functionality.
     */
    addCommand(newCommand: ICommand): void{
        this.trail.splice(this.currentTrailIndex, (this.trail.length - 1 - this.currentTrailIndex), newCommand);
        this.currentTrailIndex = this.trail.length - 1;
        this.refreshBreadcrumbs();
    }
    
    /**
     * Go back to another crumb. Really a convenience method, since
     * we will know whether it is a redo or undo internally.
     */
    undoOneStep(): void{
        this.changeCurrentTrailPosition(this.trail[this.currentTrailIndex - 1]);
    }
    
    redoOneStep(): void{
        this.changeCurrentTrailPosition(this.trail[this.currentTrailIndex + 1]);
    }
    
    changeCurrentTrailPosition(command: ICommand): void{
        if(null == command){
            // No change.
            return;
        }
        var commandIndex = this.trail.indexOf(command);
        if(commandIndex == this.currentTrailIndex){
            return;
        }
        
        var oldIndex = this.currentTrailIndex;
        this.currentTrailIndex = commandIndex;
        
        // No need to call the full refresh method, we haven't
        // changed the undo stack, just the active position.
        this.crumblez.updateActiveCommand(command);
        
        var increment;
        var undo;
        if(commandIndex < oldIndex){
            increment = -1;
            undo = true; //undoing
        } else {
            increment = +1;
            undo = false; //redoing
        }
        for(var i = oldIndex; i += increment; ){
            if(undo){
                command.executeUndo();
            } else {
                command.executeRedo();
            }
        }
    }
    
    /**
     * Call this when the command stack has changed (do to an add always,
     * whether or not the add also resulted in truncating the
     * stack).
     */
    refreshBreadcrumbs(): void{
        this.crumblez.updateView(this.trail, this.trail[this.currentTrailIndex]);
    }
    
}

export class BreadcrumbTrail {
 
    static breadcrumbMenuId = "undo_redo_breadcrumb";
    
    undoRedoModel: UndoRedoManager;
    
    trailOfCrumbs = new Array<String>();
    trailMap: { [key: string]: Breadcrumb }= {};
    
    constructor(){
        
    }
    
    /**
     * Call whenever the stack changes (commands added), and when an undo or redo has happened.
     */
    updateView(stack: Array<ICommand>, activeCommand: ICommand): void{
        // Walk stack and see what differs from rendered
        
        // Remove any clobbered ones, and add any new ones.
        // todo
        
        // Set the active rendered breadcrumb
        this.updateActiveCommand(activeCommand);
    }
    
    updateActiveCommand(activeCommand: ICommand): void{
        var crumb = this.getCrumb(activeCommand);
        
        // todo
    }
    
    getCrumb(activeCommand: ICommand): Breadcrumb{
        var crumbId = this.computeCrumbId(activeCommand);
        return this.trailOfCrumbs[crumbId];
    }
    

    computeCrumbId(command: ICommand): string{
        return command.getUniqueId();
    }
}

export class Breadcrumb {
 
    constructor(
        public command: ICommand,
        public breadcrumbTrail: BreadcrumbTrail
        ){
        
    }
    
    breadcrumbClicked(){
        // TODO How do we guarantee that the command is valid? This isn't tied as tightly as the
        // undo/redo model is internally.
        this.breadcrumbTrail.undoRedoModel.changeCurrentTrailPosition(this.command);
    }
    
    breadcrumbHovered(){
        // Very advanced functionality. Might not be implemented.
        this.command.preview();
    }
    
    breadcrumbUnhovered(){
        // Very advanced functionality. Might not be implemented.
        this.command.preview();
    }
    
    getCommand(): ICommand{
        return this.command;
    }
    
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
    
    /**
     * Add or remove elements as appropriate. Must be reversable.
     */
    executeRedo(): void;
    
    /**
     * Add or remove elements as appropriate. Must be reversable.
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
     * Perhaps that's ok. We cna close menus as necessary too. Automating this and
     * not *actually* using the ICommand data would be the best way, since the preview
     * cannot be interacted with anyway; an image would completely suffice. 
     * 
     *  Very advanced functionality. Might not be implemented.
     */
    preview(): void;
}

