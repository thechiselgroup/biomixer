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

function forceDirectedLayout(div, json){
		var jsonObject = eval('(' + json + ')');

		var w = window.innerWidth, h = window.innerHeight

		//initialize the visualization
		var vis = d3.select(div).append("svg:svg")
			.attr("width", w)
			.attr("height", h)
			.attr("pointer-events", "all")
			.call(d3.behavior.zoom().on("zoom", redraw))
		  .append('svg:g');

		vis.append('svg:rect')
			.attr('width', w)
			.attr('height', h)
			.attr('fill','white');

		//redraw on zoom
		function redraw() {
			console.log("here", d3.event.translate, d3.event.scale);
			vis.attr("transform",
				"translate(" + d3.event.translate + ")"
				+ " scale(" + d3.event.scale + ")");
		}

		window.onresize = resize;

		function resize()
		{	
			d3.select(div)
			.style("width", "100%")
			.style("height", "100%");
			
			d3.selectAll("svg")
			.attr('width', window.innerWidth)
			.attr('height', window.innerHeight);
			
			d3.selectAll("rect")
			.attr('width', window.innerWidth)
			.attr('height', window.innerHeight);
		}
		
		drawLayout(jsonObject);

		function drawLayout(json) {
			//initialize the force directed layout
			var force = self.force = d3.layout.force()
				.nodes(json.nodes)
				.links(json.links)
				.gravity(.05)
				.distance(600)
				.charge(-100)
				.size([ w, h ]).start();

			//initialize links
			var link = vis.selectAll("line.link")
				.data(json.links)
			  .enter().append("svg:line")
			  	.attr("class", "link")
			  	.attr("x1", function(d) { return d.source.x; })
			  	.attr("y1", function(d) { return d.source.y; })
			  	.attr("x2", function(d) { return d.target.x; })
			  	.attr("y2", function(d) { return d.target.y; })
			  	.style("stroke-width", function(d) { return Math.sqrt(Math.ceil(d.value / 10)); })
			  	.style("stroke", "#999")
			  	.style("stroke-opacity", 0.6);

			link.append("title")
				.text(function(d) { return "Number Of Mappings: " + d.sourceMappings; });

			link.on("mouseover", onMouseOverLink())
				.on("mouseout", onMouseOut("#496BB0", "#999"));

			//initialize nodes
			var node = vis.selectAll("g.node")
				.data(json.nodes)
			  .enter().append("svg:g")
			  	.attr("class", "node")
			  	.call(force.drag);

			node.append("svg:circle")
				.attr("class", "circle")
				.attr("cx", "0px")
				.attr("cy", "0px")
				.style("fill", "#496BB0")
				.attr("r", function(d) { return Math.sqrt((d.number) / 10); })
				.style("fill-opacity", 0.7)
				.style("stroke","#3d3d3d")
				.style("stroke-width", "4px")
				.on("mouseover", onMouseOverNode("#FC6854", "#ff1", "#ff1", .1))
				.on("mouseout", onMouseOut("#496BB0", "#999"));

			node.append("title")
				.text(function(d) { return "Number Of Terms: " + d.number;});

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

			force.on("tick", function() { 
			  link.attr("x1", function(d) { return d.source.x; })
			  	.attr("y1", function(d) { return d.source.y; })
			  	.attr("x2", function(d) { return d.target.x; })
			  	.attr("y2", function(d) { return d.target.y; });

				node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
			  
			});
		}

		// highlight nodes and link on mouse over the link
		function onMouseOverLink() {
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

		//highlight nodes and links on mouse over the node
		function onMouseOverNode(circleFill, lineFill, circlesFill, opacity) {
			return function(d, i) {

				xPos = d.x;
				yPos = d.y;

				d3.selectAll("line").style("stroke-opacity", .1);
				d3.selectAll("circle").style("fill-opacity", .1)
					.style("stroke-opacity", .2);

				d3.selectAll("text")
					.style("opacity", .2)
					.filter(function(g, i) { return g.x == d.x })
					.style("opacity", 1);

				var sourceNode = d3.select(this)
					.style("fill", circleFill)
					.style("fill-opacity", 1)
					.style("stroke-opacity", 1);

				var adjacentLinks = d3.selectAll("line")
					.filter(function(d, i) { return d.source.x == xPos && d.source.y == yPos;})
					.style("stroke-opacity", 1)
					.style("stroke", "#3d3d3d")
					.each(function(d) {
						d3.selectAll("circle")
							.filter(function(g, i) {return d.target.x == g.x && d.target.y == g.y; })
							.style("fill-opacity", 1)
							.style("stroke-opacity", 1)
							.each(function(d) {
								d3.selectAll("text")
									.filter(function(g, i) {return g.x == d.x})
									.style("opacity", 1);
							});
						});
			};
		}

		// change the colours back to the initial state
		function onMouseOut(circleFill, lineFill) {
			return function(d, i) {
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
}