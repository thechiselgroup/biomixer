///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="GraphModifierCommand" />

import UndoRedoManager = require("./UndoRedo/UndoRedoManager");
import GraphView = require("./GraphView");
import GraphModifierCommand = require("./GraphModifierCommand");



/**
 * Expansion sets are a way of collecting together nodes that were loaded for a common
 * purpose; I would say at the same time, but loading is done with so much asynchonicity
 * that this would be inaccurate.
 * 
 * By collecting nodes loaded as cohorts, we can then filter them in and out, or use that
 * data to drive an undo-redo engine.
 * 
 * Other uses might arise. 
 */
export class ExpansionSet<N extends GraphView.BaseNode>{
    
    nodes: Array<N> = new Array<N>();
    
    /**
     * As we discover nodes that *could* be loaded, this accrues them.
     * For many expansions, we do not know what the ultimate size of the
     * set will be.
     * 
     * The boolean value is currently unused. I'd use an array but I wanted a set.
     */
    allNodeIds: string[] = [];
        
    graphModifier: GraphModifierCommand.GraphAddNodesCommand<N>;
    
    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public id: ExpansionSetIdentifer,
        public parentNode: N,
        public graph: GraphView.Graph<N>,
        undoRedoBoss: UndoRedoManager.UndoRedoManager,
        public expansionType: UndoRedoManager.NodeInteraction
        ){
        if(null != parentNode){
            parentNode.expansionSetAsParent = this;
        }
        this.graphModifier = new GraphModifierCommand.GraphAddNodesCommand<N>(graph, this);
        
        if(null != undoRedoBoss){
            undoRedoBoss.addCommand(this.graphModifier);
        }
    }
            
    addAll(nodes: Array<N>): void{
        nodes.forEach(
            (node: N, i: number, arr: Array<N>)=>{
                if(node.expansionSetAsMember !== undefined && node.expansionSetAsMember !== this){
                    // The natural flow of the graph populating logic results in multiple passes, due to D3 idioms.
                    // The best place to add nodes to expansion sets are right as we are finally populating the graph
                    // with nodes from an expasions, so we will handle redundant expasion set additions here.
                    // Also, I want to know if there are attempts to add a node to multiple expansion sets.
                    // We don't want that, because it would complicate semantics, especially for undo-redo
                    // functionality that relies on expansion sets.
                    console.log("Attempted change of set expansion ID on node: "+this.id.displayId+", expansion ID "+node.getEntityId());
                } else if(node.expansionSetAsMember !== undefined && node.expansionSetAsMember === this){
                    // No need to set the id, and it should be in the node array already. I won't check.
                    // We might indeed try to add nodes to this again, due to the way that the undo/redo system is designed.
                } else {
                    node.expansionSetAsMember = this;
                    this.nodes.push(node);
                }
            }
        );
    }
    
    addIncludedNodeUri(incomingNodeId: string){
        if(this.allNodeIds.indexOf(incomingNodeId) === -1){
            this.allNodeIds.push(incomingNodeId);
        } else {
            console.log("Duplicate entry attempt in expansion set '"+this.id.internalId+"', for node: "+incomingNodeId);
        }
    }
    
    /**
     * Deprecated
     */
    getNumberOfNodesAssociatedWithExpansion(){
        // I don't think we actually want to know about the parent node for this.
        return this.allNodeIds.length; // + (this.parentNode === null ? 0 : 1);
    }
    
    getGraphModifier(){
        return this.graphModifier;
    }
    
    getNumberOfNodesCurrentlyInGraph(){
        var numInGraph = 0;
        for(var i = 0; i < this.nodes.length; i++){
            if(this.graph.containsNode(this.nodes[i])){
                numInGraph++;
            }
        }
        return numInGraph;
    }
    
    getNumberOfNodesMissing(): number{
        return this.graph.getNumberOfPotentialNodesToExpand(String(this.parentNode.getEntityId()), this.expansionType);
    }
    
    /**
     * Convenience method dispatching into publically accessible GraphModifer.
     * If the expansion is aborted due to problems with having too many nodes
     * in the graph, we need to know it for later expansion attempts, and to
     * ensure that the remaining nodes coming to this expansion are rejected too.
     */
    expansionCutShort(setToTrue: boolean = false): boolean {
        if(setToTrue){
            this.graphModifier.commandCutShort(true);
        }
        return this.graphModifier.commandCutShort();
    }
    
}

export class ExpansionSetIdentifer {
    // Only assign raw concept URI to this string
//    expansionSetIdentifer; // strengthen duck typing
    constructor(
        public internalId: string,
        public displayId: string
    ){
    }
}