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

function matrixLayout(div, json){
	
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
	
	var jsonObject = eval('(' + json + ')');
		
	var margin = {top: 200, right: 50, bottom: 50, left: 200}
		width = 2000,
	    height = 2000;
	
	var x = d3.scale.ordinal().rangeBands([0, width]),
	    z = d3.scale.linear().domain([0, 4]).clamp(true),
	    c = d3.scale.category10().domain(d3.range(10));
	
	var svg = d3.select(div).append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	    .style("margin-left", margin.left + " px")
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
		.attr("pointer-events", "all")
	  .append('svg:g')
	    .call(d3.behavior.zoom().on("zoom", redraw))
	  .append('svg:g');
		
	svg.append('svg:rect')
	    .attr('width',width)
	    .attr('height', height)
	    .attr('fill', 'white');
	
	
	function redraw() {
	  console.log("here", d3.event.translate, d3.event.scale);
	  svg.attr("transform",
	      "translate(" + d3.event.translate + ")"
	      + " scale(" + d3.event.scale + ")");
	};
	
	drawLayout(jsonObject);
	
	function drawLayout(data){
	  var matrix = [],
	      nodes = data.nodes,
	      n = nodes.length;
	
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
	
	  // The default sort order.
	  x.domain(orders.name);
	
	  svg.append("rect")
	      .attr("class", "background")
	      .attr("width", width)
	      .attr("height", height)
	      .style("fill", "#eee");
	
	  var row = svg.selectAll(".row")
	      .data(matrix)
	    .enter().append("g")
	      .attr("class", "row")
	      .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
	      .each(row);
	
	  row.append("line")
	      .attr("x2", width)
	      .style("stroke", "white")
	      .style("stroke-width", 2);
		  
	  row.append("line")
		  .attr("x1", -3)
		  .attr("x2", -3)
		  .attr("y1", 0)
		  .attr("y2", 16)
		  .style("stroke-width", 5)
		  .style("stroke", function(d, i){return c(nodes[i].group)});
	  
	  row.append("text")
	      .attr("x", -6)
	      .attr("y", x.rangeBand() / 2)
	      .attr("dy", ".32em")
	      .attr("text-anchor", "end")
		  .style("font-size", "15px")
	      .text(function(d, i) { return nodes[i].name; })
		 .on ("mouseover", sideTextMouseover)
		  .on("mouseout", mouseout);
	
	  var column = svg.selectAll(".column")
	      .data(matrix)
	    .enter().append("g")
	      .attr("class", "column")
	      .attr("transform", function(d, i) { return "translate(" + x(i) + ")rotate(-90)"; });
	
	  column.append("line")
	      .attr("x1", -width)
	      .style("stroke", "white")
	      .style("stroke-width", 2);
		  
	  column.append("line")
		  .attr("x1", 3)
		  .attr("x2", 3)
		  .attr("y1", 0)
		  .attr("y2", 16)
		  .style("stroke-width", 5)
		  .style("stroke", function(d, i){return c(nodes[i].group)});
		  
	  column.append("text")
	      .attr("x", 6)
	      .attr("y", x.rangeBand() / 2)
	      .attr("dy", ".32em")
	      .attr("text-anchor", "start")
		  .style("font-size", "15px")
	      .text(function(d, i) { return nodes[i].name; })
		  .on ("mouseover", topTextMouseover)
		  .on("mouseout", mouseout);
	
	  //create cells (mappings and empty cells) in every row
	  function row(row) {
	    var empty = d3.select(this).selectAll(".cell")
	        .data(row)
	      .enter().append("rect")
			.attr("class", "empty")
	        .attr("x", function(d) { return x(d.x); })
	        .attr("width", x.rangeBand())
	        .attr("height", x.rangeBand())
			.style("fill", "#eee");
		empty.append("title").text(function(d, i){return d.x + ", "+ d.y});
		
		
	    var cell = d3.select(this).selectAll(".cell")
	        .data(row.filter(function(d) { return d.z; }))
	      .enter().append("rect")
		    .filter(function(d){return d.x!=d.y})
	        .attr("class", "cell")
	        .attr("x", function(d) { return x(d.x); })
	        .attr("width", x.rangeBand())
	        .attr("height", x.rangeBand())
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
		  var gradient = svg.append("svg:defs")
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
			.style("font-size", "15px");
		  
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
	  
	  menu.on("change", order());
	
	  function order() {
		  	return function(d, i){
		  		x.domain(orders[this.value]);

		  		var t = svg.transition().duration(2500);

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
	  	}
	}
}