var visWidth = $(document).width()-50,
    visHeight = $(document).height()-50;
var forceLayout = undefined;

var vis = d3.select("#chart").append("svg:svg")
.attr("id", "graphSvg")
    .attr("width", visWidth)
    .attr("height", visHeight)
	.attr("pointer-events", "all")
//  .append('svg:g')
    .call(d3.behavior.zoom().on("zoom", redraw))
//  .append('svg:g')
  ;

vis.append('svg:rect')
	.attr("id", "graphRect")
    .attr('width', visWidth)
    .attr('height', visHeight)
    .attr('fill', 'AliceBlue');

resizedWindow = function()
{	
	visWidth = $(document).width()-50;
    visHeight = $(document).height()-50;
	
    if(forceLayout){
    	forceLayout.size([visWidth, visHeight]);
    }
    
    d3.select("#graphSvg")
    .attr("width", visWidth)
    .attr("height", visHeight);

	d3.select("#graphRect")
    .attr('width', visWidth)
    .attr('height', visHeight);
};

$(window).resize(resizedWindow);

resizedWindow();

function redraw() {
  console.log("redrawing D3", d3.event.translate, d3.event.scale);
  vis.attr("transform",
      "translate(" + d3.event.translate + ")"
      + " scale(" + d3.event.scale + ")");
}


// TODO
/*
 * When adding data dynamically, one guy had success using d3.layout.tree().nodes(nodes) where
 * nodes contains {source: new_node, target: new_node.parent}. Something similar should work for us.
 */

// TODO Cutting initAndPopulateGraph into two methods
// Then cutting pipulateGraph into being an incremental method

d3.json("force_files/set_data.json", initAndPopulateGraph);
//d3.json("force_files/set_data.json", initAndPopulateGraphOriginal);



/* json format for graph is/was:
{
	"nodes": [
	    {
	      "name": "Cell Behavior Ontology",
	      "number": 6
	    },
	    {
	      "name": "Drosophila development",
	      "number": 132
	    }
    ],
    "links": [
	    {
	      "source": 0,
	      "target": 18,
	      "value": 2,
	      "sourceMappings": 1
	    },
	    {
	      "source": 0,
	      "target": 2,
	      "value": 2,
	      "sourceMappings": 1
	    }
    ]
}
*/

var ontologyNeighbourhoodJsonForGraph = new Object();
ontologyNeighbourhoodJsonForGraph.nodes = [];
ontologyNeighbourhoodJsonForGraph.links = [];


function fetchOntologyNeighbourhood(centralOntologyVirtualId){
	// TODO XXX Do this in the same sort of way that it occurs in BioMixer.
	// I expect this to be very much faster.
	// Then try adding web workers around things to see if it affects it further.
	
	// TODO XXX I lose all the error handling and retry handling that I set up in BioMixer.
	// This is our first loss, that we have to futz with that again. It can be recreated, or if this
	// is fasts enough, we can adapt things so that some of the Java work in BioMixer can be used here too
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
	var centralOntologyVirtualId = centralOntologyVirtualId;
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
		
		var defaultNumOfTermsForSize = 100;

		// Create the central node
		var centralOntologyNode = new Object();
		centralOntologyNode.name = "blank";
		centralOntologyNode.weight = 1;
		centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
		centralOntologyNode.virtualId = centralOntologyVirtualId;
		ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		
		var virtualIdNodeMap = new Object();
		$(virtualIdNodeMap).attr("vid"+centralOntologyVirtualId, centralOntologyNode);
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		$.each(mappingData.success.data[0].list[0].ontologyMappingStatistics,
			function(index, element){
				var virtualId = element.ontologyId;

				if(typeof virtualId === "undefined"){
					console.log("Undefined virtual id");
				}
				
				// Create the neighbouring nodes
				var ontologyNode = new Object();
				ontologyNode.name = "blank";
				ontologyNode.weight = 1;
				ontologyNode.number = defaultNumOfTermsForSize; // number of terms
				ontologyNode.virtualId = virtualId;
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

		populateGraph({nodes:[], links:[]}, false);
			
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
		// TODO XXX This is what the node size was derived from
		// Add a transformer for this (I believe in D3 this should be easy)
		// I can use the transformation algorithm from BioMixer.
		self.node.number = nodeSizeBasis;
		
		populateGraph({nodes:[], links:[]}, false);
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
	
	var centralOntologyVirtualId = 1033;

	populateGraph(json, true);
	
	// Will do async stuff and add to graph
	fetchOntologyNeighbourhood(centralOntologyVirtualId);
	
//	console.log(json);
//	populateGraph("", true);
//	populateGraph(json, true);
//	populateGraph("", false);
//	populateGraph(json, false);
}

function initGraph(){
	forceLayout = self.forceLayout = d3.layout.force();
//	forceLayout.nodes(json.nodes)
//    .links(json.links);
	
	forceLayout
	.gravity(.05)
    .distance(600)
    .charge(-100)
    .size([visWidth, visHeight])
    .start();


}

function populateGraph(json
//		, newElementsExpected
		){
	console.log("Populating with:");
	console.log(json);
	
	if(json === "undefined" || json.length == 0 || json.nodes.length == 0 && json.links.length == 0){
		// console.log("skip");
		// return;
		newElementsExpected = false;
	} else {
		newElementsExpected = true;
	}
	
	// TODO Separate the "update" part out from the enter() part
	// This talks about the pattern to follow for an update method: http://bl.ocks.org/mbostock/3808218
	
	// Link stuff first
	var links = vis.selectAll("line.link").data(json.links);
	// Add new stuff
	links.enter().append("svg:line"); // Make svg:g like nodes if we need labels
	
	// Update Basic properties
	links
    .attr("class", "link")
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; })
	.style("stroke-width", function(d) { return Math.sqrt(Math.ceil(d.value/10)); });
	
	// Update Tool tip
	links.append("title")
		.text(function(d) { return "Number Of Mappings: "+d.sourceMappings; });
		
	// Update Behaviors
	links.on("mouseover", highlightLink())
		.on("mouseout", changeColourBack("#496BB0", "#999"));

	// Node stuff now
	
	var nodes = vis.selectAll("g.node").data(json.nodes);
	// Add new stuff
	nodes.enter().append("svg:g");
	
	// Basic properties
	nodes
	.append("svg:circle")
    .attr("class", "circle")
    .attr("cx", "0px")
    .attr("cy", "0px")
	.style("fill", "#496BB0")
    .attr("r", function(d) { return Math.sqrt((d.number)/10); })
	.on("mouseover", changeColour("#FC6854", "#ff1", "#ff1", .1))
	.on("mouseout", changeColourBack("#496BB0", "#999"));
		
	// Tool tip
	nodes.append("title")
	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
	nodes.append("svg:text")
	    .attr("class", "nodetext")
	    .attr("dx", 12)
	    .attr("dy", 1)
	    .text(function(d) { return d.name; });
		
	
	// Behaviors
	nodes
    .attr("class", "node")
    .call(forceLayout.drag);
	
	// XXX Doing this a second time destroys the visualization!
	// How would we do it on only new things?
	// Oh! It is because we are using the links and nodes references,
	// and losing references to the existing nodes and links.
	// I really want to make sure I keep trakc of whether we
	// have all nodes/links, or just new ones...
	if(newElementsExpected === true){
		forceLayout.on("tick", function() {
			// For every iteration of the layout (until it stabilizes)
			links
			  .attr("x1", function(d) { return d.source.x; })
		      .attr("y1", function(d) { return d.source.y; })
		      .attr("x2", function(d) { return d.target.x; })
		      .attr("y2", function(d) { return d.target.y; });
		
			nodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
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
	
}

function initAndPopulateGraphOriginal(json){
	    forceLayout = self.forceLayout = d3.layout.force()
	        .nodes(json.nodes)
	        .links(json.links)
	        .gravity(.05)
	        .distance(600)
	        .charge(-100)
	        .size([visWidth, visHeight])
	        .start();

	    var link = vis.selectAll("line.link")
	        .data(json.links)
	      .enter().append("svg:line")
	        .attr("class", "link")
	        .attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; })
			.style("stroke-width", function(d) { return Math.sqrt(Math.ceil(d.value/10)); });
			
		link.append("title")
			.text(function(d) { return "Number Of Mappings: "+d.sourceMappings; });
			
		link.on("mouseover", highlightLink())
			.on("mouseout", changeColourBack("#496BB0", "#999"));
			
	    var node = vis.selectAll("g.node")
	        .data(json.nodes)
	      .enter().append("svg:g")
	        .attr("class", "node")
	        .call(forceLayout.drag);

	    node.append("svg:circle")
	        .attr("class", "circle")
	        .attr("cx", "0px")
	        .attr("cy", "0px")
			.style("fill", "#496BB0")
	        .attr("r", function(d) { return Math.sqrt((d.number)/10); })
			.on("mouseover", changeColour("#FC6854", "#ff1", "#ff1", .1))
			.on("mouseout", changeColourBack("#496BB0", "#999"));
			
		node.append("title")
	      .text(function(d) { return "Number Of Terms: "+d.number; });

	    node.append("svg:text")
	        .attr("class", "nodetext")
	        .attr("dx", 12)
	        .attr("dy", 1)
	        .text(function(d) { return d.name; });
			
		node.append("svg:text")
	        .attr("class", "nodetext")
	        .attr("x", 12)
	        .attr("y", 1)
	        .text(function(d) { return d.name; });

		forceLayout.on("tick", function() {
	    	// For every iteration of the layout (until it stabilizes)
	      link.attr("x1", function(d) { return d.source.x; })
	          .attr("y1", function(d) { return d.source.y; })
	          .attr("x2", function(d) { return d.target.x; })
	          .attr("y2", function(d) { return d.target.y; });

	      node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	    });
}



function highlightLink(){
	return function(d, i){
	
		xSourcePos = d.source.x;
		ySourcePos = d.source.y;
		xTargetPos = d.target.x;
		yTargetPos = d.target.y;
		
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

function changeColour(circleFill, lineFill, circlesFill, opacity){
	return function(d, i){
		
		xPos=d.x;
		yPos=d.y;
		
		d3.selectAll("line").style("stroke-opacity", .1);
		d3.selectAll("circle").style("fill-opacity", .1)
			.style("stroke-opacity", .2);
			
		d3.selectAll("text").style("opacity", .2)
			.filter(function(g, i){return g.x==d.x})
			.style("opacity", 1);
		
		var sourceNode = d3.select(this).style("fill", circleFill)
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
	};
}

function changeColourBack(circleFill, lineFill){
	return function(d, i){
		d3.selectAll("circle")
			.style("fill", circleFill)
			.style("fill-opacity", .75)
			.style("stroke-opacity", 1);
		d3.selectAll("line")
			.style("stroke", lineFill)
			.style("stroke-opacity", .75);
		d3.selectAll("text").style("opacity", 1);
	};
}


