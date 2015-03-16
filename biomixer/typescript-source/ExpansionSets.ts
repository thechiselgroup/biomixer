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
    
    graphModifier: GraphModifierCommand.GraphAddNodesCommand<N>;
    
    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public id: ExpansionSetIdentifer,
        public parentNode: N,
        public graph: GraphView.Graph<N>,
        liveExpansionSets: Array<ExpansionSet<N>>,
        private undoRedoBoss: UndoRedoManager.UndoRedoManager,
        public expansionType: UndoRedoManager.NodeInteraction
        ){
        if(null != parentNode){
            parentNode.expansionSetAsParent = this;
        }
       
        liveExpansionSets.push(this);
        this.graphModifier = new GraphModifierCommand.GraphAddNodesCommand<N>(graph, this, liveExpansionSets);
        
        // Not really necessary, but maybe it would be convenient?
        // if(registerImmediately){
        //     this.thunderbirdsAreGo();
        // }
    }
    
    /**
     * The expansion set is cleared for application. Register in the undo/redo set.
     * Originally this was in the constructor. Using this method works when node cap checks are performed
     * when we know how many REST calls we will make, rather than when we are
     * adding the nodes subsequent to those REST calls.
     * That is, we ask the user about how many nodes they would like earlier rather than later,
     * and thus we need to register the expansion set at a different time than when we create it.
     */
    thunderbirdsAreGo(){
        if(null != this.undoRedoBoss){
            this.undoRedoBoss.addCommand(this.graphModifier);
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
        return this.graph.getNumberOfPotentialNodesToExpand(this.parentNode, this.expansionType);
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
    
    getNodes(): Array<N>{
        return this.nodes;
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