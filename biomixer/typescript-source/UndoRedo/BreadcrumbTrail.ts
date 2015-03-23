///<amd-dependency path="UndoRedo/UndoRedoManager" />

import UndoRedoManager = require("./UndoRedoManager");

export class BreadcrumbTrail {
 
    // This is external id, set up by main html page.
    static breadcrumbMenuId = "undo_redo_breadcrumb_trail";
    static breadcrumbTrailLabelId = "undo_redo_breadcrumb_label";
    
    static crumbIdPrefixAndClassName = "crumb_for_";
    
    static activeCrumbClassName = "active_crumb";
    
    static fadedCrumbClassName = "faded_crumb";
    
    static crumbTextClass = "crumb_text";
    
    static undoMenuText = "Undo/Redo >> ";
    static undoButtonSuffix = " >";
    
    undoRedoModel: UndoRedoManager.UndoRedoManager;
    
    trailOfCrumbs = new Array<string>();
    trailMap: { [key: string]: Breadcrumb }= {};
    
    activeCommandIndex: number;
    
    constructor(){
    }
    
    initGui(){
        $("#"+BreadcrumbTrail.breadcrumbMenuId)
        .append(
            $("<div>").attr("id", BreadcrumbTrail.breadcrumbTrailLabelId)
                .append(
                    $("<p>").text(BreadcrumbTrail.undoMenuText).addClass(BreadcrumbTrail.crumbTextClass)
                )
        );
    }

    private updateCrumbText(crumbNameDisplay: JQuery, command: UndoRedoManager.ICommand){
            return ()=>{ crumbNameDisplay.text(command.getDisplayName()+BreadcrumbTrail.undoButtonSuffix) };
    }
    
    /**
     * Call whenever the stack changes (commands added), and when an undo or redo has happened.
     */
    updateView(stack: Array<UndoRedoManager.ICommand>, activeCommand: UndoRedoManager.ICommand): void{
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
    
    addCrumbElement(command: UndoRedoManager.ICommand){
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
        var crumbName = $("<p>").text(command.getDisplayName()+BreadcrumbTrail.undoButtonSuffix).addClass("crumb_text")
        newCrumbElement.append(crumbName);
        command.addNameUpdateListener(
            this.generateCrumbElementId(command),
            this.updateCrumbText(crumbName, command)
        );
          
        // Use it
        crumbElementPredecessor.after(newCrumbElement);
        // Sort it
        this.trailOfCrumbs.push(command.getUniqueId());
        // Store it
        this.trailMap[command.getUniqueId()] = newCrumb;
    }
    
    removeCrumbElement(command: UndoRedoManager.ICommand){
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
    
    getActiveCrumb(): Breadcrumb{
        return this.getNthCrumb(this.activeCommandIndex);
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
    
    updateActiveCommand(activeCommand: UndoRedoManager.ICommand): void{
        this.selectAllCrumbElements()
            .removeClass(BreadcrumbTrail.activeCrumbClassName);
        
        var activeCrumb = this.selectCrumbElement(activeCommand);
        this.activeCommandIndex = this.undoRedoModel.getCommandIndex(activeCommand);
        
        if(activeCommand != null){
            this.selectCrumbElement(activeCommand)
            .addClass(BreadcrumbTrail.activeCrumbClassName);
        }
        
        for(var i = this.trailOfCrumbs.length - 1; i >= 0; i--){
            var crumb = this.selectCrumbElement(this.trailMap[this.trailOfCrumbs[i]].command);
            if(i <= this.activeCommandIndex){
                crumb.removeClass(BreadcrumbTrail.fadedCrumbClassName);
            } else {
                crumb.addClass(BreadcrumbTrail.fadedCrumbClassName);
            }
        }
    }
    
    selectAllCrumbElements(): JQuery{
        return $("."+BreadcrumbTrail.crumbIdPrefixAndClassName);
    }
    
    selectCrumbElement(crumbCommand: UndoRedoManager.ICommand): JQuery{
        return $("#"+this.generateCrumbElementId(crumbCommand));
    }
    
    generateCrumbElementId(crumbCommand: UndoRedoManager.ICommand): string{
        return BreadcrumbTrail.crumbIdPrefixAndClassName+crumbCommand.getUniqueId();
    }
    
    getCrumb(activeCommand: UndoRedoManager.ICommand): Breadcrumb{
        var crumbId = this.computeCrumbId(activeCommand);
        return this.trailOfCrumbs[crumbId];
    }
    

    computeCrumbId(command: UndoRedoManager.ICommand): string{
        return command.getUniqueId();
    }
}

export class Breadcrumb {
 
    constructor(
        public command: UndoRedoManager.ICommand,
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
    
    getCommand(): UndoRedoManager.ICommand{
        return this.command;
    }
    
}

