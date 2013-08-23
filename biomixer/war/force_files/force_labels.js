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
  console.log("here", d3.event.translate, d3.event.scale);
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


//function fetchOntologyNeighbourhood(centralOntologyVirtualId){
//	// TODO XXX Do this in the same sort of way that it occurs in BioMixer.
//	// I expect this to be very much faster.
//	// Then try adding web workers around things to see if it affects it further.
//	
//	// TODO XXX I lose all the error handling and retry handling that I set up in BioMixer.
//	// This is our first loss, that we have ot futz with that again. It can be recreated, or if this
//	// is fasts enough, we can adapt things so that some of the Java work in BioMixer can be used here too
//	// I mostly need to bypass the overall architecture of BioMixer to see how it affects loading speed
//	// and responsivity, as well as to try using web workers (which don't work with GWT 2.5 right now)
//	
//	/* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
//	1) Get the mapped ontology ids from the target ontology id [starts at line 126 in OntologyMappingNeighbourhood]
//	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F1033&callback=__gwt_jsonp__.P0.onSuccess
//	   - can create nodes and links with sparse meta-data now if we want, or we can wait for more data
//	2) Get ontology details, which is one big json return [passed to line 167 for class OntologyMappingNeighbourhoodLoader nested class OntologyDetailsCallback]
//	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F&callback=__gwt_jsonp__.P1.onSuccess
//	   - fill in nodes with details from this data
//	3) Get ontology metrics for each ontology [line 82 in AutomaticOntologyExpander]
//	   - set node size (# of concepts), and tool tip properties of classes, individuals, properties, and notes
//	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F45254&callback=__gwt_jsonp__.P7.onSuccess
//	*/
//	
//	var ontologyMappingUrl = buildOntologyMappingUrl(centralOntologyVirtualId);
//	
//	// 1) Get mappings to central ontology
//	function ontologyMappingCallback(data, textStatus, jqXHR){
//		
//		var ontologyDetailsUrl = buildOntologyDetailsUrl();
//		
//		// 2) Get details for all the ontologies (and either create or update the nodes)
//		function ontologyDetailsCallback(data textStatus, jqXHR){
//			for(ontologyId in mappedOntologies){
//				var ontologyMetricsUrl(ontologyId);
//			}
//		}
//		
//	}
//	
//}


function initAndPopulateGraph(json){
	initGraph();
	
	var centralOntologyVirtualId = 1033;

//	json = fetchOntologyNeighbourhood(centralOntologyVirtualId);
	
	console.log(json);
	populateGraph("");
	populateGraph(json);
	populateGraph("");
	populateGraph(json);
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

function populateGraph(json){
	if(json === "undefined" || json.length == 0){
		return;
	}
	
	forceLayout
	.nodes(json.nodes)
    .links(json.links);
	
	forceLayout
//	.gravity(.05)
//    .distance(600)
//    .charge(-100)
//    .size([visWidth, visHeight])
    .start();
	
	
	// TODO Separate the "update" part out from the enter() part
	
	// Link stuff first
	
	var links = vis.selectAll("line.link").data(json.links);
	// Add new stuff
	links.enter().append("svg:line"); // Make svg:g liek nodes if we need labels
	
	// Basic properties
	links
    .attr("class", "link")
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; })
	.style("stroke-width", function(d) { return Math.sqrt(Math.ceil(d.value/10)); });
	
	// Tool tip
	links.append("title")
		.text(function(d) { return "Number Of Mappings: "+d.sourceMappings; });
		
	// Behaviors
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