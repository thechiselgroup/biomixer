///<reference path="headers/require.d.ts" />

///<amd-dependency path="Utils" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="GraphView" />

import Utils = require('../Utils');
import Fetcher = require('../FetchFromApi');
import GraphView = require('../GraphView');

// Apparently all modules in the same level directory can see eachother? I deleted the imports
// above and I could still access everything!
    
// Use these caps with a sorted array of nodes
var hardNodeCap: number = 0; // 10 and 60 are nice number for dev, but set to 0 for all nodes.

export interface RawAcronym extends String {
    // Only assign the original unadulterated acronym strings to this
}

export interface AcronymForIds extends String {
    // Assign id-escaped acronyms here. These are made safe for use in HTML and SVG ids.
    
//    escapeAcronym(acronym: RawAcronym){
//        //  return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
//        // JQuery selectors do not work with things that need escaping.
//        // Let's use double underscores instead.
//        return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '__');
//    }
}

export class Node extends GraphView.BaseNode {
    name: string; // D3 // Comes from ontology details REST call // = "fetching"+" ("+centralOntologyAcronym+")";
    description: string; // Comes from description RETS call // = "fetching description";
    x: number; // = visWidth()/2;
    y: number; // = visHeight()/2;      
    weight: number; // D3 // = numberOfMappedOntologies; // will increment as we loop
    number: number; // = defaultNumOfTermsForSize; // number of terms
    acronymForIds; AcronymForIds; // = escapeAcronym(centralOntologyAcronym);
    rawAcronym; RawAcronym; // = centralOntologyAcronym;
    nodeColor: string; // = nextNodeColor();
    innerNodeColor: string; // = brightenColor(centralOntologyNode.nodeColor);
    nodeStrokeColor: string; // = darkenColor(centralOntologyNode.nodeColor);
    mapped_classes_to_central_node: number; // = 0;
    
    uriId: string; // = ontologyDetails["@id"]; // Use the URI instead of virtual id
    LABEL: string; // = ontologyDetails.name;
    
    // These come from details REST call
    numberOfClasses: number; // = numClasses;
    numberOfIndividuals: number; // = numIndividuals;
    numberOfProperties: number; // = numProperties;
    
    constructor(){
        super();
    }
}

export class Link extends GraphView.BaseLink {
    source: Node; // = centralOntologyNode;
    target: Node; // = ontologyNode;
    value: number; // = mappingCount; // This gets used for link stroke thickness later.
    numMappings: number; // = mappingCount;
        
    constructor(){
        super();
    }
}
    
export interface AcronymNodePair {
    acronym: string;
    node: Node;
}
    
export interface OntologyAcronymMap {
    // $(ontologyAcronymNodeMap).attr("vid:"+centralOntologyNode.rawAcronym, centralOntologyNode);
    [acronym: string]: Node;
}
    
export class OntologyD3Data extends GraphView.GraphDataForD3<Node, Link> {
    
}
    


function escapeAcronym(acronym){
    //  return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
    // JQuery selectors do not work with things that need escaping.
    // Let's use double underscores instead.
    return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '__');
}



export class OntologyGraph implements GraphView.Graph {
    // Need:
    // sortedAcronymsByMappingCount
    // hardNodeCap
    // softNodeCap
    // a view object to get necessary information from the outside through
    
    ontologyNeighbourhoodJsonForGraph: OntologyD3Data = new OntologyD3Data();
    
    // Stores {acronyms,node} sorted by mapping count in descending order.
    // Limit it with hardNodeCap during init in dev only.
    // Slice it with softNodeCap during init.
    public sortedAcronymsByMappingCount: Array<AcronymNodePair> = [];

    // This softNodeCap only affects API dispatch and rendering for nodes past the cap. It is used during
    // initialization only. Set to 0 means all nodes will be used.
    constructor(
        public graphView: GraphView.GraphView<Node, Link>,
        public softNodeCap: number,
        public centralOntologyAcronym: string
    ){
     
    }
    
    fetchOntologyNeighbourhood(centralOntologyAcronym){
    	// I have confirmed that this is faster than BioMixer. Without removing
    	// network latency in REST calls, it is approximately half as long from page load to
    	// graph completion (on the order of 11 sec vs 22 sec)
    	// TODO XXX Then try adding web workers around things to see if it affects it further.
    	
    	// TODO XXX I lose all the error handling and retry handling that I set up in BioMixer.
    	// This is our first loss, that we have to futz with that again. It can be recreated, or if this
    	// is fast enough, we can adapt things so that some of the Java work in BioMixer can be used here too
    	// I mostly need to bypass the overall architecture of BioMixer to see how it affects loading speed
    	// and responsivity, as well as to try using web workers (which don't work with GWT 2.5 right now)
    	
    	/* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
    	1) Get the mapped ontology ids from the target ontology id [starts at line 126 in OntologyMappingNeighbourhood]
    	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F1033&callback=__gwt_jsonp__.P0.onSuccess
    	   - can create nodes and links with sparse meta-data now if we want, or we can wait for more data
    	2) Get ontology details, which is one big json return [passed to line 167 for class OntologyMappingNeighbourhoodLoader nested class OntologyDetailsCallback]
    	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F&callback=__gwt_jsonp__.P1.onSuccess
    	   - fill in nodes with details from this data
    	3) Get ontology metrics for each ontology [line 82 in AutomaticOntologyExpander]
    	   - set node size (# of concepts), and tool tip properties of classes, individuals, properties, and notes
    	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F45254&callback=__gwt_jsonp__.P7.onSuccess
    	*/
    	
    	// 1) Get mappings to central ontology
    	var ontologyMappingUrl = buildOntologyMappingUrlNewApi(centralOntologyAcronym);
    	var ontologyMappingCallback = new OntologyMappingCallback(this, ontologyMappingUrl, centralOntologyAcronym);
    	var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMappingCallback);
    	fetcher.fetch();
    }
    
    fetchNodeRestData(node: Node){
        this.fetchMetricsAndDescriptionFunc(node);
    }
    
      /**
     * The functions attached to the nodes in here allow us to call per-node APIs as needed, rather than
     * all at once.
     * 
     * When these functions are called, all dispatching and processing should happen without further consideration
     * from the caller. The function should return true if the call has been dispatched.
     * 
     * Returns true if the dispatch was made...and if there was an error or other issue, it will not return true.
     * 
     * Once called, the functions in here should replace themselves on the owning node with a function that returns true.
     * 
     * @param node
     */
    private fetchMetricsAndDescriptionFunc(node: Node){
        // Check registry for node status
        var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.rawAcronym);
        if(Fetcher.Registry.checkUrlInRestCallWhiteList(ontologyMetricsUrl)){
            // Combined dispatch for the separate calls for metrics and descriptions.
            // The metric call has much of the info we need
            var ontologyMetricsCallback = new OntologyMetricsCallback(this, ontologyMetricsUrl, node);
            // var fetcher = new RetryingJsonpFetcher(ontologyMetricsCallback);
            // fetcher.retryFetch();
            var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMetricsCallback);
            fetcher.fetch();
        }
    
        var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.rawAcronym);
        if(Fetcher.Registry.checkUrlInRestCallWhiteList(ontologyDescriptionUrl)){
            // If we want Description, I think we need to grab the most recent submission
            // and take it fromt here. This is another API call per ontology.
            // /ontologies/:acronym:/lastest_submission
            // Descriptions are in the submissions, so we need an additional call.
            var ontologyDescriptionCallback = new OntologyDescriptionCallback(this, ontologyDescriptionUrl, node);
            var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDescriptionCallback);
            fetcher.fetch();
        }
        
        return true;
    }
    
    /**
     * Provide ontology acronyms that should be kept in the graph whiel the rest are removed.
     * Removes both nodes and links.
     * Revise as necessary if a latent-link approach is used later.
     * 
     * @param json
     */
    public cropGraphToSubset(acronymsToKeep){
        //$.each(acronymsToKeep, function(index, node){console.log("Cropping down to: "+node)});
        
        // $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("Before removal: "+node.rawAcronym)});
        // $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("Before removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
        
        this.ontologyNeighbourhoodJsonForGraph.nodes
        = $.grep(
                this.ontologyNeighbourhoodJsonForGraph.nodes,
                function(value) {
                  return $.inArray(value.rawAcronym, acronymsToKeep) != -1;
                }
        );
        
        this.ontologyNeighbourhoodJsonForGraph.links
        = $.grep(
                this.ontologyNeighbourhoodJsonForGraph.links,
                function(value) {
                  return $.inArray(value.source.rawAcronym, acronymsToKeep) != -1
                  && $.inArray(value.target.rawAcronym, acronymsToKeep) != -1;
                }
        );
        
        // $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("After removal: "+node.rawAcronym)});
        // $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("After removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
        
        this.graphView.removeGraphPopulation();
    }
    
    // Graph is responsible for its own node coloration...debate what this is: model attribute or view render?
    // In D3, the data model gets mingled with the view in this kind of way, so I feel this is ok.
    currentNodeColor: number = -1;
    nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
    nextNodeColor(){
        this.currentNodeColor = this.currentNodeColor == 19 ? 0 : this.currentNodeColor + 1;
        return this.nodeOrderedColors(this.currentNodeColor);
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
    
// Doesn't need REST call registry, so if I refactor, keep that in mind.
class OntologyMappingCallback implements Fetcher.CallbackObject {

    // Define this fetcher when one is instantiated (circular dependency)
    fetcher: Fetcher.RetryingJsonFetcher;
    
    constructor(
        public graph: OntologyGraph,
        public url: string,
        public centralOntologyAcronym: string
        ){
    }

    // Need fat arrow definition rather than regular type, so that we can get lexical scoping of
    // "this" to refer to the class instance (a lamda by use of closure, I think) rather than whatever Javascript binds "this" to
    // when the callback is executed.
    // As a further complication, when we make anonymous functions within this method, our references to "this" get re-scoped
    // again. In order to cope with those, we need to use fat arrow => again!
    // If we are passing such a function to D3, and need "this" to refer to the element that D3 is operating
    // on, then we stay with fucntion() syntax, and make a new variable pointing to the object-instance "this",
    // and allow the function() to closure onto that variable. Did you catch all that?
    // For this case, the caller has no "this" of interest to us, so fat arrow works.
	public callback = (mappingData: any, textStatus: string, jqXHR: any) => {
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

//		var errorOrRetry = self.fetcher.retryFetch(mappingData);
		var errorOrRetry = this.fetcher.fetch(mappingData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		// Sort the arcs and nodes so that we make calls on the ones with highest mappings first
		$.each(mappingData, (index, element)=>{
			// Hard cap on nodes included. Great for dev purposes.
			this.graph.sortedAcronymsByMappingCount.push({acronym: index, node: undefined});
			}
		);
		this.graph.sortedAcronymsByMappingCount.sort(function(a,b){return mappingData[b.acronym]-mappingData[a.acronym]});
		
		// Reduce to a useful number of nodes.
		if(hardNodeCap != 0 && this.graph.sortedAcronymsByMappingCount.length > hardNodeCap){
			this.graph.sortedAcronymsByMappingCount = this.graph.sortedAcronymsByMappingCount.slice(0, hardNodeCap);
		} 

		// Base this off of the possibly-filtered list.
		var numberOfMappedOntologies = this.graph.sortedAcronymsByMappingCount.length;
		// And base the total off of the original list
		var originalNumberOfMappedOntologies = Object.keys(mappingData).length;
		
		var defaultNumOfTermsForSize = 10;
		
		// New API example: http://data.bioontology.org/mappings/statistics/ontologies/SNOMEDCT/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a

        var centralOntologyAcronym: string = this.graph.centralOntologyAcronym;
        
		// Create the central node
		var centralOntologyNode: Node = new Node();
		centralOntologyNode.name = "fetching"+" ("+this.centralOntologyAcronym+")";
		centralOntologyNode.description = "fetching description";
		centralOntologyNode.fixed = true; // lock central node
		centralOntologyNode.x = this.graph.graphView.visWidth()/2;
		centralOntologyNode.y = this.graph.graphView.visHeight()/2;		
		centralOntologyNode.weight = numberOfMappedOntologies; // will increment as we loop
		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
		centralOntologyNode.acronymForIds = escapeAcronym(this.centralOntologyAcronym);
		centralOntologyNode.rawAcronym = this.centralOntologyAcronym;
		centralOntologyNode.nodeColor = this.graph.nextNodeColor();
		centralOntologyNode.innerNodeColor = this.graph.brightenColor(centralOntologyNode.nodeColor);
		centralOntologyNode.nodeStrokeColor = this.graph.darkenColor(centralOntologyNode.nodeColor);
		centralOntologyNode.mapped_classes_to_central_node = 0;
		this.graph.ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		// Lame loop to find the central node in our sorted set
		$.each(this.graph.sortedAcronymsByMappingCount,
				function(index, sortedAcronym){
					if(sortedAcronym.acronym == centralOntologyNode.rawAcronym){
						sortedAcronym.node = centralOntologyNode
					}
				}
		);
		
		var ontologyAcronymNodeMap: OntologyAcronymMap = {};
//		$(ontologyAcronymNodeMap).attr("vid:"+centralOntologyNode.rawAcronym, centralOntologyNode);
        ontologyAcronymNodeMap["vid:"+centralOntologyNode.rawAcronym] = centralOntologyNode;
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		// Original bug hidden by force layout, but I needed radians not degrees.
		// It looks very slightly different.
		var anglePerNode =2*Math.PI / numberOfMappedOntologies; // 360/numberOfMappedOntologies;
		var arcLength = this.graph.graphView.linkMaxDesiredLength();
		var i = 0;
		// Used to iterate over raw mappingData, but I wanted things loaded and API calls made in order
		// of mapping counts.
		$.each(this.graph.sortedAcronymsByMappingCount,
			(index, sortedAcronym)=>{
				var acronym = sortedAcronym.acronym;
				var mappingCount = mappingData[acronym];

				if(typeof acronym === "undefined"){
					console.log("Undefined ontology entry");
				}
				
				// Create the neighbouring nodes
				var ontologyNode: Node = new Node();
				ontologyNode.name = "fetching"+" ("+acronym+")";
				ontologyNode.description = "fetching description";
				ontologyNode.weight = 1;
				ontologyNode.fixed = false; // lock central node
				// Compute starting positions to be in a circle for faster layout
				var angleForNode = i * anglePerNode; i++;
				ontologyNode.x = this.graph.graphView.visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
				ontologyNode.y = this.graph.graphView.visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
				ontologyNode.number = defaultNumOfTermsForSize; // number of terms
				ontologyNode.acronymForIds = escapeAcronym(acronym);
				ontologyNode.rawAcronym = acronym;
				ontologyNode.nodeColor = this.graph.nextNodeColor();
				ontologyNode.innerNodeColor = this.graph.brightenColor(ontologyNode.nodeColor);
				ontologyNode.nodeStrokeColor = this.graph.darkenColor(ontologyNode.nodeColor);
				ontologyNode.mapped_classes_to_central_node = 0;
				var targetIndex = this.graph.ontologyNeighbourhoodJsonForGraph.nodes.push(ontologyNode) - 1;
				// TODO I feel like JS doesn't allow references like this...
//				$(ontologyAcronymNodeMap).attr("vid:"+ontologyNode.rawAcronym, ontologyNode);
                ontologyAcronymNodeMap["vid:"+ontologyNode.rawAcronym] = ontologyNode;
				sortedAcronym.node = ontologyNode;
				
				// Make the links at the same time; they are done now!
				var ontologyLink: Link = new Link();
				ontologyLink.source = centralOntologyNode;
				ontologyLink.target = ontologyNode;
				ontologyLink.value = mappingCount; // This gets used for link stroke thickness later.
				ontologyLink.numMappings = mappingCount;
				this.graph.ontologyNeighbourhoodJsonForGraph.links.push(ontologyLink);
				
				// Get the node the data it needs from the link
				ontologyNode.mapped_classes_to_central_node = ontologyLink.value;
	
			}
		);
		
		// Make calls on all nodes we want to show when the graph first loads up
		// Well, we could, but there are lots of ontologies that do not have metric or details accessible to us,
		// and we don't know these until later. If we do this now, it disrupts the visualization.
		//		$.each(sortedAcronymsByMappingCount, function(index, rawAcronym){
		//			// fetch the node, make the individual calls
		//			var node = $(ontologyAcronymNodeMap).attr("vid:"+rawAcronym);
		//			node.fetchMetricsAndDescriptionFunc();
		//		})
		
		// Not sure about whether to do this here or not...
		// console.log("ontologyMappingCallback");
		this.graph.graphView.populateGraph(this.graph.ontologyNeighbourhoodJsonForGraph, true);
		
		// Once we have the graph populated, we have this one node we know we can call REST calls for, the central node!
		// The other nodes need to wait, since when we get the details call back later, we will see that many
		// of them are inaccessible. Making calls for those ones is a waste of time.
		// The easiest way to benchmark is to do *all* nodes right above in the loop
		// Get to "Processing details" log entry in 45 seconds when all are allowed right away, versus 1 second when
		// we only get the first one, and let the filter trigger the rest. Labels and node sizes are subsequently
		// quicker to appear as well.
//		addNodeToRestCallRegistry(centralOntologyNode, buildOntologyMetricsUrlNewApi(centralOntologyNode.rawAcronym));
//		addNodeToRestCallRegistry(centralOntologyNode, buildOntologyLatestSubmissionUrlNewApi(centralOntologyNode.rawAcronym));
        Fetcher.Registry.addUrlToRestCallRegistry(buildOntologyMetricsUrlNewApi(centralOntologyNode.rawAcronym));
        Fetcher.Registry.addUrlToRestCallRegistry(buildOntologyLatestSubmissionUrlNewApi(centralOntologyNode.rawAcronym));
		this.graph.fetchNodeRestData(centralOntologyNode);

		//----------------------------------------------------------

		// 2) Get details for all the ontologies (and either create or update the nodes)
		var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
		var ontologyDetailsCallback = new OntologyDetailsCallback(this.graph, ontologyDetailsUrl, ontologyAcronymNodeMap);
//		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
//		fetcher.retryFetch();
		var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDetailsCallback);
		fetcher.fetch();
	}
	
}
    


   
//Doesn't need REST call registry, so if I refactor, keep that in mind.
class OntologyDetailsCallback implements Fetcher.CallbackObject {

    fetcher: Fetcher.RetryingJsonFetcher;
    
    constructor(
        public graph: OntologyGraph,
        public url: string,
        public ontologyAcronymNodeMap: OntologyAcronymMap
        ){
    }
    
    // Caller of callback has no "this" of interest, so fat arrow works
    callback = (detailsDataRaw: any, textStatus: string, jqXHR: any) => {
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

//		var errorOrRetry = self.fetcher.retryFetch(detailsDataRaw);
		var errorOrRetry = this.fetcher.fetch(detailsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		console.log("Processing details "+Utils.getTime());
		
		// Loop over ontologies and add their additional properties to the nodes
		// Recall that getting *all* ontology details is the easiest (only) way,
		// so we have to skip anything that is not defined.
		var ontologiesSkipped = 0;
		var acronymsNotSkipped = [];
		$.each(detailsDataRaw,
				(index, ontologyDetails)=>{
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var ontologyAcronym = ontologyDetails.acronym;
					// var node = ontologyNeighbourhoodJsonForGraph.;
//					var node = $(self.ontologyAcronymNodeMap).attr("vid:"+ontologyAcronym);
                    var node = this.ontologyAcronymNodeMap["vid:"+ontologyAcronym];
					
					if(typeof node === "undefined"){
						// Skip node details that aren't in our graph
						ontologiesSkipped += 1;
						return;
					}
					
					acronymsNotSkipped.push(ontologyAcronym);
					
					node.name = ontologyDetails.name;
//					node.ONTOLOGY_VERSION_ID = ontologyDetails.id;
					node.uriId = ontologyDetails["@id"]; // Use the URI instead of virtual id
					node.LABEL = ontologyDetails.name;
					// node.description = ontologyDetails.description; // Unavailable in details call
//					node.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions; // might be missing
					
					// I'm moving this all to on-demand (probably via the filter).
					// node.fetchMetricsAndDescriptionFunc();
					
					Fetcher.Registry.addUrlToRestCallRegistry(buildOntologyMetricsUrlNewApi(node.rawAcronym));
					Fetcher.Registry.addUrlToRestCallRegistry(buildOntologyLatestSubmissionUrlNewApi(node.rawAcronym));
				}
		);
		
		console.log("Cropping "+Utils.getTime());
		
		// We have to remove all nodes and edges which did not appear in details.
		this.graph.cropGraphToSubset(acronymsNotSkipped);
		
		this.graph.graphView.filterGraphOnMappingCounts();

		// We usually use very many of the ontologies, so it is likely cheaper to make the one
		// big call with no ontology acronym arguments than to cherry pick the ones we want details for.
		console.log("ontologyDetailsCallback, skipped "+ontologiesSkipped+" of total "+detailsDataRaw.length+" "+Utils.getTime());
		this.graph.graphView.updateDataForNodesAndLinks({nodes:this.graph.ontologyNeighbourhoodJsonForGraph.nodes, links:[]});
	}
}
    
    
    
class OntologyMetricsCallback implements Fetcher.CallbackObject {

    fetcher: Fetcher.RetryingJsonFetcher;
    
    constructor(
        public graph: OntologyGraph,
        public url: string,
        public node: Node 
        ){
    }

    // Caller of callback has no "this" of interest, so fat arrow works
    callback = (metricDataRaw: any, textStatus: string, jqXHR: any) => {
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
		
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	this.fetcher.fetch(metricDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var metricData = metricDataRaw;
		
		var nodeSizeBasis = 100;
		var numClasses=0, numIndividuals=0, numProperties=0;
	    if (typeof metricData !== "undefined") {
	        if (metricData.classes != null) {
	            numClasses = metricData.classes;
	            nodeSizeBasis = numClasses;
	        }
	        if (metricData.individuals != null) {
	            numIndividuals = metricData.individuals;
	        }
	        if (metricData.properties != null) {
	            numProperties = metricData.properties;
	        }
	    }
	    
	    // self.node.weight = 1; // Changing weight re-triggers layout shifting, turns into a swirling mess.
		this.node.numberOfClasses = numClasses;
		this.node.numberOfIndividuals = numIndividuals;
		this.node.numberOfProperties = numProperties;
		this.node.number = nodeSizeBasis;
		
		// console.log("ontologyMetricsCallback");
		this.graph.graphView.updateDataForNodesAndLinks({nodes:[this.node], links:[]});
		// filterGraph();
	}
}
    
    
class OntologyDescriptionCallback implements Fetcher.CallbackObject {

    fetcher: Fetcher.RetryingJsonFetcher;
    
    constructor(
        public graph: OntologyGraph,
        public url: string,
        public node: Node 
        ){
    }
    
    // Caller of callback has no "this" of interest, so fat arrow works
    callback = (latestSubmissionData: any, textStatus: string, jqXHR: any) => {
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
		
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	this.fetcher.fetch(latestSubmissionData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var description="";
	    if (typeof latestSubmissionData !== "undefined") {
	        if (latestSubmissionData.description != null) {
	            description = latestSubmissionData.description;
	        } else if(typeof latestSubmissionData.error != null){
	        	description = latestSubmissionData.error;
	        }
	    }
	    
		this.node.description = description;
		
		// console.log("ontologyDescriptionCallback");
		this.graph.graphView.updateDataForNodesAndLinks({nodes:[this.node], links:[]});
	}
}
    
    
function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
	return "http://data.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

function buildOntologyDetailsUrlNewApi(){
	return "http://data.bioontology.org/ontologies"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

function buildOntologyMetricsUrlNewApi(ontologyAcronym){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

