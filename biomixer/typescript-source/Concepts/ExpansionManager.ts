///<reference path="headers/require.d.ts" />

///<amd-dependency path="Concepts/ConceptGraph" />

import ConceptGraph = require('./ConceptGraph');

export class ExpansionManager{
    
    // To track nodes for which we want their neighbours expanded (by id and expansion type):
    private conceptsToExpand = {};
    
    // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.:
    // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type):
    private edgeRegistry: {[sourceNodeId: string]: {[targetNodeId: string]: {[relationType: string]: ConceptGraph.Link }} } = {};

    constructor(
        
    ){
        
    }
    
    /**
     * Checks if the one or two node ids provided have corresponding (nested) entries in the registry.
     */
    hasEdgeRegistryEntry(firstNodeId: string, secondNodeId?: string): boolean{
        // I'd use overloading, but it turns out the same anyway.
        if(secondNodeId === undefined){
            return firstNodeId in this.edgeRegistry;
        }
        return firstNodeId in this.edgeRegistry && secondNodeId in this.edgeRegistry[firstNodeId];
    }
    
    getRegisteredEdgeTargetsFor(nodeId: string): {[targetNodeId: string]: {[relationType: string]: ConceptGraph.Link }}{
        return this.edgeRegistry[nodeId];
    }
    
    /**
     * If this returns true, the node in question is allowed to fetch and add related nodes within the expansion
     * type specified. Those nodes do not (normally) inherit this property.
     * 
     * If the paths to root functionality were not fulfilled via a special REST call, that system would allow
     * expanded parent nodes to inherit this privelege and pass it on to their parents.
     */
    isConceptWhitelistedForExpansion(conceptUri, expansionType){
        return conceptUri in this.conceptsToExpand && expansionType in this.conceptsToExpand[conceptUri];
    }
    
    /**
     * Nodes that should be expanded (that is expanded from, their related nodes of some type fetched and
     * added to the graph) need to be whitelisted. We have to track them some how, and this is a generic approach.
     * 
     * This is normally only called for the central node on the first load of a visualization, and for nodes that
     * have had their expansion widgets widged.
     */
    addConceptIdToExpansionWhitelist(conceptUri: ConceptGraph.ConceptURI, expansionType: ConceptGraph.PathOptions){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        var conceptId = String(conceptUri);
        this.conceptsToExpand[conceptId] = {};
        this.conceptsToExpand[conceptId][expansionType] = true;
    }
    
    // Call context does not allow for ConceptURI typing...
    addEdgeToRegistry(conceptUriNotInGraph: string, conceptUriInGraph: string, edge: ConceptGraph.Link){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        var conceptIdNotInGraph = String(conceptUriNotInGraph);
        var conceptIdInGraph = String(conceptUriInGraph);
        if(!(conceptIdNotInGraph in this.edgeRegistry)){
            this.edgeRegistry[conceptIdNotInGraph] = {};
        }
        if(!(conceptIdInGraph in this.edgeRegistry[conceptIdNotInGraph])){
            this.edgeRegistry[conceptIdNotInGraph][conceptIdInGraph] = {};
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.edgeRegistry[conceptIdNotInGraph][conceptIdInGraph][edge.relationType] = edge;
    }
    
    clearEdgeFromRegistry(matchUriInRegistry: ConceptGraph.ConceptURI, otherUriInGraph: ConceptGraph.ConceptURI, edge: ConceptGraph.Link){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        var matchIdInRegistry = String(matchUriInRegistry);
        var otherIdInGraph = String(otherUriInGraph);
        delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph][edge.relationType];
        if(Object.keys(this.edgeRegistry[matchIdInRegistry][otherIdInGraph]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph];
        }
        if(Object.keys(this.edgeRegistry[matchIdInRegistry]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry];
        }
    }
    
}