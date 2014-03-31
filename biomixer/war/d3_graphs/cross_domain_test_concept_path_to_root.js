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

function visWidth(){ return $("#chart").width(); }
function visHeight(){ return $("#chart").height(); }
// TODO When *not* using a circular force based graph, I don't really need to control arc lengths like this.
function linkMaxDesiredLength(){ return Math.min(visWidth(), visHeight())/2 - 50; }
var alphaCutoff = 0.01; // used to stop the layout early in the tick() callback
var forceLayout = undefined;
var centralOntologyAcronym = purl().param("ontology_acronym");
var centralConceptUri = purl().param("full_concept_id");
var uniqueIdCounter = 0;

var dragging = false;
var ontologyTick; // needs to contain the onTick listener function

// These are needed to do a refresh of popups when new data arrives and the user has the popup open
var lastDisplayedTipsy = null, lastDisplayedTipsyData = null, lastDisplayedTipsyNodeRect = null;

//var defaultNodeColor = "#496BB0";
var defaultNodeColor = "#000000";
var defaultLinkColor = "#999";
var nodeHighlightColor = "#FC6854";
var linkThickness = 3;
var nodeHeight = 8;

var nodeLabelPaddingWidth = 10;
var nodeLabelPaddingHeight = 10;

// Had to set div#chart.gallery height = 100% in CSS,
// but this was only required in Firefox. I can't see why.
var vis = d3.select("#chart").append("svg:svg")
	.attr("id", "graphSvg")
	.attr("width", visWidth())
	.attr("height", visHeight())
	.attr("pointer-events", "all")
//  .append('svg:g')
//  .call(d3.behavior.zoom().on("zoom", redraw))
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
	
	// TODO Layouts not relying on force need additional support here.
    if(forceLayout){
    	forceLayout.size([visWidth(), visHeight()]).linkDistance(linkMaxDesiredLength());
    	// Put central node in middle of view
    	d3.select("#node_g_"+centralOntologyAcronym).each(function(d){d.px = visWidth()/2; d.py = visHeight()/2;});
    	forceLayout.resume();
    }  
};

$(window).resize(resizedWindow);

resizedWindow();

// called when we zoom...but zoom is not enabled.
//function redraw() {
//  console.log("redrawing D3", d3.event.translate, d3.event.scale);
//  vis.attr("transform",
//      "translate(" + d3.event.translate + ")"
//      + " scale(" + d3.event.scale + ")");
//}

// TODO Gravity adjustment is not going to be needed for non-force layouts
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


var graphJsonFormat = new Object();
graphJsonFormat.nodes = [];
graphJsonFormat.links = [];


// Run the graph! Don't need the json really, though...
// d3.json("force_files/set_data.json", initAndPopulateGraph);
initAndPopulateGraph();


function conceptLinkSimplePopupFunction(d) { return "From: "+d.source.id+" To: "+d.target.id};

// TODO Fix...but also it doesn't render...
function conceptNodeSimplePopupFunction(d) { return "Number Of Terms: "+d.number; }

function conceptNodeLabelFunction(d) { return d.name; }

function fetchPathToRoot(centralOntologyAcronym, centralConceptUri){
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
	1) Get the root to path for the central concept
	   http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
	   - create the nodes, and do any prep for subsequent REST calls
	2) Get relational data (children, parents and mappings) for all concepts in the path to root
	   TODO example URL here
	   - fill in nodes with details from this data TODO Look at Biomixer to see what we need 
	3) Get properties for all concepts in path to root
	   TODO example URL here
	   - set node properties...TODO Look at Biomixer for what to copy
	*/
	
	// 1) Get paths to root for the central concept
	var pathsToRootUrl = buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri);
	var pathsToRootCallback = new PathsToRootCallback(pathsToRootUrl, centralOntologyAcronym, centralConceptUri);
//	var fetcher = new RetryingJsonpFetcher(pathsToRootCallback);
//	fetcher.retryFetch();
	var fetcher = closureRetryingJsonpFetcher(pathsToRootCallback);
	fetcher();
}

function PathsToRootCallback(url, centralOntologyAcronym, centralConceptUri){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (pathsToRootData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

//		var errorOrRetry = self.fetcher.retryFetch(mappingData);
		var errorOrRetry = self.fetcher(pathsToRootData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		// New API example:
		// http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
		
		var numberOfConcepts = Object.keys(pathsToRootData).length;
		
		var conceptIdNodeMap = new Object();
		
//		var defaultNumOfTermsForSize = 10;
//
//		// Create the central node
//		var centralOntologyNode = new Object();
//		centralOntologyNode.name = "fetching";
//		centralOntologyNode.description = "fetching description";
//		centralOntologyNode.fixed = true; // lock central node
//		centralOntologyNode.x = visWidth()/2;
//		centralOntologyNode.y = visHeight()/2;		
//		centralOntologyNode.weight = numberOfMappedOntologies; // will increment as we loop
//		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
//		centralOntologyNode.acronym = centralOntologyAcronym;
//		centralOntologyNode.nodeColor = nextNodeColor();
//		graphJsonFormat.nodes.push(centralOntologyNode);
//		
//		$(ontologyAcronymNodeMap).attr("vid:"+centralOntologyAcronym, centralOntologyNode);
//		
//		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
//		// so this is the first place to try a web worker out.
//
//		// Make some graph parts!
//		// Original bug hidden by force layout, but I needed radians not degrees.
//		// It looks very slightly different.
//		var anglePerNode =2*Math.PI / numberOfMappedOntologies; // 360/numberOfMappedOntologies;
//		var arcLength = linkMaxDesiredLength();
//		var i = 0;
		$.each(pathsToRootData[0],
			function(index, element){
//				var acronym = index;
//
//				if(typeof acronym === "undefined"){
//					console.log("Undefined ontology entry");
//				}

				// Create the concept nodes that exist on the paths-to-root for the central concept,
				// including the central concept node.
				var conceptNode = new Object();
				conceptNode.id = element["@id"];
				conceptNode.escapedId = encodeURIComponent(conceptNode.id);
				conceptNode.name = element.prefLabel;
				conceptNode.description = "fetching description";
				conceptNode.weight = 1;
				conceptNode.fixed = false;
//				// Compute starting positions to be in a circle for faster layout
//				var angleForNode = i * anglePerNode; i++;
//				conceptNode.x = visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
//				conceptNode.y = visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
//				conceptNode.number = defaultNumOfTermsForSize; // number of terms
				var ontologyUri = element.links.ontology;
				conceptNode.ontologyAcronym = ontologyUri.substring(ontologyUri.lastIndexOf("http://data.bioontology.org/ontologies/"));
				conceptNode.ontologyUri = ontologyUri.substring(ontologyUri.lastIndexOf("http://data.bioontology.org/ontologies/"));
				conceptNode.nodeColor = nextNodeColor();
				var targetIndex = graphJsonFormat.nodes.push(conceptNode) - 1;
				// TODO I feel like JS doesn't allow references like this...
				$(conceptIdNodeMap).attr(conceptNode.id, conceptNode);
				
				
				// TODO Concept links come from different calls. We will probably need to use the links container
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
				
//				// Make the links at the same time; they are done now!
//				var ontologyLink = new Object();
//				ontologyLink.source = centralOntologyNode;
//				ontologyLink.target = ontologyNode;
//				ontologyLink.value = element; // This gets used for link stroke thickness later.
//				ontologyLink.numMappings = element;
//				graphJsonFormat.links.push(ontologyLink);
				
				// 2) Fire off REST call for properties (can I bulk this?)
				
			}
		);

		// Not sure about whether to do this here or not...
		// console.log("ontologyMappingCallback");
		populateGraph(graphJsonFormat, true);
		
		//----------------------------------------------------------
		
		// 2) Get relational data for all the concepts, create links from them
		var relationsUrlAndPostData = buildBatchRelationUrlAndPostData(conceptIdNodeMap);
		console.log(relationsUrlAndPostData);
		var conceptRelationsCallback = new ConceptRelationsCallback(relationsUrlAndPostData, conceptIdNodeMap);
//		var fetcher = new RetryingJsonpFetcher(conceptRelationsCallback);
//		fetcher.retryFetch();
		var fetcher = closureRetryingJsonpFetcher(conceptRelationsCallback);
		fetcher();
	}
	
}

function ConceptRelationsCallback(urlAndPostData, conceptIdNodeMap){
	this.url = urlAndPostData;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.conceptIdNodeMap = conceptIdNodeMap;
	var self = this;

	this.callback  = function conceptRelationsCallback(relationsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
		console.log("Relations callback");
console.log(relationsDataRaw);
console.log(textStatus);
console.log(jqXHR);
//		var errorOrRetry = self.fetcher.retryFetch(detailsDataRaw);
		var errorOrRetry = self.fetcher(relationsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		console.log(relationsDataRaw);
		// Loop over results and grab the relations for each concept
		$.each(relationsDataRaw,
				function(index, relations){
					console.log(relations);
					return;
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var ontologyAcronym = ontologyDetails.acronym;
					// var node = graphJsonFormat.;
					var node = $(self.conceptIdNodeMap).attr("vid:"+ontologyAcronym);
					
					if(typeof node === "undefined"){
						// Skip node details that aren't in our graph
						ontologiesSkipped += 1;
						return;
					}
					
					node.name = ontologyDetails.name;
//					node.ONTOLOGY_VERSION_ID = ontologyDetails.id;
					node.uriId = ontologyDetails["@id"]; // Use the URI isntead of virtual id
					node.LABEL = ontologyDetails.name;
					// node.description = ontologyDetails.description; // Unavailable in details call
//					node.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions; // might be missing
					
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
		console.log("ontologyDetailsCallback, skipped "+ontologiesSkipped+" of total "+relationsDataRaw.length);
		updateDataForNodesAndLinks({nodes:graphJsonFormat.nodes, links:[]});
			
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
		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
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

// http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
function buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri){
	return "http://data.bioontology.org/ontologies/"+centralOntologyAcronym+"/classes/"+encodeURIComponent(centralConceptUri)+"/paths_to_root/"+"?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?";
}

// If we can use batch calls for the parent, child and mappings of each node, we save 2 REST calls per node.
// If we can use batch calls for parent, child, and mapping for several nodes, we save a lot more, but the response
// size and response times might be too long. We can use bulk asking for just one of the three relational data
// properties.
// Nodes also need a properties call each, which might be done in bulk.

function buildBatchRelationUrlAndPostData(concepts){
	// Given a set of concepts, create a batch API call to retrieve their parents, children and mappings
	// http://stagedata.bioontology.org/documentation#nav_batch
	var url = "http://data.bioontology.org/batch/"+"?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?";
	// TEMP TEST
	// url = "http://stagedata.bioontology.org/batch?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1";
	var classCollection = [];
	var postObject = {
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
//	console.log(postObject);
	
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

function buildBasicOntologyUrl(ontologyAcronym){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym
}

/*
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/mappings/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P1.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/parents/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P2.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/children/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&page=1&callback=__gwt_jsonp__.P3.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&include=properties&callback=__gwt_jsonp__.P4.onSuccess
*/	

//function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
//	return "http://stagedata.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?";
//}
//
//function buildOntologyDetailsUrlNewApi(){
//	return "http://stagedata.bioontology.org/ontologies"+"/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?";
//}
//
//function buildOntologyMetricsUrlNewApi(ontologyAcronym){
//	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?"
//}
//
//function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
//	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1"+"&callback=?"
//}

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

//	function borrowedTest(){
////		assert bro, "BRO is not found to execute batch test."
////	    class_ids = {
////	      "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Information_Resource" => "Information Resource",
////	      "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Data_Resource" => "Data Resource",
////	      "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Clinical_Care_Data" => "Clinical Care Data",
////	      "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Aggregate_Human_Data" => "Aggregate Human Data"
////	    }
////	    collection = class_ids.keys.map { |x| { "class" => x , "ontology" => bro } }
////	    call_params = {
////	      "http://www.w3.org/2002/07/owl#Class" => {
////	        "collection" => collection,
////	        "include" => "prefLabel,synonym"
////	      }
////	    }
////	    post "/batch/", call_params
//	    
//	    $.ajax({
//	        url: "http://stagedata.bioontology.org/batch?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1",
//	        type: "POST",
//	        crossDomain: true,
//	        data: 
//	        	{
//	        	"http://www.w3.org/2002/07/owl#Class": {
//	                "collection": [
//	                               {
//										"class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Information_Resource",
//										"ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
//	                               },
//	                               {
//	                            	   "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Data_Resource",
//	                            	   "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
//	                               },
//                            	   {
//                            		   "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Clinical_Care_Data",
//                            		   "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
//	                               },
//                            	   {
//                            		   "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Aggregate_Human_Data",
//                            		   "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
//	                               }
//	                               ],
//	            "include": "prefLabel,synonym"   
//	        	}
//	        	,
//	        dataType: "json",
//	        success: callbackObject.callback,
////	        error:function(xhr,status,error){
////	            alert(status);
////	        }
////	        error: callbackObject.callback // do I really??
//	        	}
//	    });
//	}
	
	function makeCall(){
		if(typeof callbackObject.url.data === "undefined"){
			jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
		} else {
//			jQuery.post(callbackObject.url.url, JSON.stringify(callbackObject.url.data), callbackObject.callback)
//			borrowedTest();
//			return;
			$.ajax({
		        url: callbackObject.url.url,
		        type: "POST",
		        crossDomain: true,
		        data: JSON.stringify(callbackObject.url.data),
		        dataType: "json",
		        success: callbackObject.callback,
//		        error:function(xhr,status,error){
//		            alert(status);
//		        }
//		        error: callbackObject.callback // do I really??
		    });
		}	
	}
	
	/*
	 * Return values: -1 is non-retry due to error, 0 is retry, 1 is success, no error.
	 */
	var count = 0;
	callbackObject.fetcher = function(resultData, something){
		if(count > 0){
			 console.log("retryFetch for: "); console.log(callbackObject.url);
			 console.log(resultData);
			 console.log("something is "+something);
		}
		count++;
			if(typeof resultData === "undefined"){
				// If not error, call for first time
				makeCall();
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
		    			makeCall()
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
	initNonForceGraph();
	
	// Will do async stuff and add to graph
	fetchPathToRoot(centralOntologyAcronym, centralConceptUri);
	
	// If you want to toy with the original static data, try this:
	//	populateGraph(json);
}

var nodeDragBehavior;
function initNonForceGraph(){
	// We use the force layout, but not really.
	// We can set the positions of everything, such as to a tree,
	// and disable the force parameters as necessary.
	// This is preferable to using any of the D3 Hierarchy visualizations,
	// since we deal with DAGs, not hierarchies.
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

function createNodePopupTable(ontologyCircle, conceptData){
	var outerDiv = $("<div></div>");
	outerDiv.addClass("popups-Popup");
	
	var table = $("<table></table>");
	var tBody = $("<tbody></tbody>");
	 outerDiv.append(table);
	 table.append(tBody);
	 
	 tBody.append(
			 $("<tr></tr>").append(
				   $("<td></td>").append(
						   $("<div></div>").text(conceptData["name"]).attr("class","popups-Header gwt-Label avatar avatar-resourceSet GK40RFKDB dragdrop-handle")
				   )
		   )
	 );
   
     
     var urlText = "http://bioportal.bioontology.org/ontologies/"+conceptData["ontologyAcronym"]+"?p=classes&conceptid="+conceptData["fullConceptId"];
     tBody.append(
    		 $("<tr></tr>").append(
    				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
    						 $("<div></div>").addClass("gwt-HTML").css({"white-space":"nowrap"}).append(
    								 $("<a></a>").attr("href", urlText).text(urlText)
    						 )
    				 )
    		 )
     );
     
     var jsonArgs = {
    		 "Concept ID": "fullConceptId",
    		 "Ontology Acronym: ": "acronym",
    		 "Ontology Homepage: ": "uriId",
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
*      This should be easier to do while working on the incrementally-added concept node cases.
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
	var links = vis.selectAll("line.link").data(json.links, function(d){return d.source.id+"->"+d.target.id});
	// console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);

	// Add new stuff
	if(newElementsExpected === true)
	links.enter().append("svg:line")
	.attr("class", "link") // Make svg:g like nodes if we need labels
	.attr("id", function(d){ return "link_line_"+d.source.id+"->"+d.target.id})
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
    .style("stroke-width", linkThickness)
    .attr("data-thickness_basis", function(d) { return d.value;});

	// Update Tool tip
	if(newElementsExpected === true)
	links.append("title") // How would I *update* this if I needed to?
		.text(conceptLinkSimplePopupFunction)
			.attr("id", function(d){ return "link_title_"+d.source.id+"->"+d.target.id});

	// Node stuff now
	
	var nodes = vis.selectAll("g.node").data(json.nodes, function(d){return d.id});
	// console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	// Add new stuff
	if(newElementsExpected === true)
	nodes.enter().append("svg:g")
	.attr("class", "node")
	.attr("id", function(d){ return "node_g_"+d.acronym})
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
	.append("svg:rect") 
//	.attr("id", function(d){ return "node_rect_"+d.escapedId})
	.attr("id", function(d){ return "node_rect_"+(uniqueIdCounter++)})
    .attr("class", "node_rect")
    .attr("x", "0px")
    .attr("y", "0px")
//    .style("fill", defaultNodeColor)
     .style("fill", function(d) { return d.nodeColor; })
    // Concept graphs have fixed node and arc sizes.
//	.attr("data-radius_basis", function(d) { return d.number;})
//    .attr("r", function(d) { return ontologyNodeScalingFunc(d.number); })
    .attr("height", nodeHeight)
    .attr("width", nodeHeight)
	.on("mouseover", changeColour)
	.on("mouseout", changeColourBack);
	
	// TODO Don't I want to do this *only* on new nodes?
	// tipsy stickiness from:
	// http://stackoverflow.com/questions/4720804/can-i-make-this-jquery-tooltip-stay-on-when-my-cursor-is-over-it
	d3.selectAll(".node_rect").each(function(d){
		var me = this,
		meData = d,
	    leaveDelayTimer = null,
	    visible = false,
	    tipsyId = undefined;
		
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
				// offset: parseInt($(me).attr("r")), // works better without this!
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
	            lastDisplayedTipsyNodeRect = me;
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
//	  .attr("id", function(d){ return "node_title_"+d.acronym})
//	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
	if(newElementsExpected === true) // How would I *update* this if I needed to?
	nodes.append("svg:text")
		.attr("id", function(d){ return "node_text_"+d.id})
	    .attr("class", "nodetext unselectable")
//	    .attr("dx", "0em")
//	    .attr("dy", "1em") // 1em down to go below baseline, 0.5em to counter padding added below
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
	
	$(".nodetext").each(function(i, d){
		var textSize = d.getBBox();
		var rect = $(d).siblings().select(".node_rect");
		rect.attr("width", textSize.width + nodeLabelPaddingWidth);
		rect.attr("height", textSize.height + nodeLabelPaddingHeight);
		// center the label in the resized rect
		$(d).attr("dx", nodeLabelPaddingWidth/2).attr("dy", textSize.height);
	});
		
	// Would do exit().remove() here if it weren't re-entrant, so to speak.
	
	// TODO I refactored the function out here...it highlights the question ofwhether this belongs in
	// an init rather than populate method. Or is it required when we add new data? Yes, I think so.
	if(newElementsExpected === true){
		forceLayout.on("tick", ontologyTick(forceLayout, nodes, links));
	}
	
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
	
}

// TODO I need to update this for the refactoring I made. When are we calling this? Ideally *only* at initialziation, right?
function ontologyTick(forceLayout, nodes, links){
	var lastLabelShiftTime = jQuery.now();
	var lastGravityAdjustmentTime = jQuery.now();
	var firstTickTime = jQuery.now();
	var maxLayoutRunDuration = 10000;
	var maxGravityFrequency = 4000;
	return function() {
	// XXX Doing this a second time destroys the visualization!
	// How would we do it on only new things?
	// Oh! It is because we are using the links and nodes references,
	// and losing references to the existing nodes and links.
	// I really want to make sure I keep trakc of whether we
	// have all nodes/links, or just new ones...

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
//		var doLabelUpdateNextTime = false;
//		if(jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency){
//			nodes.attr("transform", function(d) { return "translate(" + gravityAdjustX(d.x) + "," + gravityAdjustY(d.y) + ")"; });
//			lastGravityAdjustmentTime = jQuery.now();
//			doLabelUpdateNextTime = true;
//		} else {
			nodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//		}
		
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
		var link = vis.select("#link_line_"+d.source.id+"->"+d.target.id);
		// Concept graphs have fixed node and arc sizes.
//		link.attr("data-thickness_basis", function(d) { return d.value;})
		link.select("title").text(conceptLinkLabelFunction);
	}
	
	var updateNodesFromJson = function(i, d){ // JQuery is i, d
		// Given a json encoded graph element, update all of the nested elements associated with it
		// cherry pick elements that we might otherwise get by class "node"
		var node = vis.select("#node_g_"+d.acronym);
		var nodeRects = node.select("node_rect");
		// Concept graphs have fixed node and arc sizes.
//		nodeRects.attr("data-radius_basis", d.number);
		nodeRects.transition().style("fill", d.nodeColor);
		node.select("title").text(conceptNodeSimplePopupFunction);
		node.select("text").text(conceptNodeLabelFunction)
		// Firefox renders dx for text poorly, shifting things around oddly,
		// but x works for both Chrome and Firefox.
//		.attr("dx", function(){ return - this.getComputedTextLength()/2; })
		.attr("x", function(){ return - this.getComputedTextLength()/2; })
		;
		
		// Refresh popup if currently open
		if(lastDisplayedTipsy != null
				&& lastDisplayedTipsy.css("visibility") == "visible"
				&& lastDisplayedTipsyData.acronym == d.acronym
				){
			$(lastDisplayedTipsy).children(".tipsy-inner").html(createNodePopupTable(lastDisplayedTipsyNodeRect, lastDisplayedTipsyData));
		}
	}
	
	$.each(json.links, updateLinksFromJson);
	$.each(json.nodes, updateNodesFromJson);
	
	// Concept graphs have fixed node and arc sizes.
//	if(nodeUpdateTimer == false){
//		nodeUpdateTimer = true;
//		window.setTimeout(function(){
//				console.log("TIMER RESET");
//				nodeUpdateTimer = false;
//				updateNodeScalingFactor();
//				// The link thickness does not receive new data right now,
//				// otherwise we'd want to call the update factor function here.
//				// updateLinkScalingFactor();
//			},
//			1000);
//	}
}
//var nodeUpdateTimer = false;


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
		d3.selectAll("node_rect").style("fill-opacity", .1)
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
	d3.selectAll("node_rect").style("fill-opacity", .1)
		.style("stroke-opacity", .2);
		
	d3.selectAll("text").style("opacity", .2)
		.filter(function(g, i){return g.x==d.x})
		.style("opacity", 1);
		
	var sourceNode;
	if(d3.select(this).attr("class") == "node_rect"){
		sourceNode = d3.select(this);
	} else if(d3.select(this).attr("class") == "nodetext"){
		// If the labels aren't wired for mouse interaction, this is unneeded
		sourceNode = d3.select(this.parentNode).select(".node_rect");
	}
	
	sourceNode.style("fill", nodeHighlightColor)
		.style("fill-opacity", 1)
		.style("stroke-opacity", 1);
		
	var adjacentLinks = d3.selectAll("line")
		.filter(function(d, i) {return d.source.x==xPos && d.source.y==yPos;})
		.style("stroke-opacity", 1)
		.style("stroke", "#3d3d3d")
		.each(function(d){
			d3.selectAll("node_rect")
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
	d3.selectAll("node_rect")
		.style("fill", function(e, i){ 
			return (typeof e.nodeColor === undefined ? defaultNodeColor : e.nodeColor); 
			})
		.style("fill-opacity", .75)
		.style("stroke-opacity", 1);
	d3.selectAll("line")
		.style("stroke", defaultLinkColor)
		.style("stroke-opacity", .75);
	d3.selectAll("text").style("opacity", 1);
}

// Concept graphs have fixed node and arc sizes.
//// Maintaining relative scaled sizes of arcs and nodes depends on updating
//// the raw size range, which in this implementation, loops over all entities.
//// Only update the ranges when appropriate.
//// BioMixer used a 500 ms delay on re-doing things.
//
//// 20 * 7 seems too big. Got 20 from other transformers.
//var NODE_MAX_ON_SCREEN_SIZE = 20 * 5;
//var NODE_MIN_ON_SCREEN_SIZE = 4;
//var minNodeRawSize = -1;
//var maxNodeRawSize = -1;
//var LINK_MAX_ON_SCREEN_SIZE = 7; // 6 looks good...but if I change colors it may not.
//var LINK_MIN_ON_SCREEN_SIZE = 1;
//var minLinkRawSize = -1;
//var maxLinkRawSize = -1;
//var REFRESH_LOOP_DELAY_MS = 500;
//
//function updateNodeScalingFactor(){
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
//}
//
//function updateLinkScalingFactor() {
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
//}
//
//
//function ontologyNodeScalingFunc(rawValue){
//	// return Math.sqrt((rawValue)/10);
//	if(maxNodeRawSize == minNodeRawSize){
//		return rawValue;
//	}
//	var factor = computeFactorOfRange(rawValue, minNodeRawSize, maxNodeRawSize);
//    var diameter = linearAreaRelativeScaledRangeValue(factor, NODE_MIN_ON_SCREEN_SIZE, NODE_MAX_ON_SCREEN_SIZE);
//    return diameter/2; // need radius for SVG
//}
//
//
//function ontologyLinkScalingFunc(rawValue){
//	if(maxLinkRawSize == minLinkRawSize){
//		return rawValue;
//	}
//	var factor = computeFactorOfRange(rawValue, minLinkRawSize, maxLinkRawSize);
//	// The linear area algorithm used for nodes happens to work really well for the edges thickness too.
//    var thickness = linearAreaRelativeScaledRangeValue(factor, LINK_MIN_ON_SCREEN_SIZE, LINK_MAX_ON_SCREEN_SIZE);
//    return thickness/2;
//}
//
//function computeRangeRawSize(minRawSize, maxRawSize) {
//	return Math.max(1, maxRawSize - minRawSize);
//}
//
//function computeFactorOfRange(rawValue, minRawSize, maxRawSize) {
//	return 1.0 - (maxRawSize - rawValue) / computeRangeRawSize(minRawSize, maxRawSize);
//}
//
//function linearAreaRelativeScaledRangeValue(factor, minOnScreenSize, maxOnScreenSize) {
//	var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor
//	      * Math.PI * Math.pow(maxOnScreenSize, 2);
//	var diameter = Math.sqrt(linearArea / Math.PI);
//	return diameter;
//}
//
///*
//    private double linearFunction(double value) {
//        // Ha! A sqrt makes this not linear. Mis-named now...
//        return 2 * (4 + Math.sqrt((value) / 10));
//        return (1 + Math.sqrt((value)));
//    }
//
//    private double logFunction(double value) {
//        return 4 + Math.log(value) * 10;
//    }
// */

var currentNodeColor = -1;
var nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
var ontologyColorMap = {};
function nextNodeColor(ontologyAcronym){
	if(typeof ontologyColorMap[ontologyAcronym] === "undefined"){
		currentNodeColor = currentNodeColor == 19 ? 0 : currentNodeColor + 1;
		ontologyColorMap[ontologyAcronym] = nodeOrderedColors(currentNodeColor);
	}
	return ontologyColorMap[ontologyAcronym];
	
}
