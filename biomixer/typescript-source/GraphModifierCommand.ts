///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedoBreadcrumbs" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="ExpansionSets" />
///<amd-dependency path="DeletionSet" />
///<amd-dependency path="LayoutModifierCommand" />

import UndoRedoBreadcrumbs = require("./UndoRedoBreadcrumbs");
import GraphView = require("./GraphView");
import ExpansionSets = require("./ExpansionSets");
import DeletionSet = require("./DeletionSet");
import LayoutModifierCommand = require("./LayoutModifierCommand");


/**
 * This command allows for the addition of nodes (and undo and redo). Edges are not really
 * added and removed in the same sense, so there is no related class for edges at this time.
 * If they were, we'd would bundle edges to be added or removed with the nodes they were
 * added or removed with.
 */
export class GraphAddNodesCommand<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    
    static counter = 0;
    
    private id: string;
    
    private redidLast = true;
    
    private layoutSnapshot: LayoutModifierCommand.LayoutModificationCommand<N>;
    
    constructor(
        public graph: GraphView.Graph<N>,
        public expansionSet: ExpansionSets.ExpansionSet<N>
        
    ){
        // This is the layout that applies when the nodes have been successfully added,
        // then when any active layout is done positioning them.
        // This is tricky because the node additions can be asynchronous when first
        // adding them, so we need last-second saving of the layout, triggered
        // either by the layout finishing, or the user modifying the layout (via triggering
        // a new one or manually dragging nodes).
        // So...we need a method to re-set the layout should these actions occur, and
        // that needs to be called on the most recent GraphAddNodesCommand by code that knows
        // that such events have occurred.
        // this.layoutSnapshot = new LayoutModifierCommand.LayoutModificationCommand();
    }
    
    snapshotLayout(){
//        if(null === this.layoutSnapshot){
            // TODO Should we allow users to go back in time, and modify existing snapshots?
            // The user may want to adjust layouts within an undo...this is a tricky
            // concept. I do not want manual layout changes (drags) triggering an undo stack
            // operation at all. Clicking a new layout...maybe that shouldn't either!
            // I will allow it for now, and see how it feels.
            this.layoutSnapshot = new LayoutModifierCommand.LayoutModificationCommand();
//        } else {
//            console.log("Attempting to snapshot positions for a graph node add command, beyond the first attempt");   
//        }
    }
    
    getUniqueId(): string{
        if(undefined === this.id){
            this.id = this.expansionSet.id.internalId+"_"+(GraphAddNodesCommand.counter++);
        }
        return this.id;
    }
    
    getDisplayName(): string{
        return this.expansionSet.id.displayId;
    }
    
    // TODO This implies that nodes should be added to the graph only
    // via the ExpansionSet, so that the logic is the same when adding a node
    // as when redoing the addition of a set. Hmmm...
    executeRedo(): void{
        if(!this.redidLast){
            this.redidLast = true;
            this.graph.addNodes(this.expansionSet.nodes, this.expansionSet);
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeRedo();
        } else {
            console.log("Trying to redo same command twice in a row");
        }
    }
    
    executeUndo(): void{
        if(this.redidLast){
            this.redidLast = false;
            this.graph.removeNodes(this.expansionSet.nodes);
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeUndo();
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
}

export class GraphRemoveNodesCommand<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    
    static counter = 0;
    
    private id: string;
    
    redidLast: boolean = false; // start being able to redo it
    
    // For node removal, we will want to generalize expansion sets, and collect adjacent node removals
    // into one set of removed nodes.
    constructor(
        public graph: GraphView.Graph<N>,
        public nodesToRemove: DeletionSet.DeletionSet<N>
    ){

    }
    
    getUniqueId(): string{
        if(undefined === this.id){
            // I don't have a useful name to give sets of removed nodes, since they are fully arbitrary,
            // unlike expansion sets.
            this.id = "remove_nodes_"+(GraphRemoveNodesCommand.counter++);
        }
        return this.id;
    }
    
    getDisplayName(): string{
        return "Removed "+this.nodesToRemove.nodes.length+" Node"+(this.nodesToRemove.nodes.length>1 ? "s" : "");
    }
    
    executeRedo(): void{
        if(!this.redidLast){
            this.redidLast = true;
            this.graph.removeNodes(this.nodesToRemove.nodes);
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeRedo();
        } else {
            console.log("Trying to redo same command twice in a row");
        }
    }
    
    executeUndo(): void{
        if(this.redidLast){
            this.redidLast = false;
            this.graph.addNodes(this.nodesToRemove.nodes, null);
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeUndo();
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
}

export class GraphCompositeNodeCommand<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    
    static counter = 0;
    
    private id: string;
    
    redidLast: boolean = false; // start being able to redo it
    
    commands: UndoRedoBreadcrumbs.ICommand[] = [];
    
    constructor(
        public graph: GraphView.Graph<N>,
        public displayName: string
    ){

    }
    
    addCommand(newCommand: UndoRedoBreadcrumbs.ICommand){
        this.commands.push(newCommand);
    }
    
    getUniqueId(): string{
        if(undefined === this.id){
            this.id = "composite_command_"+(GraphCompositeNodeCommand.counter++);
        }
        return this.id;
    }
    
    getDisplayName(): string{
        return this.displayName;
    }
    
    executeRedo(): void{
        if(!this.redidLast){
            this.redidLast = true;
            for(var i = 0; i < this.commands.length; i++){
                this.commands[i].executeRedo();
            }
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeRedo();
        } else {
            console.log("Trying to redo same command twice in a row");
        }
    }
    
    executeUndo(): void{
        if(this.redidLast){
            this.redidLast = false;
            for(var i = this.commands.length - 1; i >= 0; i--){
                this.commands[i].executeUndo();
            }
            // Ha, we don't do and undo layouts, I just realized...
            // We apply previous and next from the fenceposts!
            // this.layoutSnapshot.executeUndo();
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
}