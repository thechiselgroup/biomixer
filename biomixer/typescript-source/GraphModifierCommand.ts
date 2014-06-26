///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedoBreadcrumbs" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="ExpansionSets" />

import UndoRedoBreadcrumbs = require("./UndoRedoBreadcrumbs");
import GraphView = require("./GraphView");
import ExpansionSets = require("./ExpansionSets");


 /**
 * This command allows for the addition of nodes (and undo and redo). Edges are not really
 * added and removed in the same sense, so there is no related class for edges at this time.
 * If they were, we'd would bundle edges to be added or removed with the nodes they were
 * added or removed with.
 */
export class GraphAddNodesCommand<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    
    constructor(
        public graph: GraphView.Graph<N>,
        public expansionSet: ExpansionSets.ExpansionSet<N>
        
    ){

    }
    
    getUniqueId(): string{
        return this.expansionSet.id.internalId;
    }
    
    // TODO This implies that nodes should be added to the graph only
    // via the ExpansionSet, so that the logic is the same when adding a node
    // as when redoing the addition of a set. Hmmm...
    executeRedo(): void{
        this.graph.addNodes(this.expansionSet.nodes, this.expansionSet);
    }
    
    executeUndo(): void{
        this.graph.removeNodes(this.expansionSet.nodes);
    }
    
    preview(): void{
    
    }
}

export class GraphRemoveNodesCommand<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    
    // For node removal, we will want to generalize expansion sets, and collect adjacent node removals
    // into one set of removed nodes.
    constructor(
        public nodesToRemove: Array<N>
    ){

    }
    
    getUniqueId(): string{
        return "";
    }
    
    executeRedo(): void{
    
    }
    
    executeUndo(): void{
    
    }
    
    preview(): void{
    
    }
}