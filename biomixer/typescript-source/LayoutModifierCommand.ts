///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="UndoRedoBreadcrumbs" />
///<amd-dependency path="GraphView" />

import UndoRedoBreadcrumbs = require("./UndoRedoBreadcrumbs");
import GraphView = require("./GraphView");


 

export class LayoutModifier<N extends GraphView.BaseNode> implements UndoRedoBreadcrumbs.ICommand{
    constructor(
    ){
        // LEFTOFF
    }
    
    getUniqueId(): string{
        return "";
    }
    
    // TODO This implies that nodes should be added to the graph only
    // via the ExpansionSet, so that the logic is the same when adding a node
    // as when redoing the addition of a set. Hmmm...
    executeRedo(): void{
    
    }
    
    executeUndo(): void{
    
    }
    
    preview(): void{
    
    }
}