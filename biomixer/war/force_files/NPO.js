var w = 1400,
    h = 1000

var vis = d3.select("#chart").append("svg:svg")
    .attr("width", w)
    .attr("height", h)
	.attr("pointer-events", "all")
  .append('svg:g')
    .call(d3.behavior.zoom().on("zoom", redraw))
  .append('svg:g');

vis.append('svg:rect')
    .attr('width', w)
    .attr('height', h)
    .attr('fill', 'white');

function redraw() {
  console.log("here", d3.event.translate, d3.event.scale);
  vis.attr("transform",
      "translate(" + d3.event.translate + ")"
      + " scale(" + d3.event.scale + ")");
}

d3.json("force_files/npoMappings.json", function(json) {
    var force = self.force = d3.layout.force()
        .nodes(json.nodes)
        .links(json.links)
        .gravity(.05)
        .distance(600)
        .charge(-100)
        .size([w, h])
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
        .call(force.drag);

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

    force.on("tick", function() {
      link.attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; });

      node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    });
});

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