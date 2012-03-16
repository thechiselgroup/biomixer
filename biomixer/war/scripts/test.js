function test(div){
	
	var w = window.innerWidth, h = window.innerHeight

	//initialize the visualization
	var vis = d3.select(div).append("svg:svg")
		.attr("width", w)
		.attr("height", h)
		.attr("pointer-events", "all")
	 // .append('svg:g')
		.call(d3.behavior.zoom().on("zoom", redraw))
	  .append('svg:g');

	vis.append('svg:rect')
		.attr('width', w)
		.attr('height', h)
		.attr('fill','#FFFCC4');
	
	
	window.onresize = resize;

	function resize()
	{
		d3.selectAll("svg")
		.attr('width', window.innerWidth)
		.attr('height', window.innerHeight);
		
		d3.selectAll("rect")
		.attr('width', window.innerWidth)
		.attr('height', window.innerHeight);
	}
	
	
	//redraw on zoom
	function redraw() {
		console.log("here", d3.event.translate, d3.event.scale);
		vis.attr("transform",
			"translate(" + d3.event.translate + ")"
			+ " scale(" + d3.event.scale + ")");
	}
}