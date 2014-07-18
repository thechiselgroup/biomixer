///<reference path="headers/require.d.ts" />

///<amd-dependency path="Concepts/ConceptGraph" />

import ConceptGraph = require("./ConceptGraph");

export class ExpansionManager{
    
    // To track nodes for which we want their neighbours expanded (by id and expansion type):
    private conceptsToExpand = {};
    
    public edgeRegistry: EdgeRegistry;
    public temporaryRegistry: TemporaryEdgeRegistry;

    
    constructor(
        
    ){
        this.edgeRegistry = new EdgeRegistry();
        this.temporaryRegistry = new TemporaryEdgeRegistry();
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
    
}
    
/**
 * For edges that should only be shown when hovering over nodes triggers it.
 */
export class TemporaryEdgeRegistry{
    
    // Presumably only mapping edges are temporary. It'll be obvious if that needs to change.
    private twoWayTemporaryEdgeRegistry: {[sourceNodeId: string]: {[targetNodeId: string]: ConceptGraph.Link } } = {};
    
    addEdgeToTemporaryRenderRegistry(edge: ConceptGraph.Link){
        // Assumes only mapping edge types. Change that if things change, right? ;)
        var sourceStr = String(edge.sourceId);
        var targetStr = String(edge.targetId);
        if(!(sourceStr in this.twoWayTemporaryEdgeRegistry)){
            this.twoWayTemporaryEdgeRegistry[sourceStr] = {};
        }
        if(!(targetStr in this.twoWayTemporaryEdgeRegistry)){
            this.twoWayTemporaryEdgeRegistry[targetStr] = {};
        }
        this.twoWayTemporaryEdgeRegistry[sourceStr][targetStr] = edge;
        this.twoWayTemporaryEdgeRegistry[targetStr][sourceStr] = edge;
    }
    
    getEdgesForTemporaryRendering(conceptIdWeAreHoveringOver: ConceptGraph.ConceptURI, conceptIdNodeMap: ConceptGraph.ConceptIdMap): Array<ConceptGraph.Link> {
        var edges = [];
        var edgesToCheck = this.twoWayTemporaryEdgeRegistry[String(conceptIdWeAreHoveringOver)];
        if(edgesToCheck === undefined){
            return edges;
        }
        $.each(edgesToCheck,
            function(key, edge){
                if(edge.sourceId in conceptIdNodeMap
                    && edge.targetId in conceptIdNodeMap){
                    // Heinous checks for endpoints...but when we register them as temporary, we may not have the endpoint objects.
                    // On the other hand, if we get here we know we have both. This is a great time to do it.
                    if(edge.source === undefined){
                        edge.source = conceptIdNodeMap[String(edge.sourceId)];
                    }
                    if(edge.target === undefined){
                        edge.target = conceptIdNodeMap[String(edge.targetId)];
                    }
                    edges.push(edge);
                }
            });
        return edges;
    }
}
    
export class EdgeRegistry {
    
    // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.
    // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type).
    // Edges must be tracked before they are valid, or else a superfluous amount of REST calls would be needed.
    // Need to always keep these edges, and to map by both source and target. We only get them when parsing, and we don't re-parse
    // when we have stored objects encapsulating previous data.
     private twoWayEdgeRegistry: {[oneNodeId: string]: {[anotherNodeId: string]: {[relationType: string]: ConceptGraph.Link }} } = {};
    
    addEdgeToRegistry(edge: ConceptGraph.Link){
        // Assumes only mapping edge types. Change that if things change, right? ;)
        var sourceStr = String(edge.sourceId);
        var targetStr = String(edge.targetId);
        
        // Source oriented
        if(!(sourceStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[sourceStr] = {};
        }
        if(!(targetStr in this.twoWayEdgeRegistry[sourceStr])){
            this.twoWayEdgeRegistry[sourceStr][targetStr] = {};
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.twoWayEdgeRegistry[sourceStr][targetStr][edge.relationType] = edge;
        
        // Target oriented
        if(!(targetStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[targetStr] = {};
        }
        if(!(sourceStr in this.twoWayEdgeRegistry[targetStr])){
            this.twoWayEdgeRegistry[targetStr][sourceStr] = {};
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.twoWayEdgeRegistry[targetStr][sourceStr][edge.relationType] = edge;
    }
    
    /**
     * Checks if the one or two node ids provided have corresponding (nested) entries in the registry.
     */
    hasEdgeRegistryEntry(firstNodeId: string, secondNodeId?: string): boolean{
        // I'd use overloading, but it turns out the same anyway.
        if(secondNodeId === undefined){
            return firstNodeId in this.twoWayEdgeRegistry;
        }
        return firstNodeId in this.twoWayEdgeRegistry && secondNodeId in this.twoWayEdgeRegistry[firstNodeId];
    }
    
    getRegisteredEdgeTargetsFor(nodeId: string): {[targetNodeId: string]: {[relationType: string]: ConceptGraph.Link }}{
        return this.twoWayEdgeRegistry[nodeId];
    }
}