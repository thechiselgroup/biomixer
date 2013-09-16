// http://www.jslint.com/
// http://www.jshint.com/, also available as Eclipse or Sublime plugin
// Strict mode is safer to develop in, ya?
"use strict";

// I eventually came across the post that sort of discusses our update problem, of
// having new attributes for nodes from late coming JSON:
// https://groups.google.com/forum/#!msg/d3-js/ENMlOyUGGjk/YiPc8AUKCOwJ
// http://grokbase.com/t/gg/d3-js/12cjmqc2cx/dynamically-updating-nodes-links-in-a-force-layout-diagram
// Bostock confirms that we shouldn't bind things that aren't truly new, and instead we must
// update element properties without binding.

function visWidth(){ return $("#chart").width(); }
function visHeight(){ return $("#chart").height(); }
function linkMaxDesiredLength(){ return Math.min(visWidth(), visHeight())/2 - 50; }
var alphaCutoff = 0.01; // used to stop the layout early in the tick() callback
var forceLayout = undefined;
var centralOntologyVirtualId = purl().param("virtual_ontology_id");
var dragging = false;

//var defaultNodeColor = "#496BB0";
var defaultNodeColor = "#000000";
var defaultLinkColor = "#999";
var nodeHighlightColor = "#FC6854";

// Had to set div#chart.gallery height = 100% in CSS,
// but this was only required in Firefox. I can't see why.
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
    	d3.select("#node_g_"+centralOntologyVirtualId).each(function(d){d.px = visWidth()/2; d.py = visHeight()/2;});
    	forceLayout.resume();
    }  
};

$(window).resize(resizedWindow);

resizedWindow();

function redraw() {
  console.log("redrawing D3", d3.event.translate, d3.event.scale);
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


function fetchOntologyNeighbourhood(centralOntologyVirtualId){
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
	var ontologyMappingUrl = buildOntologyMappingUrl(centralOntologyVirtualId);
	var ontologyMappingCallback = new OntologyMappingCallback(ontologyMappingUrl, centralOntologyVirtualId);
//	var fetcher = new RetryingJsonpFetcher(ontologyMappingCallback);
//	fetcher.retryFetch();
	var fetcher = closureRetryingJsonpFetcher(ontologyMappingCallback);
	fetcher();
}

function OntologyMappingCallback(url, centralOntologyVirtualId){
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
		
		var defaultNumOfTermsForSize = 10;

		// Create the central node
		var centralOntologyNode = new Object();
		centralOntologyNode.name = "fetching";
		centralOntologyNode.fixed = true; // lock central node
		centralOntologyNode.x = visWidth()/2;
		centralOntologyNode.y = visHeight()/2;
		centralOntologyNode.weight = mappingData.success.data[0].list[0].ontologyMappingStatistics.length;
		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
		centralOntologyNode.virtualId = centralOntologyVirtualId;
		centralOntologyNode.nodeColor = nextNodeColor();
		ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		
		var virtualIdNodeMap = new Object();
		$(virtualIdNodeMap).attr("vid"+centralOntologyVirtualId, centralOntologyNode);
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		var anglePerNode = 360/mappingData.success.data[0].list[0].ontologyMappingStatistics.length;
		var arcLength = linkMaxDesiredLength();
		var i = 0;
		$.each(mappingData.success.data[0].list[0].ontologyMappingStatistics,
			function(index, element){
				var virtualId = element.ontologyId;

				if(typeof virtualId === "undefined"){
					console.log("Undefined virtual id");
				}
				
				// Create the neighbouring nodes
				var ontologyNode = new Object();
				ontologyNode.name = "fetching";
				ontologyNode.weight = 1;
				ontologyNode.fixed = false; // lock central node
				// Compute starting positions to be in a circle for faster layout
				var angleForNode = i * anglePerNode; i++;
				ontologyNode.x = visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
				ontologyNode.y = visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
				ontologyNode.number = defaultNumOfTermsForSize; // number of terms
				ontologyNode.virtualId = virtualId;
				ontologyNode.nodeColor = nextNodeColor();
				var targetIndex = ontologyNeighbourhoodJsonForGraph.nodes.push(ontologyNode) - 1;
				// TODO I feel like JS doesn't allow references like this...
				$(virtualIdNodeMap).attr("vid"+virtualId, ontologyNode);
				
				// Make the links at the same time; they are done now!
				var ontologyLink = new Object();
				ontologyLink.source = centralOntologyNode;
				ontologyLink.target = ontologyNode;
				ontologyLink.value = element.totalMappings; // This gets used for link stroke thickness later.
				ontologyLink.sourceMappings = element.sourceMappings;
				ontologyLink.targetMappings = element.targetMappings;
				ontologyNeighbourhoodJsonForGraph.links.push(ontologyLink);
			}
		);

		// Not sure about whether to do this here or not...
		console.log("ontologyMappingCallback");
		populateGraph(ontologyNeighbourhoodJsonForGraph, true);

		//----------------------------------------------------------
		
		// 2) Get details for all the ontologies (and either create or update the nodes)
		var ontologyDetailsUrl = buildOntologyDetailsUrl();
		var ontologyDetailsCallback = new OntologyDetailsCallback(ontologyDetailsUrl, virtualIdNodeMap);
//		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
//		fetcher.retryFetch();
		var fetcher = closureRetryingJsonpFetcher(ontologyDetailsCallback);
		fetcher();
	}
	
}

function OntologyDetailsCallback(url, virtualIdNodeMap){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.virtualIdNodeMap = virtualIdNodeMap;
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
		
		// Loop over ontologies and add their additional properties to the nodes
		// Recall that getting *all* ontology details is the easiest (only) way,
		// so we have to skip anything that is not defined.
		$.each(detailsDataRaw.success.data[0].list[0].ontologyBean,
				function(index, ontologyDetails){
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var virtualOntologyId = ontologyDetails.ontologyId;
					// var node = ontologyNeighbourhoodJsonForGraph.;
					var node = $(self.virtualIdNodeMap).attr("vid"+virtualOntologyId);
					
					if(typeof node === "undefined"){
						// Skip node details that aren't in our graph
						return;
					}
					
					node.name = ontologyDetails.displayLabel;
					node.ONTOLOGY_VERSION_ID = ontologyDetails.id;
					node.ONTOLOGY_ABBREVIATION = ontologyDetails.abbreviation;
					node.VIRTUAL_ONTOLOGY_ID = virtualOntologyId
					node.LABEL = ontologyDetails.displayLabel;
					node.DESCRIPTION = ontologyDetails.description;
					node.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions; // might be missing
					
					// --------------------------------------------------------------
					// Do this in the details callback, then? Do we need anything from details in
					// order to get metrics? Do we need the ontology id?
					// 3) Get metric details for each ontology
					var ontologyMetricsUrl = buildOntologyMetricsUrl(node.ONTOLOGY_VERSION_ID);
					var ontologyMetricsCallback = new OntologyMetricsCallback(ontologyMetricsUrl, node);
//					var fetcher = new RetryingJsonpFetcher(ontologyMetricsCallback);
//					fetcher.retryFetch();
					var fetcher = closureRetryingJsonpFetcher(ontologyMetricsCallback);
					fetcher();
					
				}
		);

		console.log("ontologyDetailsCallback");
		updateDataForNodesAndLinks({nodes:ontologyNeighbourhoodJsonForGraph.nodes, links:[]});
			
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
		
		var metricData = metricDataRaw.success.data[0].ontologyMetricsBean;
		
		var nodeSizeBasis = 100;
		var numClasses=0, numIndividuals=0, numProperties=0;
	    if (typeof metricData !== "undefined") {
	        if (metricData.numberOfClasses != null) {
	            numClasses = metricData.numberOfClasses;
	            nodeSizeBasis = numClasses;
	        }
	        if (metricData.numberOfIndividuals != null) {
	            numIndividuals = metricData.numberOfIndividuals;
	        }
	        if (metricData.numberOfProperties != null) {
	            numProperties = metricData.numberOfProperties;
	        }
	    }
	    
		self.node.weight = 1;
		self.node.numberOfClasses = numClasses;
		self.node.numberOfIndividuals = numIndividuals;
		self.node.numberOfProperties = numProperties;
		self.node.number = nodeSizeBasis;
		
		console.log("ontologyMetricsCallback");
		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
	}
}


function buildOntologyMappingUrl(centralOntologyVirtualId){
	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F"+centralOntologyVirtualId+"&callback=?";
}

function buildOntologyDetailsUrl(){
	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F"+"&callback=?";
}

function buildOntologyMetricsUrl(ontologyVersionId){
	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F"+ontologyVersionId+"&callback=?";
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
			
			if(typeof resultData.success === "undefined") {
				if(resultData.status == "403" && resultData.body.indexOf("Forbidden") >= 0){
					console.log("Forbidden Error, no retry: "
							+"\nURL: "+callbackObject.url
							+"\nReply: "+resultData.body);
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
		    		console.log("Error: "+callbackObject.url+" --> Data: "+resultData.status);
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
	fetchOntologyNeighbourhood(centralOntologyVirtualId);
	
	// If you want to toy with the original static data, try this:
	//	populateGraph(json);
}

function initGraph(){
	forceLayout = self.forceLayout = d3.layout.force();
	
	// See the gravityAdjust(), which is called in tick() and modualtes
	// gravity to keep nodes within the view frame.
	// If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
	forceLayout
	// .friction(0.2) // use 0.2 friction to get a very circular layout
	.gravity(.05) // 0.5
    .distance(Math.min(visWidth(), visHeight())/1.1) // 600
    .charge(-200) // -100
    .linkDistance(linkMaxDesiredLength())
    .size([visWidth(), visHeight()])
    .start();
}

function createNodePopupTable(ontologyCircle, ontologyData){
	var outerDiv = $("<div></div>");
	outerDiv.addClass("popups-Popup");
	
	// TODO Tipsy and this don't get along. Tried making another parent div, didn't work.
	// Can't make the popup opaque when mouse is hovering, but maybe that's ok.
	function enter (){
		outerDiv.fadeTo(100); // 400
		console.log("enter");
	};
	function exit(){
		outerDiv.fadeTo(80); // 200
		console.log("exit");
	}
	
//	outerDiv.hover(enter, exit);
//	outerDiv.mouseover(enter);
//	outerDiv.mouseleave(exit());
 
	
	var table = $("<table></table>");
	var tBody = $("<tbody></tbody>");
	 outerDiv.append(table);
	 table.append(tBody);
	 
	 tBody.append(
			 $("<tr></tr>").append(
				   $("<td></td>").append(
						   $("<div></div>").text("BioModels").attr("class","popups-Header gwt-Label avatar avatar-resourceSet GK40RFKDB dragdrop-handle")
				   )
		   )
	 );
   
     
     var urlText = "http://bioportal.bioontology.org/ontologies/"+ontologyData["virtualId"]+"?p=summary";
     tBody.append(
    		 $("<tr></tr>").append(
    				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
    						 $("<div></div>").addClass("gwt-HTML").css({"white-space":"nowrap"}).append(
    								 $("<a></a>").attr("href", urlText).text(urlText)
    						 )
    				 )
    		 )
     );
     
     tBody.append(
    		 $("<tr></tr>").append(
    				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
    						 $("<div></div>").addClass("gwt-HTML").css({"white-space":"nowrap"}).append(
    								 $("<b></b>").text("Ontology Acronym: ")
    						 ).append(
    								 $("<span></span>").text(ontologyData["name"])
    						 )
    				 )
    		 )
     );
     
     var jsonArgs = {
    		 "Ontology Acronym:": "ONTOLOGY_ABBREVIATION",
    		 "Ontology ID: ": "virtualId",
    		 "Description: ": "DESCRIPTION",
    		 "Num Classes: ": "numberOfClasses",
    		 "Num Individuals: ": "numberOfIndividuals",
    		 "Num Properties: ": "numberOfProperties",
     };
     
     $.each(jsonArgs,function(label, key){
    	 var style = (key === "DESCRIPTION" ? {} : {"white-space":"nowrap"});
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
	var links = vis.selectAll("line.link").data(json.links, function(d){return d.source.virtualId+"->"+d.target.virtualId});
	// console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);

	// Add new stuff
	if(newElementsExpected === true)
	links.enter().append("svg:line")
	.attr("class", "link") // Make svg:g like nodes if we need labels
	.attr("id", function(d){ return "link_line_"+d.source.virtualId+"->"+d.target.virtualId})
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
		.text(function(d) { return "Number Of Mappings: "+d.sourceMappings; })
			.attr("id", function(d){ return "link_title_"+d.source.virtualId+"->"+d.target.virtualId});

	// Node stuff now
	
	var nodes = vis.selectAll("g.node").data(json.nodes, function(d){return d.virtualId});
	// console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	// Add new stuff
	if(newElementsExpected === true)
	nodes.enter().append("svg:g")
	.attr("class", "node")
	.attr("id", function(d){ return "node_g_"+d.virtualId})
	// Is it ok to do call() here?
    .call(forceLayout.drag);
	
	forceLayout.drag().on("dragstart", function(){dragging = true;}).on("dragend", function(){dragging = false;});
	
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
	.attr("id", function(d){ return "node_circle_"+d.virtualId})
    .attr("class", "circle")
    .attr("cx", "0px")
    .attr("cy", "0px")
    .style("fill", defaultNodeColor)
    // .style("fill", function(d) { return d.nodeColor; })
	.attr("data-radius_basis", function(d) { return d.number;})
    .attr("r", function(d) { return ontologyNodeScalingFunc(d.number); })
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
				offset: parseInt($(me).attr("r")),
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
	            tipsyId = $(me).attr("id"+"_tipsy");
	            tipsy.attr("id", tipsyId);
	            tipsy.hover(enter, leave);
	            tipsy.mouseover(function(){
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
//	  .attr("id", function(d){ return "node_title_"+d.virtualId})
//	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
	if(newElementsExpected === true) // How would I *update* this if I needed to?
	nodes.append("svg:text")
		.attr("id", function(d){ return "node_text_"+d.virtualId})
	    .attr("class", "nodetext unselectable")
	    .attr("dx", 12)
	    .attr("dy", 1)
	    .text(function(d) { return d.name; })
	    // Not sure if I want interactions on labels or not. Change following as desired.
	    .style("pointer-events", "none")
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
	if(newElementsExpected === true){
		forceLayout.on("tick", function() {

			// Stop the layout early. The circular initialization makes it ok.
			if (forceLayout.alpha() < alphaCutoff) {
				forceLayout.stop();
			}
			
			// Do I want nodes to avoid one another?
			// http://bl.ocks.org/mbostock/3231298
//			var q = d3.geom.quadtree(nodes),
//		      i = 0,
//		      n = nodes.length;
//			while (++i < n) q.visit(collide(nodes[i]));
//			function collide(node) {
//				  var r = node.radius + 16,
//				      nx1 = node.x - r,
//				      nx2 = node.x + r,
//				      ny1 = node.y - r,
//				      ny2 = node.y + r;
//				  return function(quad, x1, y1, x2, y2) {
//				    if (quad.point && (quad.point !== node)) {
//				      var x = node.x - quad.point.x,
//				          y = node.y - quad.point.y,
//				          l = Math.sqrt(x * x + y * y),
//				          r = node.radius + quad.point.radius;
//				      if (l < r) {
//				        l = (l - r) / l * .5;
//				        node.x -= x *= l;
//				        node.y -= y *= l;
//				        quad.point.x += x;
//				        quad.point.y += y;
//				      }
//				    }
//				    return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
//				  };
//			 svg.selectAll("circle")
//		      .attr("cx", function(d) { return d.x; })
//		      .attr("cy", function(d) { return d.y; });
			
			// For every iteration of the layout (until it stabilizes)
			// Using this bounding box on nodes and links works, but leads to way too much overlap for the
			// labels...Bostock is correct in saying that gravity adjustments can get better results.
			// gravityAdjust() functions are pass through; they want to inspect values,
			// not modify them!
			var doLabelUpdateNextTime = false;
			if(jQuery.now() - lastGravityAdjustmentTime > 4000){
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
			if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
				$.each($(".nodetext"), function(i, text){
					text = $(text);
					if(text.position().left >= visWidth()/2){
						text.attr("dx", 12);
					} else {
						text.attr("dx", - 12 - text.get(0).getComputedTextLength());
					}
				})
				lastLabelShiftTime = jQuery.now();
			}
			
		
		});
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
		var link = vis.select("#link_line_"+d.source.virtualId+"->"+d.target.virtualId);
		link.attr("data-thickness_basis", function(d) { return d.value;})
		link.select("title").text(function(d) { return "Number Of Mappings: "+d.sourceMappings; });
	}
	
	var updateNodesFromJson = function(i, d){ // JQuery is i, d
		// Given a json encoded graph element, update all of the nested elements associated with it
		// cherry pick elements that we might otherwise get by class "node"
		var node = vis.select("#node_g_"+d.virtualId);
		var circles = node.select("circle");
		circles.attr("data-radius_basis", d.number);
		circles.transition().style("fill", d.nodeColor);
		node.select("title").text(function(d) { return "Number Of Terms: "+d.number; });
		node.select("text").text(function(d) { return d.name; });
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


function highlightLink(){
	return function(d, i){
	
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
	var xPos=d.x;
	var yPos=d.y;
	
	d3.selectAll("line").style("stroke-opacity", .1);
	d3.selectAll("circle").style("fill-opacity", .1)
		.style("stroke-opacity", .2);
		
	d3.selectAll("text").style("opacity", .2)
		.filter(function(g, i){return g.x==d.x})
		.style("opacity", 1);
		
	var sourceNode;
	if(d3.select(this).attr("class") == "circle"){
		sourceNode = d3.select(this);
	} else if(d3.select(this).attr("class") == "nodetext"){
		// If the labels aren't wired for mouse interaction, this is unneeded
		sourceNode = d3.select(this.parentNode).select(".circle");
	}
	
	sourceNode.style("fill", nodeHighlightColor)
		.style("fill-opacity", 1)
		.style("stroke-opacity", 1);
		
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
	d3.selectAll("circle")
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
	
	circles.transition().attr("r", function(d) { return ontologyNodeScalingFunc(this.getAttribute("data-radius_basis"));});
	
}

function updateLinkScalingFactor(){
	// TODO This may not ever need to be called multiple times, but it would take some time to run.
	// Make sure it actually needs to be run if it is indeed called. 
	console.log("Ran update link");
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
var nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
function nextNodeColor(){
	currentNodeColor = currentNodeColor == 19 ? 0 : currentNodeColor + 1;
	return nodeOrderedColors(currentNodeColor);
}
