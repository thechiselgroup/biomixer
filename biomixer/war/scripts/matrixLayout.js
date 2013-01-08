/*******************************************************************************
 * Copyright 2012 Elena Voyloshnikova
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/

var defaultSize = 300;

var _matrixLineSeparatorColor = "#DDDDDD"; // Light grey

// Keep this value live on update
var _numNodes = 0;

var _rectSize = 16; // Determines row/column size as well as their header font sizes
var _textSizeAdjustment = 0; // May want to shrink text relative to cell size later.

var margin = {top: 200, right: 50, bottom: 50, left: 200};

function _computeSize(){
	if(_numNodes > 0){
		return _rectSize * _numNodes;
	} else {
		return defaultSize;
	}
}

function matrixLayoutForEmbed(div, json){
	// Refactoring into an init and update section...keep original for the embed for now.
	var refObj = {};

	_initGui(div, refObj);
	_initMatrix(div, refObj);
	updateMatrixLayoutString(div, refObj, json);
}

function updateMatrixLayoutString(div, refObj, jsonString){
	var jsonObject = eval('(' + jsonString + ')');
	return updateMatrixLayout(div, refObj, jsonObject);
}

function initMatrixLayout(div){
	var refObj = {};
	_initGui(div, refObj);
	_initMatrix(div, refObj);
	return refObj;
	
} // end of initMatrixLayout


function _initGui(div, refObj){
	
	d3.select(div).append("p")
	.text("Order: ");

	// add a selection menu
	var menu = d3.select(div).append("select")
		.attr("id", "order");
	
	menu.append("option")
	  .text("by Label")
	  .attr("value", "name");
	
	menu.append("option")
	  .text("by Ontology")
	  .attr("value", "group");
	
	menu.on("change", _order(refObj));

	refObj.menu = menu[0][0];
}

function _resize(div, refObj){
	// _resize expects the svg appended in _initMatrix
	// Set all the sizes
	
	var size = _computeSize();
	
	d3.select(div).select("svg")
	// refObj.svg
	.attr("width", size 
    		+ margin.left + margin.right
	)
	.attr("height", size
			+ margin.top + margin.bottom
	);

	refObj.x.rangeBands([0, size]);
	
}

function _resizeAllScalableElements(){
	// Uses both D3 and JQuery
	// Want this as a voluntary function rather than some listener,
	// so I can control when it gets called.
	// Experimental approach to updating the size of all elements that
	// have properties set based on the size.
	// Each one may have one or more non-trivial property-to-size relations
	// that can be expressed as a closure.
	// Such objects will carry a particular class that I can grab a hold of,
	// and call their stored size-property function here.
	
	d3.selectAll(".resizeCallback").each(
			function(){
				$(this).data("resizeCallback")(this);
				}
			);
}


function _initMatrix(div, refObj){
	// TODO The margins need to be computed to account for label size,
	// rather than being an arbitrary number.
	
	var x = d3.scale.ordinal().rangeBands([0, _computeSize()]),
	    z = d3.scale.linear().domain([0, 4]).clamp(true),
	    c = d3.scale.category10().domain(d3.range(10));
		
	refObj.x = x;
	refObj.z = z;
	refObj.c = c;
	
	var svg = d3.select(div).append("svg");
	// Store away for _resize method calls in the future.
	refObj.svg = svg;
	
	// _resize expects the svg appended in _initMatrix
	_resize(div, refObj);
	
	// Store away innermost g for many uses later on. This is delicate!!
	var inner_g = svg
	    .style("margin-left", margin.left + " px")
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
		.attr("pointer-events", "all")
	  .append('svg:g')
	    .call(d3.behavior.zoom().on("zoom", _redraw))
	  .append('svg:g');
	  
	// This essentially gets hidden behind everything in the matrix, so maybe we don't need it.
//	inner_g.append('svg:rect')
//	    .attr('width',size)
//	    .attr('height', size)
//	    .attr('fill', 'white');
	    
	refObj.inner_g = inner_g;
	
	// Some context nested functions:
	function _redraw() {
		// TODO This allows the user to zoom until the SVG is too small to
		// find with the mouse, and thus the user is unable to zoom back in.
		// Also, it doesn't adjust the gap between the dropdown box and the svg,
		// making for an increase in dead space.
		
		// Context nested
		// console.log("here", d3.event.translate, d3.event.scale);
		
		inner_g.attr("transform",
		"translate(" + d3.event.translate + ")"
		+ " scale(" + d3.event.scale + ")");
	};
	
} // end of initMatrix()

function updateMatrixLayout(div, refObj, jsonObject){
	var x = refObj.x;
	var c = refObj.c;
	// var svg = refObj.svg;
	var inner_g = refObj.inner_g; // original code obfuscated as the "svg" variable
	
	// Very important to update this for resizing purposes at various points
	_numNodes = jsonObject.nodes.length;
	
	drawLayout(jsonObject);
	
	function drawLayout(data){
	  var matrix = [],
	      nodes = data.nodes,
	      n = nodes.length;
	  
	  _resize(div, refObj);
	
	  // TODO Get links prepped with node name indices. Move from arrays to associative arrays or something.
	  // Or prepare links/cells such that they have functions on their target and source to return their
	  // positions, and these functions can be called to re-order things. So nodes can still have positions
	  // assigned like this, but links shouldn't have positions...meh...
	  // TODO Really what I want is to be able to mirror the Java structure more closely in the JSON
	  // passed in. That's not hard. Will it be hard to modify this code to respond to that format
	  // instead?
	  // TODO Or looking further, can I pass some serialized version of the Java data in, which is
	  // handled here as the same object, saving on memory...OverlayTypes?
	  // Part of using the Java-BioMixer data format is that it makes links strongly dependent on nodes
	  // which is a good thing, because they are useless without nodes, and in updating links, we can iterate
	  // over nodes. Is this good D3 practice?
	  // TODO I should focus on getting this working first. The Java to convert to JSON is not hard, and is
	  // easy to change, whereas getting this modified requires me to understand the code well. Moving forward.
	  
	  // Compute index per node.
	  nodes.forEach(function(node, i) {
	      node.index = i;
	      node.count = 0;
	      matrix[i] = d3.range(n).map(function(j) { return {x: j, y: i, z: 0}; });
	  });
	
	  // Convert links to matrix; count character occurrences.
	  data.links.forEach(function(link) {
		  matrix[link.source][link.target].z += link.value;
		  matrix[link.target][link.source].z += link.value;
		  matrix[link.source][link.source].z += link.value;
		  matrix[link.target][link.target].z += link.value;
		  nodes[link.source].count += link.value;
		  nodes[link.target].count += link.value;
	  });
	
	  // Precompute the orders.
	  var orders = {
	    name: d3.range(n).sort(function(a, b) { return d3.ascending(nodes[a].name, nodes[b].name); }),
	    count: d3.range(n).sort(function(a, b) { return nodes[b].count - nodes[a].count; }),
	    group: d3.range(n).sort(function(a, b) { return nodes[b].group - nodes[a].group; })
	  };
	  
	  refObj.orders = orders;
	
	  // The default sort order.
	  x.domain(refObj.menu.options[refObj.menu.selectedIndex].value);
	
//	  inner_g.append("rect resizeCallback")
//	      .attr("class", "background")
//	      .attr("width", _computeSize())
//	      .attr("height", _computeSize())
//	      .style("fill", "#eee")
//	  	  .each(function(){
//    	  		$(this).data("resizeCallback", 
//    					  function(that){	d3.select(that).attr("width", _computeSize()).attr("height", _computeSize())	}
//    	  		)
//        });
	
	  // TODO I believe I need to grab the enter, update and exit selections here,
	  // and carry out most of the following only for enter, and make new actions for exit.
	  // Luckily, I think all the elements I need to add and remove are indeed tied to the bound
	  // elements of columns, rows and cells. Anything that isn't will be annoying to cope with...
	  
	  var rowsUpdating = inner_g.selectAll(".row")
	      .data(matrix);
	  var rowsEntering = rowsUpdating.enter();
	  var rowsExiting = rowsUpdating.exit();
	  
	  var newRows = rowsEntering.append("g")
	      .attr("class", "row")
	      .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
	      .each(_row);
	
	  // Add the resizeCallback function via jquery
	  newRows.append("line")
	  	  .attr("class", "matrixSeparatorLine resizeCallback")
	      .attr("x2", _computeSize())
	      .attr("y1", _rectSize)
		  .attr("y2", _rectSize)
	      .style("stroke", _matrixLineSeparatorColor)
	      .style("stroke-width", 2)
	      .each(function(){
	    	  $(this).data("resizeCallback", 
	    			  function(that){	d3.select(that).attr("x2", _computeSize())	}
	    	  )
	      });
		  
	  newRows.append("line")
	  	  .attr("class", "headerColorLine")
		  .attr("x1", -3)
		  .attr("x2", -3)
		  .attr("y1", 0)
		  .attr("y2", _rectSize) // used to be literal 16
		  .style("stroke-width", 5)
		  .style("stroke", function(d, i){return c(nodes[i].group)});
	  
	  // Add the row headers
	  newRows.append("text")
	      .attr("class", "headerText")
	      .attr("x", -6)
	      .attr("y", x.rangeBand() / 2)
	      .attr("dy", ".32em")
	      .attr("text-anchor", "end")
		  .style("font-size", _rectSize-_textSizeAdjustment+"px") // used to be 15px literal
	      .text(function(d, i) { return nodes[i].name; })
		 .on ("mouseover", sideTextMouseover)
		  .on("mouseout", mouseout);
	  
	  rowsExiting.remove();
	
	  var columnsUpdating = inner_g.selectAll(".column")
      	.data(matrix);
	  var columnsEntering = columnsUpdating.enter();
	  var columnsExiting = columnsUpdating.exit();
	  
	  var newCols = columnsEntering.append("g")
	      .attr("class", "column")
	      .attr("transform", function(d, i) { return "translate(" + x(i) + ")rotate(-90)"; });
	
	  // Add the resizeCallback function via jquery
	  newCols.append("line")
	  	  .attr("class", "matrixSeparatorLine resizeCallback")
	      .attr("x1", -_computeSize())
	      .attr("y1", _rectSize)
		  .attr("y2", _rectSize)
	      .style("stroke", _matrixLineSeparatorColor)
	      .style("stroke-width", 2)
	      .each(function(){
	    	  $(this).data("resizeCallback", 
	    			  function(that){	d3.select(that).attr("x1", -_computeSize())	}
	    	  )
	      });
		  
	  newCols.append("line")
	      .attr("class", "headerColorLine")
		  .attr("x1", 3)
		  .attr("x2", 3)
		  .attr("y1", 0)
		  .attr("y2", _rectSize) // used to be literal 16
		  .style("stroke-width", 5)
		  .style("stroke", function(d, i){return c(nodes[i].group)});
		  
	  // Add the col headers
	  newCols.append("text")
	      .attr("class", "headerText")
	      .attr("x", 6)
	      .attr("y", x.rangeBand() / 2)
	      .attr("dy", ".32em")
	      .attr("text-anchor", "start")
		  .style("font-size", _rectSize-_textSizeAdjustment+"px") // used to be 15px literal
	      .text(function(d, i) { return nodes[i].name; })
		  .on ("mouseover", topTextMouseover)
		  .on("mouseout", mouseout);
	  
	  columnsExiting.remove();
	  
	  // Now, we need all the orderings sorted back out. Removals created gaps,
	  // and new ones appear to be inserted willy nilly (due to usage of indices,
	  // in my opinion).
	  _resizeAllScalableElements();
	  _order(refObj)(); // Returns a closure, so call it now.
	
	  //create cells (mappings and empty cells) in every row
	  function _row(rowData) {
		  // 'this' will be a new row <g class="row"/> element.
		  // The 'rowData' function argument will be the data associated with that row.
		  var emptyUpdate = d3.select(this).selectAll(".cell")
		  	.data(rowData);
		  var emptyNew = emptyUpdate.enter();
		  var emptyExit = emptyUpdate.exit();
      
		  // If we want mouse overs, we need empty cells.
		  // There is a performance hit, so I have not added them, for now.
		  // TODO If I want mouse overs for empty cells, use some columns and rows with listeners instead.
		  // That works out to 2*n elements instead of n*n.
			//	      emptyNew
			//	      .append("rect")
			//			.attr("class", "empty")
			//	        .attr("x", function(d) { return x(d.x); })
			//	        .attr("width", x.rangeBand())
			//	        .attr("height", x.rangeBand())
			//			.style("fill", "#eee")
			//	      .append("title")
			//	        .text(function(d, i){return d.x + ", "+ d.y});
		
	      // Deal with cells that have data
	      // TODO This is rebinding data with a filter...what does that end up doing?
	      d3.select(this).selectAll(".cell")
	        .data(rowData.filter(function(d) { return d.z; }))
	      .enter()
	      .append("rect")
		    .filter(function(d){return d.x!=d.y})
	        .attr("class", "cell")
	        .attr("x", function(d) { return x(d.x); })
	        .attr("width", _rectSize)
	        .attr("height", _rectSize)
			.style("fill-opacity", 1)
			.style("fill", function(g,i) { 
				colour1 = c(nodes[g.x].group);
				colour2 = c(nodes[g.y].group);
				return  gradient(g.x, g.y, colour1, colour2);
				})
	        .on("mouseover", mouseover)
	        .on("mouseout", mouseout);
	  }
	
	  //compute cell colors
	  function gradient(index1, index2, colour1, colour2){
		  var gradient = inner_g.append("svg:defs")
		  .append("svg:linearGradient")
			.attr("id", "gradient"+index1+index2)
			.attr("x1", "0%")
			.attr("y1", "100%")
			.attr("x2", "100%")
			.attr("y2", "0%")
			.attr("spreadMethod", "pad");
	
		  gradient.append("svg:stop")
			.attr("offset", "50%")
			.attr("stop-color", colour2)
			.attr("stop-opacity", 1);
	
		  gradient.append("svg:stop")
			.attr("offset", "50%")
			.attr("stop-color", colour1)
			.attr("stop-opacity", 1);
			
		  return "url(#gradient"+index1+index2+")";
	  }
	  
	  function mouseover(p, i) {
		  d3.selectAll(".row text")
	      	.filter( function(d, i) { return i == p.y&&i!=p.x; })
	      	.style("fill", "red")
	      	.style("font-size", "20px");
		  
	      d3.selectAll(".column text")
	      	.filter(function(d, i) { return i == p.x&&i!=p.y; })
	      	.style("fill",  "red")
	      	.style("font-size", "20px");	
	      
		  d3.selectAll(".empty")
		  	.filter(function(f){return f.x==p.x})
		  	.style("fill", "#F4F3D7");	
		  
		  d3.selectAll(".empty")
		  	.filter(function(f, i){return f.y==p.y})
		  	.style("fill", "#F4F3D7");
	  }
	
	  function mouseout() {
		  d3.selectAll("text").classed("active", false)
			.style("fill", "black")
			.style("font-weight", "normal")
			.style("font-size", _rectSize-_textSizeAdjustment+"px"); // used to be 15px literal
		  
		  d3.selectAll(".cell")
		  	.filter(function(d){return d.x!=d.y})
			.style("fill", function(g){ 
				colour1 = c(nodes[g.x].group);
				colour2 = c(nodes[g.y].group);
				return  gradient(g.x, g.y, colour1, colour2);
				})
			.style("stroke-width", 0);
		  
		  d3.selectAll(".empty")
		  	.style("fill", "#eee");
	  }
	
	  function topTextMouseover(g, i){
		  d3.select(this)
		  	.style("fill", "red")
		  	.style("font-size", "20px");
		  
		  d3.selectAll(".cell")
		  	.filter(function(d){return i==d.x})
		  	.each(function(f, i){
		  		d3.selectAll(".row text")
		  			.filter(function(p, i){return i==f.y})
		  			.style("fill", "red")
		  			.style("font-size", "20px");
		  		
		  		d3.selectAll(".empty")
		  			.filter(function(d){return d.y==f.y})
		  			.style("fill", "#F4F3D7");	
		  	});
		  
		d3.selectAll(".empty")
			.filter(function(f){return f.x==i})
			.style("fill", "#F4F3D7");	
	  }
	  
	  function sideTextMouseover(g, i){
	    d3.select(this)
	    	.style("fill", "red")
	    	.style("font-size", "20px");
	    
		d3.selectAll(".cell")
			.filter(function(d){return i==d.y})
			.each(function(f, i){
				d3.selectAll(".column text")
					.filter(function(p, i){return i==f.x})
					.style("fill", "red")
					.style("font-size","20px");
				
				d3.selectAll(".empty")
					.filter(function(d){return d.x==f.x})
					.style("fill", "#F4F3D7");	
		});
		
		d3.selectAll(".empty")
			.filter(function(f){return f.y==i})
			.style("fill", "#F4F3D7");	
	   }
	} // end of drawLayout()
}

function _order(refObj) {
	// I only want one call to this to run at a time, so I have a flag for this.
	// the extraCall flag makes sure that if multiple calls are made, on last extra one always gets through.
	// This ensures we have a valid layout after skipping additional calls.
	var running;
	var extraCall;
  	return function(){ // d, i were here, but unused

  		var x = refObj.x;
  		var inner_g = refObj.inner_g;
  		var orders = refObj.orders;
  		
  		// x.domain(orders[this.value]);
  		x.domain(orders[refObj.menu.value]);

  		var t = inner_g.transition().duration(500); // originally 2500

  		t.selectAll(".row")
	      	.delay(function(d, i) { return x(i) * 4; })
	      	.attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
	      .selectAll(".cell")
	      	.delay(function(d) { return x(d.x) * 4; })
	      	.attr("x", function(d) { return x(d.x); }); 

  		t.selectAll(".empty")
  			.delay(function(d) { return x(d.x) * 4; })
  			.attr("x", function(d) { return x(d.x); });

  		t.selectAll(".column")
  			.delay(function(d, i) { return x(i) * 4; })
  			.attr("transform", function(d, i) { return "translate(" + x(i) + ")rotate(-90)"; });
  		
  	}
} // end of order()
