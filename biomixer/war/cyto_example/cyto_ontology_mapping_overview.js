// http://www.jslint.com/
// http://www.jshint.com/, also available as Eclipse or Sublime plugin
// Strict mode is safer to develop in, ya?
"use strict";

// This is using the new API that is stable in September 2013.

// This is based off of the D3 version, and uses Cytoscape.JS. I am making Cytoscape
// meet me as close as possible to the structure implied by the REST calls.


function visWidth(){ return $("#network-view").width(); } // #chart for D3 version
function visHeight(){ return $("#network-view").height(); } // #chart for D3 version
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

// D3 removable for Cyto
//// Had to set div#chart.gallery height = 100% in CSS,
//// but this was only required in Firefox. I can't see why.
//var vis = d3.select("#chart").append("svg:svg")
//	.attr("id", "graphSvg")
//	.attr("width", visWidth())
//	.attr("height", visHeight())
//	.attr("pointer-events", "all")
////  .append('svg:g')
//    .call(d3.behavior.zoom().on("zoom", redraw))
////  .append('svg:g')
//  ;

// D3 stuff, diff in cyto
//vis.append('svg:rect')
//	.attr("width", visWidth())
//	.attr("height", visHeight())
//	.attr("id", "graphRect")
//    .style('fill', 'white');

var resizedWindow = function()
{		
// D3 removable for Cyto
//	d3.select("#graphRect")
//	.attr("width", visWidth())
//	.attr("height", visHeight());
//	
//	d3.select("#graphSvg")
//	.attr("width", visWidth())
//	.attr("height", visHeight());
//	
//    if(forceLayout){
//    	forceLayout.size([visWidth(), visHeight()]).linkDistance(linkMaxDesiredLength());
//    	// Put central node in middle of view
//    	d3.select("#node_g_"+centralOntologyAcronym).each(function(d){d.px = visWidth()/2; d.py = visHeight()/2;});
//    	forceLayout.resume();
//    }  
};

$(window).resize(resizedWindow);

resizedWindow();

function redraw() {
// D3 removable for Cyto, wasn't used originally.
//  console.log("redrawing D3", d3.event.translate, d3.event.scale);
//  vis.attr("transform",
//      "translate(" + d3.event.translate + ")"
//      + " scale(" + d3.event.scale + ")");
}

// TODO D3 stuff, is it needed for Cyto?
////Seeing if I can modulate graph gravity using bounding boxes...
//// when the nodes are outside the box, tweak the gravity higher by a small amount,
//// and decrease it when the nodes are further from the edge
//// This is happening for each node as it updates, so keep that in mind...
//var minGravity = 0.1;
//var maxGravity = 3.5;
//function gravityAdjust(number, visSize){
//	var alpha = 0.2 / forceLayout.nodes().length;
//	if(number < visSize*0.05 || visSize*0.95 < number){
//		// console.log("increase");
//		forceLayout.gravity(Math.min(maxGravity, forceLayout.gravity() * (1 + alpha)));
//	} else if(visSize*0.20 < number && number < visSize*0.80){
//		// console.log("decrease");
//		forceLayout.gravity(Math.max(minGravity, forceLayout.gravity() * (1 - alpha)));
//	} else {
//		// leave gravity as it is
//	}
//	return number;
//}
//function gravityAdjustX(number){
//	return gravityAdjust(number, visWidth());
//}
//function gravityAdjustY(number){
//	return gravityAdjust(number, visHeight());
//}


var ontologyNeighbourhoodJsonForGraph = new Object();
ontologyNeighbourhoodJsonForGraph.nodes = [];
ontologyNeighbourhoodJsonForGraph.links = [];

var globalNodePositionMap = {};

initAndPopulateGraph();


function fetchOntologyNeighbourhood(centralOntologyAcronym, cy){
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
	
//	console.log("Begun fetchingOntologyNeighbourhood");
	
	// 1) Get mappings to central ontology
	var ontologyMappingUrl = buildOntologyMappingUrlNewApi(centralOntologyAcronym);
	var ontologyMappingCallback = new OntologyMappingCallback(ontologyMappingUrl, centralOntologyAcronym, cy);
//	var fetcher = new RetryingJsonpFetcher(ontologyMappingCallback);
//	fetcher.retryFetch();
	var fetcher = closureRetryingJsonpFetcher(ontologyMappingCallback);
	fetcher();
}

function OntologyMappingCallback(url, centralOntologyAcronym, cy){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (mappingData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyMappingCallback");
//		var errorOrRetry = self.fetcher.retryFetch(mappingData);
		var errorOrRetry = self.fetcher(mappingData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var numberOfMappedOntologies = Object.keys(mappingData).length;
		
		var defaultNumOfTermsForSize = 10;
		
		// New API example: http://stagedata.bioontology.org/mappings/statistics/ontologies/SNOMEDCT/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a

		// Create the central node
		var centralOntologyNode = new Object();
		centralOntologyNode.name = "fetching";
		centralOntologyNode.description = "fetching description";
		centralOntologyNode.fixed = true; // lock central node
		// Cyto needs p instead of naked x and y
		centralOntologyNode.p = {
				x: visWidth()/2,
				y: visHeight()/2
				};
		globalNodePositionMap[centralOntologyAcronym] = centralOntologyNode.p;
		centralOntologyNode.weight = numberOfMappedOntologies; // will increment as we loop
		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
		centralOntologyNode.acronym = centralOntologyAcronym;
		centralOntologyNode.nodeColor = nextNodeColor();
		
		// TODO In the example, 'weight' is added nested within 'data':
		//	{
		//	    group: "nodes",
		//	    data: { weight: 75 },
		//	    position: { x: 200, y: 200 }
		//	}
		// If things get funky, look into that. It might be required, or we might clobber cyto properties accidentally.
		// It also had id nested in data, and source, target, but *not* position or group.
		
		// Cytoscape specific stuff (x and y above work for Cytoscape)
		centralOntologyNode.data = {};
		centralOntologyNode.data.id = centralOntologyAcronym;
		centralOntologyNode.group = "nodes";
		centralOntologyNode.selected = false,
		centralOntologyNode.selectable = true,
		centralOntologyNode.locked = false,
		centralOntologyNode.grabbable = true
		
		ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		
		// Add central node to Cytoscape right away
		cy.add(centralOntologyNode);
		var ontologyAcronymNodeMap = new Object();
		var ontologyAcronymEdgeMap = new Object();
		$(ontologyAcronymNodeMap).attr(centralOntologyAcronym, centralOntologyNode);
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		// I used degrees here and it gave me a strange mandala with gaps between groups of nodes.
		var anglePerNode = 2*Math.PI / numberOfMappedOntologies; // 360/numberOfMappedOntologies;
		var arcLength = linkMaxDesiredLength();
		var i = 0;
		$.each(mappingData,
			function(index, element){
				var acronym = index;

				if(typeof acronym === "undefined"){
					console.log("Undefined ontology entry");
				}
				
				// TODO For Cytoscape.JS, I don't like using arbor.js, and the circle layout
				// doesn't have a central node. For D3, I was using the force layout with initialization
				// in a circle.
				// For Cytoscape.JS, I don't think I can initialize and get results like I want.
				// In either case, tree layouts are still required later. Cytoscape has a native
				// layout called 'breadthfirst' that might work out, and perhaps we can tweak arbor.
				// TODO Add a positions node-id: {x, y} map for use with the 'preset' Cyto layout. 
				
				// Create the neighbouring nodes
				var ontologyNode = new Object();
				ontologyNode.name = "fetching";
				ontologyNode.description = "fetching description";
				ontologyNode.weight = 1;
				ontologyNode.fixed = false; // lock central node
				// Compute starting positions to be in a circle for faster layout
				var angleForNode = i * anglePerNode; i++;
				 // Cyto needs p instead of naked x and y
				ontologyNode.p = {
						x: visWidth()/2 + arcLength*Math.cos(angleForNode), // start in middle and let them fly outward
						y: visHeight()/2 + arcLength*Math.sin(angleForNode) // start in middle and let them fly outward
				};
				globalNodePositionMap[acronym] = ontologyNode.p;
				ontologyNode.number = defaultNumOfTermsForSize; // number of terms
				ontologyNode.acronym = acronym;
				ontologyNode.nodeColor = nextNodeColor();
				
				// Cytoscape specific stuff (x and y above work for Cytoscape)
				ontologyNode.data = {};
				ontologyNode.data.id = acronym;
				ontologyNode.group = "nodes";
				ontologyNode.selected = false,
				ontologyNode.selectable = true,
				ontologyNode.locked = false,
				ontologyNode.grabbable = true
				
				// TODO Cyto doesn't need the structure here...ontologyNeighbourhoodJsonForGraph
				var targetIndex = ontologyNeighbourhoodJsonForGraph.nodes.push(ontologyNode) - 1;
				
				// But it does like this mapping of ids to edges and nodes...
				$(ontologyAcronymNodeMap).attr(acronym, ontologyNode);
				

				// Make the links at the same time; they are done now!
				var ontologyLink = new Object();
				ontologyLink.source = centralOntologyNode; // TODO This is in data for Cyto, remove this, right?
				ontologyLink.target = ontologyNode; // TODO This is in data for Cyto, remove this, right?
				ontologyLink.value = element; // This gets used for link stroke thickness later.
				ontologyLink.numMappings = element;
				
				// Cytoscape specific
				ontologyLink.data = {};
				ontologyLink.data.id = centralOntologyNode.acronym+"->"+ontologyNode.acronym;
				ontologyLink.data.source = centralOntologyNode.data.id; //centralOntologyNode;
				ontologyLink.data.target = ontologyNode.data.id; //ontologyNode;
				ontologyLink.group = "edges"; // Cytoscape specific
				
				// TODO Cyto doesn't need the structure here...ontologyNeighbourhoodJsonForGraph
				ontologyNeighbourhoodJsonForGraph.links.push(ontologyLink);
				// But it does like this mapping of ids to edges and nodes...
				$(ontologyAcronymNodeMap).attr(ontologyLink.data.id, ontologyLink);
				
				// Add the node and link
				cy.add(ontologyNode);
				cy.add(ontologyLink);
			}
		);

		console.log("Did I add the node?");
		
		// Not sure about whether to do this here or not...
		// console.log("ontologyMappingCallback");
		populateGraph(ontologyNeighbourhoodJsonForGraph, true);
		
		applyLayout(cy);

		//----------------------------------------------------------
		
		// 2) Get details for all the ontologies (and either create or update the nodes)
		var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
		var ontologyDetailsCallback = new OntologyDetailsCallback(ontologyDetailsUrl, ontologyAcronymNodeMap, cy);
//		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
//		fetcher.retryFetch();
		var fetcher = closureRetryingJsonpFetcher(ontologyDetailsCallback);
		fetcher();
	}
	
}

function OntologyDetailsCallback(url, ontologyAcronymNodeMap, cy){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.ontologyAcronymNodeMap = ontologyAcronymNodeMap;
	var self = this;

	this.callback  = function ontologyDetailsCallback(detailsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyDetailsCallback");
//		var errorOrRetry = self.fetcher.retryFetch(detailsDataRaw);
		var errorOrRetry = self.fetcher(detailsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		//TODO For Cytoscape, to update element properties, use things like cy.batchData(<id to <prop to value>>).
		// If things with the provided ids exist already, it modifies their properties (or clobber replaces them??)
		// So...after we update our stored dat,a we can pass that on in, because the stored stuff is stored in this
		// exact way in our 'ontologyAcronymNodeMap'! Delightful!
		// We can also use cy.$(<selector>), or cy.elements(), cy.nodes() cy.edges(), cy.filter().
		
		// Loop over ontologies and add their additional properties to the nodes
		// Recall that getting *all* ontology details is the easiest (only) way,
		// so we have to skip anything that is not defined.
		var ontologiesSkipped = 0;
		var ontologyUpdateMap = {};
		$.each(detailsDataRaw,
				function(index, ontologyDetails){
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var ontologyAcronym = ontologyDetails.acronym;
					
					// TODO LEFTOFF Updating for Cytoscape
					
					// TODO For cytoscape, do we want to retrieve from cy.nodes()?
					// Batch update might make us want to keep separate data.
					// TODO It might be better to *not* keep around this ontologoy map.
					// Cytoscape updates any properties provided, which means we could make smaller
					// containers with just the new values and not the whole whack of graph data.
					// This seems like it would speed updating. I will try it that way.
					var node = $(self.ontologyAcronymNodeMap).attr(ontologyAcronym);
					
					if(typeof node === "undefined"){
						// Skip node details that aren't in our graph
						ontologiesSkipped += 1;
						return;
					}
					
					var nodeUpdate = {};
					ontologyUpdateMap[node.data.id] = nodeUpdate;
					
					nodeUpdate.name = ontologyDetails.name;
//					nodeUpdate.ONTOLOGY_VERSION_ID = ontologyDetails.id;
					nodeUpdate.uriId = ontologyDetails["@id"]; // Use the URI instead of virtual id
					// In Cytoscape, use 'content' as label
					nodeUpdate.content = ontologyDetails.name;
					// nodeUpdate.description = ontologyDetails.description; // Unavailable in details call
//					nodeUpdate.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions; // might be missing

					// TODO XXX If we want Description, I think we need to grab the most recent submission
					// and take it fromt here. This is another API call per ontology.
					// /ontologies/:acronym:/lastest_submission
					
					// --------------------------------------------------------------
					// Do this in the details callback, then? Do we need anything from details in
					// order to get metrics? Do we need the ontology id?
					// 3) Get metric details for each ontology
					{
						// The metric call has much of the info we need
						var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.acronym);
						var ontologyMetricsCallback = new OntologyMetricsCallback(ontologyMetricsUrl, node);
	//					var fetcher = new RetryingJsonpFetcher(ontologyMetricsCallback);
	//					fetcher.retryFetch();
						var fetcher = closureRetryingJsonpFetcher(ontologyMetricsCallback);
						fetcher();
					}
					
					{
						// Details are in the submissions, so we need an additional call.
						var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.acronym);
						var ontologyDescriptionCallback = new OntologyDescriptionCallback(ontologyDescriptionUrl, node);
						var fetcher = closureRetryingJsonpFetcher(ontologyDescriptionCallback);
						fetcher();
					}
				}
		);

		// We usually use very many of the ontologies, so it is likely cheaper to make the one
		// big call with no ontology acronym arguments than to cherry pick the ones we want details for.
		console.log("ontologyDetailsCallback, skipped "+ontologiesSkipped+" of total "+detailsDataRaw.length);
		
		updateDataForNodesAndLinks(ontologyUpdateMap);
			
	}
}

function OntologyMetricsCallback(url, node){
	this.url = url;
	this.node = node;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (metricDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyMetricsCallback");
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
	    
		self.node.weight = 1;
		self.node.numberOfClasses = numClasses;
		self.node.numberOfIndividuals = numIndividuals;
		self.node.numberOfProperties = numProperties;
		self.node.number = nodeSizeBasis;
		
		// console.log("ontologyMetricsCallback");
//		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
		var ontologyUpdateMap = {};
		updateDataForNodesAndLinks(ontologyUpdateMap);
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
//		console.log("Begun OntologyDescriptionCallback");
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
//		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
		var ontologyUpdateMap = {};
		updateDataForNodesAndLinks(ontologyUpdateMap);
	}
}


//function buildOntologyMappingUrl(centralOntologyVirtualId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F"+centralOntologyVirtualId+"&callback=?";
//}

function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
	return "http://stagedata.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyDetailsUrl(){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F"+"&callback=?";
//}

function buildOntologyDetailsUrlNewApi(){
	return "http://stagedata.bioontology.org/ontologies"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyMetricsUrl(ontologyVersionId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F"+ontologyVersionId+"&callback=?";
//}

function buildOntologyMetricsUrlNewApi(ontologyAcronym){
	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
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
	
	var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`
	
	// Do this after first frame is rendered for Cytoscape.
	// Will do async stuff and add to graph
//	fetchOntologyNeighbourhood(centralOntologyAcronym, cy);
	
	// If you want to toy with the original static data, try this:
	//	populateGraph(json);
	console.log("Finished initAndPopulateGraph()");
}

/*
 * Cytoscape ready function
 * NB Do not call layouts, load elements into the graph, or anything else like this.
 * Use the init options of layout and elements for these purposes (any other way?)
 * This is for pure configuration init activity.
 * Do not call functions on the cy object until this function is called.
 */
function cytoReady(evt){
	console.log('readying');
//	var cy = this;
	var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`
	

	
}

function cytoRenderedFirstFrame(evt){
	console.log('rendering first frame');
//	var cy = this;
	var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`
	
	// Since we apparently cannot add elements or layouts in the ready function, this will be the place to do it.
	// Fetch data, add elements.
	// Will do async stuff and add to graph
	fetchOntologyNeighbourhood(centralOntologyAcronym, cy);
	
	applyLayout(cy);
}

function applyLayout(cy){
	// Documentation and example differ. Example has layout called in ready,
	// rather than setting it during initialization.
	// In fact, this ready function failed to call when I set layout in the init.
//	var options = {
//			// http://arborjs.org/reference
//		    name: 'arbor',
//		    liveUpdate: true, // whether to show the layout as it's running
//		    ready: undefined, // callback on layoutready 
//		    stop: undefined, // callback on layoutstop
//		    maxSimulationTime: 4000, // max length in ms to run the layout
//		    fit: true, // reset viewport to fit default simulationBounds
//		    padding: [ 50, 50, 50, 50 ], // top, right, bottom, left
//		    simulationBounds: undefined, // [x1, y1, x2, y2]; [0, 0, width, height] by default
//		    ungrabifyWhileSimulating: true, // so you can't drag nodes during layout
//
//		    // forces used by arbor (use arbor default on undefined)
//		    repulsion: undefined, // 1000 is default
//		    stiffness: undefined, // 600 is default
//		    friction: undefined, // 0.5 is default
//		    gravity: true, // false is default
//		    fps: undefined,
//		    dt: undefined, // 0.02 is default
//		    precision: undefined, // 0.6 is default
//
//		    // static numbers or functions that dynamically return what these
//		    // values should be for each element
//		    nodeMass: undefined, 
//		    edgeLength: undefined,
//
//		    stepSize: 1, // size of timestep in simulation
//
//		    // function that returns true if the system is stable to indicate
//		    // that the layout can be stopped
//		    stableEnergy: function( energy ){
//		        var e = energy; 
//		        return (e.max <= 0.5) || (e.mean <= 0.3);
//		    }
//		};
//	var options = {name: 'circle'};
	var options = {
			name: 'preset',
			fit: 'true',
			positions: globalNodePositionMap,
			};
	cy.layout(options);
	
}

// TODO D3, no Cyto equiv I bet
//var nodeDragBehavior;
function initGraph(){

	
	// TODO Need to size this with visHeight() and visWidth()...which themselves may need to be revised
	$("#network-view").cytoscape({
//		container: document.getElementById('cy'),
		ready: cytoReady, // do not call functions on the cy object until this is called
		initrender: cytoRenderedFirstFrame,
		showOverlay: false,
		zoom: 1,
		minZoom: 1e-50,
		maxZoom: 1e50,
		pan: { x: 0, y: 0 },
		renderer: { name: 'canvas', hideEdgesInViewport: true }, // 'canvas' is default
		
		// confused with the documentation for this at http://cytoscape.github.io/cytoscape.js/
		style: cytoscape.stylesheet()
			.selector('node')
		        .css({
		          'content': 'data(name)',
		          'font-family': 'helvetica',
		          'font-size': 14,
		          'text-outline-width': 3,
		          'text-outline-color': '#888',
		          'text-valign': 'center',
		          'color': '#fff',
		          'width': 'mapData(weight, 30, 80, 20, 50)',
		          'height': 'mapData(height, 0, 200, 10, 45)',
		          'border-color': '#fff'
		        })
	        .selector(':selected')
		        .css({
		          'background-color': '#000',
		          'line-color': '#000',
		          'target-arrow-color': '#000',
		          'text-outline-color': '#000'
		        })
	        .selector('edge')
		        .css({
		          'width': 2,
		          'target-arrow-shape': 'triangle'
		        })
		}
	);
	
	// Using CSS isntead
	$("#network-view")
//	.css("background", "red")
//    .css("border-color", "black"); // can use jquery functions on 'cy' div
	.css({
		/*
		'height': '98%',
		'width': '98%',
		*/
	    'border': '2px solid #888',
	    'overflow': 'hidden !important',
	    'border-radius': '0.55em',
	    'margin': '1em',
	    'color': 'darkgray',
	    'font-weight': '100',
	    'float': 'left',
	    'background-color': 'white',
    });
	
	console.log("finished configuring cyto");
	// Or Cyto fired up via JQuery
//	$("#network-view").cytoscape({ // for some div with id 'cy'
//		  ready: function(){
//		    // you can access the core object API through cy
//
//		    console.log("ready");
//		  }
//
//		  // , ...
//		});
	
	// D3 removable for Cyto
//	forceLayout = self.forceLayout = d3.layout.force();
//	
//	//	forceLayout.drag()
//	//	.on("dragstart", function(){})
//	//	.on("dragend", function(){dragging = false;});
//	
//	// nodeDragBehavior = forceLayout.drag;
//	nodeDragBehavior = d3.behavior.drag()
//    .on("dragstart", dragstart)
//    .on("drag", dragmove)
//    .on("dragend", dragend);
//
//	// See the gravityAdjust(), which is called in tick() and modulates
//	// gravity to keep nodes within the view frame.
//	// If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
//	forceLayout
//	.friction(0.9) // use 0.2 friction to get a very circular layout
//	.gravity(.05) // 0.5
//    .distance(Math.min(visWidth(), visHeight())/1.1) // 600
//    .charge(-200) // -100
//    .linkDistance(linkMaxDesiredLength())
//    .size([visWidth(), visHeight()])
//    .start();
}

// TODO Is this useless for Cyto?
function dragstart(d, i) {
//	dragging = true;
//	// $(this).tipsy('hide');
//	$(".tipsy").hide();
//	// stops the force auto positioning before you start dragging
//	// This will halt the layout entirely, so if it tends to be unfinished for
//	// long enough for a user to want to drag a node, we need to make this more complicated...
//    forceLayout.stop();
}

//TODO Is this useless for Cyto?
function dragmove(d, i) {
//	// http://bl.ocks.org/norrs/2883411
//	// https://github.com/mbostock/d3/blob/master/src/layout/force.js
//	// Original dragmove() had call to force.resume(), which I needed to remove when the graph was stable.
//    d.px += d3.event.dx;
//    d.py += d3.event.dy;
//    d.x += d3.event.dx;
//    d.y += d3.event.dy; 
//    
//    // Don't need tick if I update the node and associated arcs appropriately.
//    // forceLayout.resume();
//    // ontologyTick(); // this is the key to make it work together with updating both px,py,x,y on d !
//    
//    d3.select(this).attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//
//    vis.selectAll("line")
//		.filter(function(e, i){ return e.source == d || e.target == d; })
//		.attr("x1", function(e) { return e.source.x; })
//		.attr("y1", function(e) { return e.source.y; })
//		.attr("x2", function(e) { return e.target.x; })
//		.attr("y2", function(e) { return e.target.y; });
//   
}

//TODO Is this useless for Cyto?
function dragend(d, i) {
//	dragging = false;
//	// $(this).tipsy('show');
//	$(".tipsy").show();
//	// of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
//    d.fixed = true;
//    
//    // Don't need the tick(), don't want the resume.
//    // ontologyTick(true);
//    // forceLayout.resume();
}

// TODO This is D3 free it appears, possibly good for Cyto.
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
						   $("<div></div>").text(ontologyData["acronym"]+":"+ontologyData["name"]).attr("class","popups-Header gwt-Label avatar avatar-resourceSet GK40RFKDB dragdrop-handle")
				   )
		   )
	 );
   
     
     var urlText = "http://bioportal.bioontology.org/ontologies/"+ontologyData["acronym"]+"?p=summary";
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
    		 "Ontology Acronym: ": "acronym",
    		 "Ontology URI: ": "uriId",
    		 "Description: ": "description",
    		 "Num Classes: ": "numberOfClasses",
    		 "Num Individuals: ": "numberOfIndividuals",
    		 "Num Properties: ": "numberOfProperties",
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
	
	// TODO This is D3 way, how do we do it with Cyto?
//	// Data constancy via key function() passed to data()
//	// Link stuff first
//	var links = vis.selectAll("line.link").data(json.links, function(d){return d.source.acronym+"->"+d.target.acronym});
//	// console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
//
//	// Add new stuff
//	if(newElementsExpected === true)
//	links.enter().append("svg:line")
//	.attr("class", "link") // Make svg:g like nodes if we need labels
//	.attr("id", function(d){ return "link_line_"+d.source.acronym+"->"+d.target.acronym})
//	.on("mouseover", highlightLink())
//	.on("mouseout", changeColourBack);
//	
//	// console.log("After append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
//	
//	// Update Basic properties
////	if(newElementsExpected === true)
//	links
//    .attr("class", "link")
//    .attr("x1", function(d) { return d.source.x; })
//    .attr("y1", function(d) { return d.source.y; })
//    .attr("x2", function(d) { return d.target.x; })
//    .attr("y2", function(d) { return d.target.y; })
//    .style("stroke-linecap", "round")
//    .attr("data-thickness_basis", function(d) { return d.value;});
	
	updateLinkScalingFactor();
	
//    links.style("stroke-width", function(d) { return ontologyLinkScalingFunc(d.value); })
//	;

	// Update Tool tip
	if(newElementsExpected === true){
//	links.append("title") // How would I *update* this if I needed to?
//		.text(function(d) { return "Number Of Mappings: "+d.numMappings; })
//			.attr("id", function(d){ return "link_title_"+d.source.acronym+"->"+d.target.acronym});
	}

	// Node stuff now
	
//	var nodes = vis.selectAll("g.node").data(json.nodes, function(d){return d.acronym});
//	// console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
//	// Add new stuff
//	if(newElementsExpected === true)
//	nodes.enter().append("svg:g")
//	.attr("class", "node")
//	.attr("id", function(d){ return "node_g_"+d.acronym})
//	// Is it ok to do call() here?
//    .call(nodeDragBehavior);
	
	// console.log("After append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	
	// Easiest to use JQuery to get at existing enter() circles
	// Otherwise we futz with things like the enter()select(function) below
	
    // I think that the lack of way to grab child elements from the enter() selection while they are
	// data bound (as is usual for most D3 selections), is what is preventing me from udpating using D3
	// idioms. This means no D3 implicit selection loops.
	// Therefore I need to update using JQuery selections on unqiue element IDs
	
	// Basic properties
	if(newElementsExpected === true){ // How would I *update* this if I needed to?
//	nodes
//	.append("svg:circle") 
//	.attr("id", function(d){ return "node_circle_"+d.acronym})
//    .attr("class", "circle")
//    .attr("cx", "0px")
//    .attr("cy", "0px")
//    .style("fill", defaultNodeColor)
//    // .style("fill", function(d) { return d.nodeColor; })
//	.attr("data-radius_basis", function(d) { return d.number;})
//    .attr("r", function(d) { return ontologyNodeScalingFunc(d.number); })
//	.on("mouseover", changeColour)
//	.on("mouseout", changeColourBack);
	}
	
	
// TODO Get this working for Cyto
	// tipsy stickiness from:
	// http://stackoverflow.com/questions/4720804/can-i-make-this-jquery-tooltip-stay-on-when-my-cursor-is-over-it
//	d3.selectAll(".circle").each(
//			function(d){
//		var me = this,
//		meData = d,
//	    leaveDelayTimer = null,
//	    visible = false,
//	    tipsyId = undefined;
//		
//	    var leaveMissedTimer = undefined;
//	    function missedEventTimer() {
//	    	leaveMissedTimer = setTimeout(missedEventTimer, 1000);
//	    	// The hover check doesn't work when we are over children it seems, and the tipsy has plenty of children...
//	    	if($("#"+me.id+":hover").length != 0 && !$(tipsyId+":hover").length != 0){
//	    		console.log("Not in thing");
//	    		leave();
//	    	}
//	    }
//	    missedEventTimer();
//		
//		function leave() {
//	        // We add a 100 ms timeout to give the user a little time
//	        // moving the cursor to/from the tipsy object
//			leaveDelayTimer = setTimeout(function () {
//	            $(me).tipsy('hide');
//	            visible = false;
//	        }, 100);
//	    }
//
//	    function enter() {
//	    	if(dragging){
//	    		return;
//	    	}
//			$(me).tipsy({
//				html: true,
//				fade: true,
//				// offset: parseInt($(me).attr("r")), // works better without this!
//				fallback: "Fetching data...",
//		        title: function() {
//		          // var d = this.__data__, c = d.i; //colors(d.i);
//		          // return 'Hi there! My color is <span style="color:' + c + '">' + c + '</span>';
//		          return createNodePopupTable(me, meData);
//		        },
//		        trigger: 'manual',
//				gravity: function() {
//					var location = "";
//					
//					if($(me).offset().top > ($(document).scrollTop() + $(window).height() / 2)){
//						location += "s";
//					} else {
//						location += "n";
//					}
//					
//					if($(me).offset().left > ($(document).scrollLeft() + $(window).width() / 2)){
//						location += "e";
//					} else {
//						location += "w";
//					}
//					// console.log("Location "+location);
//			        return location;
//			    },
//			});
//	    	
//	        if (visible) {
//	            clearTimeout(leaveDelayTimer);
//	        } else {
//	            $(me).tipsy('show');
//	            // The .tipsy object is destroyed every time it is hidden,
//	            // so we need to add our listener every time its shown
//	            var tipsy = $(me).tipsy("tip");
//	            lastDisplayedTipsy = tipsy;
//	            lastDisplayedTipsyData = meData;
//	            lastDisplayedTipsyCircle = me;
//	            tipsyId = $(me).attr("id"+"_tipsy");
//	            tipsy.attr("id", tipsyId);
//	            
//	            // For the tipsy specific listeners, change opacity.
//	            tipsy.mouseenter(function(){tipsy.css("opacity",1.0); enter(); }).mouseleave(function(){tipsy.css("opacity",0.8); leave();});
//	            tipsy.mouseover(function(){
//	            	tipsy.css("opacity",1.0);
//	    	    	clearTimeout(leaveMissedTimer);
//	    		});
//	            visible = true;
//	        }
//	    }
//	    
//		$(this).hover(enter, leave);
//		$(this).mouseover(function(){
//	    	clearTimeout(leaveMissedTimer);
//		});
//		
//		
//		// TODO Use a timer, poll style, to prevent cases where mouse events are missed by browser.
//		// That happens commonly. We'll want to hide stale open tipsy panels when this happens.
//		 d3.timer(function(){}, -4 * 1000 * 60 * 60, +new Date(2012, 09, 29));
//	}
//	);
		
	// Dumb Tool tip...not needed with tipsy popups.
//	if(newElementsExpected === true)  // How would I *update* this if I needed to?
//	nodes.append("title")
//	  .attr("id", function(d){ return "node_title_"+d.acronym})
//	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
	if(newElementsExpected === true){ // How would I *update* this if I needed to?
	// TODO Update for Cyto?
//	nodes.append("svg:text")
//		.attr("id", function(d){ return "node_text_"+d.acronym})
//	    .attr("class", "nodetext unselectable")
//	    .attr("dx", 12)
//	    .attr("dy", 1)
//	    .text(function(d) { return d.name; })
//	    // Not sure if I want interactions on labels or not. Change following as desired.
//	    .style("pointer-events", "none")
//	    // Why cannot we stop selection in IE? They are rude.
//		.attr("unselectable", "on") // IE 8
//		.attr("onmousedown", "noselect") // IE ?
//		.attr("onselectstart", "function(){ return false;}") // IE 8?
//	    // .on("mouseover", changeColour)
//	    // .on("mouseout", changeColourBack)
//	    ;
	}
	
	// TODO D3 stuff, is there a Cyto equivalent? Probably not for gravity adjsutment.
//	// XXX Doing this a second time destroys the visualization!
//	// How would we do it on only new things?
//	// Oh! It is because we are using the links and nodes references,
//	// and losing references to the existing nodes and links.
//	// I really want to make sure I keep trakc of whether we
//	// have all nodes/links, or just new ones...
//	var lastLabelShiftTime = jQuery.now();
//	var lastGravityAdjustmentTime = jQuery.now();
//	var firstTickTime = jQuery.now();
//	var maxLayoutRunDuration = 10000;
//	var maxGravityFrequency = 4000;
//	ontologyTick = function() {
//		// Stop the layout early. The circular initialization makes it ok.
//		if (forceLayout.alpha() < alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
//			forceLayout.stop();
//		}
//		
//		// For every iteration of the layout (until it stabilizes)
//		// Using this bounding box on nodes and links works, but leads to way too much overlap for the
//		// labels...Bostock is correct in saying that gravity adjustments can get better results.
//		// gravityAdjust() functions are pass through; they want to inspect values,
//		// not modify them!
//		var doLabelUpdateNextTime = false;
//		if(jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency){
//			nodes.attr("transform", function(d) { return "translate(" + gravityAdjustX(d.x) + "," + gravityAdjustY(d.y) + ")"; });
//			lastGravityAdjustmentTime = jQuery.now();
//			doLabelUpdateNextTime = true;
//		} else {
//			nodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//		}
//		
//		links
//		  .attr("x1", function(d) { return d.source.x; })
//	      .attr("y1", function(d) { return d.source.y; })
//	      .attr("x2", function(d) { return d.target.x; })
//	      .attr("y2", function(d) { return d.target.y; });
//		
//		// I want labels to aim out of middle of graph, to make more room
//		// It slows rendering, so I will only do it sometimes
//		// Commented all thsi out because I liked centering them instead.
////		if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
////			$.each($(".nodetext"), function(i, text){
////				text = $(text);
////				if(text.position().left >= visWidth()/2){
////					text.attr("dx", 12);
////					text.attr("x", 12);
////				} else {
////					text.attr("dx", - 12 - text.get(0).getComputedTextLength());
////					text.attr("x", - 12 - text.get(0).getComputedTextLength());
////				}
////			})
////			lastLabelShiftTime = jQuery.now();
////		}
//		
//	
//	}
	
	// TODO D3 stuff, Cyto equivalent? I doubt it.
	if(newElementsExpected === true){
//		forceLayout.on("tick", ontologyTick);
	}
	
	// TODO D3 stuff, update for Cyto?
	// Whenever I call populate, it adds more to this layout.
	// I need to figure out how to get enter/update/exit sort of things
	// to work for the layout.
	if(newElementsExpected === true){
//		forceLayout
//		.nodes(json.nodes)
//	    .links(json.links);
//		// Call start() whenever any nodes or links get added or removed
//		forceLayout.start();
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
var collectedPropertiesToUpdate = {};
function updateDataForNodesAndLinks(ontologyAcronymNodeMap){
	// console.log("Updating with data:");
	// console.log(json);
	
	// TODO D3 stuff, update for Cyto
	// D3 need JQuery to get this done, but Cytoscape.JS knows we want to change properties.
    var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`

    // Doing a direct update is brutally slow if called on single element updates.
    // So...we have ot wrap the call to that in a timer!
    // Duplicating the line thickness one...keep separate and out of sync to prevent longer pauses.
    $.each(ontologyAcronymNodeMap, function(prop, value) { 
    	   // if (prop in collectedPropertiesToUpdate) { continue; }
    	console.log(prop+" is "+value);
    	   collectedPropertiesToUpdate[prop] = value;
    });
    
    if(cytoscapeBatchUpdateTimer == false){
    	cytoscapeBatchUpdateTimer = true;
		window.setTimeout(function(){
				console.log("CYTOSCAPE BATCH TIMER RESET");
				cytoscapeBatchUpdateTimer = false;
				// Woudl implement locking for this and the map update if Javascript weren't single threaded
				cy.batchData(collectedPropertiesToUpdate);
				collectedPropertiesToUpdate = {};
			},
			1200);
	}
	
//	var updateLinksFromJson = function(i, d){ // JQuery is i, d
//		// Given a json encoded graph element, update all of the nested elements associated with it
//		// cherry pick elements that we might otherwise get by class "link"
//		var link = vis.select("#link_line_"+d.source.acronym+"->"+d.target.acronym);
//		link.attr("data-thickness_basis", function(d) { return d.value;})
//		link.select("title").text(function(d) { return "Number Of Mappings: "+d.numMappings; });
//	}
//	
//	var updateNodesFromJson = function(i, d){ // JQuery is i, d
//		// Given a json encoded graph element, update all of the nested elements associated with it
//		// cherry pick elements that we might otherwise get by class "node"
//		var node = vis.select("#node_g_"+d.acronym);
//		var circles = node.select("circle");
//		circles.attr("data-radius_basis", d.number);
//		circles.transition().style("fill", d.nodeColor);
//		node.select("title").text(function(d) { return "Number Of Terms: "+d.number; });
//		node.select("text")
//		.text(function(d) { return d.name; })
//		// Firefox renders dx for text poorly, shifting things around oddly,
//		// but x works for both Chrome and Firefox.
////		.attr("dx", function(){ return - this.getComputedTextLength()/2; })
//		.attr("x", function(){ return - this.getComputedTextLength()/2; })
//		;
//		
//		// Refresh popup if currently open
//		if(lastDisplayedTipsy != null
//				&& lastDisplayedTipsy.css("visibility") == "visible"
//				&& lastDisplayedTipsyData.acronym == d.acronym
//				){
//			$(lastDisplayedTipsy).children(".tipsy-inner").html(createNodePopupTable(lastDisplayedTipsyCircle, lastDisplayedTipsyData));
//		}
//	}
//	
//	$.each(json.links, updateLinksFromJson);
//	$.each(json.nodes, updateNodesFromJson);
	
	if(nodeUpdateTimer == false){
		nodeUpdateTimer = true;
		window.setTimeout(function(){
				console.log("NODE SCALING TIMER RESET");
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
var cytoscapeBatchUpdateTimer = false;

// TODO D3 stuff, but how do I make Cyto equivalent?
function highlightLink(){
	return function(d, i){
//		if(dragging){
//			return;
//		}
//	
//		var xSourcePos = d.source.x;
//		var ySourcePos = d.source.y;
//		var xTargetPos = d.target.x;
//		var yTargetPos = d.target.y;
//		
//		d3.selectAll("text").style("opacity", .2)
//			.filter(function(g, i){return g.x==d.source.x||g.y==d.source.y||g.x==d.target.x||g.y==d.target.y;})
//			.style("opacity", 1);
//			
//		d3.selectAll("line").style("stroke-opacity", .1);
//		d3.selectAll("circle").style("fill-opacity", .1)
//			.style("stroke-opacity", .2)
//			.filter(function(g, i){return g.x==d.source.x||g.y==d.source.y||g.x==d.target.x||g.y==d.target.y})
//			.style("fill-opacity", 1)
//			.style("stroke-opacity", 1);
//		d3.select(this).style("stroke-opacity", 1)
//			.style("stroke", "#3d3d3d");
//
	}
}

//TODO D3 stuff, but how do I make Cyto equivalent?
function changeColour(d, i){
//	if(dragging){
//		return;
//	}
//	var xPos=d.x;
//	var yPos=d.y;
//	
//	d3.selectAll("line").style("stroke-opacity", .1);
//	d3.selectAll("circle").style("fill-opacity", .1)
//		.style("stroke-opacity", .2);
//		
//	d3.selectAll("text").style("opacity", .2)
//		.filter(function(g, i){return g.x==d.x})
//		.style("opacity", 1);
//		
//	var sourceNode;
//	if(d3.select(this).attr("class") == "circle"){
//		sourceNode = d3.select(this);
//	} else if(d3.select(this).attr("class") == "nodetext"){
//		// If the labels aren't wired for mouse interaction, this is unneeded
//		sourceNode = d3.select(this.parentNode).select(".circle");
//	}
//	
//	sourceNode.style("fill", nodeHighlightColor)
//		.style("fill-opacity", 1)
//		.style("stroke-opacity", 1);
//		
//	var adjacentLinks = d3.selectAll("line")
//		.filter(function(d, i) {return d.source.x==xPos && d.source.y==yPos;})
//		.style("stroke-opacity", 1)
//		.style("stroke", "#3d3d3d")
//		.each(function(d){
//			d3.selectAll("circle")
//			.filter(function(g, i){return d.target.x==g.x && d.target.y==g.y;})
//			.style("fill-opacity", 1)
//			.style("stroke-opacity", 1)
//			.each(function(d){
//				d3.selectAll("text")
//				.filter(function(g, i){return g.x==d.x})
//				.style("opacity", 1);});
//	});
}

//TODO D3 stuff, but how do I make Cyto equivalent?
function changeColourBack(d, i){
//	d3.selectAll("circle")
//		.style("fill", function(e, i){ 
//			return (typeof e.nodeColor === undefined ? defaultNodeColor : e.nodeColor); 
//			})
//		.style("fill-opacity", .75)
//		.style("stroke-opacity", 1);
//	d3.selectAll("line")
//		.style("stroke", defaultLinkColor)
//		.style("stroke-opacity", .75);
//	d3.selectAll("text").style("opacity", 1);
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

//TODO Update for Cyto
function updateNodeScalingFactor(){
//	// Call this prior to redrawing. The alternative is to track on every size
//	// modification. That worked well for BioMixer, but perhaps we're better
//	// off doing a bulk computation per size-refreshing redraw that we want to make.
//	
//	var circles = vis.selectAll(".circle");
//	circles.each(function(d){
//				var basis = parseInt(this.getAttribute("data-radius_basis"));
//				if(-1 == maxNodeRawSize || basis > maxNodeRawSize){
//					maxNodeRawSize = basis;
//				}
//				if(-1 == minNodeRawSize || basis < minNodeRawSize){
//					minNodeRawSize = basis;
//				}
//		});
//	
//	circles.transition().attr("r", function(d) { return ontologyNodeScalingFunc(this.getAttribute("data-radius_basis"));});
//	
}

// TODO Update for Cyto
function updateLinkScalingFactor(){
//	// TODO This may not ever need to be called multiple times, but it would take some time to run.
//	// Make sure it actually needs to be run if it is indeed called. 
//	console.log("Ran update link");
//	// Call this prior to redrawing. The alternative is to track on every size
//	// modification. That worked well for BioMixer, but perhaps we're better
//	// off doing a bulk computation per size-refreshing redraw that we want to make.
//	$.each(vis.selectAll("line.link")[0], function(i, link){
//		link = $(link);
//		var basis = parseInt(link.attr("data-thickness_basis"));
//		if(-1 == maxLinkRawSize || basis > maxLinkRawSize){
//			maxLinkRawSize =  basis;
//		}
//		if(-1 == minLinkRawSize || basis < minLinkRawSize){
//			minLinkRawSize =  basis;
//		}
//	});
//		
//	$.each(vis.selectAll("line.link")[0], function(i, link){
//		// Given a json encoded graph element, update all of the nested elements associated with it
//		// cherry pick elements that we might otherwise get by class "node"
//		link = $(link);
//		link.css("stroke-width", function(d) { return ontologyLinkScalingFunc(link.attr("data-thickness_basis")); });
//	});
}


function ontologyNodeScalingFunc(rawValue){
	// return Math.sqrt((rawValue)/10);
	if(maxNodeRawSize == minNodeRawSize){
		return rawValue;
	}
	var factor = computeFactorOfRange(rawValue, minNodeRawSize, maxNodeRawSize);
    var diameter = linearAreaRelativeScaledRangeValue(factor, NODE_MIN_ON_SCREEN_SIZE, NODE_MAX_ON_SCREEN_SIZE);
    return diameter/2; // need radius for SVG
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
//var nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
function nextNodeColor(){
//	console.log("Don't use D3 dependency for this!");
//	currentNodeColor = currentNodeColor == 19 ? 0 : currentNodeColor + 1;
//	return nodeOrderedColors(currentNodeColor);
	return "#ff0000";
}
