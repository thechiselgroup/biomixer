///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="JQueryExtension" />

///<amd-dependency path="Utils" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="Concepts/ConceptFilterSliders" />
///<amd-dependency path="Concepts/ConceptLayouts" />
///<amd-dependency path="Concepts/ConceptRenderScaler" />

import Utils = require('../Utils');
import GraphView = require('../GraphView');
import ConceptGraph = require('./ConceptGraph');
import ConceptRenderScaler = require('./ConceptRenderScaler');
import ConceptFilterSliders = require('./ConceptFilterSliders');
import ConceptLayouts = require('./ConceptLayouts');

export class ConceptPathsToRoot extends GraphView.BaseGraphView implements GraphView.GraphView<ConceptGraph.Node, ConceptGraph.Link> {
    
    // Core objects (used to float around prior to TypeScript)
    conceptGraph: ConceptGraph.ConceptGraph;
    renderScaler: ConceptRenderScaler.ConceptRendererScaler;
    filterSliders: ConceptFilterSliders.ConceptRangeSliders;
    layouts: ConceptLayouts.ConceptLayouts;
    
    vis: D3.Selection;
    
    // TODO Is this overshadowing or is this using the member defined in the parent class?
    // Put a re-callable layout function in runCurrentLayout.


    
    
    uniqueIdCounter = 0;
 
    
    linkThickness = 3;
    nodeHeight = 8;
    
    nodeLabelPaddingWidth = 10;
    nodeLabelPaddingHeight = 10;
    
  

    
    // TODO Refactor something. Leaving this way to prevent too much code change that isn't simpyl TypeScript refactoring.
    filterGraphOnMappingCounts(){
//        this.filterSliders.filterGraphOnOntologyAndLinkSelections();
    }
    
    visualization: string;
    
    constructor(
        public centralConceptUri: string,
        public softNodeCap: number
        ){
        super();
        
        this.visualization = $("#visualization_selector option:selected").text();
        $("#visualization_selector").change(
            () => {
                console.log("Changing visualization mode.");
                if(this.visualization != $("#visualization_selector option:selected").text()){
                    this.visualization = $("#visualization_selector option:selected").text();
                    this.cleanSlate();
                    this.runGraph();
                }
            }
        );
        
        this.runCurrentLayout = this.layouts.runForceLayoutLambda();
        
        // Had to set div#chart.gallery height = 100% in CSS,
        // but this was only required in Firefox. I can't see why.
        this.vis = d3.select("#chart").append("svg:svg")
            .attr("id", "graphSvg")
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("pointer-events", "all")
            .on("click", this.closeMenu())
        //  .append('svg:g')
            .call(d3.behavior.zoom().on("zoom", this.redraw))
        //  .append('svg:g')
          ;
        
        this.vis.append('svg:rect')
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("id", "graphRect")
            .style('fill', 'white');
        
       
        
        $(window).resize(this.resizedWindow);
        
        this.resizedWindow();
    }
    
    cleanSlate(){
        // TODO Much of this is now ConceptGraph territory.
        // We probably abandon the existing graph and make a new one.
//        this.graphD3Format = new Object();
//        this.edgeRegistry = {}; 
//        this.conceptIdNodeMap = new Object();
//        this.conceptsToExpand = new Object();
        
        console.log("Need to trigger new graph creation here (just use 'new'?");
        
        // Had to set div#chart.gallery height = 100% in CSS,
        // but this was only required in Firefox. I can't see why.
        console.log("Deleting and recreating graph."); // Could there be issues with D3 here?
        $("#chart").empty();
        
        this.vis = d3.select("#chart").append("svg:svg")
            .attr("id", "graphSvg")
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("pointer-events", "all")
            .on("click", this.closeMenu());
        //  .call(d3.behavior.zoom().on("zoom", redraw))
          
        
        this.vis.append('svg:rect')
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("id", "graphRect")
            .style('fill', 'white');
        
        // Keeps links below nodes, and cleans up document a fair bit.
        this.vis.append("g").attr("id", "link_container");
        this.vis.append("g").attr("id", "node_container");
        
        this.resizedWindow();
    }

    resizedWindow(){       
        d3.select("#graphRect")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        d3.select("#graphSvg")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        // TODO Layouts not relying on force need additional support here.
        if(this.forceLayout){
            this.forceLayout.size([this.visWidth(), this.visHeight()]).linkDistance(this.linkMaxDesiredLength());
            // If needed, move all the nodes towards the new middle here.
            this.forceLayout.resume();
        }  
    }
    
    
    redraw() {
    //  console.log("redrawing D3", d3.event.translate, d3.event.scale);
    //  vis.attr("transform",
    //      "translate(" + d3.event.translate + ")"
    //      + " scale(" + d3.event.scale + ")");
    }
    
    initAndPopulateGraph(){
        // Used to happen on window load.
        console.log("Window loaded,starting visualization")
        this.cleanSlate();
        this.runGraph();
        
        // These here or elsewhere like in runGraph??
        this.conceptGraph = new ConceptGraph.ConceptGraph(this, this.centralConceptUri, this.softNodeCap);
//        this.renderScaler = new ConceptRenderScaler.ConceptRenderScaler(this.vis);
//        this.filterSliders = new ConceptFilterSliders.ConceptRangeSliders(this.conceptGraph, this, this.centralConceptUri);
        this.layouts = new ConceptLayouts.ConceptLayouts(this.forceLayout, this.conceptGraph, this, this.centralConceptUri);
        }
        
    runGraph(){
        this.initGraph();
        
        this.prepGraphMenu();
        
        this.initPopulateGraph();
        
        // Will do async stuff and add to graph
//        this.conceptGraph.fetchOntologyNeighbourhood(this.centralOntologyAcronym);
        // That snippet is from ontologies. We do that work in initPopulateGraph().
        
        // If you want to toy with the original static data, try this:
        //  populateGraph(json);
    }
    
    initPopulateGraph(){
        if(this.visualization === ConceptGraph.PathOptions.pathsToRootConstant){
            this.conceptGraph.fetchPathToRoot(this.centralConceptUri, this.centralConceptUri);
        } else if(this.visualization === ConceptGraph.PathOptions.termNeighborhoodConstant){
            this.conceptGraph.fetchTermNeighborhood(this.centralConceptUri, this.centralConceptUri);
        } else if(this.visualization === ConceptGraph.PathOptions.mappingsNeighborhoodConstant){
            this.conceptGraph.fetchMappingsNeighborhood(this.centralConceptUri, this.centralConceptUri);
        }
    }
    
    // TODO I don't believe this is rendering...
    conceptLinkSimplePopupFunction(d){
        return "From: "+d.source.id+" To: "+d.target.id;
    }
    
    // TODO Fix...but also it doesn't render...
    conceptNodeSimplePopupFunction(d){
        return "Number Of Terms: "+d.number;
    }
    
    conceptNodeLabelFunction(d){ 
        return d.name;
    }
    
    nodeDragBehavior: D3.Behavior.Drag;
    initGraph(){ // initNonForceGraph
        // We use the force layout, but not really.
        // We can set the positions of everything, such as to a tree,
        // and disable the force parameters as necessary.
        // This is preferable to using any of the D3 Hierarchy visualizations,
        // since we deal with DAGs, not hierarchies.
        this.forceLayout = d3.layout.force();
        
        //  forceLayout.drag()
        //  .on("dragstart", function(){})
        //  .on("dragend", function(){dragging = false;});
        
        // nodeDragBehavior = forceLayout.drag;
        this.nodeDragBehavior = d3.behavior.drag()
        .on("dragstart", this.dragstartLambda(this))
        .on("drag", this.dragmoveLambda(this))
        .on("dragend", this.dragendLambda(this));
        
        this.forceLayout.on("tick", this.onLayoutTick());
    
        // See the gravityAdjust(), which is called in tick() and modulates
        // gravity to keep nodes within the view frame.
        // If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
        this.forceLayout
        .friction(0.3) // use 0.2 friction to get a very circular layout
        .gravity(0.05) // 0.5
        // .distance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        // .linkDistance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        // .forceDistance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        .charge(-30) // -100
        .linkDistance(this.linkMaxDesiredLength())
        .size([this.visWidth(), this.visHeight()])
        .start();
        console.log("Is it force distance or link distance above?");
        
        // TODO Do I actually need these?
        this.conceptGraph.graphD3Format.nodes = <ConceptGraph.Node[]>this.forceLayout.nodes();
        this.conceptGraph.graphD3Format.links = <ConceptGraph.Link[]>this.forceLayout.links();
    }
    
    //TODO I need to update this for the refactoring I made. When are we calling this? Ideally *only* at initialization, right?
    onLayoutTick(){
        var lastLabelShiftTime = jQuery.now();
        var lastGravityAdjustmentTime = jQuery.now();
        var firstTickTime = jQuery.now();
        var maxLayoutRunDuration = 10000;
        var maxGravityFrequency = 4000;
    
        return () => {
            // This improved layout behavior dramatically.
            var boundNodes = this.vis.selectAll("g.node");
            // Links have a g element aroudn them too, for ordering effects, but we set the link endpoints, not the g positon.
            var boundLinks = this.vis.selectAll("line.link");
                
            // Stop the layout early. The circular initialization makes it ok.
            if (this.forceLayout.alpha() < this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
                this.forceLayout.stop();
            }
            
            
            // Do I want nodes to avoid one another?
            // http://bl.ocks.org/mbostock/3231298
    //      var q = d3.geom.quadtree(nodes),
    //        i = 0,
    //        n = nodes.length;
    //      while (++i < n) q.visit(collide(nodes[i]));
    //      function collide(node) {
    //            var r = node.radius + 16,
    //                nx1 = node.x - r,
    //                nx2 = node.x + r,
    //                ny1 = node.y - r,
    //                ny2 = node.y + r;
    //            return function(quad, x1, y1, x2, y2) {
    //              if (quad.point && (quad.point !== node)) {
    //                var x = node.x - quad.point.x,
    //                    y = node.y - quad.point.y,
    //                    l = Math.sqrt(x * x + y * y),
    //                    r = node.radius + quad.point.radius;
    //                if (l < r) {
    //                  l = (l - r) / l * .5;
    //                  node.x -= x *= l;
    //                  node.y -= y *= l;
    //                  quad.point.x += x;
    //                  quad.point.y += y;
    //                }
    //              }
    //              return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
    //            };
    //       svg.selectAll("circle")
    //        .attr("cx", function(d) { return d.x; })
    //        .attr("cy", function(d) { return d.y; });
            
            // For every iteration of the layout (until it stabilizes)
            // Using this bounding box on nodes and links works, but leads to way too much overlap for the
            // labels...Bostock is correct in saying that gravity adjustments can get better results.
            // gravityAdjust() functions are pass through; they want to inspect values,
            // not modify them!
    //      var doLabelUpdateNextTime = false;
    //      if(jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency){
    //          nodes.attr("transform", function(d) { return "translate(" + gravityAdjustX(d.x) + "," + gravityAdjustY(d.y) + ")"; });
    //          lastGravityAdjustmentTime = jQuery.now();
    //          doLabelUpdateNextTime = true;
    //      } else {
    
            boundNodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
                
            if(boundLinks.length > 0)
                boundLinks
              .attr("x1", function(d) { return d.source.x; })
              .attr("y1", function(d) { return d.source.y; })
              .attr("x2", function(d) { return d.target.x; })
              .attr("y2", function(d) { return d.target.y; });
            
            // I want labels to aim out of middle of graph, to make more room
            // It slows rendering, so I will only do it sometimes
            // Commented all this out because I liked centering them instead.
    //      if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
    //          $.each($(".nodetext"), function(i, text){
    //              text = $(text);
    //              if(text.position().left >= visWidth()/2){
    //                  text.attr("dx", 12);
    //                  text.attr("x", 12);
    //              } else {
    //                  text.attr("dx", - 12 - text.get(0).getComputedTextLength());
    //                  text.attr("x", - 12 - text.get(0).getComputedTextLength());
    //              }
    //          })
    //          lastLabelShiftTime = jQuery.now();
    //      }
            
        }
            
    }
    
    dragstartLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            this.dragging = true;
            // $(this).tipsy('hide');
            $(".tipsy").hide();
            // stops the force auto positioning before you start dragging
            // This will halt the layout entirely, so if it tends to be unfinished for
            // long enough for a user to want to drag a node, we need to make this more complicated...
            this.forceLayout.stop();
        }
    }
    
    dragmoveLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            // http://bl.ocks.org/norrs/2883411
            // https://github.com/mbostock/d3/blob/master/src/layout/force.js
            // Original dragmove() had call to force.resume(), which I needed to remove when the graph was stable.
            d.px += d3.event.dx;
            d.py += d3.event.dy;
            d.x += d3.event.dx;
            d.y += d3.event.dy; 
            
            d3.select(this).attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
        
            this.vis.selectAll("line")
                .filter(function(e, i){ return e.source == d || e.target == d; })
                .attr("x1", function(e) { return e.source.x; })
                .attr("y1", function(e) { return e.source.y; })
                .attr("x2", function(e) { return e.target.x; })
                .attr("y2", function(e) { return e.target.y; });
           
        }
    }
    
    dragendLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            this.dragging = false;
            // $(this).tipsy('show');
            $(".tipsy").show();
            // no need to make the node fixed because we stop the layout when drag event begins
            // if it is set to fixed, the node interferes with other layouts
            //d.fixed = true;
        }
    }
    
    createNodePopupTable(conceptRect, conceptData){
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
    updateDataForNodesAndLinks(json){
        var outerThis = this;
        var updateLinksFromJson = function(i, d){ // JQuery is i, d
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "link"
            var link = outerThis.vis.select("#link_line_"+d.source.id+"-to-"+d.target.id);
            // Concept graphs have fixed node and arc sizes.
            // link.attr("data-thickness_basis", function(d) { return d.value;})
            // link.select("title").text(outerThis.conceptLinkLabelFunction);
            link.select("title").text(function(d) { return d.value;});
        }
        
        var updateNodesFromJson = function(i, d){ // JQuery is i, d
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "node"
            var node = outerThis.vis.select("#node_g_"+d.escapedId);
            var nodeRects = node.select("node_rect");
            // Concept graphs have fixed node and arc sizes.
            // nodeRects.attr("data-radius_basis", d.number);
            nodeRects.transition().style("fill", d.nodeColor);
            node.select("title").text(outerThis.conceptNodeSimplePopupFunction);
            node.select("text").text(outerThis.conceptNodeLabelFunction)
            // Firefox renders dx for text poorly, shifting things around oddly,
            // but x works for both Chrome and Firefox.
            // .attr("dx", function(){ return - this.getComputedTextLength()/2; })
            .attr("x", function(){ return - this.getComputedTextLength()/2; })
            ;
            
            // Refresh popup if currently open
            if(outerThis.lastDisplayedTipsy != null
                    && outerThis.lastDisplayedTipsy.css("visibility") == "visible"
                    && outerThis.lastDisplayedTipsyData.acronym == d.acronym
                    ){
                $(outerThis.lastDisplayedTipsy).children(".tipsy-inner").html(outerThis.createNodePopupTable(outerThis.lastDisplayedTipsySvg, outerThis.lastDisplayedTipsyData));
            }
        }
        
        $.each(json.links, updateLinksFromJson);
        $.each(json.nodes, updateNodesFromJson);
        
        // Concept graphs have fixed node and arc sizes.
    //  if(nodeUpdateTimer == false){
    //      nodeUpdateTimer = true;
    //      window.setTimeout(function(){
    //              console.log("TIMER RESET");
    //              nodeUpdateTimer = false;
    //              updateNodeScalingFactor();
    //              // The link thickness does not receive new data right now,
    //              // otherwise we'd want to call the update factor function here.
    //              // updateLinkScalingFactor();
    //          },
    //          1000);
    //  }
    }
    //var nodeUpdateTimer = false;
    
    
    /**
    * This function should be used when adding brand new nodes and links to the
    * graph. Do not call it to update properties of graph elements.
    */
    updateGraphPopulation(){  
        this.populateGraphEdges(this.conceptGraph.graphD3Format.links);
        this.populateGraphNodes(this.conceptGraph.graphD3Format.nodes);
        this.forceLayout.start();
    }
    
    populateGraph(graphD3Format: ConceptGraph.ConceptD3Data, newElementsExpected: boolean){
        console.log("Design problem...concept and ontology graphs need to be brought into alignment for this method and updateGraphPopulation above");
    }
    
//    var i = 0;
    populateGraphEdges(linksData){
        // Advice from http://stackoverflow.com/questions/9539294/adding-new-nodes-to-force-directed-layout
        if(linksData.length == 0){
            return [];
        }
        
        // Data constancy via key function() passed to data()
        // Link stuff first
        // console.log("enter() getting data for counter time: "+(i=i+1));  console.log(d); 
        var links = this.vis.select("#link_container")
        .selectAll("line.link").data(linksData, function(d){return d.source.id+"-to-"+d.target.id});
        // console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length);
        // console.log(" links from selectAll: "+vis.selectAll("line.link")[0].length);
         
        // Add new stuff
        // Make svg:g like nodes if we need labels
        // Would skip the g element here for links, but it cleans up the document and bundles text with line.
        var enteringLinks = links.enter().append("svg:g")
        .attr("class", "link")
        .attr("id", function(d){ return "link_g_"+d.source.id+"-to-"+d.target.id});
        
        enteringLinks.append("svg:line")
        .attr("class", function(d){return "link link_"+d.edgeType;}) 
        .attr("id", function(d){ return "link_line_"+d.source.id+"-to-"+d.target.id})
        .on("mouseover", this.highlightLinkLambda(this))
        .on("mouseout", this.changeColourBackLambda(this))
        .attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; })
        .style("stroke-linecap", "round")
        .style("stroke-width", this.linkThickness)
        .attr("data-thickness_basis", function(d) { return d.value;});
    
        // console.log("After append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
        
        // Update Tool tip
        enteringLinks // this is new...used to do to all linked data...
        .append("title") // How would I *update* this if I needed to?
            .text(this.conceptLinkSimplePopupFunction)
                .attr("id", function(d){ return "link_title_"+d.source.id+"-to-"+d.target.id});
    
        links.exit().remove();
        
    }
    
    populateGraphNodes(nodesData){
        // Advice from http://stackoverflow.com/questions/9539294/adding-new-nodes-to-force-directed-layout
        if(nodesData.length == 0){
            return [];
        }
        
        var outerThis = this;
        
        var nodes = this.vis.select("#node_container")
        .selectAll("g.node").data(nodesData, function(d){return d.id});
        // console.log("Before append nodes: "+nodes[0].length+" nodes.enter(): "+nodes.enter()[0].length+" nodes.exit(): "+nodes.exit()[0].length+" Nodes from selectAll: "+vis.selectAll("g.node")[0].length);
        // Add new stuff
        var nodesEnter = nodes.enter().append("svg:g")
        .attr("class", "node")
        .attr("id", function(d){ return "node_g_"+d.escapedId})
        .call(this.nodeDragBehavior);
        
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
        .attr("id", function(d){ return "node_rect_"+(this.uniqueIdCounter++)})
        .attr("class", "node_rect")
         .style("fill", function(d) { return d.nodeColor; })
        // Concept graphs have fixed node and arc sizes.
        // .attr("data-radius_basis", function(d) { return d.number;})
        // .attr("r", function(d) { return ontologyNodeScalingFunc(d.number); })
        .attr("height", this.nodeHeight)
        .attr("width", this.nodeHeight)
        .on("mouseover", this.changeColourLambda(this))
        .on("mouseout", this.changeColourBackLambda(this));
        
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
                if($("#"+me.id+":hover").length != 0 && $(tipsyId+":hover").length != 0){
                    // console.log("Not in thing "+me.id+" and tipsyId "+tipsyId);
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
                if(outerThis.dragging){
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
                      return outerThis.createNodePopupTable(me, meData);
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
                    outerThis.lastDisplayedTipsy = tipsy;
                    outerThis.lastDisplayedTipsyData = meData;
                    outerThis.lastDisplayedTipsySvg = me;
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
            // d3.timer(function(){}, -4 * 1000 * 60 * 60, +new Date(2012, 09, 29));
        });
            
        // Dumb Tool tip...not needed with tipsy popups.
        // nodesEnter.append("title")
        //   .attr("id", function(d){ return "node_title_"+d.acronym})
        //   .text(function(d) { return "Number Of Terms: "+d.number; });
        
        // Label
        nodesEnter.append("svg:text")
        .attr("id", function(d){ return "node_text_"+(outerThis.uniqueIdCounter++)})
        .attr("class", "nodetext unselectable")
        // .attr("dx", "0em")
        // .attr("dy", "1em") // 1em down to go below baseline, 0.5em to counter padding added below
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
        
        // Resize each node to encompass the label we just created.
        $(".nodetext").each(function(i, d){
            var textSize = this.getBBox; // d.getBBox();
            var rect = $(d).siblings().filter(".node_rect"); // .select(".node_rect");
            rect.attr("width", textSize.width + outerThis.nodeLabelPaddingWidth);
            rect.attr("height", textSize.height + outerThis.nodeLabelPaddingHeight);
            // We need to adjust the rectangle position within its svg:g object so that arcs are positioned relative
            // to the rectangle center. Circles automatically end up this way.
            rect.attr("x", -textSize.width/2 - outerThis.nodeLabelPaddingWidth/2);
            rect.attr("y", -textSize.height/2 - outerThis.nodeLabelPaddingHeight/2);
            // center the label in the resized rect
            $(d).attr("dx", -textSize.width/2).attr("dy", outerThis.nodeLabelPaddingHeight/2);
            // The following was for when rects were not centered by accounting for width
            // $(d).attr("dx", nodeLabelPaddingWidth/2).attr("dy", textSize.height);
        });
        
        nodes.exit().remove();
        
    }

    removeGraphPopulation(){
        console.log("Removing some graph elements "+Utils.getTime());
    
        var nodes = this.vis.selectAll("g.node").data(this.conceptGraph.graphD3Format.nodes, function(d){return d.rawAcronym});
        var links = this.vis.selectAll("line.link").data(this.conceptGraph.graphD3Format.links, function(d){return d.source.rawAcronym+"-to-"+d.target.rawAcronym});
        
        
        
        //  console.log("Before "+vis.selectAll("g.node").data(ontologyNeighbourhoodJsonForGraph.nodes, function(d){return d.rawAcronym}).exit()[0].length);
        nodes.exit().remove();
        links.exit().remove();
        // Do I need start() or not? Number of elements before and after implies not.
        //  forceLayout.start();
        //  console.log("After "+vis.selectAll("g.node").data(ontologyNeighbourhoodJsonForGraph.nodes, function(d){return d.rawAcronym}).exit()[0].length);
        
        // Update filter sliders. Filtering and layout refresh should be updated within the slider event function.
        this.filterSliders.updateTopMappingsSliderRange();
        this.filterSliders.rangeSliderSlideEvent(null, null); // Bad to pass nulls when I know it will work, or ok?
    }
    
    highlightLinkLambda(outerThis: ConceptPathsToRoot){
        return function(linkLine, i){
            if(outerThis.dragging){
                return;
            }
        
            var xSourcePos = linkLine.source.x;
            var ySourcePos = linkLine.source.y;
            var xTargetPos = linkLine.target.x;
            var yTargetPos = linkLine.target.y;
            
            d3.selectAll("text").style("opacity", .2)
                .filter(function(g, i){return g.x==linkLine.source.x||g.y==linkLine.source.y||g.x==linkLine.target.x||g.y==linkLine.target.y;})
                .style("opacity", 1);
                
            d3.selectAll("line").style("stroke-opacity", .1);
            d3.selectAll("node_rect").style("fill-opacity", .1)
                .style("stroke-opacity", .2)
                .filter(function(g, i){return g.x==linkLine.source.x||g.y==linkLine.source.y||g.x==linkLine.target.x||g.y==linkLine.target.y})
                .style("fill-opacity", 1)
                .style("stroke-opacity", 1);
            d3.select(this).style("stroke-opacity", 1)
                .style("stroke", "#3d3d3d");
        }
    }
    
    changeColourLambda(outerThis: ConceptPathsToRoot){
        return function(nodeData, i){
            if(outerThis.dragging){
                return;
            }
            var xPos=nodeData.x;
            var yPos=nodeData.y;
            
            d3.selectAll("line").style("stroke-opacity", .1);
            d3.selectAll("node_rect").style("fill-opacity", .1)
                .style("stroke-opacity", .2);
                
            d3.selectAll("text").style("opacity", .2)
                .filter(function(g, i){return g.x==nodeData.x})
                .style("opacity", 1);
                
            var sourceNode;
            if(d3.select(this).attr("class") == "node_rect"){
                sourceNode = d3.select(this);
            } else if(d3.select(this).attr("class") == "nodetext"){
                // If the labels aren't wired for mouse interaction, this is unneeded
                sourceNode = d3.select(this.parentNode).select(".node_rect");
            }
            
            sourceNode.style("fill", outerThis.nodeHighlightColor)
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
    }
    
    changeColourBackLambda(outerThis: ConceptPathsToRoot){
        return function(nodeData, i){
            d3.selectAll(".node_rect")
                .style("fill", function(e, i){ 
                    return (typeof e.nodeColor === undefined ? outerThis.defaultNodeColor : e.nodeColor); 
                    })
                .style("fill-opacity", .75)
                .style("stroke-opacity", 1);
            d3.selectAll("line")
                .style("stroke", outerThis.defaultLinkColor)
                .style("stroke-opacity", .75);
            d3.selectAll("text").style("opacity", 1);
        }
    }
    
    prepGraphMenu(){
        // Layout selector for concept graphs.
        
        // Append the pop-out panel. It will stay hidden except when moused over.
        var trigger = $("<div>").attr("id", "trigger");
        $("#chart").append(trigger);
        trigger.append($("<p>").text("<< Layouts"));
        trigger.append($("<div>").attr("id", "hoveringGraphMenu"));
    
        $('#trigger').hover(
                function(e) {
                    $(this.menuSelector).show(); //.css('top', e.pageY).css('left', e.pageX);
                     // Looks bad when it's not fully visible, due to children inheriting transparency
                    $(this.menuSelector).fadeTo(0, 1.0);
                },
                function() {
                //  $(menuSelector).hide();
                }
        );
        
        this.layouts.addMenuComponents(this.menuSelector, this.softNodeCap);
//        this.filterSliders.addMenuComponents(this.menuSelector, this.softNodeCap);
    }
    
    
    
}

