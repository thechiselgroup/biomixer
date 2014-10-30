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
export class DeletionSet<N extends GraphView.BaseNode>{

    private graphModifier: GraphModifierCommand.GraphRemoveNodesCommand<N>;

    // would use set, but we have to convert to array to pass through...
    nodes: Array<N> = new Array<N>();
    
    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public graph: GraphView.Graph<N>,
        private undoRedoBoss: UndoRedoManager.UndoRedoManager
        ){
        this.graphModifier = new GraphModifierCommand.GraphRemoveNodesCommand<N>(graph, this);
        
        if(null != undoRedoBoss){
            undoRedoBoss.addCommand(this.graphModifier);
        }
    }
    
    addAll(nodes: Array<N>): void{
        nodes.forEach(
            (node: N, i: number, arr: Array<N>)=>{
                if(this.nodes.indexOf(node) === -1){
                    this.nodes.push(node);
                }
            }
        );
        if(null != this.undoRedoBoss){
            this.undoRedoBoss.updateUI(this.graphModifier);
        }
    }
    
    getGraphModifier(){
        return this.graphModifier;
    }
    
}

