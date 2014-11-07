///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="ExpansionSets" />
///<amd-dependency path="DeletionSet" />
///<amd-dependency path="LayoutProvider" />

import UndoRedoManager = require("./UndoRedo/UndoRedoManager");
import GraphView = require("./GraphView");
import ExpansionSets = require("./ExpansionSets");
import DeletionSet = require("./DeletionSet");
import LayoutProvider = require("./LayoutProvider");

/**
 * I really want an abstract class for this, but Typescript doesn't allow for that.
 * My method of a combination of an interface and a stubbed class isn't feeling right here,
 * so I will use this as a static implementor for the classes in this module.
 */
export class CommonImplementor<N extends GraphView.BaseNode> {
       
    fixedLayout: { [nodeId: string]: {x: number; y: number} } = {};
    storedLayoutRunner: LayoutProvider.LayoutRunner;
    
    // This *should* be set as a constructor arg, but my current compiler does not support super(this)
    // in the child constructor calls.
    childImpl: UndoRedoManager.ICommand;
    
    constructor(
        public graph: GraphView.Graph<N>
    ){
        
    }
    
    applyLayoutImpl(){
        this.graph.getLayoutProvider().setLayoutFixedCoordinates(this.fixedLayout);
        // Trigger the fixed layout algorithm immediately
        this.graph.getLayoutProvider().applyFixedLayout();
        // Set up the original algorithm to be used
        if(undefined !== this.storedLayoutRunner){
            // Can be undefined in the case of composite commands that do not defer snapshotting to composed commands.
            this.graph.getLayoutProvider().setNewLayoutWithoutRunning(this.storedLayoutRunner);
        }
    }
    
    finalSnapshotTaken = false;
    /**
     * If the snapshot is the final one, it cannot be updated or overwritten again. Only do this when
     * a new expansion has occurred, and it is acceptable to freeze the undo step's node positions.
     */
    snapshotLayoutImpl(finalSnapshot: boolean){
        // Record position of all nodes, for undo/redo. Also keep track of the layout algorithm
        // in force, so that if the user expands from that step in the undo/redo stack, it will
        // retrigger the appropriate algorithm.
        // But...once we have made this step "undoable", we freeze the fixed layout and applied algorithm
        if(!this.finalSnapshotTaken){
            // Using a simple method of snapshotting layouts.
            this.fixedLayout = this.graph.getLayoutProvider().getLayoutPositionSnapshot();
            this.storedLayoutRunner = this.graph.getLayoutProvider().getLayoutRunner();
        }
        if(finalSnapshot){
            this.finalSnapshotTaken = true;
        }
    }
    
    
}

/**
 * This command allows for the addition of nodes (and undo and redo). Edges are not really
 * added and removed in the same sense, so there is no related class for edges at this time.
 * If they were, we'd would bundle edges to be added or removed with the nodes they were
 * added or removed with.
 */
export class GraphAddNodesCommand<N extends GraphView.BaseNode> extends CommonImplementor<N> implements UndoRedoManager.ICommand {
    
    static addedNodeInteraction: UndoRedoManager.NodeInteraction = <UndoRedoManager.NodeInteraction><any>"added node";
    
    static counter = 0;
    
    private id: string;
    
    private redidLast = true;
    
    
    constructor(
        public graph: GraphView.Graph<N>,
        public expansionSet: ExpansionSets.ExpansionSet<N>
        
    ){
        super(graph);
        this.childImpl = this;
    }

    
    snapshotLayout(finalSnapshot: boolean){
        this.snapshotLayoutImpl(finalSnapshot);
    }
    
    
    applyLayout(){
        this.applyLayoutImpl();
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
            this.applyLayout();
        } else {
            console.log("Trying to redo same command twice in a row");
        }
    }
    
    executeUndo(): void{
        if(this.redidLast){
            this.redidLast = false;
            this.graph.removeNodes(this.expansionSet.nodes);
            // NB We don't undo layouts, we only do them.
            // The incoming command will apply its layout.
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
    
    nodeInteraction(nodeId: string): UndoRedoManager.NodeInteraction{
        if(null === this.expansionSet.parentNode || this.expansionSet.parentNode.getEntityId() === nodeId){
            return this.expansionSet.expansionType;
        }
        for(var i = 0; i < this.expansionSet.nodes.length; i++){
            var node = this.expansionSet.nodes[i];
            if(node.getEntityId() === nodeId){
                return GraphAddNodesCommand.addedNodeInteraction;
            }
        }
        return null;
    }
    
}

export class GraphRemoveNodesCommand<N extends GraphView.BaseNode> extends CommonImplementor<N> implements UndoRedoManager.ICommand{
    
    static deletionNodeInteraction: UndoRedoManager.NodeInteraction = <UndoRedoManager.NodeInteraction><any>"deleted node";
    
    static counter = 0;
    
    private id: string;
    
    redidLast: boolean = false; // start being able to redo it
       
    // For node removal, we will want to generalize expansion sets, and collect adjacent node removals
    // into one set of removed nodes.
    constructor(
        public graph: GraphView.Graph<N>,
        public nodesToRemove: DeletionSet.DeletionSet<N>
    ){
        super(graph);
        this.childImpl = this;
    }
    
    snapshotLayout(finalSnapshot: boolean){
        this.snapshotLayoutImpl(finalSnapshot);
    }
    
    applyLayout(){
        this.applyLayoutImpl();
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
            // Don't undo layouts, onyl redo, so that incoming step will apply its layout.
            this.applyLayout();
        } else {
            console.log("Trying to redo same command twice in a row");
        }
    }
    
    executeUndo(): void{
        if(this.redidLast){
            this.redidLast = false;
            this.graph.addNodes(this.nodesToRemove.nodes, null);
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
    
    nodeInteraction(nodeId: string): UndoRedoManager.NodeInteraction{
        for(var i = 0; i < this.nodesToRemove.nodes.length; i++){
            var node = this.nodesToRemove.nodes[i];
            if(node.getEntityId() === nodeId){
                return GraphRemoveNodesCommand.deletionNodeInteraction;
            }
        }
        return null;
    }
    
}

export class GraphCompositeNodeCommand<N extends GraphView.BaseNode> extends CommonImplementor<N> implements UndoRedoManager.ICommand{
    
    static counter = 0;
    
    private id: string;
    
    redidLast: boolean = false; // start being able to redo it
    
    commands: UndoRedoManager.ICommand[] = [];
    

    constructor(
        public graph: GraphView.Graph<N>,
        public displayName: string
    ){
        super(graph);
        this.childImpl = this;
    }
    
    addCommand(newCommand: UndoRedoManager.ICommand){
        this.commands.push(newCommand);
    }

    snapshotLayout(finalSnapshot: boolean){
        this.snapshotLayoutImpl(finalSnapshot);
    }
    
    applyLayout(){
        this.applyLayoutImpl();
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
            
    setDisplayName(newName: string): void{
        // Danger! Caller is wholly responsible for content of this display name.
        // It shouldn't change twice, and the caller shouldn't be trying to change it twice.
        this.displayName = newName;
    }
    
    executeRedo(): void{
        if(!this.redidLast){
            this.redidLast = true;
            for(var i = 0; i < this.commands.length; i++){
                this.commands[i].executeRedo();
            }
            // Only apply layouts on redo, so that incoming steps get their layout.
            this.applyLayout();
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
        } else {
            console.log("Trying to undo same command twice in a row");
        }
    }
    
    preview(): void{
    
    }
    
    nodeInteraction(nodeId: string): UndoRedoManager.NodeInteraction {
        // We look in reverse at all the composite commands
        for(var i = this.commands.length - 1; i >= 0; i--){
            var interaction = this.commands[i].nodeInteraction(nodeId);
            if(null !== interaction){
                return interaction;
            }
        }
        return null;
    }
}