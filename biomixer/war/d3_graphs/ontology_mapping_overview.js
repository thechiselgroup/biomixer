// http://www.jslint.com/
// http://www.jshint.com/, also available as Eclipse or Sublime plugin
// Strict mode is safer to develop in, ya?
"use strict";

// This is using the new API that is stable in September 2013.

// I eventually came across the post that sort of discusses our update problem, of
// having new attributes for nodes from late coming JSON:
// https://groups.google.com/forum/#!msg/d3-js/ENMlOyUGGjk/YiPc8AUKCOwJ
// http://grokbase.com/t/gg/d3-js/12cjmqc2cx/dynamically-updating-nodes-links-in-a-force-layout-diagram
// Bostock confirms that we shouldn't bind things that aren't truly new, and instead we must
// update element properties without binding.

// Some ontologies now have bad names with dots in them. May need to change out id matching with:
// '[id="node_g_'+centralOntologyAcronym+'"]'

// Use these caps with a sorted array of nodes
var hardNodeCap = 0; // 10 and 60 are nice number for dev, but set to 0 for all nodes.

// This cap only affects API dispatch and rendering for nodes past the cap. It is used during
// initialization only. Set to 0 means all nodes will be used.
var softNodeCap = 0; 

// Stores acronyms sorted by mapping count in descending order.
// Limit it with hardNodeCap during init in dev only.
// Slice it with softNodeCap during init.
var sortedAcronymsByMappingCount= [];

//Keep track of node mapping values in order, so we can filter through them in ranks
// This container is separate from the array of acronyms sorted by mappign counts...shall
// they be combined?
var sortedLinksByMapping = [];

function visWidth(){ return $("#chart").width(); }
function visHeight(){ return $("#chart").height(); }
function linkMaxDesiredLength(){ return Math.min(visWidth(), visHeight())/2 - 50; }
var alphaCutoff = 0.01; // used to stop the layout early in the tick() callback
var forceLayout = undefined;
var centralOntologyAcronym = purl().param("ontology_acronym");
var dragging = false;
var ontologyTick; // needs to contain the onTick listener function

// These are needed to do a refresh of popups when new data arrives and the user has the popup open
var lastDisplayedTipsy = null, lastDisplayedTipsyData = null, lastDisplayedTipsyCircle = null;

//var defaultNodeColor = "#496BB0";
var defaultNodeColor = "#000000";
var defaultLinkColor = "#999";
var nodeHighlightColor = "#FC6854";

function getTime(){
	var now = new Date();
	return now.getMinutes()+':'+now.getSeconds();
}


// Had to set div#chart.gallery height = 100% in CSS,
// but this was only required in Firefox. I can't see why.
prepGraphMenu();
var vis = d3.select("#chart").append("svg:svg")
	.attr("id", "graphSvg")
	.attr("width", visWidth())
	.attr("height", visHeight())
	.attr("pointer-events", "all")
//  .append('svg:g')
    .call(d3.behavior.zoom().on("zoom", redraw))
//  .append('svg:g')
  ;



vis.append('svg:rect')
	.attr("width", visWidth())
	.attr("height", visHeight())
	.attr("id", "graphRect")
    .style('fill', 'white');

var resizedWindow = function()
{		
	d3.select("#graphRect")
	.attr("width", visWidth())
	.attr("height", visHeight());
	
	d3.select("#graphSvg")
	.attr("width", visWidth())
	.attr("height", visHeight());
	
    if(forceLayout){
    	forceLayout.size([visWidth(), visHeight()]).linkDistance(linkMaxDesiredLength());
    	// Put central node in middle of view
    	d3.select("#node_g_"+centralOntologyAcronym).each(function(d){d.px = visWidth()/2; d.py = visHeight()/2;});
    	forceLayout.resume();
    }  
};

$(window).resize(resizedWindow);

resizedWindow();

function redraw() {
//  console.log("redrawing D3", d3.event.translate, d3.event.scale);
//  vis.attr("transform",
//      "translate(" + d3.event.translate + ")"
//      + " scale(" + d3.event.scale + ")");
}

//Seeing if I can modulate graph gravity using bounding boxes...
// when the nodes are outside the box, tweak the gravity higher by a small amount,
// and decrease it when the nodes are further from the edge
// This is happening for each node as it updates, so keep that in mind...
var minGravity = 0.1;
var maxGravity = 3.5;
function gravityAdjust(number, visSize){
	var alpha = 0.2 / forceLayout.nodes().length;
	if(number < visSize*0.05 || visSize*0.95 < number){
		// console.log("increase");
		forceLayout.gravity(Math.min(maxGravity, forceLayout.gravity() * (1 + alpha)));
	} else if(visSize*0.20 < number && number < visSize*0.80){
		// console.log("decrease");
		forceLayout.gravity(Math.max(minGravity, forceLayout.gravity() * (1 - alpha)));
	} else {
		// leave gravity as it is
	}
	return number;
}
function gravityAdjustX(number){
	return gravityAdjust(number, visWidth());
}
function gravityAdjustY(number){
	return gravityAdjust(number, visHeight());
}


var ontologyNeighbourhoodJsonForGraph = new Object();
ontologyNeighbourhoodJsonForGraph.nodes = [];
ontologyNeighbourhoodJsonForGraph.links = [];


// Run the graph! Don't need the json really, though...
// d3.json("force_files/set_data.json", initAndPopulateGraph);
initAndPopulateGraph();


function fetchOntologyNeighbourhood(centralOntologyAcronym){
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
	var ontologyMappingCallback = new OntologyMappingCallback(ontologyMappingUrl, centralOntologyAcronym);
//	var fetcher = new RetryingJsonpFetcher(ontologyMappingCallback);
//	fetcher.retryFetch();
	var fetcher = closureRetryingJsonpFetcher(ontologyMappingCallback);
	fetcher();
}

function escapeAcronym(acronym){
	return acronym.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
}

function OntologyMappingCallback(url, centralOntologyAcronym){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (mappingData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

//		var errorOrRetry = self.fetcher.retryFetch(mappingData);
		var errorOrRetry = self.fetcher(mappingData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		
		// Sort the arcs and nodes so that we make calls on the ones with highest mappings first
		$.each(mappingData, function(index, element){
			// Hard cap on nodes included. Great for dev purposes.
			sortedAcronymsByMappingCount.push(index);
			}
		);
		sortedAcronymsByMappingCount.sort(function(a,b){return mappingData[b]-mappingData[a]});
		 
		// Reduce to a useful number of nodes.
		if(hardNodeCap != 0 && sortedAcronymsByMappingCount.length > hardNodeCap){
			sortedAcronymsByMappingCount = sortedAcronymsByMappingCount.slice(0, hardNodeCap);
		} 

		// Base this off of the possibly-filtered list.
		var numberOfMappedOntologies = sortedAcronymsByMappingCount.length;
		// And base the total off of the original list
		var originalNumberOfMappedOntologies = Object.keys(mappingData).length;
		
		var defaultNumOfTermsForSize = 10;
		
		// New API example: http://data.bioontology.org/mappings/statistics/ontologies/SNOMEDCT/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a

		// Create the central node
		var centralOntologyNode = new Object();
		centralOntologyNode.name = "fetching";
		centralOntologyNode.description = "fetching description";
		centralOntologyNode.fixed = true; // lock central node
		centralOntologyNode.x = visWidth()/2;
		centralOntologyNode.y = visHeight()/2;		
		centralOntologyNode.weight = numberOfMappedOntologies; // will increment as we loop
		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
		centralOntologyNode.acronymForIds = escapeAcronym(centralOntologyAcronym);
		centralOntologyNode.rawAcronym = centralOntologyAcronym;
		centralOntologyNode.nodeColor = nextNodeColor();
		centralOntologyNode.innerNodeColor = brightenColor(centralOntologyNode.nodeColor);
		centralOntologyNode.nodeStrokeColor = darkenColor(centralOntologyNode.nodeColor);
		centralOntologyNode.mapped_classes_to_central_node = 0;
		centralOntologyNode.displayedArcs = 0;
		ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		
		attachOnDemandApiFunctions(centralOntologyNode);
		
		var ontologyAcronymNodeMap = new Object();
		$(ontologyAcronymNodeMap).attr("vid:"+centralOntologyNode.rawAcronym, centralOntologyNode);
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		// Original bug hidden by force layout, but I needed radians not degrees.
		// It looks very slightly different.
		var anglePerNode =2*Math.PI / numberOfMappedOntologies; // 360/numberOfMappedOntologies;
		var arcLength = linkMaxDesiredLength();
		var i = 0;
		// Used to iterate over raw mappingData, but I wanted things loaded and API calls made in order
		// of mapping counts.
		$.each(sortedAcronymsByMappingCount,
			function(index, acronym){
				var mappingCount = mappingData[acronym];

				if(typeof acronym === "undefined"){
					console.log("Undefined ontology entry");
				}
				
				// Create the neighbouring nodes
				var ontologyNode = new Object();
				ontologyNode.name = "fetching";
				ontologyNode.description = "fetching description";
				ontologyNode.weight = 1;
				ontologyNode.fixed = false; // lock central node
				// Compute starting positions to be in a circle for faster layout
				var angleForNode = i * anglePerNode; i++;
				ontologyNode.x = visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
				ontologyNode.y = visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
				ontologyNode.number = defaultNumOfTermsForSize; // number of terms
				ontologyNode.acronymForIds = escapeAcronym(acronym);
				ontologyNode.rawAcronym = acronym;
				ontologyNode.nodeColor = nextNodeColor();
				ontologyNode.innerNodeColor = brightenColor(ontologyNode.nodeColor);
				ontologyNode.nodeStrokeColor = darkenColor(ontologyNode.nodeColor);
				ontologyNode.mapped_classes_to_central_node = 0;
				ontologyNode.displayedArcs = 0;
				var targetIndex = ontologyNeighbourhoodJsonForGraph.nodes.push(ontologyNode) - 1;
				// TODO I feel like JS doesn't allow references like this...
				$(ontologyAcronymNodeMap).attr("vid:"+ontologyNode.rawAcronym, ontologyNode);
				
				attachOnDemandApiFunctions(ontologyNode);
				
				// Make the links at the same time; they are done now!
				var ontologyLink = new Object();
				ontologyLink.source = centralOntologyNode;
				ontologyLink.target = ontologyNode;
				ontologyLink.source.displayedArcs++;
				ontologyLink.target.displayedArcs++;
				ontologyLink.value = mappingCount; // This gets used for link stroke thickness later.
				ontologyLink.numMappings = mappingCount;
				ontologyNeighbourhoodJsonForGraph.links.push(ontologyLink);
				
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
		populateGraph(ontologyNeighbourhoodJsonForGraph, true);

		//----------------------------------------------------------

		// 2) Get details for all the ontologies (and either create or update the nodes)
		var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
		var ontologyDetailsCallback = new OntologyDetailsCallback(ontologyDetailsUrl, ontologyAcronymNodeMap);
//		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
//		fetcher.retryFetch();
		var fetcher = closureRetryingJsonpFetcher(ontologyDetailsCallback);
		fetcher();
	}
	
}



function OntologyDetailsCallback(url, ontologyAcronymNodeMap){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.ontologyAcronymNodeMap = ontologyAcronymNodeMap;
	var self = this;

	this.callback  = function ontologyDetailsCallback(detailsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

//		var errorOrRetry = self.fetcher.retryFetch(detailsDataRaw);
		var errorOrRetry = self.fetcher(detailsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		console.log("Processing details "+getTime());
		
		// Loop over ontologies and add their additional properties to the nodes
		// Recall that getting *all* ontology details is the easiest (only) way,
		// so we have to skip anything that is not defined.
		var ontologiesSkipped = 0;
		var acronymsNotSkipped = [];
		$.each(detailsDataRaw,
				function(index, ontologyDetails){
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var ontologyAcronym = ontologyDetails.acronym;
					// var node = ontologyNeighbourhoodJsonForGraph.;
					var node = $(self.ontologyAcronymNodeMap).attr("vid:"+ontologyAcronym);
					
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
				}
		);
		
		console.log("Cropping "+getTime());
		
		// We have to remove all nodes and edges which did not appear in details.
		cropGraphToSubset(acronymsNotSkipped);
		
		filterGraphOnMappingCounts();

		// We usually use very many of the ontologies, so it is likely cheaper to make the one
		// big call with no ontology acronym arguments than to cherry pick the ones we want details for.
		console.log("ontologyDetailsCallback, skipped "+ontologiesSkipped+" of total "+detailsDataRaw.length+" "+getTime());
		updateDataForNodesAndLinks({nodes:ontologyNeighbourhoodJsonForGraph.nodes, links:[]});
			
	}
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
function attachOnDemandApiFunctions(node){
	node.fetchMetricsAndDescriptionFunc = function(){
		{
			// Combined dispatch for the separate calls for metrics and descriptions.
			// The metric call has much of the info we need
			var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.rawAcronym);
			var ontologyMetricsCallback = new OntologyMetricsCallback(ontologyMetricsUrl, node);
			// var fetcher = new RetryingJsonpFetcher(ontologyMetricsCallback);
			// fetcher.retryFetch();
			var fetcher = closureRetryingJsonpFetcher(ontologyMetricsCallback);
			fetcher();
		}

		{
			// If we want Description, I think we need to grab the most recent submission
			// and take it fromt here. This is another API call per ontology.
			// /ontologies/:acronym:/lastest_submission
			// Descriptions are in the submissions, so we need an additional call.
			var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.rawAcronym);
			var ontologyDescriptionCallback = new OntologyDescriptionCallback(ontologyDescriptionUrl, node);
			var fetcher = closureRetryingJsonpFetcher(ontologyDescriptionCallback);
			fetcher();
		}
		
		node.fetchMetricsAndDescriptionFunc = function(){return false;};
		
		return true;
	};
}

function OntologyMetricsCallback(url, node){
	this.url = url;
	this.node = node;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (metricDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
		
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	self.fetcher(metricDataRaw);
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
		self.node.numberOfClasses = numClasses;
		self.node.numberOfIndividuals = numIndividuals;
		self.node.numberOfProperties = numProperties;
		self.node.number = nodeSizeBasis;
		
		// console.log("ontologyMetricsCallback");
		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
		// filterGraph();
	}
}

function OntologyDescriptionCallback(url, node){
	this.url = url;
	this.node = node;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (latestSubmissionData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
		
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	self.fetcher(latestSubmissionData);
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
	    
		self.node.description = description;
		
		// console.log("ontologyDescriptionCallback");
		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
	}
}


//function buildOntologyMappingUrl(centralOntologyVirtualId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F"+centralOntologyVirtualId+"&callback=?";
//}

function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
	return "http://data.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyDetailsUrl(){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F"+"&callback=?";
//}

function buildOntologyDetailsUrlNewApi(){
	return "http://data.bioontology.org/ontologies"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyMetricsUrl(ontologyVersionId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F"+ontologyVersionId+"&callback=?";
//}

function buildOntologyMetricsUrlNewApi(ontologyAcronym){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

//function RetryingJsonpFetcher(callbackObject){
//	this.callbackObject = callbackObject;
//	// Has circular dependency with the callback
//	this.callbackObject.fetcher = this;
//	this.previousRetriesMade = 0;
//	var self = this;
//
//		/*
//		 * Return values: -1 is non-retry due to error, 0 is retry, 1 is success, no error.
//		 */
//		RetryingJsonpFetcher.prototype.retryFetch = function(resultData){
//			// console.log("retryFetch for "+self.callbackObject.url);
//			if(typeof resultData === "undefined"){
//				// If not error, call for first time
//				jQuery.getJSON(self.callbackObject.url, null, self.callbackObject.callback);
//				return 0;
//			}
//			
//			if(typeof resultData.success === "undefined") {
//				if(resultData.status == "403" && resultData.body.indexOf("Forbidden") >= 0){
//					console.log("No retry, Forbidden Error: "+self.callbackObject.url);
//					console.log("No retry, Forbidden Error: "+resultData.body);
//		    		return -1;
//				} else if(resultData.status == "500" || resultData.status == "403"){
//		    		if(self.previousRetriesMade < 4){
//		    			self.previousRetriesMade++;
//		    			jQuery.getJSON(self.callbackObject.url, null, self.callbackObject.callback);
//		    			return 0;
//		    		} else {
//			    		// Error, but we are done retrying.
//			    		console.log("No retry, Error: "+resultData);
//			    		return -1;
//		    		}
//		    	} else {
//			    	// Don't retry for other errors
//		    		console.log("Error: "+self.callbackObject.url+" --> Data: "+resultData.status);
//			    	return -1;
//		    	}
//		    } else {
//		    	// Success, great!
//		    	return 1;
//		    }
//		}
//}

/*
 * This fetcher system allows the success receiver to call it to see if there has been an error that
 * allows for a retry. It is fairly clean on the user side, though it does require checking of
 * return values.
 * 
 * Tried to implement as a class object and failed...see above this if you want to try again...
 */
function closureRetryingJsonpFetcher(callbackObject){
	var callbackObject = callbackObject;
	// Has circular dependency with the callback
	var previousRetriesMade = 0;

	/*
	 * Return values: -1 is non-retry due to error, 0 is retry, 1 is success, no error.
	 */
	callbackObject.fetcher = function(resultData){
			// console.log("retryFetch for "+callbackObject.url);
			if(typeof resultData === "undefined"){
				// If not error, call for first time
				jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
				return 0;
			}
			
			if(typeof resultData.errors !== "undefined") {
				if(resultData.status == "404"){
					// 404 Error should fill in some popup data points, so let through...
					console.log("Error: "+callbackObject.url+" --> Data: "+resultData.error);
			    	return 1;
				} else if(resultData.status == "403" && resultData.error.indexOf("Forbidden") >= 0){
					console.log("Forbidden Error, no retry: "
							+"\nURL: "+callbackObject.url
							+"\nReply: "+resultData.error);
		    		return -1;
				} else if(resultData.status == "500" || resultData.status == "403"){
		    		if(previousRetriesMade < 4){
		    			previousRetriesMade++;
		    			console.log("Retrying: "+callbackObject.url);
		    			jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
		    			return 0;
		    		} else {
			    		// Error, but we are done retrying.
			    		console.log("No retry, Error: "+resultData);
			    		return -1;
		    		}
		    	} else {
			    	// Don't retry for other errors
		    		console.log("Error: "+callbackObject.url+" --> Data: "+resultData.error);
			    	return -1;
		    	}
		    } else {
		    	// Success, great!
		    	return 1;
		    }
		}
	
	return callbackObject.fetcher;
}

function initAndPopulateGraph(json){
	initGraph();
	

	
	// Will do async stuff and add to graph
	fetchOntologyNeighbourhood(centralOntologyAcronym);
	
	// If you want to toy with the original static data, try this:
	//	populateGraph(json);
}

var nodeDragBehavior;
function initGraph(){
	forceLayout = self.forceLayout = d3.layout.force();
	
	//	forceLayout.drag()
	//	.on("dragstart", function(){})
	//	.on("dragend", function(){dragging = false;});
	
	// nodeDragBehavior = forceLayout.drag;
	nodeDragBehavior = d3.behavior.drag()
    .on("dragstart", dragstart)
    .on("drag", dragmove)
    .on("dragend", dragend);

	// See the gravityAdjust(), which is called in tick() and modulates
	// gravity to keep nodes within the view frame.
	// If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
	forceLayout
	.friction(0.9) // use 0.2 friction to get a very circular layout
	.gravity(.05) // 0.5
    .distance(Math.min(visWidth(), visHeight())/1.1) // 600
    .charge(-200) // -100
    .linkDistance(linkMaxDesiredLength())
    .size([visWidth(), visHeight()])
    .start();
}

function dragstart(d, i) {
	dragging = true;
	// $(this).tipsy('hide');
	$(".tipsy").hide();
	// stops the force auto positioning before you start dragging
	// This will halt the layout entirely, so if it tends to be unfinished for
	// long enough for a user to want to drag a node, we need to make this more complicated...
    forceLayout.stop();
}

function dragmove(d, i) {
	// http://bl.ocks.org/norrs/2883411
	// https://github.com/mbostock/d3/blob/master/src/layout/force.js
	// Original dragmove() had call to force.resume(), which I needed to remove when the graph was stable.
    d.px += d3.event.dx;
    d.py += d3.event.dy;
    d.x += d3.event.dx;
    d.y += d3.event.dy; 
    
    // Don't need tick if I update the node and associated arcs appropriately.
    // forceLayout.resume();
    // ontologyTick(); // this is the key to make it work together with updating both px,py,x,y on d !
    
    d3.select(this).attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

    vis.selectAll("line")
		.filter(function(e, i){ return e.source == d || e.target == d; })
		.attr("x1", function(e) { return e.source.x; })
		.attr("y1", function(e) { return e.source.y; })
		.attr("x2", function(e) { return e.target.x; })
		.attr("y2", function(e) { return e.target.y; });
   
}

function dragend(d, i) {
	dragging = false;
	// $(this).tipsy('show');
	$(".tipsy").show();
	// of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
    d.fixed = true;
    
    // Don't need the tick(), don't want the resume.
    // ontologyTick(true);
    // forceLayout.resume();
}

function createNodePopupTable(ontologyCircle, ontologyData){
	var outerDiv = $("<div></div>");
	outerDiv.addClass("popups-Popup");
	
	var table = $("<table></table>");
	var tBody = $("<tbody></tbody>");
	 outerDiv.append(table);
	 table.append(tBody);
	 
	 tBody.append(
			 $("<tr></tr>").append(
				   $("<td></td>").append(
						   $("<div></div>").text(ontologyData["displayAcronym"]+":"+ontologyData["name"]).attr("class","popups-Header gwt-Label avatar avatar-resourceSet GK40RFKDB dragdrop-handle")
				   )
		   )
	 );
   
     
     var urlText = "http://bioportal.bioontology.org/ontologies/"+ontologyData["displayAcronym"]+"?p=summary";
     tBody.append(
    		 $("<tr></tr>").append(
    				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
    						 $("<div></div>").addClass("gwt-HTML").css({"white-space":"nowrap"}).append(
    								 $("<a></a>").attr("href", urlText).text(urlText)
    						 )
    				 )
    		 )
     );
     
//     tBody.append(
//    		 $("<tr></tr>").append(
//    				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
//    						 $("<div></div>").addClass("gwt-HTML").css({"white-space":"nowrap"}).append(
//    								 $("<b></b>").text("Ontology Name: ")
//    						 ).append(
//    								 $("<span></span>").text(ontologyData["name"])
//    						 )
//    				 )
//    		 )
//     );
     
     var jsonArgs = {
    		 "Ontology Name: ": "name",
    		 "Ontology Acronym: ": "displayAcronym",
    		 "Ontology URI: ": "uriId",
    		 "Description: ": "description",
    		 "Num Classes: ": "numberOfClasses",
    		 "Num Individuals: ": "numberOfIndividuals",
    		 "Num Properties: ": "numberOfProperties",
    		 "Num Mappings: ": "mapped_classes_to_central_node",
     };
     
     $.each(jsonArgs,function(label, key){
    	 var style = (key === "description" ? {} : {"white-space":"nowrap"});
    	 tBody.append(
        		 $("<tr></tr>").append(
        				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
        						 $("<div></div>").addClass("gwt-HTML").css(style).append(
        								 $("<b></b>").text(label)
        						 ).append(
        								 $("<span></span>").text(ontologyData[key])
        						 )
        				 )
        		 )
         );
     });

     return outerDiv.prop("outerHTML");
}

/**
* This function should be used when adding brand new nodes and links to the
* graph. Do not call it to update properties of graph elements.
* TODO Make this function cleaner and fully compliant with the above description!
*/
function populateGraph(json, newElementsExpected){
//	console.log("Populating with:");
//	console.log(json);
	
	if(json === "undefined" || json.length == 0 || json.nodes.length == 0 && json.links.length == 0){
		// console.log("skip");
		// return;
		newElementsExpected = false;
	}
	
	// Data constancy via key function() passed to data()
	// Link stuff first
	var links = vis.selectAll("line.link").data(json.links, function(d){return d.source.rawAcronym+"->"+d.target.rawAcronym});
	// console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);

	// Add new stuff
	if(newElementsExpected === true)
	links.enter().append("svg:line")
	.attr("class", "link") // Make svg:g like nodes if we need labels
	.attr("id", function(d){ return "link_line_"+d.source.acronymForIds+"->"+d.target.acronymForIds})
	.on("mouseover", highlightLink())
	.on("mouseout", changeColourBack);
	
	// console.log("After append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
	
	// Update Basic properties
//	if(newElementsExpected === true)
	links
    .attr("class", "link")
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; })
    .style("stroke-linecap", "round")
    .attr("data-thickness_basis", function(d) { return d.value;});
	
	updateLinkScalingFactor();
	
    links.style("stroke-width", function(d) { return ontologyLinkScalingFunc(d.value); })
	;

	// Update Tool tip
	if(newElementsExpected === true)
	links.append("title") // How would I *update* this if I needed to?
		.text(function(d) { return "Number Of Mappings: "+d.numMappings; })
			.attr("id", function(d){ return "link_title_"+d.source.acronymForIds+"->"+d.target.acronymForIds});

	updateTopMappingsSliderRange();
	
	// Node stuff now
	
	var nodes = vis.selectAll("g.node").data(json.nodes, function(d){return d.rawAcronym});
	// console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	// Add new stuff
	if(newElementsExpected === true)
	nodes.enter().append("svg:g")
	.attr("class", "node")
	.attr("id", function(d){ return "node_g_"+d.acronymForIds})
	// Is it ok to do call() here?
    .call(nodeDragBehavior);
	
	// console.log("After append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	
	// Easiest to use JQuery to get at existing enter() circles
	// Otherwise we futz with things like the enter()select(function) below
	
    // I think that the lack of way to grab child elements from the enter() selection while they are
	// data bound (as is usual for most D3 selections), is what is preventing me from udpating using D3
	// idioms. This means no D3 implicit selection loops.
	// Therefore I need to update using JQuery selections on unqiue element IDs
	
	// Basic properties
	if(newElementsExpected === true) // How would I *update* this if I needed to?
	nodes
	.append("svg:circle") 
	.attr("id", function(d){ return "node_circle_"+d.acronymForIds})
    .attr("class", "circle")
    .attr("cx", "0px")
    .attr("cy", "0px")
    .style("fill", defaultNodeColor)
    .style("stroke", darkenColor(defaultNodeColor))
	.attr("data-radius_basis", function(d) { return d.number;})
    .attr("r", function(d) { return ontologyNodeScalingFunc(d.number, d.rawAcronym); })
	.on("mouseover", changeColour)
	.on("mouseout", changeColourBack);
	
	if(newElementsExpected === true) // How would I *update* this if I needed to?
	// Add a second circle that represents the mapped classes of the ontology.
	nodes
	.append("svg:circle") 
	.attr("id", function(d){ return "node_circle_inner_"+d.acronymForIds})
    .attr("class", "inner_circle")
    .attr("cx", "0px")
    .attr("cy", "0px")
    .attr("pointer-events", "none") // genius SVG API design! Without this, the central circle messes with popups.
    .style("fill", brightenColor(defaultNodeColor))
    .style("stroke", darkenColor(defaultNodeColor))
	.attr("data-inner_radius_basis", function(d) { return d.mapped_classes_to_central_node;})
	.attr("data-outer_radius_basis", function(d) { return d.number;})
    .attr("r", function(d) { return ontologyInnerNodeScalingFunc(d.mapped_classes_to_central_node, d.number, d.rawAcronym); })
	.on("mouseover", changeColour)
	.on("mouseout", changeColourBack);
	
	// tipsy stickiness from:
	// http://stackoverflow.com/questions/4720804/can-i-make-this-jquery-tooltip-stay-on-when-my-cursor-is-over-it
	d3.selectAll(".circle").each(function(d){
		var me = this,
		meData = d,
	    leaveDelayTimer = null,
	    visible = false,
	    tipsyId = undefined;
		
		// TODO This creates a timer per popup, which is sort of silly. Figure out another way.
	    var leaveMissedTimer = undefined;
	    function missedEventTimer() {
	    	leaveMissedTimer = setTimeout(missedEventTimer, 1000);
	    	// The hover check doesn't work when we are over children it seems, and the tipsy has plenty of children...
	    	if($("#"+me.id+":hover").length != 0 && !$(tipsyId+":hover").length != 0){
	    		console.log("Not in thing");
	    		leave();
	    	}
	    }
	    missedEventTimer();
		
		function leave() {
	        // We add a 100 ms timeout to give the user a little time
	        // moving the cursor to/from the tipsy object
			leaveDelayTimer = setTimeout(function () {
	            $(me).tipsy('hide');
	            visible = false;
	        }, 100);
	    }

	    function enter() {
	    	if(dragging){
	    		return;
	    	}
			$(me).tipsy({
				html: true,
				fade: true,
//				 offset: parseInt($(me).attr("r")), // works better without this!
				offset: 15, // need this for the gravity sout-east cases. It makes it quite far for the other cases though...
				fallback: "Fetching data...",
		        title: function() {
		          // var d = this.__data__, c = d.i; //colors(d.i);
		          // return 'Hi there! My color is <span style="color:' + c + '">' + c + '</span>';
		          return createNodePopupTable(me, meData);
		        },
		        trigger: 'manual',
				gravity: function() {
					var location = "";
					
					if($(me).offset().top > ($(document).scrollTop() + $(window).height() / 2)){
						location += "s";
					} else {
						location += "n";
					}
					
					if($(me).offset().left > ($(document).scrollLeft() + $(window).width() / 2)){
						location += "e";
					} else {
						location += "w";
					}
					// console.log("Location "+location);
			        return location;
			    },
			});
	    	
	        if (visible) {
	            clearTimeout(leaveDelayTimer);
	        } else {
	            $(me).tipsy('show');
	            // The .tipsy object is destroyed every time it is hidden,
	            // so we need to add our listener every time its shown
	            var tipsy = $(me).tipsy("tip");
	            lastDisplayedTipsy = tipsy;
	            lastDisplayedTipsyData = meData;
	            lastDisplayedTipsyCircle = me;
	            tipsyId = $(me).attr("id"+"_tipsy");
	            tipsy.attr("id", tipsyId);
	            
	            // For the tipsy specific listeners, change opacity.
	            tipsy.mouseenter(function(){tipsy.css("opacity",1.0); enter(); }).mouseleave(function(){tipsy.css("opacity",0.8); leave();});
	            tipsy.mouseover(function(){
	            	tipsy.css("opacity",1.0);
	    	    	clearTimeout(leaveMissedTimer);
	    		});
	            visible = true;
	        }
	    }
	    
		$(this).hover(enter, leave);
		$(this).mouseover(function(){
	    	clearTimeout(leaveMissedTimer);
		});
		
		
		// TODO Use a timer, poll style, to prevent cases where mouse events are missed by browser.
		// That happens commonly. We'll want to hide stale open tipsy panels when this happens.
//		 d3.timer(function(){}, -4 * 1000 * 60 * 60, +new Date(2012, 09, 29));
	});
		
	// Dumb Tool tip...not needed with tipsy popups.
//	if(newElementsExpected === true)  // How would I *update* this if I needed to?
//	nodes.append("title")
//	  .attr("id", function(d){ return "node_title_"+d.acronymForIds})
//	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
	if(newElementsExpected === true) // How would I *update* this if I needed to?
	nodes.append("svg:text")
		.attr("id", function(d){ return "node_text_"+d.acronymForIds})
	    .attr("class", "nodetext unselectable")
	    .attr("dx", 12)
	    .attr("dy", 1)
	    .text(function(d) { return d.name; })
	    // Not sure if I want interactions on labels or not. Change following as desired.
	    .style("pointer-events", "none")
	    // Why cannot we stop selection in IE? They are rude.
		.attr("unselectable", "on") // IE 8
		.attr("onmousedown", "noselect") // IE ?
		.attr("onselectstart", "function(){ return false;}") // IE 8?
	    // .on("mouseover", changeColour)
	    // .on("mouseout", changeColourBack)
	    ;
		
	// Would do exit().remove() here if it weren't re-entrant, so to speak.
	
	
	
	// XXX Doing this a second time destroys the visualization!
	// How would we do it on only new things?
	// Oh! It is because we are using the links and nodes references,
	// and losing references to the existing nodes and links.
	// I really want to make sure I keep trakc of whether we
	// have all nodes/links, or just new ones...
	var lastLabelShiftTime = jQuery.now();
	var lastGravityAdjustmentTime = jQuery.now();
	var firstTickTime = jQuery.now();
	var maxLayoutRunDuration = 10000;
	var maxGravityFrequency = 4000;
	ontologyTick = function() {
		// Stop the layout early. The circular initialization makes it ok.
		if (forceLayout.alpha() < alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
			forceLayout.stop();
		}
		
		// Do I want nodes to avoid one another?
		// http://bl.ocks.org/mbostock/3231298
//		var q = d3.geom.quadtree(nodes),
//	      i = 0,
//	      n = nodes.length;
//		while (++i < n) q.visit(collide(nodes[i]));
//		function collide(node) {
//			  var r = node.radius + 16,
//			      nx1 = node.x - r,
//			      nx2 = node.x + r,
//			      ny1 = node.y - r,
//			      ny2 = node.y + r;
//			  return function(quad, x1, y1, x2, y2) {
//			    if (quad.point && (quad.point !== node)) {
//			      var x = node.x - quad.point.x,
//			          y = node.y - quad.point.y,
//			          l = Math.sqrt(x * x + y * y),
//			          r = node.radius + quad.point.radius;
//			      if (l < r) {
//			        l = (l - r) / l * .5;
//			        node.x -= x *= l;
//			        node.y -= y *= l;
//			        quad.point.x += x;
//			        quad.point.y += y;
//			      }
//			    }
//			    return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
//			  };
//		 svg.selectAll("circle")
//	      .attr("cx", function(d) { return d.x; })
//	      .attr("cy", function(d) { return d.y; });
		
		// For every iteration of the layout (until it stabilizes)
		// Using this bounding box on nodes and links works, but leads to way too much overlap for the
		// labels...Bostock is correct in saying that gravity adjustments can get better results.
		// gravityAdjust() functions are pass through; they want to inspect values,
		// not modify them!
		var doLabelUpdateNextTime = false;
		if(jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency){
			nodes.attr("transform", function(d) { return "translate(" + gravityAdjustX(d.x) + "," + gravityAdjustY(d.y) + ")"; });
			lastGravityAdjustmentTime = jQuery.now();
			doLabelUpdateNextTime = true;
		} else {
			nodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
		}
		
		links
		  .attr("x1", function(d) { return d.source.x; })
	      .attr("y1", function(d) { return d.source.y; })
	      .attr("x2", function(d) { return d.target.x; })
	      .attr("y2", function(d) { return d.target.y; });
		
		// I want labels to aim out of middle of graph, to make more room
		// It slows rendering, so I will only do it sometimes
		// Commented all thsi out because I liked centering them instead.
//		if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
//			$.each($(".nodetext"), function(i, text){
//				text = $(text);
//				if(text.position().left >= visWidth()/2){
//					text.attr("dx", 12);
//					text.attr("x", 12);
//				} else {
//					text.attr("dx", - 12 - text.get(0).getComputedTextLength());
//					text.attr("x", - 12 - text.get(0).getComputedTextLength());
//				}
//			})
//			lastLabelShiftTime = jQuery.now();
//		}
		
	
	}
	
	
	if(newElementsExpected === true){
		forceLayout.on("tick", ontologyTick);
	}
	
	// Make sure we have initialized the filter slider to be at the softNodeCap.
	// The filter function will lead to individual API calls being dispatched on nodes.
	// It will (in the future) also trigger layout adaptation to added or removed nodes.
	// changeTopMappingSliderValues(null, softNodeCap);
	
	// We have a situation where only our third REST calls determine which nodes and links actually stay in the graph.
	// We would like to filter early, based on the soft cap.
//	 filterGraphOnMappingCounts();
	
	// Whenever I call populate, it adds more to this layout.
	// I need to figure out how to get enter/update/exit sort of things
	// to work for the layout.
	if(newElementsExpected === true){
		// forceLayout
		// .nodes(nodes.enter())
	    // .links(links.enter());
		forceLayout
		.nodes(json.nodes)
	    .links(json.links);
		// Call start() whenever any nodes or links get added or removed
		forceLayout.start();
	}
	
	// Don't have sizes here, but still...
	updateNodeScalingFactor();
	// Do have link sizes though? Now e called it earlier at a better time.
	// updateLinkScalingFactor();
	
}

/**
 * We cannot update the graph with new node or link properties *efficiently* using D3.
 * This is because, although you can use the enter() selection, you cannot sub-select within
 * it to access the children DOM elements, and using other D3 ways of getting at the elements
 * fails to have them bound to the data as they are in the enter() selection [meaning that
 * data based property settings fail].
 * 
 * Explicit looping allows us to cherry pick data, and do fewer DOM changes than I could
 * when using D3's data().enter() selection results.
 * 
 * @param json
 */
function updateDataForNodesAndLinks(json){
	// console.log("Updating with data:");
	// console.log(json);
	
	var updateLinksFromJson = function(i, d){ // JQuery is i, d
		// Given a json encoded graph element, update all of the nested elements associated with it
		// cherry pick elements that we might otherwise get by class "link"
		var link = vis.select("#link_line_"+d.source.acronymForIds+"->"+d.target.acronymForIds);
		link.attr("data-thickness_basis", function(d) { return d.value;})
		link.select("title").text(function(d) { return "Number Of Mappings: "+d.numMappings; });
	}
	
	var updateNodesFromJson = function(i, d){ // JQuery is i, d
		// Given a json encoded graph element, update all of the nested elements associated with it
		// cherry pick elements that we might otherwise get by class "node"
		var node = vis.select("#node_g_"+d.acronymForIds);
		
		node.select("title").text(function(d) { return "Number Of Terms: "+d.number+"<br/> and <br/>"+"Number Of Mappings: "+d.mapped_classes_to_central_node; });
		node.select("text")
		.text(function(d) { return d.name; })
		// Firefox renders dx for text poorly, shifting things around oddly,
		// but x works for both Chrome and Firefox.
//		.attr("dx", function(){ return - this.getComputedTextLength()/2; })
		.attr("x", function(){ return - this.getComputedTextLength()/2; })
		;
		
		var circles = node.select(".circle");
		circles.attr("data-radius_basis", d.number);
		circles.transition().style("fill", d.nodeColor).style("stroke", d.nodeStrokeColor);
			
		// Update the inner circles too
		var inner_circles = node.select(".inner_circle");
		inner_circles.attr("data-inner_radius_basis", d.mapped_classes_to_central_node);
		inner_circles.attr("data-outer_radius_basis", d.number);
		inner_circles.transition().style("fill", d.innerNodeColor).style("stroke", d.nodeStrokeColor);
		
		// Refresh popup if currently open
		if(lastDisplayedTipsy != null
				&& lastDisplayedTipsy.css("visibility") == "visible"
				&& lastDisplayedTipsyData.acronymForIds == d.acronymForIds
				){
			$(lastDisplayedTipsy).children(".tipsy-inner").html(createNodePopupTable(lastDisplayedTipsyCircle, lastDisplayedTipsyData));
		}
	}
	
	$.each(json.links, updateLinksFromJson);
	$.each(json.nodes, updateNodesFromJson);
	
	if(nodeUpdateTimer == false){
		nodeUpdateTimer = true;
		window.setTimeout(function(){
				console.log("TIMER RESET");
				nodeUpdateTimer = false;
				updateNodeScalingFactor();
				// The link thickness does not receive new data right now,
				// otherwise we'd want to call the update factor function here.
				// updateLinkScalingFactor();
			},
			1000);
	}
}
var nodeUpdateTimer = false;

/**
 * Provide ontology acronyms that should be kept in the graph whiel the rest are removed.
 * Removes both nodes and links.
 * Revise as necessary if a latent-link approach is used later.
 * 
 * @param json
 */
function cropGraphToSubset(acronymsToKeep){
	//$.each(acronymsToKeep, function(index, node){console.log("Cropping down to: "+node)});
	
	// $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("Before removal: "+node.rawAcronym)});
	// $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("Before removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
	
	ontologyNeighbourhoodJsonForGraph.nodes
	= jQuery.grep(
			ontologyNeighbourhoodJsonForGraph.nodes,
			function(value) {
			  return jQuery.inArray(value.rawAcronym, acronymsToKeep) != -1;
			}
	);
	
	ontologyNeighbourhoodJsonForGraph.links
	= jQuery.grep(
			ontologyNeighbourhoodJsonForGraph.links,
			function(value) {
			  return jQuery.inArray(value.source.rawAcronym, acronymsToKeep) != -1
			  && jQuery.inArray(value.target.rawAcronym, acronymsToKeep) != -1;
			}
	);
	
	// $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("After removal: "+node.rawAcronym)});
	// $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("After removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
	
	removeGraphPopulation(ontologyNeighbourhoodJsonForGraph);
}

function removeGraphPopulation(ontologyNeighbourhoodJsonForGraph){
	console.log("Removing some graph elements "+getTime());

	var nodes = vis.selectAll("g.node").data(ontologyNeighbourhoodJsonForGraph.nodes, function(d){return d.rawAcronym});
	var links = vis.selectAll("line.link").data(ontologyNeighbourhoodJsonForGraph.links, function(d){return d.source.rawAcronym+"->"+d.target.rawAcronym});
	
	nodes.exit().remove();
	links.exit().remove();
	forceLayout.start();
}

function highlightLink(){
	return function(d, i){
		if(dragging){
			return;
		}
	
		var xSourcePos = d.source.x;
		var ySourcePos = d.source.y;
		var xTargetPos = d.target.x;
		var yTargetPos = d.target.y;
		
		d3.selectAll("text").style("opacity", .2)
			.filter(function(g, i){return g.x==d.source.x||g.y==d.source.y||g.x==d.target.x||g.y==d.target.y;})
			.style("opacity", 1);
			
		d3.selectAll("line").style("stroke-opacity", .1);
		d3.selectAll("circle").style("fill-opacity", .1)
			.style("stroke-opacity", .2)
			.filter(function(g, i){return g.x==d.source.x||g.y==d.source.y||g.x==d.target.x||g.y==d.target.y})
			.style("fill-opacity", 1)
			.style("stroke-opacity", 1);
		d3.select(this).style("stroke-opacity", 1)
			.style("stroke", "#3d3d3d");

	}
}

function changeColour(d, i){
	if(dragging){
		return;
	}
	var xPos=d.x;
	var yPos=d.y;
	
	d3.selectAll("line").style("stroke-opacity", .1);
	d3.selectAll("circle").style("fill-opacity", .1)
		.style("stroke-opacity", .2);
		
	d3.selectAll("text").style("opacity", .2)
		.filter(function(g, i){return g.x==d.x})
		.style("opacity", 1);
		
	//	if(d3.select(this).attr("class") == "circle"){
	//	if(d3.select(this).attr("class") == "inner_circle"){
	//	if(d3.select(this).attr("class") == "nodetext"){
	// This works when the mouse goes over the nodetext, circle, or inner_circle
	// If the labels aren't wired for mouse interaction, this is unneeded
	var sourceNode = d3.select(this.parentNode).select(".circle");
	sourceNode.style("fill", nodeHighlightColor)
		.style("fill-opacity", 1)
		.style("stroke-opacity", 1)
		;
		
	var innerSourceNode = d3.select(this.parentNode).select(".inner_circle");
	innerSourceNode.style("fill", brightenColor(nodeHighlightColor))
		.style("fill-opacity", 1)
		.style("stroke-opacity", 1)
	;

		
	var adjacentLinks = d3.selectAll("line")
		.filter(function(d, i) {return d.source.x==xPos && d.source.y==yPos;})
		.style("stroke-opacity", 1)
		.style("stroke", "#3d3d3d")
		.each(function(d){
			d3.selectAll("circle")
			.filter(function(g, i){return d.target.x==g.x && d.target.y==g.y;})
			.style("fill-opacity", 1)
			.style("stroke-opacity", 1)
			.each(function(d){
				d3.selectAll("text")
				.filter(function(g, i){return g.x==d.x})
				.style("opacity", 1);});
	});
}

function changeColourBack(d, i){
	d3.selectAll(".circle")
		.style("fill", function(e, i){ 
			return (typeof e.nodeColor === undefined ? defaultNodeColor : e.nodeColor); 
			})
		.style("fill-opacity", .75)
		.style("stroke-opacity", 1);
	
	d3.selectAll(".inner_circle")
	.style("fill", function(e, i){ 
			return (typeof e.innerNodeColor === undefined ? defaultNodeColor : e.innerNodeColor); 
		})
	.style("fill-opacity", .75)
	.style("stroke-opacity", 1);

	
	d3.selectAll("line")
		.style("stroke", defaultLinkColor)
		.style("stroke-opacity", .75);
	d3.selectAll("text").style("opacity", 1);
}


// Maintaining relative scaled sizes of arcs and nodes depends on updating
// the raw size range, which in this implementation, loops over all entities.
// Only update the ranges when appropriate.
// BioMixer used a 500 ms delay on re-doing things.

// 20 * 7 seems too big. Got 20 from other transformers.
var NODE_MAX_ON_SCREEN_SIZE = 20 * 5;
var NODE_MIN_ON_SCREEN_SIZE = 4;
var minNodeRawSize = -1;
var maxNodeRawSize = -1;
var LINK_MAX_ON_SCREEN_SIZE = 7; // 6 looks good...but if I change colors it may not.
var LINK_MIN_ON_SCREEN_SIZE = 1;
var minLinkRawSize = -1;
var maxLinkRawSize = -1;
var REFRESH_LOOP_DELAY_MS = 500;

function updateNodeScalingFactor(){
	// Call this prior to redrawing. The alternative is to track on every size
	// modification. That worked well for BioMixer, but perhaps we're better
	// off doing a bulk computation per size-refreshing redraw that we want to make.
	
	var circles = vis.selectAll(".circle");
	circles.each(function(d){
				var basis = parseInt(this.getAttribute("data-radius_basis"));
				if(-1 == maxNodeRawSize || basis > maxNodeRawSize){
					maxNodeRawSize = basis;
				}
				if(-1 == minNodeRawSize || basis < minNodeRawSize){
					minNodeRawSize = basis;
				}
		});
	
	circles.transition().attr("r", function(d) { return ontologyNodeScalingFunc(this.getAttribute("data-radius_basis"), this.getAttribute("id"));});
	
	// Inner circles use the same scaling factor.
	var innerCircles = vis.selectAll(".inner_circle");
	innerCircles.transition().attr("r", function(d) { return ontologyInnerNodeScalingFunc(this.getAttribute("data-inner_radius_basis"), this.getAttribute("data-outer_radius_basis"), this.getAttribute("id"));});

}

function updateLinkScalingFactor(){
	// TODO This may not ever need to be called multiple times, but it would take some time to run.
	// Make sure it actually needs to be run if it is indeed called. 
	console.log("Ran update link "+getTime());
	// Call this prior to redrawing. The alternative is to track on every size
	// modification. That worked well for BioMixer, but perhaps we're better
	// off doing a bulk computation per size-refreshing redraw that we want to make.
	$.each(vis.selectAll("line.link")[0], function(i, link){
		link = $(link);
		var basis = parseInt(link.attr("data-thickness_basis"));
		if(-1 == maxLinkRawSize || basis > maxLinkRawSize){
			maxLinkRawSize =  basis;
		}
		if(-1 == minLinkRawSize || basis < minLinkRawSize){
			minLinkRawSize =  basis;
		}
	});
		
	$.each(vis.selectAll("line.link")[0], function(i, link){
		// Given a json encoded graph element, update all of the nested elements associated with it
		// cherry pick elements that we might otherwise get by class "node"
		link = $(link);
		link.css("stroke-width", function(d) { return ontologyLinkScalingFunc(link.attr("data-thickness_basis")); });
	});
}

var defaultNumOfTermsForSize = 10;

function ontologyNodeScalingFunc(rawValue, acronym){
		// return Math.sqrt((rawValue)/10);
		
	if(rawValue == 0){
		return defaultNumOfTermsForSize;
	}
	
 	if(maxNodeRawSize == minNodeRawSize){
		return defaultNumOfTermsForSize;
 	}
 	
	var factor = computeFactorOfRange(rawValue, minNodeRawSize, maxNodeRawSize);
    var diameter = linearAreaRelativeScaledRangeValue(factor, NODE_MIN_ON_SCREEN_SIZE, NODE_MAX_ON_SCREEN_SIZE);
    if(isNaN(diameter)){
    	return 0;
    }
//    console.log(diameter/2+" for "+acronym);
    return diameter/2; // need radius for SVG
}
	
function ontologyInnerNodeScalingFunc(rawValue, outerRawValue, acronym){
	if(rawValue == 0 || rawValue == minNodeRawSize || maxNodeRawSize == minNodeRawSize || rawValue > outerRawValue){
		// If there is no mapping, I want no dot. This applies to the central node specifically.
		// I also don't want a teeny weeny inner circle completely covering the outer circle,
		// so let's scale away thsoe that match the minimum render size.
		// Otherwise we'll scale exactly the same as the outer circle.
		return 0;
	}
	
	return ontologyNodeScalingFunc(rawValue, acronym);
	
	// var outerRadius = ontologyNodeScalingFunc(rawValue, acronym);
	// var outerArea = Math.PI*(outerRadius*outerRadius);
	// var innerArea = outerArea * (rawValue / outerRawValue);
	// var innerRadius = outerRadius * (rawValue / outerRawValue);
	// // var innerRadius = Math.sqrt(innerArea/Math.PI);
	//  console.log([acronym, "raw", rawValue / outerRawValue, rawValue, outerRawValue, "area", outerArea/innerArea, outerArea, innerArea, "radius", outerRadius/innerRadius, outerRadius, innerRadius]);
	// return innerRadius;
}

function ontologyLinkScalingFunc(rawValue){
	if(maxLinkRawSize == minLinkRawSize){
		return rawValue;
	}
	var factor = computeFactorOfRange(rawValue, minLinkRawSize, maxLinkRawSize);
	// The linear area algorithm used for nodes happens to work really well for the edges thickness too.
    var thickness = linearAreaRelativeScaledRangeValue(factor, LINK_MIN_ON_SCREEN_SIZE, LINK_MAX_ON_SCREEN_SIZE);
    return thickness/2;
}

function computeRangeRawSize(minRawSize, maxRawSize) {
	return Math.max(1, maxRawSize - minRawSize);
}

function computeFactorOfRange(rawValue, minRawSize, maxRawSize) {
	return 1.0 - (maxRawSize - rawValue) / computeRangeRawSize(minRawSize, maxRawSize);
}

function linearAreaRelativeScaledRangeValue(factor, minOnScreenSize, maxOnScreenSize) {
	var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor
	      * Math.PI * Math.pow(maxOnScreenSize, 2);
	var diameter = Math.sqrt(linearArea / Math.PI);
	return diameter;
}

/*
    private double linearFunction(double value) {
        // Ha! A sqrt makes this not linear. Mis-named now...
        return 2 * (4 + Math.sqrt((value) / 10));
        return (1 + Math.sqrt((value)));
    }

    private double logFunction(double value) {
        return 4 + Math.log(value) * 10;
    }
 */

var currentNodeColor = -1;
var nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
function nextNodeColor(){
	currentNodeColor = currentNodeColor == 19 ? 0 : currentNodeColor + 1;
	return nodeOrderedColors(currentNodeColor);
}

function brightenColor(outerColor){
	// Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
	// Using lab() converts from hex RGB to the Cie L*A*B equivalent.
	return d3.lab(outerColor).brighter(1).toString();
}

function darkenColor(outerColor){
	// Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
	// Using lab() converts from hex RGB to the Cie L*A*B equivalent.
	return d3.lab(outerColor).darker(1).toString();
}

function prepGraphMenu(){
	// Node filter for ontology graphs. Allows filtering of nodes by size, and arcs by size.
	var menuSelector = 'div#hoveringGraphMenu';
	// Append the pop-out panel. It will stay hidden except when moused over.
	var trigger = $("<div>").attr("id", "trigger");
	$("#chart").append(trigger);
	trigger.append($("<p>").text("<< Menu"));
	trigger.append($("<div>").attr("id", "hoveringGraphMenu"));

	$('#trigger').hover(
			function(e) {
				$(menuSelector).show(); //.css('top', e.pageY).css('left', e.pageX);
				 // Looks bad when it's not fully visible, due to children inheriting transparency
				$(menuSelector).fadeTo(0, 1.0);
			},
			function() {
				$(menuSelector).hide();
			}
	);
	
	addMenuComponents(menuSelector);
}

function addMenuComponents(menuSelector){
	var minSliderAbsolute = 0;
	var maxSliderAbsolute = 0 == softNodeCap ? sortedLinksByMapping.length : softNodeCap;
	
	$(menuSelector).append($("<label>").attr("for", "top-mappings-slider-amount").text("Ranked Mapping Range: "));
	$(menuSelector).append($("<label>").attr("type", "text").attr("id", "top-mappings-slider-amount")) // .css("border:0; color:#f6931f; font-weight:bold;"));
	$(menuSelector).append($("<div>").attr("id",  "top-mappings-slider-range" ));
	
	$( "#top-mappings-slider-range" ).slider({
		range: true,
		min: minSliderAbsolute,
		max: maxSliderAbsolute,
		values: [ minSliderAbsolute, maxSliderAbsolute ],
		slide: function( event, ui ) {
			// Need to make it wider than 100% due to UI bugginess
			var bottom = $( "#top-mappings-slider-range" ).slider( "values", 0 ) + 1;
			var top = $( "#top-mappings-slider-range" ).slider( "values", 1 ) + 1;
			$( "#top-mappings-slider-amount" ).text( "Top "+ bottom + " - " + top );
			filterGraphOnMappingCounts();
			}
		}
	);
	
	updateTopMappingsSliderRange();
	
	// Need separate initialization for input text
	$( "#top-mappings-slider-amount" ).text( "Top "+ minSliderAbsolute + " - " + maxSliderAbsolute );
	
}

function changeTopMappingSliderValues(bottom, top){
	if(null == bottom){
		bottom = $( "#top-mappings-slider-range" ).slider('values', 0);
	}
	if(null == top){
		top = $( "#top-mappings-slider-range" ).slider('values', 1);
	}
	$( "#top-mappings-slider-range" ).slider('values', [bottom, top]);
	// $( "#top-mappings-slider-range" ).slider("refresh");
}

function updateTopMappingsSliderRange(){
	if(typeof(sortedLinksByMapping) === undefined || sortedLinksByMapping.length == 0){
		sortedLinksByMapping = [];
		// Fill the sorted set for the first time
		var i = 0;
		d3.selectAll("line").each( 
				function(d,i){
					sortedLinksByMapping[i] = d.value;
				}
		);
	}
	
	// Descending sort so we can pick the top n.
	sortedLinksByMapping.sort(function(a,b){return b-a});
	
	var mappingMin = 1;
	var mappingMax = sortedLinksByMapping.length;
	
	$( "#top-mappings-slider-range" ).slider("option", "min", 0);
	$( "#top-mappings-slider-range" ).slider("option", "max", sortedLinksByMapping.length - 1);
	$( "#top-mappings-slider-range" ).slider("option", "values", [0, sortedLinksByMapping.length - 1]);
	$( "#top-mappings-slider-amount" ).text( "Top "+ mappingMin + " - " + mappingMax );
}

/**
 * Old filtering code. Based off of deleted sliders. Filtered on node size and/or arc size.
 * May be useful later, perhaps. Be sure to check that it still works with changes made since it was deprecated.
 */
function filterGraphDeprecated(){
	var minNodeAbsolute = minNodePercentile * (nodeMax - nodeMin) + nodeMin;
	var maxNodeAbsolute = maxNodePercentile * (nodeMax - nodeMin) + nodeMin;
	var minArcAbsolute = minArcPercentile * (arcMax - arcMin) + arcMin;
	var maxArcAbsolute = maxArcPercentile * (arcMax - arcMin) + arcMin;
	
	// Iterate through all arcs, remove if their node or arc fails to pass
	// We don't need to iterate through all the nodes, because we will do so here.
	// That is, we know that our ontologies do not have detached nodes, so going over all arcs gets us all nodes.
	d3.selectAll("line").each( 
			function(d,i){
				var hideArc = (parseInt(d.value) < minArcAbsolute || parseInt(d.value) > maxArcAbsolute);
				var hideSourceNode = (parseInt(d.source.number) < minNodeAbsolute || parseInt(d.source.number) > maxNodeAbsolute);
				var hideTargetNode = (parseInt(d.target.number) < minNodeAbsolute || parseInt(d.target.number) > maxNodeAbsolute);
				
				if(d.source.rawAcronym == centralOntologyAcronym){
					hideSourceNode = false;
				}
				if(d.target.rawAcronym == centralOntologyAcronym){
					hideTargetNode = false;
				}
				
				$(this).css("display", (hideArc || hideSourceNode || hideTargetNode) ? "none" : "");
				
				$("#node_circle_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
				$("#node_circle_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
				
				// TODO If we want this to be generic and refactorable, we should iterate over the parent of the circles...
				// These inner circles only really apply to the ontology nodes
				$("#node_circle_inner_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
				$("#node_circle_inner_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
								
				$("#node_text_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
				$("#node_text_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
								
				// The nodes have API calls they might need to make. This might change a little when expansion commands
				// are added to the system.
				if(!(hideArc || hideSourceNode)){
					d.source.fetchMetricsAndDescriptionFunc();
				}
				if(!(hideArc || hideTargetNode)){
					d.target.fetchMetricsAndDescriptionFunc();
				}
			}
		);
	
}
	
function filterGraphOnMappingCounts(){
	// Grabbing min from 1 and max from 0 looks funny, but it does the trick. Pinky swear.
	var minNodeAbsolute = sortedLinksByMapping[$( "#top-mappings-slider-range" ).slider( "values", 1 )]; // starts at big number
	var maxNodeAbsolute = sortedLinksByMapping[$( "#top-mappings-slider-range" ).slider( "values", 0 )]; // starts at 0
	var minArcAbsolute = minNodeAbsolute;
	var maxArcAbsolute = maxNodeAbsolute;
	
	// Iterate through all arcs, remove if their node or arc fails to pass
	// We don't need to iterate through all the nodes, because we will do so here.
	// That is, we know that our ontologies do not have detached nodes, so going over all arcs gets us all nodes.
	d3.selectAll("line").each( 
			function(d,i){
				// Work with arc first, then the attached nodes.
				var hideArc = (parseInt(d.value) < minArcAbsolute || parseInt(d.value) > maxArcAbsolute);
				$(this).css("display", (hideArc || hideSourceNode || hideTargetNode) ? "none" : "");
				
				var hideSourceNode = (parseInt(d.source.mapped_classes_to_central_node) < minNodeAbsolute || parseInt(d.source.mapped_classes_to_central_node) > maxNodeAbsolute);
				var hideTargetNode = (parseInt(d.target.mapped_classes_to_central_node) < minNodeAbsolute || parseInt(d.target.mapped_classes_to_central_node) > maxNodeAbsolute);
				
				
				if(d.source.rawAcronym == centralOntologyAcronym){
					hideSourceNode = false;
				}
				if(d.target.rawAcronym == centralOntologyAcronym){
					hideTargetNode = false;
				}
				
				// For more general graphs than the ontology graph, we'd want to see if all arcs attached
				// to the node are hidden or not. For ontology mapping graph, there's only one arc so I cheat.
				// For that method, I'd maintain a counter on each node.
				var hideSourceNodeBecauseOfHiddenArc = modifyNodeDisplayedArcCounter(d.source, hideArc);
				var hideTargetNodeBecauseOfHiddenArc = modifyNodeDisplayedArcCounter(d.target, hideArc);
				
				$("#node_circle_"+d.source.acronymForIds).css("display", (hideSourceNodeBecauseOfHiddenArc || hideSourceNode) ? "none" : "");
				$("#node_circle_"+d.target.acronymForIds).css("display", (hideTargetNodeBecauseOfHiddenArc || hideTargetNode) ? "none" : "");
				
				$("#node_circle_inner_"+d.source.acronymForIds).css("display", (hideSourceNodeBecauseOfHiddenArc || hideSourceNode) ? "none" : "");
				$("#node_circle_inner_"+d.target.acronymForIds).css("display", (hideTargetNodeBecauseOfHiddenArc || hideTargetNode) ? "none" : "");
								
				$("#node_text_"+d.source.acronymForIds).css("display", (hideSourceNodeBecauseOfHiddenArc || hideSourceNode) ? "none" : "");
				$("#node_text_"+d.target.acronymForIds).css("display", (hideTargetNodeBecauseOfHiddenArc || hideTargetNode) ? "none" : "");
				
				// The nodes have API calls they might need to make. This might change a little when expansion commands
				// are added to the system.
				if(!(hideSourceNodeBecauseOfHiddenArc || hideSourceNode)){
					d.source.fetchMetricsAndDescriptionFunc();
				}
				if(!(hideTargetNodeBecauseOfHiddenArc || hideTargetNode)){
					d.target.fetchMetricsAndDescriptionFunc();
				}
			}
		);
}

function modifyNodeDisplayedArcCounter(node, hidingArc){
	// The ontology overview system is not currently set up to remove arcs or nodes.
	// If it adapted to that, or this concept used elsewhere, this counter needs to be
	// maintained when those arcs or nodes are removed.
	if(hidingArc){
		node.displayedArcs--;
	} else {
		node.displayedArcs++;
	}
	return 0 === node.displayedArcs;
}
