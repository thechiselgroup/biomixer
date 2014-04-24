///<reference path="headers/require.d.ts" />

///<amd-dependency path="Utils" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="Concepts/ExpansionManager" />

import Utils = require('../Utils');
import Fetcher = require('../FetchFromApi');
import GraphView = require('../GraphView');
import ExpansionManager = require('./ExpansionManager');

declare var purl;

export interface PathOption extends String {
    // Only assign raw concept URI to this string
    pathological; // strengthen duck typing
}


export class PathOptionConstants {
    // Tricky trick to allow casting of string up to PathOption. Fancy!
    static termNeighborhoodConstant: PathOption = <PathOption><any>"term neighborhood";
    static pathsToRootConstant: PathOption = <PathOption><any>"path to root";
    static mappingsNeighborhoodConstant: PathOption = <PathOption><any>"mappings neighborhood";
}

export interface ConceptURI extends String {
    // Only assign raw concept URI to this string
    conceptId; // strengthen duck typing
}

export interface RawAcronym extends String {
    // Only assign the original unadulterated acronym strings to this
    rawAcronym; // strengthen duck typing
}

export interface ConceptUriForIds extends String {
    // Assign id-escaped acronyms here. These are made safe for use in HTML and SVG ids.
    
//    escapeAcronym(acronym: RawAcronym){
//        //  return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
//        // JQuery selectors do not work with things that need escaping.
//        // Let's use double underscores instead.
//        return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '__');
//    }
}

export interface AcronymForIds extends String {
    // Assign id-escaped acronyms here. These are made safe for use in HTML and SVG ids.
    
//    escapeAcronym(acronym: RawAcronym){
//        //  return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
//        // JQuery selectors do not work with things that need escaping.
//        // Let's use double underscores instead.
//        return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '__');
//    }
    acronymForId; // strengthen duck typing
}

export interface ConceptIdMap {
    // $(ontologyAcronymNodeMap).attr("vid:"+centralOntologyNode.rawAcronym, centralOntologyNode);
    [id: string]: Node;
}

export class Node extends GraphView.BaseNode {
    rawConceptUri: ConceptURI; // used to be 'id' but that conflicts with D3. Get this id from conceptData["@id"]
    conceptUriForIds: ConceptUriForIds; // encodeURIComponent(conceptNode.id);
    name: string; // conceptData.prefLabel
    type: string; // conceptData.type
    description: string; // Comes from description RETS call // = "fetching description";
    fixed: boolean; // = true; // lock central node
//    x: number; // = visWidth()/2;
//    y: number; // = visHeight()/2;      
    weight: number; // = numberOfMappedOntologies; // will increment as we loop
    ontologyAcronym: RawAcronym; // ontologyUri.substring(ontologyUri.lastIndexOf("ontologies/")+"ontologies/".length);
    ontologyUri: string; // ontologyUri.substring(ontologyUri.lastIndexOf("ontologies/")+"ontologies/".length);
    ontologyUriForIds: string; // encodeURIComponent(conceptNode.ontologyUri);
    nodeColor: string; // nextNodeColor(conceptNode.ontologyAcronym);
            
//    uriId: string; // = ontologyDetails["@id"]; // Use the URI instead of virtual id
//    LABEL: string; // = ontologyDetails.name;

    getEntityId(): string{
        return String(this.conceptUriForIds);
    }
    
    constructor(){
        super();
    }
}

export class Link extends GraphView.BaseLink<Node> {
    // We get the ids before we can construct the nodes...
    sourceId: ConceptURI; // = parentId;
    targetId: ConceptURI; // = childId;
    source: Node; // = centralOntologyNode;
    target: Node; // = ontologyNode;
    rawId: string; //  = edge.sourceId+"-to-"+edge.targetId;
    id: string; // Escaped node ids, otherwise like rawId above. = edge.sourceId+"-to-"+edge.targetId;
    value: number = 1; // This gets used for link stroke thickness later...not needed for concepts?
    relationType: string; // = relationType;
    
    constructor(){
        super();
    }
}

export class ConceptD3Data extends GraphView.GraphDataForD3<Node, Link> {
    
}


export class ConceptGraph implements GraphView.Graph {
    
    expMan: ExpansionManager.ExpansionManager = new ExpansionManager.ExpansionManager();
    
    graphD3Format: ConceptD3Data = new ConceptD3Data();
    
      // To track nodes that we have in the graph (by id):
    conceptIdNodeMap: ConceptIdMap = {};
    
    addNodeToIdMap(conceptNode: Node){
        this.conceptIdNodeMap[String(conceptNode.rawConceptUri)]= conceptNode;
    }
    
    getNodeByUri(uri: ConceptURI): Node{
        return this.conceptIdNodeMap[String(uri)];
    }
    
    convertEdgeTypeLabelToEdgeClass(){
        
    }
    
    relationTypeCssClasses = {
            "is_a": "inheritanceLink",
            "part_of": "compositionLink",
            "maps to": "mappingLink",
    };
    relationLabelConstants = {
            "inheritance": "is_a",
            "composition": "part_of",
            "mapping": "maps to",
    };
    
    constructor(
        public graphView: GraphView.GraphView<Node, Link>,
        public centralConceptUri: ConceptURI,
        public softNodeCap: number
        ){
        
    }
    
    private getOntologyAcronymFromOntologyUrl(ontologyUri){
        var urlBeforeAcronym = "ontologies/";
        var urlAfterAcronym = "/";
        return ontologyUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym)+urlBeforeAcronym.length);
    }   
    
     //Needs the arguments index, concept because the function will be called in JQuery loop. Write wrappers in callers if you don't like that.
    public parseNode(index, conceptData){
            // Create the concept nodes that exist on the paths-to-root for the central concept,
            // including the central concept node.
            var conceptNode = new Node();
            conceptNode.rawConceptUri = <ConceptURI>conceptData["@id"];
            conceptNode.conceptUriForIds = Utils.escapeIdentifierForId(conceptData["@id"]);
            conceptNode.name = conceptData.prefLabel;
            conceptNode.type = conceptData["@type"];
            conceptNode.description = "fetching description";
            conceptNode.weight = 1;
            conceptNode.fixed = false;
            // conceptNode.x = this.graphView.visWidth()/2; // start in middle and let them fly outward
            // conceptNode.y = this.graphView.visHeight()/2; // start in middle and let them fly outward
            var ontologyUri = conceptData.links.ontology;
            // "http://data.bioontology.org/ontologies/<acronym>"
            var urlBeforeAcronym = "ontologies/";
            conceptNode.ontologyAcronym = ontologyUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym)+urlBeforeAcronym.length);
            conceptNode.ontologyUri = ontologyUri;
            conceptNode.ontologyUriForIds = encodeURIComponent(conceptNode.ontologyUri);
            conceptNode.nodeColor = this.nextNodeColor(conceptNode.ontologyAcronym);
            
            this.graphD3Format.nodes.push(conceptNode);
            this.addNodeToIdMap(conceptNode);
            // TODO Shall I reference the caller, or handle these in another way? How did I do similar stuff in ontology graph?
            // Could accumulate in caller?
            this.graphView.populateNewGraphElements(this.graphD3Format, true);
            
            // Understanding arcs:
            // Concept links come from different calls. We will probably need to use the links container
            // to collect all possible links that we know about, indexed by the concept that is not currently
            // included in our graph. When we get another concept added to the graph, we look it up in there,
            // add all the links to the graph, and remove the entries from the possible-links object.
            // This works only if we are able to add any given node prior to having to sort through its relations.
            // This also means that adding links has to be done in a separate process, and can't happen
            // in a smooth way when processing node information.
            // In Biomixer, these links were added as unrendered objects as they came up I think. We don't want
            // unrendered SVG in D3.
            // In any case, relations don't show up in the paths_to_root data anyway, so we need a separate process
            // because of that alone :)
            // We will need to inspect for relations in the registry, to see if there are any
            // implicit ones that have now been fulfilled by this node being added...is that correct to do here?
            // Registry should probably only have edges indexed by the *non-present* nodes, so that there is a simple
            // lookup for incoming nodes.
            // We also check for node endpoints in the graph before registering the implicit edges, so there's no risk of
            // adding an edge when it should instead be manifested in the graph.
            
            // If there are implicit edges from before that link from an existing node to this new one,
            // we can now manifest them.
            this.manifestEdgesForNewNode(conceptNode);
                        
            return conceptNode;
    }
    
    /**
     * Created for composition expansions, where we never have node data available. Can be used any time we need to expand a specific node,
     * and when we know the ontology of that node (such as when doing concept expansions).
     */
    public expandMappedConcept(newConceptId: ConceptURI, newConceptMappingData, relatedConceptId: ConceptURI, expansionType: PathOption){
        if(expansionType === PathOptionConstants.mappingsNeighborhoodConstant
            && this.expMan.isConceptWhitelistedForExpansion(relatedConceptId, PathOptionConstants.mappingsNeighborhoodConstant)
            && !(String(newConceptId) in this.conceptIdNodeMap)){
            var url = newConceptMappingData.links.self;
            var callback = new FetchOneConceptCallback(this, url, newConceptId, null);
            var fetcher = new Fetcher.RetryingJsonFetcher(url);
            fetcher.fetch(callback);
        }
    }
    
    /**
     * Created for composition expansions, where we never have node data available. Can be used any time we need to expand a specific node,
     * and when we know the ontology of that node (such as when doing concept expansions).
     */
    public expandRelatedConcept(conceptsOntology: RawAcronym, newConceptId: ConceptURI, relatedConceptId: ConceptURI, expansionType: PathOption){
        if(this.expMan.isConceptWhitelistedForExpansion(relatedConceptId, expansionType)
            && !(String(newConceptId) in this.conceptIdNodeMap)){
            
            var url = this.buildConceptUrlNewApi(conceptsOntology, newConceptId);
            var callback = new FetchOneConceptCallback(this, url, newConceptId, null);
            var fetcher = new Fetcher.RetryingJsonFetcher(url);
            fetcher.fetch(callback);
        }
    }
    
    public expandAndParseNodeIfNeeded(newConceptId: ConceptURI, relatedConceptId: ConceptURI, conceptPropertiesData, expansionType: PathOption){
        // Can determine on the basis of the relatedConceptId if we should request data for the
        // conceptId provided, or if we should parse provided conceptProperties (if any).
        // TODO PROBLEM What if the conceptId is already going to be fetched and processed because
        // it has a fetcher running on the basis of some other relation?
        // In paths to root, it won't happen, because we would only want to parse from the original call.
        // In mappings, we only expand mapped nodes in the original call.
        // In term neighbourhood, we do indeed parse nodes on the basis of parent and child
        // relations, as well as composition relations. But...only if they are related to the
        // central one. So simply checking for that combination of facts here works out fine.
        
        // For path to root, we only expand those path to root nodes.
        // For term neighbourhood, we only expand the direct neighbours of the central node.
        // For mappings, we only expand based on the first mapping call.
        // This will go through a whole process of adding the node, if the node is supposed to be
        // expanded for the current visualization (children and parents for term neighbourhood).
        
        // Because we expand for term neighbourhood relation calls, and those come in two flavors
        // (node with properties for children and parents, and just node IDs for compositions)
        // we want to support parsing the data directly as well as fetching additional data.
        if(this.expMan.isConceptWhitelistedForExpansion(relatedConceptId, expansionType)
            && !(String(newConceptId) in this.conceptIdNodeMap)){
            // Manifest the node; parse the properties if available.
            // We know that we will get the composition relations via a properties call,
            // and that has all the data we need from a separate call for properties...
            // but that subsystem relies on the fact that the node is created already.
            
            if(!(conceptPropertiesData === undefined) && Object.keys(conceptPropertiesData).length > 0
                && expansionType !== PathOptionConstants.mappingsNeighborhoodConstant){
                // Would process the node data available for mappings, but we need the "prefLabel" property,
                // which is not included therein, so we need a separate call rather than immediate parsing.
                // The delay in rendering seems to be negligable in practice.
                // Otherwise, we parse such as when it is a child or parent inheritance relation for term neighbourhood
                var conceptNode = this.parseNode(undefined, conceptPropertiesData);
                this.fetchConceptRelations(conceptNode, conceptPropertiesData);
            } else {
                console.log("Error: no data passed to expansion and parsing method");
            }
        }
    }
    
     /*
     * Parent and child arguments determine arrow direction. Relation type can 
     * reflect inheritance, composition, or mapping.
     * I *think* that every time we register one of these, we should check and see if
     * the endpoints are in the graph, and if so, manifest the edge right away.
     * Likewise, I think, we should check for edge inclusions every time a node is
     * manifested. Otherwise we end up with problems...if...data integrity is not perfect
     * in a given ontology (has_part and part_of are not symmetrically stated, even though
     * semantically they necessitate each other; if not symmetrically defined, we will only
     * find the relation when manifesting nodes in one order, unless we always look for
     * edges when manifesting nodes).
     */
    public manifestOrRegisterImplicitRelation(parentIdUri: ConceptURI, childIdUri: ConceptURI, relationType: string){
        if(parentIdUri === childIdUri){
            // Some mappings data is based off of having the same URI, which is mind boggling to me.
            // We have no use for self relations in this domain.
            return;
        }
        var parentId: string = String(parentIdUri);
        var childId: string = String(childIdUri);
        
        // Either register it as an implicit relation, or manifest it if both nodes are in graph.
        var edge = new Link();
        // edge source and targe tobjects will be set when manifesting the edge (when we know we have
        // node objects to add there). They are looked up by these ids.
        // TODO source/target and parent/child are not clear...which way do we need this to be?
        // I prefer using parent/child in model, but for the graph, arrow representation is clearer
        // using source and target.
        edge.sourceId = parentIdUri;
        edge.targetId = childIdUri;
        edge.rawId = edge.sourceId+"-to-"+edge.targetId+"-of-"+relationType;
        edge.relationType = relationType;
        edge.id = Utils.escapeIdentifierForId(edge.sourceId)+"-to-"+Utils.escapeIdentifierForId(edge.targetId)+"-of-"+relationType;
        edge.value = 1; // This gets used for link stroke thickness later...not needed for concepts?
        
        if(this.isEdgeForTemporaryRenderOnly(edge)){
            this.registerTemporaryRenderEdge(edge);
            return;
        }
        
        
        // We expect neither or just one of the ids will be in the registry, since we only register
        // node ids that do not exist in our graph. This should be enforced by processing edges
        // whenever we add a node to the graph.
        
        var matchIdInRegistry = undefined, otherIdInGraph = undefined;
        var parentIdInRegistry = false, childIdInRegistry = false;
        if(this.expMan.hasEdgeRegistryEntry(parentId, childId)){
            matchIdInRegistry = parentId;
            otherIdInGraph = childId;
            parentIdInRegistry = true;
        }
        if(this.expMan.hasEdgeRegistryEntry(childId, parentId)){
            if(matchIdInRegistry){
                // This can happen due to race conditions among relation calls. There's four, and ndoes are instantiated first...
                // The parent receive parents, then the child receives children, then the child receives parents and we are in this situation.
                // So if both match, what do we do? Ah, narrowed logic above to check for the other node beneath in the registry.
                console.log("Logical error; cannot have both edge ends in the graph already. The edge would have been added before if so.")
            }
            matchIdInRegistry = childId;
            otherIdInGraph = parentId;
            childIdInRegistry = true;
        }
        
        
        // Logic...begs for assertions.
        var parentIdInGraph = parentId in this.conceptIdNodeMap;
        var childIdInGraph = childId in this.conceptIdNodeMap;
        if((parentIdInGraph && childIdInGraph) && (parentIdInRegistry && childIdInRegistry)){
            console.log("Problem: Both ids are already in the graph, and both in the registry. Should we be here?");
        }
        if(matchIdInRegistry && !(parentIdInGraph || childIdInGraph)){
            console.log("Problem: If matchId is true, there must be at least one of the concepts in the graph already.");
        }
        if(!parentIdInGraph && !childIdInGraph){
            console.log("Problem: If neither node is in graph already.");
        }
        
        // Register edges for which we have one in the graph, and none in the registry.
        if(!matchIdInRegistry && (!parentIdInGraph != !childIdInGraph)){
            // Register this implicit edge.
            this.registerImplicitEdge(parentId, childId, edge);
        } else if(parentIdInGraph && childIdInGraph) {
            // If both are in the graph, we'll be manifesting it immediately.
            // Manifest this edge. We have a matching id in the registry, and the other end of the edge.
            this.manifestEdge(matchIdInRegistry, otherIdInGraph, edge);
        }
    }
    
    private manifestEdge(matchIdInRegistry: ConceptURI, otherIdInGraph: ConceptURI, edge: Link){
        if(this.isEdgeForTemporaryRenderOnly(edge)){
            console.log("Attempting to manifest edge that is meant for temporary rendering only:");
            console.log(edge);
            return;
        }
        
        edge.source = this.conceptIdNodeMap[String(edge.sourceId)];
        edge.target = this.conceptIdNodeMap[String(edge.targetId)];
        if(this.edgeNotInGraph(edge)){
            this.graphD3Format.links.push(edge);
            this.graphView.populateNewGraphEdges(this.graphD3Format.links);
            
        }
        
        if(matchIdInRegistry){
            this.expMan.clearEdgeFromRegistry(matchIdInRegistry, otherIdInGraph, edge);
        }
    }
    
    /**
     * Can be used when the edge does not have both endpoints in the graph, or when removing
     * edges that were added only temporarily.
     */
    private registerImplicitEdge(parentId: string, childId: string, edge: Link){
        if(this.isEdgeForTemporaryRenderOnly(edge)){
            console.log("Attempting to register as implicit an edge that is meant for temporary rendering only:");
            console.log(edge);
            return;
        }
        
        var conceptIdNotInGraph = (parentId in this.conceptIdNodeMap) ?  childId : parentId;
        var conceptInGraph = (parentId in this.conceptIdNodeMap) ?  parentId : childId;
        this.expMan.addEdgeToRegistry(conceptIdNotInGraph, conceptInGraph, edge);
    }
    
    public manifestEdgesForNewNode(conceptNode: Node){
        var conceptId = String(conceptNode.rawConceptUri);
        // Because registry contains edges for which there *was* no node for the index,
        // and there *are* nodes for the other ends of the edge, we can manifest all of
        /// them when we are doing so due to a new node appearing.
        if(this.expMan.hasEdgeRegistryEntry(conceptId, undefined)){
            $.each(this.expMan.getRegisteredEdgeTargetsFor(conceptId), (index, conceptsEdges)=>{
                $.each(conceptsEdges, (index, edge: Link)=>{
                    var otherId = (edge.sourceId == conceptNode.rawConceptUri) ? edge.targetId : edge.sourceId ;
        
                    edge.source = this.conceptIdNodeMap[String(edge.sourceId)];
                    edge.target = this.conceptIdNodeMap[String(edge.targetId)];
                    if(this.edgeNotInGraph(edge)){
                        this.graphD3Format.links.push(edge);
                        this.graphView.populateNewGraphEdges(this.graphD3Format.links);
                    }
                    
                    // Clear that one out...safe while in the loop?
                    this.expMan.clearEdgeFromRegistry(conceptNode.rawConceptUri, otherId, edge);
                })
            });
        }
    }
    
    isEdgeForTemporaryRenderOnly(edge: Link) : boolean{
        // For mapping edges, if neither endpoint has triggered a mapping expansion, we won't
        // want to render the edge all the time.
        
        if(edge.relationType === this.relationLabelConstants["mapping"]){
            if(this.expMan.isConceptWhitelistedForExpansion(edge.sourceId, PathOptionConstants.mappingsNeighborhoodConstant)
                || this.expMan.isConceptWhitelistedForExpansion(edge.targetId, PathOptionConstants.mappingsNeighborhoodConstant)
            ){
                // If one of the endpoints was expanded along mapping neighbourhood space, we will render the edge.
                return false;
            } else {
                return true;   
            }
        }
        
        return false;
    }
    
    registerTemporaryRenderEdge(edge: Link){
        this.expMan.addEdgeToTemporaryRenderRegistry(edge);
    }
    
    manifestTemporaryHoverEdges(conceptNode: Node){
        var edges: Link[] = this.expMan.getEdgesForTemporaryRendering(conceptNode.rawConceptUri, this.conceptIdNodeMap);
        $.merge(this.graphD3Format.links, edges);
        this.graphView.populateNewGraphEdges(this.graphD3Format.links);
    }
    
    removeTemporaryHoverEdges(conceptNode: Node){
        var edges: Link[] = this.expMan.getEdgesForTemporaryRendering(conceptNode.rawConceptUri, this.conceptIdNodeMap);
        // So...the nicest way to do this would be to slice the edges out. They were
        // pushed all at once, and it is an array, so we can do this. So, let's find
        // the start and end index. This feels a bit hairy to me, but let's go for it.
        var startIndex = this.graphD3Format.links.indexOf(edges[0]);
        var endIndex = this.graphD3Format.links.indexOf(edges[edges.length-1]);
        // this.graphD3Format.links = this.graphD3Format.links.slice(startIndex, endIndex+1);
        this.graphD3Format.links = $(this.graphD3Format.links).not(edges).toArray();
        this.graphView.removeMissingGraphElements(this.graphD3Format);
    }
    
    /**
     * This is important because children and parent calls can result in the same relations
     * being returned. I am not yet confident that we only need one of these calls though.
     * I am concerned that they may not always return equivalent results.
     * 
     * @param edge
     * @returns {Boolean}
     */
    private edgeNotInGraph(edge: Link){
        var length = this.graphD3Format.links.length;
        for(var i = 0; i < length; i++) {
            var item = this.graphD3Format.links[i];
            if(item.sourceId === edge.sourceId && item.targetId === edge.targetId && item.relationType === edge.relationType){
                return false;
            }
        }
        return true;
    }

    public fetchPathToRoot(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // I have confirmed that this is faster than BioMixer. Without removing
        // network latency in REST calls, it is approximately half as long from page load to
        // graph completion (on the order of 11 sec vs 22 sec)
        // Tried web workers, but D3 doesn't play well with that, and they aren't appropriate
        // for REST call handling.
        
        /* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
        1) Get the root to path for the central concept
           http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
           - create the nodes, and do any prep for subsequent REST calls
        2) Get relational data (children, parents and mappings) for all concepts in the path to root
           http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/parents/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
           - fill in nodes with details from this data TODO Look at Biomixer to see what we need 
        3) Get properties for all concepts in path to root
           http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/properties/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
           - set node properties
        */
        
        // 1) Get paths to root for the central concept
        // Technically, the path to root does *not* use the normal wildfire expansion technique,
        // since we can get the full et of nodes to expand directly from the path to root REST call.
        // This mean that we don't need to enter the root node (nor path nodes) into the expansion registry...
        this.expMan.addConceptIdToExpansionWhitelist(centralConceptUri, PathOptionConstants.pathsToRootConstant);
        var pathsToRootUrl = this.buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri);
        var pathsToRootCallback = new PathsToRootCallback(this, pathsToRootUrl, centralOntologyAcronym, centralConceptUri);
        var fetcher = new Fetcher.RetryingJsonFetcher(pathsToRootUrl);
        fetcher.fetch(pathsToRootCallback);
    }
    
    public expandConceptNeighbourhood(nodeData: Node){
        this.expMan.addConceptIdToExpansionWhitelist(nodeData.rawConceptUri, PathOptionConstants.termNeighborhoodConstant);
        var centralConceptUrl = this.buildConceptUrlNewApi(nodeData.ontologyAcronym, nodeData.rawConceptUri);
        var centralCallback = new FetchConceptRelationsCallback(this, centralConceptUrl, nodeData, PathOptionConstants.termNeighborhoodConstant);
        var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
        fetcher.fetch(centralCallback);
    }
    
    public expandMappingNeighbourhood(nodeData: Node){
        // Cannot just call fetchMappings() directly because we need the link from the base concept URL
        this.expMan.addConceptIdToExpansionWhitelist(nodeData.rawConceptUri, PathOptionConstants.mappingsNeighborhoodConstant);
        var centralConceptUrl = this.buildConceptUrlNewApi(nodeData.ontologyAcronym, nodeData.rawConceptUri);
        var centralCallback = new FetchConceptRelationsCallback(this, centralConceptUrl, nodeData, PathOptionConstants.mappingsNeighborhoodConstant);
        var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
        fetcher.fetch(centralCallback);
    }
    
    public fetchTermNeighborhood(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // 1) Get term neighbourhood for the central concept by fetching term and marking it for expansion
        // Parsers that follow will expand neighbourhing concepts.
        this.expMan.addConceptIdToExpansionWhitelist(centralConceptUri, PathOptionConstants.termNeighborhoodConstant);
        var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
        var centralCallback = new FetchOneConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.termNeighborhoodConstant);
        var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
        fetcher.fetch(centralCallback);
    }
    
    public fetchMappingsNeighborhood(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // Should I call the mapping, inferring the URL, or should I call for the central node, add it, and use conditional expansion in the relation parser?
        // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F410607006/mappings/?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P109.onSuccess
        
        // Get central concept immediately, and let the relation parser that will be called expand
        // related nodes conditioned on whether their related node is this to-be-expanded one.
        // Loading that node will get the mappings, and subsequently the concepts mapped to.
        // The mapping parser will fetch individual mapped concepts as it finds them by checking to see
        // if we are in the mapping visualization. I could make this explicit by copying the mappings code
        // here, but then we have duplicate code. If we decide it reads poorly to have it so detached
        // in the process, we can copy it here.
        this.expMan.addConceptIdToExpansionWhitelist(centralConceptUri, PathOptionConstants.mappingsNeighborhoodConstant);
        var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
        var centralCallback = new FetchOneConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.mappingsNeighborhoodConstant);
        var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
        fetcher.fetch(centralCallback);
    }
    
    public fetchConceptRelations(conceptNode: Node, conceptData, directCallForExpansionType?: PathOption){
        // 2) Get relational data for all the concepts, create links from them
        // fetchBatchRelations(); // don't exist, because of COR issues on server, cross domain, and spec issues.
        
        // Children requests have paging, which needs cycling internally.
        // If the PathOptions argument is compatible with any of the below methods, it wil auto-expand
        // the nodes therein.
        this.fetchChildren(conceptNode, conceptData.links.children, 1, directCallForExpansionType);
        this.fetchParents(conceptNode, conceptData.links.parents, directCallForExpansionType);
        this.fetchMappings(conceptNode, conceptData.links.mappings, directCallForExpansionType);
        this.fetchCompositionRelations(conceptNode, directCallForExpansionType);
    }
    
    fetchChildren(conceptNode: Node, relationsUrl: string, pageRequested: number, directCallForExpansionType: PathOption){
        // Children requests have paging, which needs cycling internally.
        relationsUrl = Utils.addOrUpdateUrlParameter(relationsUrl, "page", pageRequested+"");
        var conceptRelationsCallback = new ConceptChildrenRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType);
        var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
        fetcher.fetch(conceptRelationsCallback);
    }
    
    fetchParents(conceptNode: Node, relationsUrl: string, directCallForExpansionType: PathOption){
        var conceptRelationsCallback = new ConceptParentsRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType);
        var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
        fetcher.fetch(conceptRelationsCallback);
    }
    
    fetchMappings(conceptNode: Node, relationsUrl: string, directCallForExpansionType: PathOption){
        var conceptRelationsCallback = new ConceptMappingsRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType);
        var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
        fetcher.fetch(conceptRelationsCallback);
    }
    
     fetchCompositionRelations(conceptNode: Node, directCallForExpansionType: PathOption){
        var relationsUrl = this.buildConceptCompositionsRelationUrl(conceptNode);
        var conceptRelationsCallback = new ConceptCompositionRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType);
        var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
        fetcher.fetch(conceptRelationsCallback);
    }
    
    // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
    buildPathToRootUrlNewApi(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // String() converts object String back to primitive string. Go figure.
        return "http://"+Utils.getBioportalUrl()+"/ontologies/"+centralOntologyAcronym+"/classes/"+encodeURIComponent(String(centralConceptUri))+"/paths_to_root/";
    }
    
    // This is unused. See description. Leaving as documentation.
    buildTermNeighborhoodUrlNewApi(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // Term neighborhood requires the core concept call, then properties, mappings, children and parents (in no particular order).
        // Since those all need to be called for *any* node being loaded, this visualization mode relies upon cascading expansion as
        // relations are parsed. Thus, the URL for this call is really just a concept node URL. The subsquent functions
        // will check the visualization mode to decide whether they are expanding the fetched relations or not.
        // String() converts object String back to primitive string. Go figure.
        return this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
    }
    
    // This might be unused, because we may navigate to the mappings URL along the link data provided from the new API.
    buildMappingsNeighborhoodUrlNewApi(centralOntologyAcronym: RawAcronym, centralConceptUri: ConceptURI){
        // From the mappings results, we add all of the discovered nodes.
        // String() converts object String back to primitive string. Go figure.
        return "http://"+Utils.getBioportalUrl()+"/ontologies/"+centralOntologyAcronym+"/classes/"+encodeURIComponent(String(centralConceptUri))+"/mappings/";
    }
    
    buildConceptUrlNewApi(ontologyAcronym: RawAcronym, conceptUri: ConceptURI){
        // String() converts object String back to primitive string. Go figure.
        return "http://"+Utils.getBioportalUrl()+"/ontologies/"+ontologyAcronym+"/classes/"+encodeURIComponent(String(conceptUri));
    }
    
    buildConceptCompositionsRelationUrl(concept: Node){
        return "http://"+Utils.getBioportalUrl()+"/ontologies/"+concept.ontologyAcronym+"/classes/"+encodeURIComponent(String(concept.rawConceptUri))
        +"?include=properties";
    }
    
    //If we can use batch calls for the parent, child and mappings of each node, we save 2 REST calls per node.
    //If we can use batch calls for parent, child, and mapping for several nodes, we save a lot more, but the response
    //size and response times might be too long. We can use bulk asking for just one of the three relational data
    //properties.
    //Nodes also need a properties call each, which might be done in bulk.
    buildBatchRelationUrl(concept: Node){
        // Unused currently due to specification issues
        // 400-800 for children, properties each, 500-900 for parents, 500-900 for mappings
        // 500-1.2s for all four combined. Looks like savings to me.
        return "http://"+Utils.getBioportalUrl()+"/ontologies/"+concept.ontologyAcronym+"/classes/"+concept.conceptUriForIds
        +"?include=children,parents,mappings,properties";
    }
    
    buildBatchRelationUrlAndPostData(concepts: Array<Node>){
        // Given a set of concepts, create a batch API call to retrieve their parents, children and mappings
        // http://stagedata.bioontology.org/documentation#nav_batch
        var url = "http://"+Utils.getBioportalUrl()+"/batch/";
        // TEMP TEST
        // url = "http://stagedata.bioontology.org/batch?";
        var classCollection = [];
        var postObject: any = {
                "http://www.w3.org/2002/07/owl#Class": {
                    "collection": classCollection
                    },
                "include": "children, parents, mappings, properties",
                
        };
        $.each(concepts, function(i, d){
            classCollection.push({
                "class": d.id, // unescaped uri
                "ontology": d.ontologyUri, // unescaped uri
            });
        });
    //  console.log(postObject);
        
        // TEMP TEST
        postObject = {
                "http://www.w3.org/2002/07/owl#Class": {
                    "collection": [
                                   {
                                        "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Information_Resource",
                                        "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                                   },
                                   {
                                       "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Data_Resource",
                                       "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                                   },
                                   {
                                       "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Clinical_Care_Data",
                                       "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                                   },
                                   {
                                       "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Aggregate_Human_Data",
                                       "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                                   }
                                   ],
                "include": "prefLabel,synonym"   
                }
        };
        
        return {
                "url": url,
                "data": postObject,
                };
    }

     // Graph is responsible for its own node coloration...debate what this is: model attribute or view render?
    // In D3, the data model gets mingled with the view in this kind of way, so I feel this is ok.
    currentNodeColor: number = -1;
    nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
    ontologyColorMap = {};
    nextNodeColor(ontologyRawAcronym: RawAcronym){
        var ontologyAcronym: string = String(ontologyRawAcronym);
        if(!(ontologyAcronym in this.ontologyColorMap)){
            this.currentNodeColor = this.currentNodeColor == 19 ? 0 : this.currentNodeColor + 1;
            this.ontologyColorMap[ontologyAcronym] = this.nodeOrderedColors(this.currentNodeColor);
        }
        return this.ontologyColorMap[ontologyAcronym];
        
    }
    
    brightenColor(outerColor){
        // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
        // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
        return d3.lab(outerColor).brighter(1).toString();
    }
    
    darkenColor(outerColor){
        // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
        // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
        return d3.lab(outerColor).darker(1).toString();
    }
    
}

class PathsToRootCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph, // shadowing
        url: string,
        public centralOntologyAcronym: RawAcronym,
        public centralConceptUri: ConceptURI
        ){
            super(graph, url, String(centralOntologyAcronym)+":"+String(centralConceptUri));
        }
        
    public callback = (pathsToRootData: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

        var numberOfConcepts = Object.keys(pathsToRootData).length;
        
        $.each(pathsToRootData[0],
            (index, nodeData) => {
                var conceptNode = this.graph.parseNode(undefined, nodeData);
                this.graph.fetchConceptRelations(conceptNode, nodeData);
            }
        );
        
        this.graph.graphView.populateNewGraphElements(this.graph.graphD3Format, true);
    }
}

class FetchOneConceptCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public conceptUri: ConceptURI,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(conceptUri)); //+":"+directCallForExpansionType);
        }
        
    public callback = (conceptPropertiesData: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

        var conceptNode = this.graph.parseNode(undefined, conceptPropertiesData);

        // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
        this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, this.directCallForExpansionType);
    }
}

/**
 * Similar to FetchOneConcept, except for when we knwo we have the node already.
 */
class FetchConceptRelationsCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public node: Node,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(node.rawConceptUri)); //+":"+directCallForExpansionType);
        }
        
    public callback = (conceptPropertiesData: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
        // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
        this.graph.fetchConceptRelations(this.node, conceptPropertiesData, this.directCallForExpansionType);
    }
}
    
// currently oriented to grabbing data for a single concept. Might do batch later when that works server side
// for cross domain requests.
// Can process mapping, parent, properties, and children, even if not all are passed in.
// This is useful given that parents don't show up if children are requested.
class ConceptCompositionRelationsCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public conceptNode: Node,
        public conceptNodeIdMap: ConceptIdMap,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(conceptNode.rawConceptUri)); //+":"+directCallForExpansionType);
        }

    public callback = (relationsDataRaw: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

        // Loop over results, properties, then mappings, parents, children.
        $.each(relationsDataRaw.properties,
            (index, propertyObject)=>{
                // NB Composition relations can only be parsed from properties received with the "include=properties"
                // parameter. This means that although properties are received elsewhere (path to root, children),
                // those property sets never give us the composition relations. 
                // But...children property sets do have all the other things we need to get the seed of data for a node
                // (being the @id and the ontology link from which we need to extract the true-and-valid ontology acronym)
            
                // See line 71 TermWithoutRelationsJsonParser for how it was dealt with in Java.
                // We already parsed for other (automatic) properties when we first got this node's
                // data, so here we only do composite relations and maybe additional properties if needed.
                // This is properties such as: "http://purl.bioontology.org/ontology/SNOMEDCT/has_part"
                // I know, not the most general property name...
                if(Utils.endsWith(index, "has_part")){
                    $.each(propertyObject, (index, childPartId: ConceptURI)=>{
                        // TODO Need to register all node ids we get, so that for the different visualizations, we can expand differently.
                        // For path to root, we only expand those path to root nodes (determined at beginning)
                        // For term neighbourhood, we only expand the direct neighbours of the central node (determined during fetches).
                        // For mappings, we only expand based on the first mapping call (determined during fetches).
                        // Ergo, we need to expand composition mappings if we are in the term neighbourhood vis.
                        
                        // PROBLEM Seems like I want to manifest nodes before doing arcs, but in this case, I want to know
                        // if the relation exists so I can fetch the node data...
                        this.graph.manifestOrRegisterImplicitRelation(this.conceptNode.rawConceptUri, childPartId, this.graph.relationLabelConstants.composition);
                        this.graph.expandRelatedConcept(this.conceptNode.ontologyAcronym, childPartId, this.conceptNode.rawConceptUri, PathOptionConstants.termNeighborhoodConstant);
                    });
                }
                
                if(Utils.endsWith(index, "is_part")){
                    $.each(propertyObject, (index, parentPartId: ConceptURI)=>{
                        this.graph.manifestOrRegisterImplicitRelation(parentPartId, this.conceptNode.rawConceptUri, this.graph.relationLabelConstants.composition);
                        this.graph.expandRelatedConcept(this.conceptNode.ontologyAcronym,parentPartId, this.conceptNode.rawConceptUri, PathOptionConstants.termNeighborhoodConstant);
                    });
                }
                
            }
        );
    }
}
        
class ConceptChildrenRelationsCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public conceptNode: Node,
        public conceptIdNodeMap: ConceptIdMap,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(conceptNode.rawConceptUri)); //+":"+directCallForExpansionType);
        }
        
    public callback = (relationsDataRaw: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
        
        // Example: http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F91837002/children
        $.each(relationsDataRaw.collection,
                (index, child) => {
                // Was parsed in ConceptRelationshipJsonParser near line 75 (parseNewChildren)
                // We have a complication though...paged results! Oh great...
                // That alone is reason to fire these events separately anyway, but we can keep all the parsing stuck in this same
                // place and fire off an additional REST call.
                var childId = child["@id"];
                
                this.graph.expandAndParseNodeIfNeeded(childId, this.conceptNode.rawConceptUri, child, PathOptionConstants.termNeighborhoodConstant);
                this.graph.manifestOrRegisterImplicitRelation(this.conceptNode.rawConceptUri, childId, this.graph.relationLabelConstants.inheritance);
            }
        );
        
        // Children paging...only if children called directly?
         var pageNumber = relationsDataRaw["page"];
         var maxPageNumber = relationsDataRaw["pageCount"];
         if(maxPageNumber > pageNumber){
             this.graph.fetchChildren(this.conceptNode, this.url, pageNumber+1, this.directCallForExpansionType);
         }
    }
}


class ConceptParentsRelationsCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public conceptNode: Node,
        public conceptIdNodeMap: ConceptIdMap,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(conceptNode.rawConceptUri)); //+":"+directCallForExpansionType);
        }
        
    public callback = (relationsDataRaw: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
        
        $.each(relationsDataRaw,
                (index, parent) => {
                    var parentId = parent["@id"];
                    
                    // Save the data in case we expand to include this node
                    this.graph.expandAndParseNodeIfNeeded(parentId, this.conceptNode.rawConceptUri, parent, PathOptionConstants.termNeighborhoodConstant);
                    this.graph.manifestOrRegisterImplicitRelation(parentId, this.conceptNode.rawConceptUri, this.graph.relationLabelConstants.inheritance);
        });
    }
}
        
class ConceptMappingsRelationsCallback extends Fetcher.CallbackObject {
    
    constructor(
        public graph: ConceptGraph,
        url: string,
        public conceptNode: Node,
        public conceptNodeIdMap: ConceptIdMap,
        public directCallForExpansionType: PathOption
        ){
            super(graph, url, String(conceptNode.rawConceptUri)); //+":"+directCallForExpansionType);
        }
        
    public callback = (relationsDataRaw: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
        // We have to collect the mappings to prevent some infinite loops. They can appear multiple times.
        var mappingTargets = {};
        $.each(relationsDataRaw,
                (index, mapping)=>{
            var firstConceptId = mapping.classes[0]["@id"];
            var secondConceptId = mapping.classes[1]["@id"];
            
            // Check the ids, grab the one opposite the sourcing concept
            var newConceptData = undefined;
            if(this.conceptNode.rawConceptUri === firstConceptId){
                newConceptData = mapping.classes[1];
            }
            if(this.conceptNode.rawConceptUri === secondConceptId){
                newConceptData = mapping.classes[0];
            }
            
            var newConceptId = newConceptData["@id"];
            
            if(newConceptId === this.conceptNode.rawConceptUri){
                // This data error is not very helpful to see...
                // console.log("Error: mapping occurred without source as at both endpoints: "+firstConceptId+" and "+secondConceptId+" for call to "+this.url);   
            } else if(newConceptId === undefined || newConceptId === "" || newConceptId === null){
                console.log("Error: mapping occurred without source as an endpoint: "+firstConceptId+" and "+secondConceptId+" for source "+this.conceptNode.rawConceptUri+" for call to "+this.url);   
            }  else {
                // Catches self referential maps,
                mappingTargets[newConceptId] = newConceptData;
            }
        
        });
        
        $.each(mappingTargets, (newConceptId, newConceptData) => {
            // Sort endpoints to make mapping edges singular. Otherwise we get an edge going each way.
            var firstId = newConceptId > this.conceptNode.rawConceptUri ? newConceptId : this.conceptNode.rawConceptUri ;
            var secondId = newConceptId > this.conceptNode.rawConceptUri ? this.conceptNode.rawConceptUri : newConceptId ;
            this.graph.manifestOrRegisterImplicitRelation(firstId, secondId, this.graph.relationLabelConstants.mapping);
            this.graph.expandMappedConcept(newConceptId, newConceptData, this.conceptNode.rawConceptUri, this.directCallForExpansionType);
            });
    }
}