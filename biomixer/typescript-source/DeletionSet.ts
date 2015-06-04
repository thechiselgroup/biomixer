///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="GraphModifierCommand" />
///<amd-dependency path="ExpansionSets" />

import UndoRedoManager = require("./UndoRedo/UndoRedoManager");
import GraphView = require("./GraphView");
import GraphModifierCommand = require("./GraphModifierCommand");
import ExpansionSets = require("./ExpansionSets");


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
export class DeletionSet<N extends GraphView.BaseNode>{

    private graphModifier: GraphModifierCommand.GraphRemoveNodesCommand<N>;
    
    private associatedExpansionSet: ExpansionSets.ExpansionSet<N> = null;

    // would use set, but we have to convert to array to pass through...
    nodes: Array<N> = new Array<N>();
    
    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public graph: GraphView.Graph<N>,
        public liveExpansionSets: Array<ExpansionSets.ExpansionSet<N>>,
        private undoRedoBoss: UndoRedoManager.UndoRedoManager
        ){
        this.graphModifier = new GraphModifierCommand.GraphRemoveNodesCommand<N>(graph, this, this.liveExpansionSets);
        
        if(null != undoRedoBoss){
            undoRedoBoss.addCommand(this.graphModifier);
        }
    }
    
    addAll(incomingNodes: Array<N>): void{
        incomingNodes.forEach(
            (node: N, i: number)=>{
                if(this.nodes.indexOf(node) === -1){
                    this.nodes.push(node);
                }
            }
        );
        
        // We need to recompute what expansion sets are still alive. Any that
        // have had all of their nodes deleted need to be removed from the
        // live expansion set collection.
        // This is like the elephant graveyard of expansion sets. Or death row.
        var deathRow = [];
        for(var expSetIndex in this.liveExpansionSets){
            var expSet = this.liveExpansionSets[expSetIndex];
            var expSetNodes = expSet.getNodes();
            var guilty = true;
            for(var nodeIndex in expSetNodes){
                var node = expSetNodes[nodeIndex];
                // if the graph contains a given node, and that node
                // is not being deleted in this deletion set, then
                // the expansion set is still alive (assuming
                // the expansion set was alive just prior to now).
                if(this.graph.containsNode(node)
                    && this.nodes.indexOf(node) === -1){
                    guilty = false;
                    break;
                }
            }
            
            if(guilty && expSet !== this.associatedExpansionSet){
                deathRow.push(expSet);
            }
        }
        // Execute them.
        this.liveExpansionSets = this.liveExpansionSets.filter((expSet, i)=>{ return -1 === deathRow.indexOf(expSet) });
        this.graphModifier.displayNameUpdated();
    }
    
    getGraphModifier(){
        return this.graphModifier;
    }
    
    /**
     * Sort of odd, but useful for testing things in a way consistent with ExpansionSets
     */
    numberOfNodesCurrentlyInGraph(){
        var numInGraph = 0;
        for(var i = 0; i < this.nodes.length; i++){
            if(this.graph.containsNode(this.nodes[i])){
                numInGraph++;
            }
        }
        return numInGraph;
    }
    
    addAssociatedExpansionSet(expSet: ExpansionSets.ExpansionSet<N>){
        this.associatedExpansionSet = expSet;
    }
}

