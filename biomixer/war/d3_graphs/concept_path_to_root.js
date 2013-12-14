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

// maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.
var edgeRegistry = {}; 
var conceptIdNodeMap = new Object();

var dragging = false;
var ontologyTick; // needs to contain the onTick listener function

// These are needed to do a refresh of popups when new data arrives and the user has the popup open
var lastDisplayedTipsy = null, lastDisplayedTipsyData = null, lastDisplayedTipsyNodeRect = null;


var relationLabelConstants = {
		"inheritance": "is_a",
		"composition": "part_of",
		"mapping": "maps to",
};

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


var graphD3Format = new Object();
// Set interior when we ahve initialized the graph
//graphD3Format.nodes = forceLayout.nodes();
//graphD3Format.links = forceLayout.links();


// Run the graph! Don't need the json really, though...
// d3.json("force_files/set_data.json", initAndPopulateGraph);
initNonForceGraph();
initPopulateGraph();


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
	   http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P0.onSuccess
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
		// http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P0.onSuccess
		
		var numberOfConcepts = Object.keys(pathsToRootData).length;
		
		// Moved to global. If that's wrong, I will catch it. Will hold all node data.
//		var conceptIdNodeMap = new Object();
		
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
//		graphD3Format.nodes.push(centralOntologyNode);
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
			function(index, nodeData){
				var conceptNode = parseNode(undefined, nodeData);
				fetchConceptRelations(conceptNode, nodeData);
			}
		);
		
//		// Separate loop so that we can have the complete set of path-to-root nodes prior to firing off requests for
//		// relations.
//		oops! I think his is fubarred. Need conceptNode to this method, but passing raw data.
//		$.each(pathsToRootData[0],
//			fetchConceptRelations
//		);

		// Not sure about whether to do this here or not...
		// console.log("ontologyMappingCallback");
		updateGraphPopulation();
		
		//----------------------------------------------------------
		
	}
	
}

// Needs the arguments index, concept because the function will be called in JQuery loop. Write wrappers in callers if you don't like that.
function parseNode(index, conceptData){
//		var acronym = index;
//
//		if(typeof acronym === "undefined"){
//			console.log("Undefined ontology entry");
//		}

		// Create the concept nodes that exist on the paths-to-root for the central concept,
		// including the central concept node.
		var conceptNode = new Object();
		conceptNode.id = conceptData["@id"];
		conceptNode.escapedId = encodeURIComponent(conceptNode.id);
		conceptNode.name = conceptData.prefLabel;
		conceptNode.type = conceptData.type;
		conceptNode.description = "fetching description";
		conceptNode.weight = 1;
		conceptNode.fixed = false;
//		// Compute starting positions to be in a circle for faster layout
//		var angleForNode = i * anglePerNode; i++;
//		conceptNode.x = visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
//		conceptNode.y = visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
//		conceptNode.number = defaultNumOfTermsForSize; // number of terms
		var ontologyUri = conceptData.links.ontology;
		// "http://data.bioontology.org/ontologies/<acronym>"
		var urlBeforeAcronym = "ontologies/";
		conceptNode.ontologyAcronym = ontologyUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym)+urlBeforeAcronym.length);
		conceptNode.ontologyUri = ontologyUri;
		conceptNode.escapedOntologyUri = encodeURIComponent(conceptNode.ontologyUri);
//		conceptNode.x = 0;
//		conceptNode.y = 0;
		conceptNode.nodeColor = nextNodeColor();
		graphD3Format.nodes.push(conceptNode);
		// TODO I feel like JS doesn't allow references like this...
		$(conceptIdNodeMap).attr(conceptNode.id, conceptNode);
		
		// Could accumulate in caller?
		updateGraphPopulation();
		
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
		
		// TODO I think that I need to inspect for relations in the registry, to see if there are any
		// implicit ones that have now been fulfilled by this node being added...is that correct to do here?
		// Registry should probably only have edges indexed by the *non-present* nodes, so that there is a simple
		// lookup for incoming nodes.
		// We also check for node endpoints in the graph before registering the implicit edges, so there's no risk of
		// adding an edge when it should instead be manifested in the graph.
		
		// We manifest them inside that function, but we could just as well collect them all and do a bulk populate.
		// Maybe pass a flag in that controls whether they are accumulated or populated?
		// updateGraphPopulation({nodes:graphD3Format.nodes, links:[]}, true);
		
		// If there are implicit edges from before that link from an existing node to this new one,
		// we can now manifest them.
		manifestEdgesForNewNode(conceptNode);
		
//		// Make the links at the same time; they are done now!
//		var ontologyLink = new Object();
//		ontologyLink.source = centralOntologyNode;
//		ontologyLink.target = ontologyNode;
//		ontologyLink.value = element; // This gets used for link stroke thickness later.
//		ontologyLink.numMappings = element;
//		graphD3Format.links.push(ontologyLink);
					
		return conceptNode;
}

function fetchConceptRelations(conceptNode, conceptData){
	// 2) Get relational data for all the concepts, create links from them
	// fetchBatchRelations(); // don't exist, because of COR issues on server, cross domain, and spec issues.
	fetchParents(conceptNode, conceptData.links.parents);
	fetchChildren(conceptNode, conceptData.links.children, 1);
	fetchMappings(conceptNode, conceptData.links.mappings);
	fetchCompositionRelations(conceptNode);
}

function fetchParents(conceptNode, baseUrl){
	var relationsUrl = appendJsonpAndApiKeyArgumentsToExistingUrl(baseUrl);
	var conceptRelationsCallback = new ConceptParentsRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap);
	var fetcher = closureRetryingJsonpFetcher(conceptRelationsCallback);
	fetcher();
}

// Children requests have paging, which needs cycling internally.
function fetchChildren(conceptNode, baseUrl, pageRequested){
	var relationsUrl = appendJsonpAndApiKeyArgumentsToExistingUrl(baseUrl);
	relationsUrl += "&page="+pageRequested;
	var conceptRelationsCallback = new ConceptChildrenRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap);
	var fetcher = closureRetryingJsonpFetcher(conceptRelationsCallback);
	fetcher();
}

function fetchMappings(conceptNode, baseUrl){
	var relationsUrl = appendJsonpAndApiKeyArgumentsToExistingUrl(baseUrl);
	var conceptRelationsCallback = new ConceptMappingsRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap);
	var fetcher = closureRetryingJsonpFetcher(conceptRelationsCallback);
	fetcher();
}

function fetchCompositionRelations(conceptNode){
	var relationsUrl = buildConceptCompositionsRelationUrl(conceptNode);
	var conceptRelationsCallback = new ConceptCompositionRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap);
	var fetcher = closureRetryingJsonpFetcher(conceptRelationsCallback);
	fetcher();
}

function appendJsonpAndApiKeyArgumentsToExistingUrl(url){
	return url+"?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
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
function manifestOrRegisterImplicitRelation(parentId, childId, relationType){
	// Either register it as an implicit relation, or manifest it if both nodes are in graph.
	var edge = new Object();
	// edge source and targe tobjects will be set when manifesting the edge (when we know we have
	// node objects to add there). They are looked up by these ids.
	// TODO source/target and parent/child are not clear...which way do we need this to be?
	// I prefer using parent/child in model, but for the graph, arrow representation is clearer
	// using source and target.
	edge.sourceId = parentId;
	edge.targetId = childId;
	edge.value = 1; // This gets used for link stroke thickness later...not needed for concepts?
	edge.edgeType = relationType;
	
	// We expect neither or just one of the ids will be in the registry, since we only register
	// node ids that do not exist in our graph. This should be enforced by processing edges
	// whenever we add a node to the graph.
	
	var matchId = undefined, otherId = undefined;
	var parentIdInRegistry = false, childIdInRegistry = false;
	if(parentId in edgeRegistry){
		matchId = parentId;
		otherId = childId;
		parentIdInRegistry = true;
	}
	if(childId in edgeRegistry){
		if(matchId){
			console.log("Logical error; cannot have both edge ends in the graph already.")
		}
		matchId = childId;
		otherId = parentId;
		childIdInRegistry = true;
	}
	
	
	// Logic...begs for assertions.
	var parentIdInGraph = parentId in conceptIdNodeMap;
	var childIdInGraph = childId in conceptIdNodeMap;
	console.log(parentIdInGraph+" and "+parentIdInGraph);
	if((parentIdInGraph && childIdInGraph) && (parentIdInRegistry && childIdInRegistry)){
		console.log("Problem: Both ids are already in the graph, and both in the registry. Should we be here?");
	}
	if(matchId && !(parentIdInGraph || childIdInGraph)){
		console.log("Problem: If matchId is true, there must be at least one of the concepts in the graph already.");
	}
	if(!parentIdInGraph && !childIdInGraph){
		console.log("Problem: If neither node is in graph already.");
	}
	
	// For non-symmetric relations, this assertion is not valid.
//	if(!matchId && (!parentIdInGraph != !childIdInGraph)){ // ! != ! is an XOR...with safety in case values aren't boolean
//		console.log("Problem: If matchId is false, then only one of the concepts can be in the graph already");
////		console.log(childId+" and parent "+parentId);
////		console.log(edgeRegistry);
////		console.log(conceptIdNodeMap);
//	}
	
	// Register edges for which we have one in the graph, and none in the registry.
	if(!matchId && (!parentIdInGraph != !childIdInGraph)){
		// Register this implicit edge.
		var conceptIdNotInGraph = (parentId in conceptIdNodeMap) ?  childId : parentId;
		var conceptInGraph = (parentId in conceptIdNodeMap) ?  parentId : childId;
		if(!(conceptIdNotInGraph in edgeRegistry)){
			edgeRegistry[conceptIdNotInGraph] = {};
		}
		edgeRegistry[conceptIdNotInGraph][conceptInGraph] = edge;
		
	} else if(parentIdInGraph && childIdInGraph) {
		// If both are in the graph, we'll be manifesting it immediately.
		// Manifest this edge. We have a matching id in the registry, and the other end of the edge.
		edge.source = conceptIdNodeMap[edge.sourceId];
		edge.target = conceptIdNodeMap[edge.targetId];
		graphD3Format.links.push(edge);
		updateGraphPopulation();
		
		if(matchId){
			// Clear our cruft
			delete edgeRegistry[matchId][otherId];
			
			if(Object.keys(edgeRegistry[matchId]).length == 0){
				delete edgeRegistry[matchId];
			}
		}
	}
}

function manifestEdgesForNewNode(conceptId){
	// Because registry contains edges for which there *was* no node for the idnex,
	// and there *are* nodes for the other ends of the edge, we can manifest all of
	/// them when we are doign so due to a new node appearing.
	if(conceptId in edgeRegistry){
		$.each(edgeRegistry[conceptId], function(index, edge){
			var otherId = (edge.targetId == conceptId) ? edge.targetId : edge.sourceId ;

			edge.source = conceptIdNodeMap[edge.sourceId];
			edge.target = conceptIdNodeMap[edge.targetId];
			graphD3Format.links.push(edge);
			updateGraphPopulation();
			
			// Clear that one out...safe while in the loop?
			delete edgeRegistry[conceptId][otherId];
		});
		
		if(Object.keys(edgeRegistry[conceptId]).length == 0){
			delete edgeRegistry[conceptId];
		}
	}
}

function expandAndParseNodeIfNeeded(conceptId, relatedConceptId, conceptPropertiesData){
	// Can determine on the basis of the relatedConceptId if we should request data for the
	// conceptId provided, or if we should parse provided conceptProperties (if any).
	// TODO PROBLEM What if the conceptId is already going to be fetched and processed because
	// it has a fetcher running on the basis of some other relation?
	// In paths to root, it won't happen, because we would only want to parse from the original call.
	// In mappings, we only expand mapped nodes in the original call.
	// In term neighbourhood, we do indeed parse nodes on the basis of parent and child
	// relations, as well as composition relations. But...only if they are related to the
	// central one. So simply checking for that combination of facts here works out fine.
	
	// Because we expand for term neighbourhood relation calls, and those come in two flavors
	// (node with properties for children and parents, and just node IDs for compositions)
	// we want to support parsing the data directly as well as fetching additional data.
	
	if(relatedConceptId === centralConceptUri && visualization === "term_neighbourhood"
		&& !(conceptId in conceptIdNodeMap)){
		// Manifest the node; parse the properties if available.
		// TODO Shall I make any changed so save on REST calls for this?
		// We know that we will get the composition relations via a properties call,
		// and that has all the data we need from a separate call for properties...
		// but that subsystem relies on the fact that the node is created already.
		
		if(!typeof conceptPropertiesData === "undefined" && Object.keys(conceptPropertiesData).length > 0){
			// This happens when it is a child or parent inheritance relation for term neighbourhood
			var conceptNode = parseNode(undefined, conceptPropertiesData);
			// Populate or return for callers-in-loops?
			fetchConceptRelations(conceptNode, conceptPropertiesData);
		} else {
			// This happens when it is a composite relation for term neighbourhood
			// Making the call to create it will get all relations automatically.
			// 1) Get paths to root for the central concept
			// "http://purl.bioontology.org/ontology/SNOMEDCT/16089004","
			var urlBeforeAcronym = "ontologies/";
			var urlAfterAcronym = "/";
			var chunk = conceptUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym)+urlBeforeAcronym.length);
			var ontologyAcronym = chunk.substring(0, chunk.lastIndexOf(urlAfterAcronym));
			console.log("single node ontology acronym is "+ontologyAcronym);
			
			// Pretty sure I shouldn't bother using a single fetch to grab what is in front of us...
			
			var url = buildConceptUrlNewApi(ontologyAcronym, conceptUri);
			var callback = new FetchOneConceptCallback(url, conceptUri);
			var fetcher = closureRetryingJsonpFetcher(callback);
			fetcher();
		}
	}
}

function FetchOneConceptCallback(url, conceptUri){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (conceptPropertiesData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

		var errorOrRetry = self.fetcher(conceptPropertiesData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		parseNode(undefined, conceptPropertiesData);

		fetchConceptRelations(conceptPropertiesData);

		// Do I need to do the update? Sure I do...
		console.log("didn't do anything to add to graph now, did I?");
	}
	
}

function endsWith(string, suffix) {
    return string.indexOf(suffix, string.length - suffix.length) !== -1;
};

// currently oriented to grabbing data for a single concept. Might do batch later when that works server side
// for cross domain requests.
// Can process mapping, parent, properties, and children, even if not all are passed in.
// This is useful given that parents don't show up if children are requested.
function ConceptCompositionRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap){
	this.url = relationsUrl;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.conceptNode = conceptNode;
	this.conceptIdNodeMap = conceptIdNodeMap;
	var self = this;

	this.callback  = function ConceptCompositionRelationsCallback(relationsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

		var errorOrRetry = self.fetcher(relationsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}

		// Loop over results, properties, then mappings, parents, children.
		$.each(relationsDataRaw.properties,
			function(index, propertyObject){
				// Composition relations can only be parsed from properties received with the "include=properties"
				// parameter. This means that although properties are received elsewhere (path to root, children),
				// those property sets never give us the composition relations. 
				// But...children property sets do have all the other things we need to get the seed of data for a node
				// (being the @id and the ontology link from which we need to extract the true-and-valid ontology acronym)
			
				// See line 71 TermWithoutRelationsJsonParser for how it was dealt with in Java.
				// We already parsed for other (automatic) properties when we first got this node's
				// data, so here we only do composite relations and maybe additional properties if needed.
				// This is properties such as: "http://purl.bioontology.org/ontology/SNOMEDCT/has_part"
				// I know, not the most general property name...
				if(endsWith(index, "has_part")){
					$.each(has_part, function(index, childPartId){
						// TODO Need to register all node ids we get, so that for the different visualizations, we can expand differently.
						// For path to root, we only expand those path to root nodes (determined at beginning)
						// For term neighbourhood, we only expand the direct neighbours of the central node (determined during fetches).
						// For mappings, we only expand based on the first mapping call (determined during fetches).
						// Ergo, we need to expand composition mappings if we are in the term neighbourhood vis.
						
						// PROBLEM Seems like I want to manifest nodes before doing arcs, but in this case, I want to know
						// if the relation exists so I can fetch the node data...
						manifestOrRegisterImplicitRelation(conceptNode.id, childPartId, relationTypes.composition);
						expandAndParseNodeIfNeeded(childPartId, conceptNode.id, {});
					});
				}
				
				if(endsWith(index, "is_part")){
					$.each(is_part, function(index, parentPartId){
						expandAndParseNodeIfNeeded(parentPartId, conceptNode.id, {});
						manifestOrRegisterImplicitRelation(parentPartId, conceptNode.id, relationTypes.composition);
					});
				}
				
			}
		);
				
		// We manifest them inside that function, but we could just as well collect them all and do a bulk populate.
		// Maybe pass a flag in that controls whether they are accumulated or populated?
//		updateGraphPopulation({nodes:graphD3Format.nodes, links:[]}, true);
				
	}
}
		
function ConceptChildrenRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap){
	this.url = relationsUrl;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.conceptNode = conceptNode;
	this.conceptIdNodeMap = conceptIdNodeMap;
	var self = this;

	this.callback  = function ConceptChildrenRelationsCallback(relationsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

		var errorOrRetry = self.fetcher(relationsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}

		// Example: http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F91837002/children
		$.each(relationsDataRaw.collection,
				function(index, child){
				// TODO Should we be doing first step property parsing for all nodes in children and parents? Properties come with them...
				
				// Was parsed in ConceptRelationshipJsonParser near line 75 (parseNewChildren)
				// We have a complication though...paged results! Oh great...
				// That alone is reason to fire these events separately anyway, but we can keep all the parsing stuck in this same
				// place and fire off an additional REST call.
				var childId = child["@id"];
				
				// TODO Need to register all node ids we get, so that for the different visualizations, we can expand differently.
				// For path to root, we only expand those path to root nodes.
				// For term neighbourhood, we only expand the direct neighbours of the central node.
				// For mappings, we only expand based on the first mapping call.
				// This will go through a whole process of adding the node, if the node is supposed to be
				// expanded for the current visualization (children and parents for term neighbourhood).
				expandAndParseNodeIfNeeded(childId, conceptNode.id, child);
				
				// Save the data in case we expand to include this node
				manifestOrRegisterImplicitRelation(conceptNode.id, childId, relationLabelConstants.inheritance);
			}
		);
		
		// We manifest them inside that function, but we could just as well collect them all and do a bulk populate.
		// Maybe pass a flag in that controls whether they are accumulated or populated?
//		updateDataForNodesAndLinks({nodes:graphD3Format.nodes, links:[]});
		
		// Children paging...only if children called directly?
		 var pageNumber = relationsDataRaw["page"];
		 var maxPageNumber = relationsDataRaw["pageCount"];
		 if(maxPageNumber > pageNumber){
			 fetchChildren(conceptNode, this.url, pageNumber+1);
		 }
	}
}


function ConceptParentsRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap){
	this.url = relationsUrl;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.conceptNode = conceptNode;
	this.conceptIdNodeMap = conceptIdNodeMap;
	var self = this;

	this.callback  = function ConceptParentsRelationsCallback(relationsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

		var errorOrRetry = self.fetcher(relationsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		$.each(relationsDataRaw,
				function(index, parent){
			// TODO Should we be doing first step property parsing for all nodes in children and parents? Properties come with them...
			
					var parentId = parent["@id"];
					
					// Save the data in case we expand to include this node
					manifestOrRegisterImplicitRelation(parentId, conceptNode.id, relationLabelConstants.inheritance);
		
					// TODO Need to register all node ids we get, so that for the different visualizations, we can expand differently.
					// For path to root, we only expand those path to root nodes.
					// For term neighbourhood, we only expand the direct neighbours of the central node.
					// For mappings, we only expand based on the first mapping call.
					// This will go through a whole process of adding the node, if the node is supposed to be
					// expanded for the current visualization (children and parents for term neighbourhood).
					expandAndParseNodeIfNeeded(parentId, conceptNode.id, parent);
		});
		
		// We manifest them inside that function, but we could just as well collect them all and do a bulk populate.
		// Maybe pass a flag in that controls whether they are accumulated or populated?
//		updateDataForNodesAndLinks({nodes:graphD3Format.nodes, links:[]});
		
		
	}
}
		
function ConceptMappingsRelationsCallback(relationsUrl, conceptNode, conceptIdNodeMap){
	this.url = relationsUrl;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.conceptNode = conceptNode;
	this.conceptIdNodeMap = conceptIdNodeMap;
	var self = this;

	this.callback  = function ConceptMappingsRelationsCallback(relationsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

		var errorOrRetry = self.fetcher(relationsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}

		$.each(relationsDataRaw,
				function(index, mapping){
			// ConceptMappingImplementation, we get partial properties on the basis of the mappings REST call
			manifestOrRegisterImplicitRelation(mapping.classes[0]["@id"], mapping.classes[1]["@id"], relationLabelConstants.mapping);
		});
		
		// We manifest them inside that function, but we could just as well collect them all and do a bulk populate.
		// Maybe pass a flag in that controls whether they are accumulated or populated?
//		updateDataForNodesAndLinks({nodes:[], links:graphD3Format.links});
		
	}
}

// http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P0.onSuccess
function buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri){
	return "http://data.bioontology.org/ontologies/"+centralOntologyAcronym+"/classes/"+encodeURIComponent(centralConceptUri)+"/paths_to_root/"+"?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

// If we can use batch calls for the parent, child and mappings of each node, we save 2 REST calls per node.
// If we can use batch calls for parent, child, and mapping for several nodes, we save a lot more, but the response
// size and response times might be too long. We can use bulk asking for just one of the three relational data
// properties.
// Nodes also need a properties call each, which might be done in bulk.

function buildBatchRelationUrl(concept){
	// Unused currently due to specification issues
	// 400-800 for children, properties each, 500-900 for parents, 500-900 for mappings
	// 500-1.2s for all four combined. Looks like savings to me.
	return "http://data.bioontology.org/ontologies/"+concept.ontologyAcronym+"/classes/"+concept.escapedId
	+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"
	+"&include=children,parents,mappings,properties"
	+"&callback=?";
}

function buildConceptUrlNewApi(ontologyAcronym, conceptUri){
	return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/classes/"+encodeURIComponent(conceptUri)
	+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"
	+"&callback=?";
}

function buildConceptCompositionsRelationUrl(concept){
	return "http://data.bioontology.org/ontologies/"+concept.ontologyAcronym+"/classes/"+concept.escapedId
	+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"
	+"&include=properties"
	+"&callback=?";
}

function buildBatchRelationUrlAndPostData(concepts){
	// Given a set of concepts, create a batch API call to retrieve their parents, children and mappings
	// http://stagedata.bioontology.org/documentation#nav_batch
	var url = "http://data.bioontology.org/batch/"+"?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
	// TEMP TEST
	url = "http://stagedata.bioontology.org/batch?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";
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
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/mappings/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P1.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/parents/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P2.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/children/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&page=1&callback=__gwt_jsonp__.P3.onSuccess
http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F138875005/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&include=properties&callback=__gwt_jsonp__.P4.onSuccess
*/	

//function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
//	return "http://stagedata.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
//}
//
//function buildOntologyDetailsUrlNewApi(){
//	return "http://stagedata.bioontology.org/ontologies"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
//}
//
//function buildOntologyMetricsUrlNewApi(ontologyAcronym){
//	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
//}
//
//function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
//	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
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
	
	function makeCall(){
		if(typeof callbackObject.url.data === "undefined"){
			jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
		} else {
//			jQuery.post(callbackObject.url.url, JSON.stringify(callbackObject.url.data), callbackObject.callback)
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
	callbackObject.fetcher = function(resultData, something){
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

function initPopulateGraph(json){
	
	// Will do async stuff and add to graph
	fetchPathToRoot(centralOntologyAcronym, centralConceptUri);
	
	// If you want to toy with the original static data, try this:
	//	updateGraphPopulation(json);
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
	
	graphD3Format.nodes = forceLayout.nodes();
	graphD3Format.links = forceLayout.links();
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

function createNodePopupTable(conceptRect, conceptData){
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
   
     
     var urlText = "http://bioportal.bioontology.org/ontologies/"+conceptData["ontologyAcronym"]+"?p=classes&conceptid="+conceptData["escapedId"];
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
    		 "Concept ID: ": "id",
    		 "Ontology Acronym: ": "ontologyAcronym",
    		 "Ontology Homepage: ": "ontologyUri",
     };
     $.each(jsonArgs,function(label, key){
    	 var style = (key === "description" ? {} : {"white-space":"nowrap"});
    	 tBody.append(
        		 $("<tr></tr>").append(
        				 $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
        						 $("<div></div>").addClass("gwt-HTML").css(style).append(
        								 $("<b></b>").text(label)
        						 ).append(
        								 $("<span></span>").text(conceptData[key])
        						 )
        				 )
        		 )
         );
     });

     return outerDiv.prop("outerHTML");
}

//// From http://stackoverflow.com/questions/9539294/adding-new-nodes-to-force-directed-layout
//function myGraph(el) {
//	
//	// I already do all this in initNonForceGraph()
////	// set up the D3 visualisation in the specified element
////	var w = $(el).innerWidth(),
////	h = $(el).innerHeight();
////	
////	var vis = this.vis = d3.select(el).append("svg:svg")
////	.attr("width", w)
////	.attr("height", h);
////	
////	var force = d3.layout.force()
////	.gravity(.05)
////	.distance(100)
////	.charge(-100)
////	.size([w, h]);
//	
//	var nodes = force.nodes(),
//	links = force.links();
//
//    // Add and remove elements on the graph object
//    this.addNode = function (id) {
//        nodes.push({"id":id});
//        update();
//    }
//
//    this.removeNode = function (id) {
//        var i = 0;
//        var n = findNode(id);
//        while (i < links.length) {
//            if ((links[i]['source'] == n)||(links[i]['target'] == n)) links.splice(i,1);
//            else i++;
//        }
//        nodes.splice(findNodeIndex(id),1);
//        update();
//    }
//
//    this.addLink = function (source, target) {
//        links.push({"source":findNode(source),"target":findNode(target)});
//        update();
//    }
//
//    var findNode = function(id) {
//        for (var i in nodes) {if (nodes[i]["id"] === id) return nodes[i]};
//    }
//
//    var findNodeIndex = function(id) {
//        for (var i in nodes) {if (nodes[i]["id"] === id) return i};
//    }
//
//
//    var update = function () {
//
//        var link = vis.selectAll("line.link")
//            .data(links, function(d) { return d.source.id + "-" + d.target.id; });
//
//        link.enter().insert("line")
//            .attr("class", "link");
//
//        link.exit().remove();
//
//        var node = vis.selectAll("g.node")
//            .data(nodes, function(d) { return d.id;});
//
//        var nodeEnter = node.enter().append("g")
//            .attr("class", "node")
//            .call(force.drag);
//
//        nodeEnter.append("image")
//            .attr("class", "circle")
//            .attr("xlink:href", "https://d3nwyuy0nl342s.cloudfront.net/images/icons/public.png")
//            .attr("x", "-8px")
//            .attr("y", "-8px")
//            .attr("width", "16px")
//            .attr("height", "16px");
//
//        nodeEnter.append("text")
//            .attr("class", "nodetext")
//            .attr("dx", 12)
//            .attr("dy", ".35em")
//            .text(function(d) {return d.id});
//
//        node.exit().remove();
//
//        force.on("tick", function() {
//          link.attr("x1", function(d) { return d.source.x; })
//              .attr("y1", function(d) { return d.source.y; })
//              .attr("x2", function(d) { return d.target.x; })
//              .attr("y2", function(d) { return d.target.y; });
//
//          node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//        });
//
//        // Restart the force layout.
//        force.start();
//    }
//
//    // Make it all go
//    update();
//}

// Add new stuff like this:
//graph = new myGraph("#graph");
//
//// You can do this from the console as much as you like...
//graph.addNode("Cause");
//graph.addNode("Effect");
//graph.addLink("Cause", "Effect");
//graph.addNode("A");
//graph.addNode("B");
//graph.addLink("A", "B");

/**
* This function should be used when adding brand new nodes and links to the
* graph. Do not call it to update properties of graph elements.
* TODO Make this function cleaner and fully compliant with the above description!
*      This should be easier to do while working on the incrementally-added concept node cases.
*/
function updateGraphPopulation(){
//	console.log("Populating with:");
//	console.log(json);
	
//	if(json === "undefined" || json.length == 0 || json.nodes.length == 0 && json.links.length == 0){
//		// console.log("skip");
//		// return;
//		newElementsExpected = false;
//	}
	
	var boundLinks = populateGraphEdges(graphD3Format.links);
	
	var boundNodes = populateGraphNodes(graphD3Format.nodes);
	
	// Would do exit().remove() here if it weren't re-entrant, so to speak.
	
	// TODO I refactored the function out here...it highlights the question ofwhether this belongs in
	// an init rather than populate method. Or is it required when we add new data? Yes, I think so.
//	if(newElementsExpected === true){
	// Do this after enter() and exit() are handled
		forceLayout.on("tick", ontologyTick(forceLayout, boundNodes, boundLinks));
//	}
	
	// Whenever I call populate, it adds more to this layout.
	// I need to figure out how to get enter/update/exit sort of things
	// to work for the layout.
//	if(newElementsExpected === true){
		// forceLayout
		// .nodes(nodes.enter())
	    // .links(links.enter());
		forceLayout
//		.nodes(json.nodes)
//	    .links(json.links);
		// Call start() whenever any nodes or links get added or removed
		forceLayout.start();
//	}
	
}

function populateGraphEdges(linksData){
	if(linksData.length == 0){
		return [];
	}
	
	// Data constancy via key function() passed to data()
	// Link stuff first
	var links = vis.selectAll("line.link").data(linksData, function(d){	console.log(d); return d.source.id+"->"+d.target.id});
	 console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);

	 
	// Add new stuff
	links.enter().append("svg:line")
	.attr("class", "link") // Make svg:g like nodes if we need labels
	.attr("id", function(d){ return "link_line_"+d.source.id+"->"+d.target.id})
	.on("mouseover", highlightLink())
	.on("mouseout", changeColourBack)
    .attr("class", "link")
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; })
    .style("stroke-linecap", "round")
    .style("stroke-width", linkThickness)
    .attr("data-thickness_basis", function(d) { return d.value;});

	console.log("After append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
	
	// Update Tool tip
	links
	.enter() // this is new...used to do to all linked data...
	.append("title") // How would I *update* this if I needed to?
		.text(conceptLinkSimplePopupFunction)
			.attr("id", function(d){ return "link_title_"+d.source.id+"->"+d.target.id});

	links.exit().remove();
	
	return links;
}

function populateGraphNodes(nodesData){
	
	if(nodesData.length == 0){
		return [];
	}
	
	var nodes = vis.selectAll("g.node").data(nodesData, function(d){return d.id});
	// console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	// Add new stuff
	var nodesEnter = nodes.enter().append("svg:g")
	.attr("class", "node")
	.attr("id", function(d){ return "node_g_"+d.acronym})
    .call(nodeDragBehavior);
	
	// console.log("After append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
	
	// Easiest to use JQuery to get at existing enter() circles
	// Otherwise we futz with things like the enter()select(function) below
	
    // I think that the lack of way to grab child elements from the enter() selection while they are
	// data bound (as is usual for most D3 selections), is what is preventing me from udpating using D3
	// idioms. This means no D3 implicit selection loops.
	// Therefore I need to update using JQuery selections on unqiue element IDs
	
	// Basic properties
	nodesEnter
	.append("svg:rect") 
//	.attr("id", function(d){ return "node_rect_"+d.escapedId})
	.attr("id", function(d){ return "node_rect_"+(uniqueIdCounter++)})
    .attr("class", "node_rect")
//    .attr("x", "0px")
//    .attr("y", "0px")
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
		
		// TODO This creates a timer per popup, which is sort of silly. Figure out another way.
	    var leaveMissedTimer = undefined;
	    function missedEventTimer() {
	    	leaveMissedTimer = setTimeout(missedEventTimer, 1000);
	    	// The hover check doesn't work when we are over children it seems, and the tipsy has plenty of children...
	    	if($("#"+me.id+":hover").length != 0 && !$(tipsyId+":hover").length != 0){
//	    		console.log("Not in thing "+me.id+" and tipsyId "+tipsyId);
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
	            
	            // <circle id="node_circle_PR" class="circle" cx="0px" cy="0px" data-radius_basis="80856" r="15.569889228495246" original-title="" style="fill: #d62728; fill-opacity: 0.75; stroke-opacity: 1;"></circle>
	            // <rect id="node_rect_8" class="node_rect" x="0px" y="0px" height="29" width="146" original-title="" style="fill: #fc6854; fill-opacity: 1; stroke-opacity: 1;"></rect>
	            
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
//	nodesEnter.append("title")
//	  .attr("id", function(d){ return "node_title_"+d.acronym})
//	  .text(function(d) { return "Number Of Terms: "+d.number; });
	
	// Label
		nodesEnter.append("svg:text")
//		.attr("id", function(d){ return "node_text_"+d.id})
		.attr("id", function(d){ return "node_text_"+(uniqueIdCounter++)})
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
	
	nodes.exit().remove();
	
	return nodes;
	
}

// TODO I need to update this for the refactoring I made. When are we calling this? Ideally *only* at initialziation, right?
function ontologyTick(forceLayout, boundNodes, boundLinks){
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
//		if(nodes.length > 0)
			boundNodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//		}
		
		if(boundLinks.length > 0)
			boundLinks
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
		var node = vis.select("#node_g_"+d.escapedId);
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
	d3.selectAll(".node_rect")
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
