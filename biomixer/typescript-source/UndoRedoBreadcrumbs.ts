///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

/**
 * An undo model with a breadcrumb view composited into it.
 */
export class UndoRedoManager {
    
    public crumblez: BreadcrumbTrail;
    private trail = new Array<ICommand>();
    private currentTrailIndex: number = -1;
    
    constructor(
        initGui: boolean
        ){
        
        if(initGui){
            this.initGui();
        }
    }
    
    initGui(){
        this.crumblez = new BreadcrumbTrail();
        this.crumblez.undoRedoModel = this;
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
                  anotherCommand.executeUndo();
            } else {
                  anotherCommand.executeRedo();
            }
        }
        
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

export class BreadcrumbTrail {
 
    // This is external id, set up by main html page.
    static breadcrumbMenuId = "undo_redo_breadcrumb_trail";
    static breadcrumbTrailLabelId = "undo_redo_breadcrumb_label";
    
    static crumbIdPrefixAndClassName = "crumb_for_";
    
    static activeCrumbClassName = "active_crumb";
    
    static undoneCrumbClassName = "undone_crumb";
    
    static undoMenuText = "Undo/Redo >> ";
    static undoButtonSuffix = " >";
    
    undoRedoModel: UndoRedoManager;
    
    trailOfCrumbs = new Array<string>();
    trailMap: { [key: string]: Breadcrumb }= {};
    
    constructor(){
    }
    
    initGui(){
        $("#"+BreadcrumbTrail.breadcrumbMenuId)
        .append(
            $("<div>").attr("id", BreadcrumbTrail.breadcrumbTrailLabelId)
                .append(
                    $("<p>").text(BreadcrumbTrail.undoMenuText).addClass("crumb_text")
                )
        );
    }
    
    updateElementText(existingCommand: ICommand){
        $("div#"+this.generateCrumbElementId(existingCommand))
            .children(".crumb_text")
            .text(existingCommand.getDisplayName()+BreadcrumbTrail.undoButtonSuffix);
    }
    
    /**
     * Call whenever the stack changes (commands added), and when an undo or redo has happened.
     */
    updateView(stack: Array<ICommand>, activeCommand: ICommand): void{
        // Walk stack and see what differs from rendered
        
        // Locate elements to remove; don't wreck loop by changing container contents, right?
        var toRemove: Breadcrumb[] = [];
        for(var i = 0; i < this.trailOfCrumbs.length; i++){
            // Order doesn't matter, so we can use the unordered map.
            var crumbCommand = this.trailMap[this.trailOfCrumbs[i]];
            if(undefined === crumbCommand){
                // Nothing; this one is new and unadded.
            } else if(stack.indexOf(crumbCommand.command) < 0){
                toRemove.push(crumbCommand);
            }
        }
        
        // Actually remove
        for(var i = 0; i < toRemove.length; i++){
            this.removeCrumbElement(toRemove[i].command);
        }
        
        // Now, add the new ones
        for(var i = 0; i < stack.length; i++){
            var crumbElement = this.selectCrumbElement(stack[i]);
            if(0 === crumbElement.length || undefined === this.trailMap[stack[i].getUniqueId()]){
                this.addCrumbElement(stack[i]);
            }
        }
        
        // Set the active rendered breadcrumb
        this.updateActiveCommand(activeCommand);
    }
    
    addCrumbElement(command: ICommand){
        var finalCrumb = this.getFinalCrumb();
        var crumbElementPredecessor;
        if(null === finalCrumb){
            // No prev breadcrumb? Use label as sibling.
            crumbElementPredecessor = $("#"+BreadcrumbTrail.breadcrumbTrailLabelId);
        } else {
            crumbElementPredecessor = this.selectCrumbElement(finalCrumb.getCommand());
        }
        
        var newCrumb = new Breadcrumb(command, this)
        // Make it
        var newCrumbElement = $("<div>")
            .attr("id", this.generateCrumbElementId(command))
            .addClass(BreadcrumbTrail.crumbIdPrefixAndClassName)
            .click(newCrumb.breadcrumbClickedLambda(newCrumb))
            .hover(newCrumb.breadcrumbHoveredLambda(newCrumb), newCrumb.breadcrumbUnhoveredLambda(newCrumb))
            ;
        newCrumbElement.append($("<p>").text(command.getDisplayName()+BreadcrumbTrail.undoButtonSuffix).addClass("crumb_text"));
        // Use it
        crumbElementPredecessor.after(newCrumbElement);
        // Sort it
        this.trailOfCrumbs.push(command.getUniqueId());
        // Store it
        this.trailMap[command.getUniqueId()] = newCrumb;
    }
    
    removeCrumbElement(command: ICommand){
        // Remove the crumb's element from the GUI
        this.selectCrumbElement(command).remove();
        
        // Clean up three containers
        var popped: string = this.trailOfCrumbs.pop();
        if(popped !== command.getUniqueId()){
            console.log("Sequence problem in breadcrumbs: popped element does not match expected.");
        }
        var crumbElement = this.selectCrumbElement(this.trailMap[command.getUniqueId()].command);
        var isActiveCrumb = crumbElement.hasClass(BreadcrumbTrail.activeCrumbClassName);
        delete this.trailMap[command.getUniqueId()];
        
        // Activate next crumb if this popped one was indeed the active one.
        if(isActiveCrumb){
            this.updateActiveCommand(this.getNthCrumb(this.trailOfCrumbs.length).command);
        }
    }
    
    getNthCrumb(n: number): Breadcrumb{
        if(n > this.trailOfCrumbs.length || n < 0){
            return null;
        }
        return this.trailMap[this.trailOfCrumbs[n]];
    }
    
    getFinalCrumb(){
        return this.getNthCrumb(this.trailOfCrumbs.length-1);
    }
    
    updateActiveCommand(activeCommand: ICommand): void{
        this.selectAllCrumbElements()
            .removeClass(BreadcrumbTrail.activeCrumbClassName);
        
        var activeCrumb = this.selectCrumbElement(activeCommand);
        var activeCommandIndex = this.undoRedoModel.getCommandIndex(activeCommand);
        
        if(activeCommand != null){
            this.selectCrumbElement(activeCommand)
            .addClass(BreadcrumbTrail.activeCrumbClassName);
        }
        
        for(var i = this.trailOfCrumbs.length - 1; i >= 0; i--){
            var crumb = this.selectCrumbElement(this.trailMap[this.trailOfCrumbs[i]].command);
            if(i <= activeCommandIndex){
                crumb.removeClass(BreadcrumbTrail.undoneCrumbClassName);
            } else {
                crumb.addClass(BreadcrumbTrail.undoneCrumbClassName);
            }
        }
    }
    
    selectAllCrumbElements(): JQuery{
        return $("."+BreadcrumbTrail.crumbIdPrefixAndClassName);
    }
    
    selectCrumbElement(crumbCommand: ICommand): JQuery{
        return $("#"+this.generateCrumbElementId(crumbCommand));
    }
    
    generateCrumbElementId(crumbCommand: ICommand): string{
        return BreadcrumbTrail.crumbIdPrefixAndClassName+crumbCommand.getUniqueId();
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
    
    // Just a reminder on why I do Lambda(classInstance)...
    // If we don't, we might misuse the lambda by not *calling* it, but instead providing it.
    // Doing so would rescope "this" to be the calling context rather than the lexical scope.
    // Setting outerThis inside the method but outside the function that is returned will
    // be subject to dynamic rescoping, whereas forcing the caller to explicitly provide a
    // first argument that is the object on which we are operating is safer, though very
    // slightly verbose. It is the safest way.
    breadcrumbClickedLambda(outerThis: Breadcrumb){
        return function(){
            // TODO How do we guarantee that the command is valid? This isn't tied as tightly as the
            // undo/redo model is internally.
            outerThis.breadcrumbTrail.undoRedoModel.changeCurrentTrailPosition(outerThis.command);
        }
    }
    
    breadcrumbHoveredLambda(outerThis: Breadcrumb){
        return function(){
            // Very advanced functionality. Might not be implemented.
            outerThis.command.preview();
        }
    }
    
    breadcrumbUnhoveredLambda(outerThis: Breadcrumb){
        return function(){
            // Very advanced functionality. Might not be implemented.
            outerThis.command.preview();
        }
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
    
    getDisplayName(): string;
    
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
    
    /**
     * This tells the caller what happens to the provided node id, when
     * mvoing in the forward (redo) direction. If the ICommand does not
     * interact with the node, it will return null.
     */
    nodeInteraction(nodeId: string): NodeInteraction;
}

export interface NodeInteraction extends String {
    gorgonzola; // strengthen duck typing
}

