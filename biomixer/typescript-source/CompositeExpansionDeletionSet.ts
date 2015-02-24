///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="GraphModifierCommand" />
///<amd-dependency path="DeletionSet" />
///<amd-dependency path="ExpansionSets" />

import UndoRedoManager = require("./UndoRedo/UndoRedoManager");
import GraphView = require("./GraphView");
import GraphModifierCommand = require("./GraphModifierCommand");
import DeletionSet = require("./DeletionSet");
import ExpansionSet = require("./ExpansionSets");


/**
 * This class is a version of the deletion set that is oriented towards re-initialization of the view,
 * which occurs when a base expansion change is triggered. It needs to track nodes deleted,
 * but it also needs the expansion. It is a composite action that should be bundled together.
 * Pardon the poor naming.
 */
export class InitializationDeletionSet<N extends GraphView.BaseNode>{

    public graphModifier: GraphModifierCommand.GraphCompositeNodeCommand<N>;

    public expansionSet: ExpansionSet.ExpansionSet<N>;
    public deletionSet: DeletionSet.DeletionSet<N>;

    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public graph: GraphView.Graph<N>,
        id: ExpansionSet.ExpansionSetIdentifer,
        private undoRedoBoss: UndoRedoManager.UndoRedoManager,
        expansionType: UndoRedoManager.NodeInteraction,
        private labelUpdateFunc: (target: ExpansionSet.ExpansionSet<N>)=>void,
        parentNode: N = null
        ){
        // We don't know the parent node for initial expansions. Before this composite class, we used ExpansionSet
        // with null parent node and it worked.
        // We always want this for the initialization deletion set.
        var liveExpansionSets = [];
        this.deletionSet = new DeletionSet.DeletionSet<N>(this.graph, liveExpansionSets, null);
        this.expansionSet = new ExpansionSet.ExpansionSet<N>(id, parentNode, this.graph, liveExpansionSets, null, expansionType)
        this.deletionSet.addAssociatedExpansionSet(this.expansionSet);

        this.graphModifier = new GraphModifierCommand.GraphCompositeNodeCommand<N>(graph, id.displayId,
            this.deletionSet, this.expansionSet, liveExpansionSets);
        
        undoRedoBoss.addCommand(this.graphModifier);
    }
    
    updateExpansionNodeDisplayName(nodeDisplayName:string){
        this.graphModifier.setDisplayName(this.graphModifier.getDisplayName()+": "+nodeDisplayName);
        this.undoRedoBoss.updateUI(this.graphModifier);
        // This is intended to update filter UI components from the GraphView component, but I want light coupling...
        this.expansionSet.id.displayId = this.graphModifier.getDisplayName();
        this.labelUpdateFunc(this.expansionSet);
    }
    
    addAllExpanding(nodes: Array<N>): void{
        this.expansionSet.addAll(nodes);
    }
    
    addAllDeleting(nodes: Array<N>): void{
        this.deletionSet.addAll(nodes);
    }
    
    getGraphModifier(){
        return this.graphModifier;
    }
    
}