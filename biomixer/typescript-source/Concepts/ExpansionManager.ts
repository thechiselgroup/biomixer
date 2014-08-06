///<reference path="headers/require.d.ts" />

///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="UndoRedoBreadcrumbs" />
///<amd-dependency path="GraphModifierCommand" />

import ConceptGraph = require("./ConceptGraph");
import UndoRedoBreadcrumbs = require("../UndoRedoBreadcrumbs");
import GraphModifierCommand = require("../GraphModifierCommand");

export class ExpansionManager{
    
    public edgeRegistry: EdgeRegistry;
    
    constructor(
        private undoBoss: UndoRedoBreadcrumbs.UndoRedoManager
    ){
        this.edgeRegistry = new EdgeRegistry();
    }
    
    /**
     * This is used to determine things like if a given mapping arc is to be fully rendered,
     * or if the neighbors of a term we are processing should be fetched. That depends on hwo
     * we got the term to begin with. That context is available in the undo stack of expansions,
     * which works well when we have undone or redone them, and works better than a previous
     * system of explicit whitelisting for expansions.
     *
     * If this returns true, the node in question is allowed to fetch and add related nodes within the expansion
     * type specified. Those nodes do not (normally) inherit this property.
     * 
     * If the paths to root functionality were not fulfilled via a special REST call, that system would allow
     * expanded parent nodes to inherit this privelege and pass it on to their parents.
     */
     isConceptClearedForExpansion(conceptUri: ConceptGraph.ConceptURI, expansionType: ConceptGraph.PathOption){
        var crumbTrail = this.undoBoss.getCrumbHistory();
        var conceptUriForIds: string = String(conceptUri);
        for(var i = crumbTrail.length - 1; i >= 0; i--){
            var nodeInteraction: UndoRedoBreadcrumbs.NodeInteraction = crumbTrail[i].nodeInteraction(conceptUriForIds);
            if(nodeInteraction === expansionType){
                return true;
            } else if(nodeInteraction === GraphModifierCommand.GraphRemoveNodesCommand.deletionNodeInteraction){
                return false;
            }
        }
        return false;
    }
    
}

export class EdgeRegistry {
    
    // NB We cannot simplify this away by adding edges directly to node objects, since node objects are not instantiated when
    // edges are discovered, but we can associate them with node ids.
    // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.
    // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type).
    // Edges must be tracked before they are valid, or else a superfluous amount of REST calls would be needed.
    // Need to always keep these edges, and to map by both source and target. We only get them when parsing, and we don't re-parse
    // when we have stored objects encapsulating previous data.
    
    // Made registry flat; used to be more structured, but it complicated without fulfilled requirements for said structure.
    private twoWayEdgeRegistry: {[oneNodeId: string]: ConceptGraph.Link[] } = {};
    
    addEdgeToRegistry(edge: ConceptGraph.Link){
        // Assumes only mapping edge types. Change that if things change, right? ;)
        var sourceStr = String(edge.sourceId);
        var targetStr = String(edge.targetId);
        
        // Source oriented
        if(!(sourceStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[sourceStr] = [];
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.twoWayEdgeRegistry[sourceStr].push(edge);
        
        // Target oriented
        if(!(targetStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[targetStr] = [];
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.twoWayEdgeRegistry[targetStr].push(edge);
    }
    
    getEdgesFor(nodeId: string):  ConceptGraph.Link[] {
        if(undefined === this.twoWayEdgeRegistry[nodeId]){
            return [];
        }
        return this.twoWayEdgeRegistry[nodeId];
    }

}