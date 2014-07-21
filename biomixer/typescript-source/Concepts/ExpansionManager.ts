///<reference path="headers/require.d.ts" />

///<amd-dependency path="Concepts/ConceptGraph" />

import ConceptGraph = require("./ConceptGraph");

export class ExpansionManager{
    
    // To track nodes for which we want their neighbours expanded (by id and expansion type):
    private conceptsToExpand = {};
    
    public edgeRegistry: EdgeRegistry;
    
    constructor(
        
    ){
        this.edgeRegistry = new EdgeRegistry();
    }
    
    /**
     * If this returns true, the node in question is allowed to fetch and add related nodes within the expansion
     * type specified. Those nodes do not (normally) inherit this property.
     * 
     * If the paths to root functionality were not fulfilled via a special REST call, that system would allow
     * expanded parent nodes to inherit this privelege and pass it on to their parents.
     */
    isConceptWhitelistedForExpansion(conceptUri: ConceptGraph.ConceptURI, expansionType: ConceptGraph.PathOption){
        var conceptIdStr = String(conceptUri);
        return conceptIdStr in this.conceptsToExpand && String(expansionType) in this.conceptsToExpand[conceptIdStr];
    }
    
    /**
     * Nodes that should be expanded (that is expanded from, their related nodes of some type fetched and
     * added to the graph) need to be whitelisted. We have to track them some how, and this is a generic approach.
     * 
     * This is normally only called for the central node on the first load of a visualization, and for nodes that
     * have had their expansion widgets widged.
     */
    addConceptIdToExpansionWhitelist(conceptUri: ConceptGraph.ConceptURI, expansionType: ConceptGraph.PathOption){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        var conceptId = String(conceptUri);
        this.conceptsToExpand[conceptId] = {};
        this.conceptsToExpand[conceptId][expansionType] = true;
    }
    
    /**
     * When nodes are deleted, we want to remove them from the whitelist, to allow us to use this for other purposes,
     * and to prevent occassional re-expansions from misbehaving after a subsequent expansion. 
     */
    removeConceptIdFromExpansionWhitelist(conceptUri: ConceptGraph.ConceptURI){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        var conceptId = String(conceptUri);
        delete this.conceptsToExpand[conceptId];
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