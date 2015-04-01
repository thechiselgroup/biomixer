///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="GraphModifierCommand" />
///<amd-dependency path="ExpansionSets" />

import ConceptGraph = require("./ConceptGraph");
import UndoRedoManager = require("../UndoRedo/UndoRedoManager");
import GraphModifierCommand = require("../GraphModifierCommand");
import ExpansionSets = require("../ExpansionSets");

export class ExpansionManager{
    
    public edgeRegistry: EdgeRegistry;
    
    constructor(
        private undoBoss: UndoRedoManager.UndoRedoManager
    ){
        this.edgeRegistry = new EdgeRegistry();
    }
    
    public purgeInaccessibleNode(conceptUri: ConceptGraph.ConceptURI){
        this.edgeRegistry.purgeInaccessibleNode(conceptUri);
    }
    
     /**
     * Some nodes result in 403, 404 or other errors when REST calls re amde, and they will not be available.
     * We have to account for this nodes to offer the user accurate (and non-confusing) expansion estimates.
     */
    public nodeIsInaccessible(conceptUri: ConceptGraph.ConceptURI){
        return this.edgeRegistry.nodeIsInaccessible(conceptUri);
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
     * 
     * The optional flag should really only be used internally by the class, but change the method semantics to
     * determine if the expansion set associated with the node is currently fully loaded into the graph.
     */
     wasConceptClearedForExpansion(conceptUri: ConceptGraph.ConceptURI, expansionType: ConceptGraph.PathOption): boolean{
        var returnVal = false;
        var crumbTrail = this.undoBoss.getCrumbHistory();
        var conceptUriForIds: string = String(conceptUri);
        for(var i = crumbTrail.length - 1; i >= 0; i--){
            var command = crumbTrail[i];
            var nodeInteractions = command.nodeInteraction(conceptUriForIds);
            if(null == nodeInteractions){
                return returnVal;
            } else if(nodeInteractions.indexOf(expansionType) !== -1){
                returnVal = true;
                return returnVal;
            } else if(nodeInteractions.indexOf(GraphModifierCommand.GraphRemoveNodesCommand.deletionNodeInteraction) !== -1){
                returnVal = false;
                return returnVal;
            }
        }
        return returnVal;
    }
        
    /**
     * Collect all expansion sets that are from the current undo level backwards.
     * Do not return ones that are empty (expansions that resulted in no nodes being added),
     * and do not return expansion sets for which no node is rendered currently.
     * Addendum: Do not return expansion sets for which all nodes it contains are also
     * included in *younger* expansion sets.
     * NB When implementing that last part, the easiest way is probably to maintain the
     * active expansion sets as a complete collection for each undo level, checking for
     * each set to see whether it died. It may also require explicitly associating
     * nodes with which set they are in (a node could be deleted then re-added, and thus
     * be associated with two active expansion sets, when we only want it associated with
     * the youngest one. That is a problem above this level; this just gives the sets, not
     * which has the priority...but if we have  anode-to-set registry implementation, then
     * we can easily inspect that to find which set each node will belong to...
     */
    getActiveExpansionSets(): Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>{
        var history = this.undoBoss.getCrumbHistory();
        var currentUndoLevel = history[history.length - 1];
        return this.getExpansionSets(currentUndoLevel).slice(); // slice to make a copy of the array
    }
    
    private getExpansionSets(command: UndoRedoManager.ICommand): Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>{
        // Refactor, don't check types, but don't entangle expansion sets with ICommand either...
        if(command instanceof GraphModifierCommand.GraphAddNodesCommand){
            return (<GraphModifierCommand.GraphAddNodesCommand<any>>command).liveExpansionSets;
        } else if(command instanceof GraphModifierCommand.GraphCompositeNodeCommand){
            return (<GraphModifierCommand.GraphCompositeNodeCommand<any>>command).liveExpansionSets;
        } else if(command instanceof GraphModifierCommand.GraphRemoveNodesCommand){
            return (<GraphModifierCommand.GraphRemoveNodesCommand<any>>command).liveExpansionSets;
        }
        return [];
    }
    
    getExpansionSetsThatNodeIsParentOf(node: ConceptGraph.Node): Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>{
        var parentageSets = new Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>();
        var allSets = this.getActiveExpansionSets();
        for(var i = 0; i < allSets.length; i++){
            var parentSet = allSets[i];
            if(parentSet.parentNode === node){
               parentageSets.push(parentSet);
            }
        }
        return parentageSets;
    }
    
    getExpansionSetsThatNodeIsChildOf(node: ConceptGraph.Node){
        var childSets = new Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>();
        var allSets = this.getActiveExpansionSets();
        for(var i = 0; i < allSets.length; i++){
            var childSet = allSets[i];
            if(childSet.nodes.indexOf(node) != -1){
               childSets.push(childSet);
            }
        }
        return childSets;
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
    private inaccessibleNodes: Array<ConceptGraph.ConceptURI> = [];
    
    /**
     * If the edge is already represented in the registry, it is returned, and the instance passed in will
     * not be added to the registry.
     */
    addEdgeToRegistry(edge: ConceptGraph.Link, graph: ConceptGraph.ConceptGraph): ConceptGraph.Link{
        // Assumes only mapping edge types. Change that if things change, right? ;)
        var sourceStr = String(edge.sourceId);
        var targetStr = String(edge.targetId);
        
        // Should have guarded before, but I'll guard here too
        if(!graph.nodeIsAccessible(edge.sourceId) || !graph.nodeIsAccessible(edge.targetId)){
            return null; // same as if the edge provided was newly registered??? Hmm...
        }
        
        // Source oriented
        if(!(sourceStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[sourceStr] = [];
        }
        
        // Target oriented
        if(!(targetStr in this.twoWayEdgeRegistry)){
            this.twoWayEdgeRegistry[targetStr] = [];
        }
        
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        var existingEdges = $.grep(this.twoWayEdgeRegistry[sourceStr],
            function(e: ConceptGraph.Link, i: number): boolean{
                return e.targetId === edge.targetId && e.sourceId === edge.sourceId && e.relationType === edge.relationType;
            }
        );
        
        if(existingEdges.length > 0){
            return existingEdges[0];
        } else {
            this.twoWayEdgeRegistry[sourceStr].push(edge);
            this.twoWayEdgeRegistry[targetStr].push(edge);
            return null;
        }
        
        
    }
    
    getEdgesFor(firstNodeIdRaw: ConceptGraph.ConceptURI, secondNodeIdRaw?: ConceptGraph.ConceptURI):  ConceptGraph.Link[] {
        var firstNodeId = String(firstNodeIdRaw);
        var secondNodeId = String(secondNodeIdRaw);
        if(undefined === this.twoWayEdgeRegistry[firstNodeId]){
            return [];
        }
        
        if(null == secondNodeIdRaw){
            return this.twoWayEdgeRegistry[firstNodeId];
        } else {
            var edgesToTarget = [];
            for(var i = 0; i < this.twoWayEdgeRegistry[firstNodeId].length; i++){
                var edge = this.twoWayEdgeRegistry[firstNodeId][i];
                if(String(edge.sourceId) === secondNodeId || String(edge.targetId) === secondNodeId){
                    edgesToTarget.push(edge);
                }   
            }
            return edgesToTarget;
        }
    }
    
    public purgeInaccessibleNode(conceptUri: ConceptGraph.ConceptURI){
        // Although I wanted to purge these invalid edges, they will be created
        // again due to re-parsing of data as necessary, so really I need to tag
        // them and not use them.
        // Wait...the caller is creating an edge and registering it, not just getting the edge from here...
        // when the edge is registered, it means we use the one passed in...
        var sourceIdStr = String(conceptUri);
                
        for(var oneIdStr in this.twoWayEdgeRegistry){
            var edges: Array<ConceptGraph.Link> = this.twoWayEdgeRegistry[oneIdStr];

            this.twoWayEdgeRegistry[oneIdStr] = $.grep(edges,
                (edge)=>{
                    return !(edge.sourceId === conceptUri || edge.targetId === conceptUri);
                }
            );
            
            // Clean out map entries that have no array within them
            if(this.twoWayEdgeRegistry[oneIdStr].length === 0){
                delete this.twoWayEdgeRegistry[oneIdStr];
            }
        }
        
        this.inaccessibleNodes.push(conceptUri);
        console.log("Purged "+conceptUri);
    }
    
    public nodeIsInaccessible(conceptUri: ConceptGraph.ConceptURI){
        return this.inaccessibleNodes.indexOf(conceptUri) !== -1;
    }

}