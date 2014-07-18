///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedoBreadcrumbs" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="GraphModifierCommand" />
///<amd-dependency path="DeletionSet" />
///<amd-dependency path="ExpansionSets" />

import UndoRedoBreadcrumbs = require("./UndoRedoBreadcrumbs");
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

    private graphModifier: GraphModifierCommand.GraphCompositeNodeCommand<N>;

    public expansionSet: ExpansionSet.ExpansionSet<N>;
    public deletionSet: DeletionSet.DeletionSet<N>;

    /**
     * Parent node can be null for the initial expansion, when the expansion is not triggered
     * by a menu on an existing node.
     */
    constructor(
        public graph: GraphView.Graph<N>,
        displayName: string,
        id: ExpansionSet.ExpansionSetIdentifer,
        undoRedoBoss: UndoRedoBreadcrumbs.UndoRedoManager
        ){
        this.graphModifier = new GraphModifierCommand.GraphCompositeNodeCommand<N>(graph, displayName);
        // We don't know the parent node for initial expansions. Before this composite class, we used ExpansionSet
        // with null parent node and it worked.
        this.expansionSet = new ExpansionSet.ExpansionSet<N>(id, null, this.graph, null)
        this.deletionSet = new DeletionSet.DeletionSet<N>(this.graph, null);
        
        this.graphModifier.addCommand(this.deletionSet.getGraphModifier());
        this.graphModifier.addCommand(this.expansionSet.getGraphModifier());
        
        undoRedoBoss.addCommand(this.graphModifier);
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