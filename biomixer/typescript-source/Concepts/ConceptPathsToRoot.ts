///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="JQueryExtension" />

///<amd-dependency path="Utils" />
///<amd-dependency path="Menu" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="ExpansionSets" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="TipsyToolTips" />
///<amd-dependency path="TipsyToolTipsOnClick" />
///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="CompositeExpansionDeletionSet" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="Concepts/ConceptFilterSliders" />
///<amd-dependency path="Concepts/CherryPickConceptFilter" />
///<amd-dependency path="Concepts/OntologyConceptFilter" />
///<amd-dependency path="Concepts/NestedOntologyConceptFilter" />
///<amd-dependency path="Concepts/ExpansionSetFilter" />
///<amd-dependency path="Concepts/ConceptNodeFilterWidget" />
///<amd-dependency path="Concepts/ConceptEdgeTypeFilter" />
///<amd-dependency path="Concepts/ConceptFilterSliders" />
///<amd-dependency path="Concepts/ConceptLayouts" />
///<amd-dependency path="Concepts/GraphImporterExporter" />
///<amd-dependency path="Concepts/NodeDeleterWidgets" />
///<amd-dependency path="NodeFinderWidgets" />
///<amd-dependency path="Concepts/ConceptRenderScaler" />

import Utils = require("../Utils");
import Fetch = require("../FetchFromApi");
import Menu = require("../Menu");
import GraphView = require("../GraphView");
import ExpansionSets = require("../ExpansionSets");
import TipsyToolTips = require("../TipsyToolTips");
import TipsyToolTipsOnClick = require("../TipsyToolTipsOnClick");
import UndoRedoManager = require("../UndoRedo/UndoRedoManager");
import CompositeExpansionDeletionSet = require("../CompositeExpansionDeletionSet");
import ConceptGraph = require("./ConceptGraph");
import ConceptRenderScaler = require("./ConceptRenderScaler");
import CherryPickConceptFilter = require("./CherryPickConceptFilter");
import OntologyConceptFilter = require("./OntologyConceptFilter");
import NestedOntologyConceptFilter = require("./NestedOntologyConceptFilter");
import ExpansionSetFilter = require("./ExpansionSetFilter");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import ConceptEdgeTypeFilter = require("./ConceptEdgeTypeFilter");
import ConceptFilterSliders = require("./ConceptFilterSliders");
import ConceptLayouts = require("./ConceptLayouts");
import ImporterExporter = require("./GraphImporterExporter");
import NodeDeleter = require("./NodeDeleterWidgets");
import NodeFinder = require("../NodeFinderWidgets");


export class ConceptPathsToRoot extends GraphView.BaseGraphView<ConceptGraph.Node, ConceptGraph.Link> implements GraphView.GraphView<ConceptGraph.Node, ConceptGraph.Link> {
    
    // Core objects
    conceptGraph: ConceptGraph.ConceptGraph;
    renderScaler: ConceptRenderScaler.ConceptRendererScaler;
    filterSliders: ConceptFilterSliders.ConceptRangeSliders;
    
    /**
     * Specialized or casted version of parent classes' layoutProvider, for convenience.
     */
    layouts: ConceptLayouts.ConceptLayouts;
    
    nodeFinder: NodeFinder.NodeFinder<ConceptGraph.Node, ConceptGraph.Link>;
    
    importerExporterWidget: ImporterExporter.Widget;
    edgeTypeFilter: ConceptEdgeTypeFilter.ConceptEdgeTypeFilter;
    nodeDeleter: NodeDeleter.NodeDeleterWidgets;
    // individualConceptFilter: CherryPickConceptFilter.CherryPickConceptFilter;
    // ontologyFilter: OntologyConceptFilter.OntologyConceptFilter;
    nestedOntologyConceptFilter: NestedOntologyConceptFilter.NestedOntologyConceptFilter;
    expansionSetFilter: ExpansionSetFilter.ExpansionSetFilter;
    
    menu: Menu.Menu;
    
    vis: D3.Selection;
    
    // TODO Is this overshadowing or is this using the member defined in the parent class?
    // Put a re-callable layout function in runCurrentLayout.
    
    nodeHeight = 8;
    
    expansionBoxWidth = 30;
    expansionBoxHeight = 8;
    
    nodeLabelPaddingWidth = 10;
    nodeLabelPaddingHeight = 10;

    
    // TODO Refactor something. Leaving this way to prevent too much code change that isn't simply TypeScript refactoring.
    filterGraphOnMappingCounts(){
//        this.filterSliders.filterGraphOnOntologyAndLinkSelections();
    }
    
    visualization: ConceptGraph.PathOption;
    
    constructor(
        public centralOntologyAcronym: ConceptGraph.RawAcronym,
        public centralConceptUri: ConceptGraph.ConceptURI,
        public softNodeCap: number
    ){
        super();
        // Minimal constructor, most work done in initAndPopulateGraph().
        this.undoRedoBoss.initGui();
               
        this.menu = new Menu.Menu();
        
        // This cast is ok since we assign values for the selector from the ConceptGraph.PathOptionConstants
        this.visualization = <ConceptGraph.PathOption><any>$("#visualization_selector option:selected").text();
        $("#visualization_selector").change(
            () => {
                console.log("Changing visualization mode.");
                var selected  = <ConceptGraph.PathOption><any>$("#visualization_selector option:selected").text();
                if(this.visualization !== selected){
                    this.visualization = selected;
                    this.fetchInitialExpansion();
                }
            }
        );
    }
    
    public recomputeVisualizationOntoNode(nodeData: ConceptGraph.Node){

        // var message = "Are you sure you want to recreate the graph focussed on '"+nodeData.name+"' ("+nodeData.ontologyAcronym+")?";
        
        // if(confirm(message)){
        this.centralConceptUri = nodeData.rawConceptUri;
        this.centralOntologyAcronym = nodeData.ontologyAcronym;
        this.fetchInitialExpansion();
        // }
    }
    
    cleanSlate(){
        // Had to set div#chart.gallery height = 100% in CSS,
        // but this was only required in Firefox. I can't see why.
        console.log("Deleting and recreating graph."); // Could there be issues with D3 here?
        
        // Experimental...seems like a good idea
        if(this.forceLayout !== undefined){
            this.forceLayout.nodes([]);
            this.forceLayout.links([]);
        }
        $("#chart").empty();
        d3.select("#chart").remove;
        
        var outerThis = this;
        this.vis = d3.select("#chart").append("svg:svg")
            .attr("id", "graphSvg")
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("pointer-events", "all")
            .on("click",
             function(){
                            // outerThis.menu.closeMenuLambda()()
                            TipsyToolTipsOnClick.closeOtherTipsyTooltips();
                        }
                )
            ;
        //  .call(d3.behavior.zoom().on("zoom", redraw))
          
        // Old, faster way of makign arc triangles. Doesn't work in IE really, and Firefox got fussy with it too.
        // this.defineCustomSVG();
        
        this.vis.append("svg:rect")
            .attr("width", this.visWidth())
            .attr("height", this.visHeight())
            .attr("id", "graphRect")
            .style("fill", "white");
        
        // Keeps links below nodes, and cleans up document a fair bit.
        this.vis.append("svg:g").attr("id", "link_container");
        this.vis.append("svg:g").attr("id", "node_container");
        
        $(window).resize(this.resizedWindowLambda);
        
        this.resizedWindowLambda();
        
        Fetch.CacheRegistry.clearAllServiceRecordsKeepCacheData();
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
        
        // These here or elsewhere like in runGraph??
        this.conceptGraph = new ConceptGraph.ConceptGraph(this, this.centralConceptUri, this.softNodeCap, this.undoRedoBoss);
        
        this.initGraph();
        
//        this.renderScaler = new ConceptRenderScaler.ConceptRenderScaler(this.vis);
        this.filterSliders = new ConceptFilterSliders.ConceptRangeSliders(this.conceptGraph, this, this.centralConceptUri);
        
        this.layouts = new ConceptLayouts.ConceptLayouts(this.forceLayout, this.conceptGraph, this, this.centralConceptUri);
        this.conceptGraph.setLayoutProvider(this.layouts);
        
        this.nodeFinder = new NodeFinder.NodeFinder(this, this.conceptGraph);
        
        this.importerExporterWidget = new ImporterExporter.Widget(this);
         
        this.edgeTypeFilter = new ConceptEdgeTypeFilter.ConceptEdgeTypeFilter(this.conceptGraph, this, this.centralConceptUri);
        // this.ontologyFilter = new OntologyConceptFilter.OntologyConceptFilter(this.conceptGraph, this, this.centralConceptUri);
        // this.individualConceptFilter = new CherryPickConceptFilter.CherryPickConceptFilter(this.conceptGraph, this, this.centralConceptUri);
        this.nestedOntologyConceptFilter = new NestedOntologyConceptFilter.NestedOntologyConceptFilter(this.conceptGraph, this, this.centralConceptUri);
        this.expansionSetFilter = new ExpansionSetFilter.ExpansionSetFilter(this.conceptGraph, this);
        
        this.nodeDeleter = new NodeDeleter.NodeDeleterWidgets(this.conceptGraph, this, this.undoRedoBoss);
        
        this.setCurrentLayout(this.layouts.runForceLayoutLambda());
        
        // TODO Trying to get layout to begin anew when we swap visualization subtypes.
        // Why do none of these achieve that?
        this.forceLayout.stop();
        this.forceLayout.start();
        this.forceLayout.resume();
        this.runCurrentLayout();
        
        this.prepGraphMenu();
        
        this.fetchInitialExpansion();
    }
    
    /**
     * This is used for both initial expansions and refocus expansions.
     * It is also used for importing graphs.
     */
    public prepareForExpansionFromScratch(expId: ExpansionSets.ExpansionSetIdentifer, expansionType: ConceptGraph.PathOption): CompositeExpansionDeletionSet.InitializationDeletionSet<ConceptGraph.Node>{
        // We may have nodes that we are getting rid of in order to do the expansion, so we do it this way
        // expansionType is typically this.visualization (the PathOption gotten from the drop down), but in the case of importing data
        // it could be null.
        var initSet = new CompositeExpansionDeletionSet.InitializationDeletionSet<ConceptGraph.Node>(this.conceptGraph, expId, this.undoRedoBoss, expansionType);
        this.nodeDeleter.deleteNodesForGraphInitialization(initSet);
        return initSet;
    }
    
    fetchInitialExpansion(){
        if(this.centralOntologyAcronym === undefined || this.centralConceptUri === undefined){
            console.log("No ontology acoronym or no central concept id, empty graph left unfilled.");
            this.importerExporterWidget.openShareAndImportMenu();
            return;
        }

        var expId = new ExpansionSets.ExpansionSetIdentifer("conceptPathToRootInitialExpansion_"+this.centralOntologyAcronym+"__"+Utils.escapeIdentifierForId(this.centralConceptUri), String(this.visualization));
        var initSet = this.prepareForExpansionFromScratch(expId, this.visualization);
        var expansionSet = initSet.expansionSet;
        
		// All of the initial expansions rely ont he expansion set getting the parent node at a slightly delayed time. See each specialized callback
		// to see where this occurs.
        if(this.visualization === ConceptGraph.PathOptionConstants.pathsToRootConstant){
            this.setCurrentLayout(this.layouts.runVerticalTreeLayoutLambda());
            this.conceptGraph.fetchPathToRoot(this.centralOntologyAcronym, this.centralConceptUri, expansionSet, initSet);
        } else if(this.visualization === ConceptGraph.PathOptionConstants.termNeighborhoodConstant){
            this.conceptGraph.fetchTermNeighborhood(this.centralOntologyAcronym, this.centralConceptUri, expansionSet, initSet);
        } else if(this.visualization === ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant){
            this.setCurrentLayout(this.layouts.runCenterLayoutLambda());
            this.conceptGraph.fetchMappingsNeighborhood(this.centralOntologyAcronym, this.centralConceptUri, expansionSet, initSet);
            this.runCurrentLayout();
        }
    }
    
    // TODO I don't believe this is rendering...
    conceptLinkSimplePopupFunction(d: ConceptGraph.Link){
        return "From: "+d.source.name+" ("+d.source.ontologyAcronym+")"+" To: "+d.target.name+" ("+d.target.ontologyAcronym+")"+" ["+d.relationLabel+"]";
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
        
        // Can inject or extract these arrays. Either way, we need to re-inject
        // them whenever the model changes and D3 needs to respond.
        this.forceLayout.nodes(this.conceptGraph.graphD3Format.nodes);
        this.forceLayout.links(this.conceptGraph.graphD3Format.links);
        
        // nodeDragBehavior = forceLayout.drag;
        this.nodeDragBehavior = d3.behavior.drag()
        .on("dragstart", this.dragstartLambda(this))
        .on("drag", this.dragmoveLambda(this))
        .on("dragend", this.dragendLambda(this));
        
        // See the gravityAdjust(), which is called in tick() and modulates
        // gravity to keep nodes within the view frame.
        // If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
        this.forceLayout
        .size([this.visWidth(), this.visHeight()])
        .linkDistance(this.linkMaxDesiredLength())
        // Moved most settings into the ConceptLayouts force triggering method.
        //.linkStrength(0.1)
        //.charge(-800) // If we use collision, we may not want repulsion, so set to 0 in that case.
        //.gravity(0.01)
        //.friction(0.3)
        // .distance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        // .linkDistance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        // .forceDistance(Math.min(this.visWidth(), this.visHeight())/1.1) // 600
        ;
        // console.log("Is it force distance or link distance above?");
        
    }
    
    collide(node: ConceptGraph.Node){
        // var r = node.radius + 16;
        var nodeR = parseInt(d3.select("#node_rect_"+node.conceptUriForIds).attr("width"))/2;
        var nx1 = node.x - nodeR,
        nx2 = node.x + nodeR,
        ny1 = node.y - nodeR,
        ny2 = node.y + nodeR;
        return function(quad, x1, y1, x2, y2) {
            if (quad.point && (quad.point !== node)) {
                var x = node.x - quad.point.x,
                y = node.y - quad.point.y,
                l = Math.sqrt(x * x + y * y);
                // r = node.radius + quad.point.radius;
                var qpnoder =  nodeR + parseInt(d3.select("#node_rect_"+quad.point.conceptUriForIds).attr("width"))/2;
                if (l < qpnoder) {
                    l = (l - qpnoder) / l * .5;
                    node.x -= x *= l;
                    node.y -= y *= l;
                    quad.point.x += x;
                    quad.point.y += y;
                }
            }
            return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
        };
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
            var boundNodes = this.vis.selectAll("g.node_g");
            // Links have a g element aroudn them too, for ordering effects, but we set the link endpoints, not the g positon.
            var boundLinks = this.vis.selectAll("polyline"+GraphView.BaseGraphView.linkSvgClass);
            var boundLinkMarkers = this.vis.selectAll("polyline"+GraphView.BaseGraphView.linkMarkerSvgClass);
            // Stop the layout early. The circular initialization makes it ok.
            if (this.forceLayout.alpha() < this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
                this.forceLayout.stop();
                this.forceLayout.alpha(0);
            }
            
            
            if(boundNodes.length > 0){
                //box bounding
                var width = this.visWidth();
                var height = this.visHeight();
                var nodeHeight = this.nodeHeight+this.nodeLabelPaddingHeight/2;
                boundNodes.attr("transform", function(d: ConceptGraph.Node) { 
                    var nodeWidth = parseInt(d3.select("#node_rect_"+d.conceptUriForIds).attr("width"))/2;
                    d.x = Math.max(nodeWidth, Math.min(width - nodeWidth, d.x));
                   
                    d.y = Math.max(nodeHeight, Math.min(height - nodeHeight, d.y));
                    return "translate(" + d.x + "," + d.y + ")";
                });
                
                
                // Node collisions. Yes, we rebuild the quadtree a lot.
                // We should only use this or larg-ish (neagative) graph charge() settings.
                // The charge() approach does not have jitter, and uses a quadtree as this one
                // does. I suspect it is more efficient ultimately.
                // To re-enable collision, simpyl uncomment the chunk below, and set charge(0) further above.
                //var quadtreeFactory = d3.geom.quadtree();
                //var q = d3.geom.quadtree(this.conceptGraph.graphD3Format.nodes, this.visWidth(), this.visHeight());
                //var i = 0;
                //var n = this.conceptGraph.graphD3Format.nodes.length;
                //while (++i < n){
                //    q.visit(this.collide(this.conceptGraph.graphD3Format.nodes[i]));
                //}
                
            }
                
            if(boundLinks.length > 0){
                boundLinks.attr("points", this.updateArcLineFunc);
                boundLinkMarkers.attr("points", this.updateArcMarkerFunc);
            }
            
        }
            
    }
    
    
    alreadyHidTipsy = false;
    dragstartLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            outerThis.dragging = true;
            outerThis.alreadyHidTipsy = false;

            // stops the force auto positioning before you start dragging
            // This will halt the layout entirely, so if it tends to be unfinished for
            // long enough for a user to want to drag a node, we need to make this more complicated...
            outerThis.forceLayout.stop();
        }
    }
    
    dragmoveLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            if(!outerThis.alreadyHidTipsy && (d3.event.dx != 0 || d3.event.dy != 0)){
                TipsyToolTipsOnClick.closeOtherTipsyTooltips();
                outerThis.alreadyHidTipsy = true;
            }
            // http://bl.ocks.org/norrs/2883411
            // https://github.com/mbostock/d3/blob/master/src/layout/force.js
            // Original dragmove() had call to force.resume(), which I needed to remove when the graph was stable.
            d.px += d3.event.dx;
            d.py += d3.event.dy;
            d.x += d3.event.dx;
            d.y += d3.event.dy; 
            
            
            var width = outerThis.visWidth();
            var height =  outerThis.visHeight();            
            var nodeHeight = outerThis.nodeHeight+outerThis.nodeLabelPaddingHeight/2;
            
            
            d3.select(this).attr("transform", function(d: ConceptGraph.Node) { 
                var nodeWidth = parseInt(d3.select("#node_rect_"+d.conceptUriForIds).attr("width"))/2;
                d.x = Math.max(nodeWidth, Math.min(width - nodeWidth, d.x));
                    
                d.y = Math.max(nodeHeight, Math.min(height - nodeHeight, d.y));
                return "translate(" + d.x + "," + d.y + ")"; });
        
            
            outerThis.vis.selectAll("polyline"+GraphView.BaseGraphView.linkSvgClass)
                .filter(function(e: ConceptGraph.Link){ return e.source === d || e.target === d; })

                .attr("points", outerThis.updateArcLineFunc);
            outerThis.vis.selectAll("polyline"+GraphView.BaseGraphView.linkMarkerSvgClass)
                .filter(function(e: ConceptGraph.Link){ return e.source === d || e.target === d; })
                .attr("points", outerThis.updateArcMarkerFunc);

           
        }
    }
    
    pixelMap = [0, 10, -10, -7, -4, 4, 7]; // currently only supports 5 extra edges, could cut to diffs of 2 pixels instead.
    public updateArcLineFunc = (linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string => {
        // This is a lot easier than markers, except that we also have to offset
        // the line if there are two arc types.
        
        // In order to prevent arcs from overlapping, we need to assign slots to them. These slots
        // will be convertable to physical pixel offsets via an array.
        var offset = this.pixelMap[linkData.edgePositionSlot];
        if(ignoreOffset === true){ // need === because it might get a numeric arg via D3 or JQuery!
            offset = 0;
        }
        
        var sourceX = linkData.source.x;
        var sourceY = linkData.source.y;
        var targetX = linkData.target.x;
        var targetY = linkData.target.y;
    
        if(offset != 0){
            // Make is_a and has_a arcs move away from each other by enough that we can see them both
            // for when both relations exist in a pair of nodes
            
            // Get orthogonal vector, by changing x and y and flipping sign on first component (x).
            // We'll want the vector relative to source, then the same repeated for target...but since
            // we know the target orthogonal vector is parallel to the source orthogonal vector, we can
            // infer it.
            var targetVectorX = targetX - sourceX;
            var targetVectorY = targetY - sourceY;
            var norm = Math.sqrt(targetVectorX*targetVectorX + targetVectorY * targetVectorY);
            var targetOrthVectorX = -1 * targetVectorY / norm;
            var targetOrthVectorY = targetVectorX / norm;
            var xDist = offset * targetOrthVectorX;
            var yDist = offset * targetOrthVectorY;
            
            
            // Kick the composition arcs a coupel pixels away
            sourceX += xDist;
            sourceY += yDist;
            targetX += xDist;
            targetY += yDist;
        }
        
         var points =
           sourceX+","+sourceY+" "
         + targetX+","+targetY+" "
        ;
        
        return points;
    }
    
    public updateArcMarkerFunc = (linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string => {
        if(linkData.relationType === this.conceptGraph.relationLabelConstants.inheritance){
            return this.computeArcMarkerForInheritance(linkData, ignoreOffset);
        } else if(linkData.relationType === this.conceptGraph.relationLabelConstants.composition) {
            return this.computeArcMarkerForComposition(linkData, ignoreOffset);
        } else if(linkData.relationType === this.conceptGraph.relationLabelConstants.mapping) {
            return this.computeArcMarkerForMapping(linkData, ignoreOffset);
        } else {
            // No edge for relation property arcs, as defined per ontology
            return this.computeArcMarkerPropertyRelations(linkData, ignoreOffset);
        }
    }
    
    // For drawing the triangular marker on the arcs
    triSegLen = 14;
    triangleMarkerPointAngle = (25/360) * (2 * Math.PI);
    private computeArcMarkerForInheritance(linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string{
        var sourceX = linkData.source.x;
        var sourceY = linkData.source.y;
        var targetX = linkData.target.x;
        var targetY = linkData.target.y;

        // This is supposed to be division by 2 to find the endpoint, but moving it toward the target
        // a bit gives better marker positioning.
        var midPointX = sourceX + (targetX - sourceX)/2;
        var midPointY = sourceY + (targetY - sourceY)/2;
        
        // But...I need to make a triangular convolution, to replace the markers.
        // Marker path was: path.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
        // Rotate a triangleSide line in both directions, and add the midpoint to each.
        // From the endpoint of the first and startpoint of the second, draw an additional segment.
        // 0.5 * PI because I need to rotate the whole thing by 90 degrees
        var atanSourceTarget = Math.PI * 0.5 - Math.atan2(sourceX - targetX, sourceY - targetY);
        var triAngle1 = atanSourceTarget + this.triangleMarkerPointAngle;
        var triAngle2 = atanSourceTarget - this.triangleMarkerPointAngle;
        // Since y = 0 always, let's save some CPU cycles
        // var y = 0;
        var triPointX1 = (this.triSegLen * Math.cos(triAngle1) ); //+ y * Math.sin(triAngle1)); // x*cos(theta) + y*sin(theta)
        var triPointY1 = (this.triSegLen * Math.sin(triAngle1) ); // + y * Math.cos(triAngle1)); // x*sin(theta) + y*cos(theta)
        var triPointX2 = (this.triSegLen * Math.cos(triAngle2) ); // + y * Math.sin(triAngle2));
        var triPointY2 = (this.triSegLen * Math.sin(triAngle2) ); // + y * Math.cos(triAngle2));
        // Make relative to the midPoint
        // I would move the marker half of its length back towards the source, but I want to save
        // CPU cycles...this method is called a lot.
        triPointX1 += midPointX;
        triPointY1 += midPointY;
        triPointX2 += midPointX;
        triPointY2 += midPointY;
        
        var points =
         + midPointX+","+midPointY+" "
         + triPointX1+","+triPointY1+" "
         + triPointX2+","+triPointY2+" "
         + midPointX+","+midPointY+" "
        ;
        return points;
    }
    
    diamondLength = 14.0; // And height, if we use 45 degrees
    diamondAngle = (45/360) * (2 * Math.PI);
    private computeArcMarkerForComposition(linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string{
        
        // In order to prevent arcs from overlapping, we need to assign slots to them. These slots
        // will be convertable to physical pixel offsets via an array.
        var offset = this.pixelMap[linkData.edgePositionSlot];
        if(ignoreOffset === true){ // need === because it might get a numeric arg via D3 or JQuery!
            offset = 0;
        }
        
        var sourceX = linkData.source.x;
        var sourceY = linkData.source.y;
        var targetX = linkData.target.x;
        var targetY = linkData.target.y;
    
        // Get orthogonal vector, by changing x and y and flipping sign on first component (x).
        // We'll want the vector relative to source, then the same repeated for target...but since
        // we know the target orthogonal vector is parallel to the source orthogonal vector, we can
        // infer it.
        var targetVectorX = targetX - sourceX;
        var targetVectorY = targetY - sourceY;
        var norm = Math.sqrt(targetVectorX*targetVectorX + targetVectorY * targetVectorY);
        var targetOrthVectorX = -1 * targetVectorY / norm;
        var targetOrthVectorY = targetVectorX / norm;
        var xDist = offset * targetOrthVectorX;
        var yDist = offset * targetOrthVectorY;
        
        // Make is_a and has_a arcs move away from eachother by enough that we can see them both
        // for when both relations exist in a pair of nodes
        if(linkData.relationType === this.conceptGraph.relationLabelConstants.composition){
            // Kick the composition arcs a coupel pixels away
            sourceX += xDist;
            sourceY += yDist;
            targetX += xDist;
            targetY += yDist;
        }
        
        // the path will go from the tip of a diamond shape, around the perimeter, then cut again through
        // the middle to recommence the line.
        var atanSourceTarget = Math.PI * 0.5 - Math.atan2(sourceX - targetX, sourceY - targetY);
        
        var diamondAngle1 = atanSourceTarget + (this.diamondAngle); // sign controls above/below line
        var diamondAngle2 = atanSourceTarget - (this.diamondAngle);
        // Since y = 0 always, let's save some CPU cycles
        // var y = 0;
        var asq = (this.diamondLength/2) * (this.diamondLength/2);
        var diamondSideLength =  Math.sqrt(asq + asq);
        var triPointX1 = (diamondSideLength * Math.cos(diamondAngle1) ); //+ y * Math.sin(triAngle1)); // x*cos(theta) + y*sin(theta)
        var triPointY1 = (diamondSideLength * Math.sin(diamondAngle1) ); // + y * Math.cos(triAngle1)); // x*sin(theta) + y*cos(theta)
        var triPointX2 = -1*(diamondSideLength * Math.cos(diamondAngle1) ); // + y * Math.sin(triAngle2));
        var triPointY2 = -1*(diamondSideLength * Math.sin(diamondAngle1) ); // + y * Math.cos(triAngle2));
        
        // This is supposed to be division by 2 to find the endpoint, but moving it toward the target
        // a bit gives better marker positioning.
        var diamondXDelta = (this.diamondLength/2 * Math.cos(atanSourceTarget) );
        var diamondYDelta = (this.diamondLength/2 * Math.sin(atanSourceTarget) );
        var midPointX1 = sourceX + (targetX - sourceX)/2 - diamondXDelta;
        var midPointY1 = sourceY + (targetY - sourceY)/2 - diamondYDelta;
        var midPointX2 = sourceX + (targetX - sourceX)/2 + diamondXDelta;
        var midPointY2 = sourceY + (targetY - sourceY)/2 + diamondYDelta;
        
        // Make relative to the midPoint
        // I would move the marker half of its length back towards the source, but I want to save
        // CPU cycles...this method is called a lot.
        triPointX1 += midPointX1;
        triPointY1 += midPointY1;
        triPointX2 += midPointX2;
        triPointY2 += midPointY2;
        
        var points =
         + midPointX1+","+midPointY1+" "
         + triPointX1+","+triPointY1+" "
         + midPointX2+","+midPointY2+" "
         + triPointX2+","+triPointY2+" "
         + midPointX1+","+midPointY1+" "
        ;
        return points;
    }
    
    private computeArcMarkerForMapping(linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string{
        return "";
    }
     
    private computeArcMarkerPropertyRelations(linkData: ConceptGraph.Link, ignoreOffset: boolean = false): string{
        return "";
    }
    
    
    dragendLambda(outerThis: ConceptPathsToRoot): {(d: any, i: number): void} {
        return function(d, i) {
            outerThis.dragging = false;
            // $(this).tipsy('show');
            // Added click-for-toooltip, and it seems better if dragging fully cancels tooltips.
            // $(".tipsy").show();
            // no need to make the node fixed because we stop the layout when drag event begins
            // if it is set to fixed, the node interferes with other layouts
            //d.fixed = true;
        }
    }
    
    createNodePopupTable(conceptRect, conceptData){
        var outerDiv = $("<div></div>");
        outerDiv.addClass("popups-Popup");
        
        var noWrapStyle = {"white-space":"nowrap"};
        var wrapStyle = {};

        var table = $("<table></table>");
        var tBody = $("<tbody></tbody>");
         outerDiv.append(table);
         table.append(tBody);

         
         tBody.append(
                 $("<tr></tr>").append(
                       $("<td></td>").append(
                               $("<div></div>").text(conceptData["name"]).attr("class","popups-Header")
                       )
               )
         );
         
         {
         var outerThis = this;
         var checkboxInput = $("<input type='checkbox' "+(this.isNodeHidden(conceptData) ? "checked=checked" : "")+">")
         .attr("id", "popupCheckId");
         // Since this is turned into html, any JQuery event bindings will get lost. We have to do the binding later,
         // and we need to prevent re-binding by calling off() first, as seen here.
         $(document.body).off().on("change", "#popupCheckId", function(){
                            console.log("hey");
                            outerThis.toggleHideNodeLambda(outerThis)(conceptData, 0);
                            outerThis.refreshOtherFilterCheckboxStates([conceptData], null);
                        }
                    );
         
         var checkboxUnit = $("<span>")
             .addClass("popupNodeHideCheckbox")
                .append(
                   checkboxInput
                )
                .append(
                    $("<label>").attr("for", "popupCheckId")
                    .append("Hide Node")
                )
            ;

         
        tBody.append($("<tr></tr>").append($("<td></td>").append(checkboxUnit)));
         }
         
         var urlText = "http://bioportal.bioontology.org/ontologies/"+conceptData["ontologyAcronym"]+"?p=classes&conceptid="+conceptData["rawConceptUri"];
         tBody.append(
                 $("<tr></tr>").append(
                         $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
                                 $("<div></div>").css(noWrapStyle).append(
                                         $("<a></a>").attr("target", "_blank").attr("href", urlText).text("Open concept homepage in tab")
                                 )
                         )
                 )
         );
         
         var ontologyUrlText = "http://bioportal.bioontology.org/ontologies/"+conceptData["ontologyAcronym"];
         tBody.append(
                     $("<tr></tr>").append(
                             $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
                                     $("<div></div>").css(noWrapStyle).append(
                                             $("<b></b>").text("Ontology: ")
                                     ).append(
                                             $("<a></a>").attr("target", "_blank").attr("href", ontologyUrlText).text(conceptData["ontologyAcronym"])
                                     )
                             )
                     )
             );
         
         var jsonArgs = {
                "Concept ID: ": {"key": "rawConceptUri", "style": noWrapStyle},
                "Synonyms: ": {"key": "synonym", "style": wrapStyle},
                "Definition: ": {"key": "definition", "style": wrapStyle}
         };
         $.each(jsonArgs,function(label, properties){
             var style: {} = properties["style"]
             var propertyKey: string = properties["key"];
             tBody.append(
                     $("<tr></tr>").append(
                             $("<td></td>").attr("align","left").css({"vertical-align": "top"}).append(
                                     $("<div></div>").css(style).append(
                                             $("<b></b>").text(label)
                                     ).append(
                                             $("<span></span>").text(conceptData[propertyKey])
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
        var updateLinksFromJson = function(i, d: ConceptGraph.Link){ // JQuery is i, d
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "link"
            var link = outerThis.vis.select("#link_line_"+d.id);
            // Concept graphs have fixed node and arc sizes.
            // link.attr("data-thickness_basis", function(d) { return d.value;})
            // link.select("title").text(outerThis.conceptLinkLabelFunction);
            link.select("title").text(function(d: ConceptGraph.Link) { return d.value;});
        }
        
        var updateNodesFromJson = function(i, d: ConceptGraph.Node){ // JQuery is i, d
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "node"
            var node = outerThis.vis.select("#node_g_"+d.conceptUriForIds);
            var nodeRects = node.select(GraphView.BaseGraphView.nodeSvgClassSansDot);
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
//                    && outerThis.lastDisplayedTipsyData.nodeIdentifier == d.rawConceptUri
                    ){
                console.log("This wont' work anymore");
                // $(outerThis.lastDisplayedTipsy).children(".tipsy-inner").html(outerThis.createNodePopupTable(outerThis.lastDisplayedTipsySvg, outerThis.lastDisplayedTipsyData));
            }
        }
        
        $.each(json.links, updateLinksFromJson);
        $.each(json.nodes, updateNodesFromJson);
    }
    
    
    /**
    * This function should be used when adding brand new nodes and links to the
    * graph. Do not call it to update properties of graph elements.
    */
    populateNewGraphElements(graphD3Format: ConceptGraph.ConceptD3Data){
        this.populateNewGraphEdges(graphD3Format.links);
        this.populateNewGraphNodes(graphD3Format.nodes);
        // this.runCurrentLayout();
        // Call start() only if we actually added (or removed) anything
        // this.forceLayout.start();
    }
    
    populateNewGraphEdges(linksData: ConceptGraph.Link[], temporaryEdges?: boolean){
        // Advice from http://stackoverflow.com/questions/9539294/adding-new-nodes-to-force-directed-layout
        if(linksData.length == 0){
            return [];
        }
        
        var outerThis = this;
        
        // Data constancy via key function() passed to data()
        // Link stuff first
        var links = this.vis.select("#link_container")
        .selectAll("polyline"+GraphView.BaseGraphView.linkSvgClass).data(linksData, ConceptGraph.Link.d3IdentityFunc);
        
        // Add new stuff
        // Make svg:g like nodes if we need labels
        // Would skip the g element here for links, but it cleans up the document and bundles text with line.
        var enteringLinks = links.enter().append("svg:g")
        .attr("class",
            (d: ConceptGraph.Link)=>{
                return GraphView.BaseGraphView.linkSvgClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+d.relationType
                +" "+outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }
        )
        .attr("id", function(d: ConceptGraph.Link){ return "link_g_"+d.id});
        
        var enteringPolylines = enteringLinks.append("svg:polyline")
        .attr("class",
            function(d: ConceptGraph.Link){
                return GraphView.BaseGraphView.linkSvgClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+d.relationType
                +" "+outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }
        )
        .attr("id", function(d: ConceptGraph.Link){ return "link_line_"+d.id})
        .on("mouseover", this.highlightHoveredLinkLambda(this))
        .on("mouseout", this.unhighlightHoveredLinkLambda(this))
        // Old, faster but not-so-cross-browser way of adding triangular markers
        // .attr("marker-mid", this.markerAdderLambda() )
        .attr("data-thickness_basis", function(d) { return d.value;});
        
        var enteringArcMarkers = enteringLinks.append("svg:polyline")
        .attr("class",
            function(d: ConceptGraph.Link){
                return GraphView.BaseGraphView.linkMarkerSvgClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+d.relationType
                +" "+outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }
        )
        .attr("id", function(d: ConceptGraph.Link){ return "link_marker_"+d.id})
        .on("mouseover", this.highlightHoveredLinkLambda(this))
        .on("mouseout", this.unhighlightHoveredLinkLambda(this));
                    
        // Update Tool tip
        enteringLinks // this is new...used to do to all linked data...
        .append("title") // How would I *update* this if I needed to?
            .text(this.conceptLinkSimplePopupFunction)
                .attr("id", function(d: ConceptGraph.Link){ return "link_title_"+d.id});
        
        
        if(!enteringLinks.empty()){
            if(!temporaryEdges){
                this.runCurrentLayout(true);
            } else {
                enteringPolylines.classed(GraphView.BaseGraphView.temporaryEdgeClass, true);
                // Using CSS class so we can later pin down temporary edge data,
                // so we don't actually need the markers tagged this way.
                // enteringArcMarkers.classed(GraphView.BaseGraphView.temporaryEdgeClass);
            }
            this.updateStartWithoutResume();
            enteringPolylines.attr("points", this.updateArcLineFunc);
            enteringArcMarkers.attr("points", this.updateArcMarkerFunc);
            this.edgeTypeFilter.updateFilterUI();
        }
    }
    
    private giveIEMarkerWarning = true;
    private markerAdderLambda(){
        var outerThis = this;
        return function(e: ConceptGraph.Link){
        // IE doesn't support defs for markers, or at least *use* of markers.
        // See marker def construction for details.
           if(outerThis.isIE()){
                if(outerThis.giveIEMarkerWarning){
                    console.log("WARNING: Line markers not used for IE due to lack of support for valid SVG marker defs. Known IE SVG bug, they won't fix it.");
                    outerThis.giveIEMarkerWarning = false;
                }
                return "";
            } else {
                return "url(#"+"LinkHeadMarker_"+outerThis.getLinkCssClass(e.relationType, e.relationSpecificToOntologyAcronym)+")";
            }
        }
    }

    /** Retrieve classes for styling control only...either direct style, or
     *  for logic controls in the case of property relation links.
     */
    public propertyRelationClassNames: Array<String> = ["inheritanceStyleLink", "compositionStyleLink", "mappingStyleLink"];
    public getLinkCssClass(relationType: string, relationSpecificToOntologyAcronym: ConceptGraph.RawAcronym): string{
        if(undefined !== relationSpecificToOntologyAcronym) {
			// Relation type as defined by the ontology, and showing up in the concept properties.
			// See the PropertyRelationsExpander for more details.
            var cssClassName = relationSpecificToOntologyAcronym+"__"+relationType+"LinkStyle";
            if($.stylesheet("."+cssClassName).rules().length === 0){
                // Define this new class, give it default properties.
                var ontColor = this.conceptGraph.nextNodeColor(relationSpecificToOntologyAcronym);
                // JQuery-Stylesheet plugin: https://github.com/f0r4y312/jquery-stylesheet
                var sheet = $.stylesheet("."+cssClassName);
                sheet.css("stroke", ontColor);
                sheet.css("fill", ontColor);
                this.propertyRelationClassNames.push(cssClassName);
            }
            return cssClassName;
        } else if(-1 !== relationType.indexOf("is_a")){
            return this.conceptGraph.relationTypeCssClasses["is_a"];
        } else if(-1 !== relationType.indexOf("part_of") || -1 !== relationType.indexOf("has_part")){
        	// This is a special case hard coding. These relations have some special status, but hardly
        	// show up in all ontologies. They made their way here because they were present in the earliest
        	// versions of Biomixer.
            return this.conceptGraph.relationTypeCssClasses["part_of"];
        } else if(-1 !== $.inArray(relationType, ["ncbo-mapping", "maps_to"])){
            return this.conceptGraph.relationTypeCssClasses["maps_to"];
        } else {
            // I never want this. Nothing should get here, really, since things are either
            // created as one of the predefined arc types, or came from an ontology, whose
            // acronym is passed in.
            console.log("Generated invalid link CSS class for type and acronym: "+relationType+" and "+relationSpecificToOntologyAcronym);
            return "undefined_link_css_class";
        }
    }
    
    // Modified from http://stackoverflow.com/questions/2400935/browser-detection-in-javascript
    private isIE(): boolean {
        var ua= navigator.userAgent, tem, 
        M= ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
        if(/trident/i.test(M[1])){
            return true;
        } else if(/msie/i.test(M[1])){
            return true;
        } else {
            return false;
        }
    }
    
    private defineCustomSVG(){
        // Define markers to be used as heads on arcs
        var svgNode = $("#graphSvg");
        // HacK: IE does not support markers on lines. It is recognized as a bug, with the "Won't Fix"
        // closure status: https://connect.microsoft.com/IE/feedback/details/781964/
        // Therefore, for now, we simply don't define the markers for any IE version.
        // Versions with the bug are uncertain, but run the gamut from 9 to 11 (confirmed 11).
        // So, for IE< don't run this function!
        // Hmm...doign thsi here doesn't fix it, even though deleting the def in browser fixes it.
        // Preventing adding of marker to polylines instead, when we populate the graph.
        // if(this.isIE()){
        //     console.log("Line markers not used for IE due to lack of support for valid SVG marker defs.");
        //     return;
        // }
        
        var defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
        
        // http://www.alt-soft.com/tutorial/svg_tutorial/marker.html
        // http://stackoverflow.com/questions/3290392/creating-svg-markers-programatically-with-javascript        
        var arcCssClassArray = ["inheritanceStyleLink", "compositionStyleLink", "mappingStyleLink"];
        var arcCssLabelArray = ["is a", "has a", "maps to"];
        for(var i = 0; i < arcCssClassArray.length; i++){
            // Do the arrow markers first
            {
                var cssClass = arcCssClassArray[i];
                var marker = document.createElementNS("http://www.w3.org/2000/svg", "marker");
                marker.setAttribute("id", "LinkHeadMarker_"+cssClass);
                marker.setAttribute("class", cssClass+" linkMarker");
                marker.setAttribute("viewBox", "0 0 10 10");
                marker.setAttribute("refX", "0");
                marker.setAttribute("refY", "5");
                //  marker.setAttribute("markerUnits", "strokeWidth");
                marker.setAttribute("markerUnits", "userSpaceOnUse");
                marker.setAttribute("markerWidth", "10");
                marker.setAttribute("markerHeight", "8");
                marker.setAttribute("orient", "auto");
                marker.setAttribute("overflow", "visible");
                
                var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                path.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
                path.setAttribute("class", "linkMarker "+cssClass);
                marker.appendChild(path);
                
                // These labels make the visualization slow down a lot. If someone asks for them, we'll add them.
                // We should brainstorm alternatives.
//                var label = document.createElementNS("http://www.w3.org/2000/svg", "text");
//                label.textContent = arcCssLabelArray[i];
//                label.setAttribute("id", "LinkLabelMarker_"+cssClass);
//                label.setAttribute("class", "linkText "+cssClass);
//                label.setAttribute("unselectable", "on"); // IE 8
//                //  label.setAttribute("font-size", "10px"); // IE 8
//                label.setAttribute("onmousedown", "noselect"); // IE ?
//                label.setAttribute("onselectstart", "function(){ return false;}"); // IE 8?
//                label.setAttribute("dx", "1em");
//                label.setAttribute("dy", "1em"); // 1em down to go below baseline, 0.5em to counter padding added below
//                marker.appendChild(label);
                
                defs.appendChild(marker);
            }
        }
        svgNode.append(defs);
    }
    
    populateNewGraphNodes(nodesData: ConceptGraph.Node[]){
        // Advice from http://stackoverflow.com/questions/9539294/adding-new-nodes-to-force-directed-layout
        if(nodesData.length == 0){
            return [];
        }
        
        var outerThis = this;
        
        var nodes = this.vis.select("#node_container")
        .selectAll("g.node_g").data(nodesData, ConceptGraph.Node.d3IdentityFunc);
        // Add new stuff
        var enteringNodes = nodes.enter()
        .append("svg:g")
        .attr("class", GraphView.BaseGraphView.nodeGSvgClassSansDot)
        .attr("id", function(d: ConceptGraph.Node){ return "node_g_"+d.conceptUriForIds})
        .call(this.nodeDragBehavior)
        .on("mouseover", this.highlightHoveredNodeLambda(this, true))
        .on("mouseout", this.unhighlightHoveredNodeLambda(this, true))
        ;
        
        // Basic properties
        enteringNodes
        .append("svg:rect")
        .attr("id", function(d: ConceptGraph.Node){ return "node_rect_"+d.conceptUriForIds})
        .attr("class",
            function(d: ConceptGraph.Node){
                var classes = GraphView.BaseGraphView.nodeSvgClassSansDot+" "+GraphView.BaseGraphView.conceptNodeSvgClassSansDot;
                if(d.rawConceptUri === outerThis.conceptGraph.centralConceptUri){
                    classes += " centralNode";
                }
                return classes;
            })
        .style("fill", function(d: ConceptGraph.Node) { return d.nodeColor; })
        .attr("height", this.nodeHeight)
        .attr("width", this.nodeHeight)
        ;
        
        
        // tipsy stickiness from:
        // http://stackoverflow.com/questions/4720804/can-i-make-this-jquery-tooltip-stay-on-when-my-cursor-is-over-it
        enteringNodes.each(TipsyToolTipsOnClick.nodeTooltipOnClickLambda(this));
            
        // Dumb Tool tip...not needed with tipsy popups.
        // nodesEnter.append("title")
        //   .attr("id", function(d){ return "node_title_"+d.acronym})
        //   .text(function(d) { return "Number Of Terms: "+d.number; });
        
        // Label
        enteringNodes.append("svg:text")
        .attr("id", function(d: ConceptGraph.Node){ return "node_text_"+d.conceptUriForIds})
        .attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot+" unselectable")
        // .attr("dx", "0em")
        // .attr("dy", "1em") // 1em down to go below baseline, 0.5em to counter padding added below
        .text(function(d: ConceptGraph.Node) { return d.name; })
        // Not sure if I want interactions on labels or not. Change following as desired.
        .style("pointer-events", "none")
        // Why cannot we stop selection in IE? They are rude.
        .attr("unselectable", "on") // IE 8
        .attr("onmousedown", "noselect") // IE ?
        .attr("onselectstart", "function(){ return false;}") // IE 8?
        ;
        
        // Resize each node to encompass the label we just created.
        $(GraphView.BaseGraphView.nodeLabelSvgClass).each(function(i, d: Element){
            var textSize = this.getBBox(); // d.getBBox();
            var rect = $(d).siblings().filter(GraphView.BaseGraphView.nodeSvgClass);
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
        
        this.attachNodeMenu(enteringNodes);
        
        // TODO I made a different method for removing nodes that we see below. This is bad now, yes?
        // nodes.exit().remove();
        
        
        if(!enteringNodes.empty()){
            this.runCurrentLayout(true);
            this.updateStartWithoutResume();
            enteringNodes.attr("transform", function(d: ConceptGraph.Node) { return "translate(" + d.x + "," + d.y + ")"; });
        
            this.edgeTypeFilter.updateFilterUI();
            // this.individualConceptFilter.updateFilterUI();
            // this.ontologyFilter.updateFilterUI();
            this.nestedOntologyConceptFilter.updateFilterUI();
            this.expansionSetFilter.updateFilterUI();
        
        }
    }

    removeMissingGraphElements(){
        //console.log("Removing some graph elements "+Utils.getTime());
        
        // Have problems if we don't pass the containers back in like this.
        // Removed edges will not be properly removed, and exceptions will
        // be thrown.
        this.forceLayout.nodes(this.conceptGraph.graphD3Format.nodes);
        this.forceLayout.links(this.conceptGraph.graphD3Format.links);
        
        // I would null out the source and target of the edges we are removing,
        // but we don't kow if the caller has other plans for deleted edges .
        
        var nodes = this.vis.selectAll("g.node_g").data(this.conceptGraph.graphD3Format.nodes, ConceptGraph.Node.d3IdentityFunc);
        var links = this.vis.selectAll("g."+ GraphView.BaseGraphView.linkSvgClassSansDot).data(this.conceptGraph.graphD3Format.links, ConceptGraph.Link.d3IdentityFunc);
        
        var nodesRemoved = nodes.exit().remove();
        var linksRemoved = links.exit().remove();
        
        // Update filter sliders. Filtering and layout refresh should be updated within the slider event function.
        this.filterSliders.updateTopMappingsSliderRange();
        this.filterSliders.rangeSliderSlideEvent(null, null); // Bad to pass nulls when I know it will work, or ok?
        
        // Note that the length of the Selection is not the definition of empty().
        if(!nodesRemoved.empty() || !linksRemoved.empty()){
            // This is somewhat correct, but if we do this, then when people delete nodes
            // the view they are working with will be shifting around...
            //if(!nodesRemoved.empty()){
                // this.runCurrentLayout(true);
            //}
            this.updateStartWithoutResume();
            // this.individualConceptFilter.updateFilterUI();
            // this.ontologyFilter.updateFilterUI();
            this.nestedOntologyConceptFilter.updateFilterUI();
            this.edgeTypeFilter.updateFilterUI();
            this.expansionSetFilter.updateFilterUI();
        }
    }
    
    attachNodeMenu(enteringNodes: D3.Selection){
        // Menu indicator:
        var expanderSvgs = enteringNodes
        .append("svg:svg").attr("overflow", "visible")
        .attr("x", function(d: ConceptGraph.Node){ return -1 * parseInt($("#node_rect_"+d.conceptUriForIds)[0].getAttribute("height"), 0)/2; } )
        .attr("y", function(d: ConceptGraph.Node){ return parseInt($("#node_rect_"+d.conceptUriForIds)[0].getAttribute("height"), 0)/2; })
        .on("click", this.showNodeExpanderPopupMenuLambda(this))
        ;
        
        expanderSvgs
        .append("svg:rect")
        .attr("id", function(d: ConceptGraph.Node){ return "node_expander_indicator_"+d.conceptUriForIds})
        // .attr("class", GraphView.BaseGraphView.nodeSvgClassSansDot+" "+GraphView.BaseGraphView.conceptNodeSvgClassSansDot)
        .style("fill", "#c5effd")
        .style("stroke", "#afc6e5")
        .attr("height", this.expansionBoxHeight)
        .attr("width", this.expansionBoxWidth)
        .attr("overflow", "visible")
        ;
        
        expanderSvgs
        .append("svg:polygon")
        .attr("points", "11.25,2 18.75,2 15,6 ")
        .style("fill", "#000000")
        .attr("x", function(d: ConceptGraph.Node){ return -1 * (this.getAttribute("width")/2);} )
        .attr("y", function(d: ConceptGraph.Node){ return parseInt($("#node_rect_"+d.conceptUriForIds)[0].getAttribute("height"), 0)/2; })
        .attr("overflow", "visible")
        ;
     }
    
    showNodeExpanderPopupMenuLambda(outerThis: ConceptPathsToRoot){
        return function(nodeData: ConceptGraph.Node){
            var rectWidth = 110;
            var rectHeight = 35;
            var fontXSvgPadding = 7;
            var fontYSvgPadding = 23;
            
            // JQuery does not allow the specification of a namespace when creating elements.
            // If the namespace is not specified for svg elements, they do not render, though they do get added to the DOM.
            // To do so, you need to do verbose things like: document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            // So, I don't get to use JQuery as much as D3 it turns out.
            
            var innerSvg = d3.select(this).append("svg:svg")
                    .attr("id", "expanderMenu")
                    .attr("overflow", "visible").attr("y", 0).attr("x", -1 * (rectWidth/2 + parseInt(d3.select(this).attr("x"), 0)))
                    .attr("width", rectWidth).attr("height", rectHeight * 2)
                    .style("z-index", 100)
                    .on("mouseleave", function(){ outerThis.unhighlightHoveredNodeLambda(outerThis, false)(nodeData, 0); $("#expanderMenu").first().remove(); })
                    // The mouseup one is required due to a silly graphical bug I could not fix.
                    // If a greyed-out menu item was clicked, it also triggered a re-dispatch of the menu creating function,
                    // and produced a broken container relative to the text elements within.
                    // I tried many things which didn't work. Eventually I decided to get rid of the box altogether
                    // whenever a greyed-out menu item is clicked; that is, if the item doesn't have a body to its handler,
                    // it allows this one to close the menu.
                    .on("mouseup", function(){ $("#expanderMenu").first().remove(); })
            ;
            // We also add hover effects to text children lower down
            
            // Create concept expander button
            {
            var conceptExpandSvg = innerSvg.append("svg:svg")
                    .attr("overflow", "visible").attr("y", 0)
                    .classed("expanderMenuItem", true)
            ;
                        
            // If this node is currently cleared for expansion within the undo/stack current context,
            // then it means we already did this expansion (possibly via another means).
            // Let's alter the menu to reflect this.
            var conceptExpandTextValue;
            var conceptExpandFontFillColor;
            var conceptExpandMouseUpFunc;

            var hardTermExpansionCount = outerThis.conceptGraph.getNumberOfPotentialNodesToExpand(String(nodeData.rawConceptUri), ConceptGraph.PathOptionConstants.termNeighborhoodConstant);

            if(hardTermExpansionCount != 0){
                conceptExpandTextValue = "Expand Concepts";
                conceptExpandTextValue +=" ("+hardTermExpansionCount+")"; // +" ("+conceptExpState.numMissing+";

                
                conceptExpandFontFillColor = ""; // empty works to *not* add a value at all
                conceptExpandMouseUpFunc = function(){
                            $("#expanderMenu").first().remove();
                            var expId = new ExpansionSets.ExpansionSetIdentifer("concept_expand_"+nodeData.conceptUriForIds, "Concepts: "+nodeData.name+" ("+nodeData.ontologyAcronym+")");
                            var expansionSet = new ExpansionSets.ExpansionSet(expId, nodeData, outerThis.conceptGraph, outerThis.undoRedoBoss,
                                ConceptGraph.PathOptionConstants.termNeighborhoodConstant);
                            outerThis.conceptGraph.expandConceptNeighbourhood(nodeData, expansionSet);
                        };
            } else {
                conceptExpandTextValue = "Concepts Already Expanded";
                conceptExpandFontFillColor = "#AAAAAA"; // grey out font when we can't use the item
                conceptExpandMouseUpFunc = function(){return false;};
            }
            
            conceptExpandSvg.append("svg:rect")
                    .style("fill","#FFFFFF").style("stroke","#000000").attr("x",0).attr("y",0).attr("width",rectWidth).attr("height",rectHeight)
                    .on("mouseup", conceptExpandMouseUpFunc);
            conceptExpandSvg.append("svg:text")
                .text(conceptExpandTextValue)
                .style("font-family","Arial, sans-serif").style("font-size","12px").style("fill",conceptExpandFontFillColor).attr("dx", fontXSvgPadding).attr("dy", fontYSvgPadding)
                .style("font-weight", "inherit")
                .attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot+" unselectable "+" expanderMenuText")
                .style("pointer-events", "none")
                // Why cannot we stop selection in IE? They are rude.
                .attr("unselectable", "on") // IE 8
                .attr("onmousedown", "noselect") // IE ?
                .attr("onselectstart", "function(){ return false;}") // IE 8?
            ;
            }
            
            // Create mapping expander button
            {
            var mappingExpandSvg = innerSvg.append("svg:svg")
                    .attr("overflow", "visible").attr("y", rectHeight)
                    .classed("expanderMenuItem", true)
            ;
            
            // If this node is currently cleared for expansion within the undo/stack current context,
            // then it means we already did this expansion (possibly via another means).
            // Let's alter the menu to reflect this.
            var mappingExpandTextValue;
            var mappingExpandFontFillColor;
            var mappingExpandMouseUpFunc;

            var hardMappingExpansionCount = outerThis.conceptGraph.getNumberOfPotentialNodesToExpand(String(nodeData.rawConceptUri), ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant);

            if(hardMappingExpansionCount !== 0){
                mappingExpandTextValue = "Expand Mappings";
                mappingExpandTextValue +=" ("+hardMappingExpansionCount+")"; // +" ("+mappingExpState.numMissing+")";
                mappingExpandFontFillColor = ""; // empty works to *not* add a value at all
                mappingExpandMouseUpFunc = function(){
                            $("#expanderMenu").first().remove();
                            var expId = new ExpansionSets.ExpansionSetIdentifer("mapping_expand_"+nodeData.conceptUriForIds, "Mappings: "+nodeData.name+" ("+nodeData.ontologyAcronym+")")
                            var expansionSet = new ExpansionSets.ExpansionSet(expId, nodeData, outerThis.conceptGraph, outerThis.undoRedoBoss,
                                ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant);
                            outerThis.conceptGraph.expandMappingNeighbourhood(nodeData, expansionSet);
                        };
            } else {
                mappingExpandTextValue = "Mappings Already Expanded";
                mappingExpandFontFillColor = "#AAAAAA"; // grey out font when we can't use the item
                mappingExpandMouseUpFunc = function(){return false;};
            }
            
            mappingExpandSvg.append("svg:rect")
                    .style("fill","#FFFFFF").style("stroke","#000000").attr("x",0).attr("y",0).attr("width",rectWidth).attr("height",rectHeight)
                    .on("mouseup", mappingExpandMouseUpFunc);
            
            mappingExpandSvg.append("svg:text")
                .text(mappingExpandTextValue)
                .style("font-family","Arial, sans-serif").style("font-size","12px").style("fill",mappingExpandFontFillColor).attr("x", fontXSvgPadding).attr("y", fontYSvgPadding)
                .style("font-weight", "inherit")
                .attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot+" unselectable "+" expanderMenuText")
                .style("pointer-events", "none")
                // Why cannot we stop selection in IE? They are rude.
                .attr("unselectable", "on") // IE 8
                .attr("onmousedown", "noselect") // IE ?
                .attr("onselectstart", "function(){ return false;}") // IE 8?
            ;
            }
            
            // Create menu item for refocussing on node
            {
            var centralizeNodeSvg = innerSvg.append("svg:svg")
                    .attr("overflow", "visible").attr("y", 2*rectHeight)
                    .classed("expanderMenuItem", true)
            ;
            centralizeNodeSvg.append("svg:rect")
                    .style("fill","#FFFFFF").style("stroke","#000000").attr("x",0).attr("y",0).attr("width",rectWidth).attr("height",rectHeight)
                    .on("mouseup",  function(){ $("#expanderMenu").first().remove(); outerThis.recomputeVisualizationOntoNode(nodeData); })
            ;
            centralizeNodeSvg.append("svg:text")
                .text("Refocus Node")
                .style("font-family","Arial, sans-serif").style("font-size","12px").attr("x", fontXSvgPadding).attr("y", fontYSvgPadding)
                .style("font-weight", "inherit")
                .attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot+" unselectable "+" expanderMenuText")
                .style("pointer-events", "none")
                // Why cannot we stop selection in IE? They are rude.
                .attr("unselectable", "on") // IE 8
                .attr("onmousedown", "noselect") // IE ?
                .attr("onselectstart", "function(){ return false;}") // IE 8?
            ;
            }
        
            // Create menu item for hiding or dimming the node (also marking it for deletion, in the current design)[[
            {
            var hideNodeSvg = innerSvg.append("svg:svg")
                    .attr("overflow", "visible").attr("y", 3*rectHeight)
                    .classed("expanderMenuItem", true)
            ;
            hideNodeSvg.append("svg:rect")
                    .style("fill","#FFFFFF").style("stroke","#000000").attr("x",0).attr("y",0).attr("width",rectWidth).attr("height",rectHeight)
                    .on("mouseup",  function(){ $("#expanderMenu").first().remove(); outerThis.toggleHideNodeLambda(outerThis)(nodeData, 0); outerThis.refreshOtherFilterCheckboxStates([nodeData], null)})
            ;
            hideNodeSvg.append("svg:text")
                .text(outerThis.isNodeHidden(nodeData) ? "Unhide Node" : "Hide Node")
                .style("font-family","Arial, sans-serif").style("font-size","12px").attr("x", fontXSvgPadding).attr("y", fontYSvgPadding)
                .style("font-weight", "inherit")
                .attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot+" unselectable "+" expanderMenuText")
                .style("pointer-events", "none")
                // Why cannot we stop selection in IE? They are rude.
                .attr("unselectable", "on") // IE 8
                .attr("onmousedown", "noselect") // IE ?
                .attr("onselectstart", "function(){ return false;}") // IE 8?
            ;
            }
            
            // Resize the parent rectangles as necessary based on all of the children text elements
            // It does things fairly automatically and agnostic of the number of menu item text elements.
            // Obviously if we change the overall design of the menu this won't work as is.
            {
            // Size the parent rect according to the longest text child
            var maxWidth = 0;
            $("#"+innerSvg.attr("id")).find("text").each(
                function(index, element){
                    // Works for Chrome
                    // Only one to work for Firefox
                    // Only one to work for IE
                    var box = element.getBoundingClientRect();
                    var elemWidth = box.right-box.left;
                    
                    maxWidth = Math.max(maxWidth, elemWidth);
                }
            );
            
            // Need to account for the effective left padding (not actual padding, since it's SVG positioning)
            // The right side will need the same effective padding as well.
            maxWidth += 2*fontXSvgPadding + 4; // + 4 for compensate by bold making text wider 
            $("#"+innerSvg.attr("id")).attr("width", maxWidth);
            $("#"+innerSvg.attr("id")).find("rect").attr("width", maxWidth);
            console.log("Resized things, maxWidth: "+maxWidth);
            }
            
            
            // Make the menu labels bold when hovered over
            d3.selectAll(".expanderMenuItem")
                .on("mouseover", function(node: Node){ d3.select(this).classed("boldText", true); })
                .on("mouseout", function(node: Node){ d3.select(this).classed("boldText", false); })
            ;
        }
        
    }
    
    beforeNodeHighlight(targetNodeData){
         this.conceptGraph.manifestTemporaryHoverEdges(targetNodeData);
    }
    
    afterNodeUnhighlight(targetNodeData){
        this.conceptGraph.removeTemporaryHoverEdges(targetNodeData);
    }
    
    prepGraphMenu(){
        // Layout selector for concept graphs.
        this.menu.initializeMenu("Layout & Filter Menu");
        this.nodeFinder.addMenuComponents(this.menu.getMenuSelector());
        this.layouts.addMenuComponents(this.menu.getMenuSelector());
        this.importerExporterWidget.addMenuComponents(this.menu.getMenuSelector());
        this.edgeTypeFilter.addMenuComponents(this.menu.getMenuSelector(), true);
        this.nodeDeleter.addMenuComponents(this.menu.getMenuSelector());
        this.nestedOntologyConceptFilter.addMenuComponents(this.menu.getMenuSelector(), false);
        this.expansionSetFilter.addMenuComponents(this.menu.getMenuSelector(), true);
        
        // These two filters do not need to be included so long as I have the composite of them
        // included, via the nested ontology concept filter.
        // this.ontologyFilter.addMenuComponents(this.menu.getMenuSelector(), true);
        // this.individualConceptFilter.addMenuComponents(this.menu.getMenuSelector(), true);
        
//        this.filterSliders.addMenuComponents(this.menu.getMenuSelector(), this.softNodeCap);
    }
    
    /**
     * Synchronize checkboxes with changes made via other checkboxes.
     */
    refreshOtherFilterCheckboxStates(affectedNodes: ConceptGraph.Node[], triggeringFilter: ConceptFilterWidget.AbstractConceptNodeFilterWidget<any>){
        // if(triggeringFilter !== this.individualConceptFilter){
        //     this.individualConceptFilter.updateCheckboxStateFromView(affectedNodes);
        // }
        // if(triggeringFilter !== this.ontologyFilter){
        //     this.ontologyFilter.updateCheckboxStateFromView(affectedNodes);
        // }
        if(triggeringFilter !== this.expansionSetFilter){
            this.expansionSetFilter.updateCheckboxStateFromView(affectedNodes);
        }
        if(triggeringFilter !== this.nestedOntologyConceptFilter){
            this.nestedOntologyConceptFilter.updateCheckboxStateFromView(affectedNodes);
        }
    }
    
    revealAllNodesAndRefreshFilterCheckboxes(){
        // Add expansion sets, ontologies, and individual nodes all on the basis
        // of their hidden status (as determined via CSS classes set by the filters).
        // We are actually pretty agnostic about how they got that way...but if we
        // use that CSS class via any other thing that filter boxes, we could have a problem.
        // Trying to use filter statuses directly would have worse repercussions.
        // Grab all the hidden nodes and remove that class.
        // JQuery doesn't get along with SVG, so we have to use D3 for this work
        d3.selectAll("."+GraphView.BaseGraphView.hiddenNodeClass).classed(GraphView.BaseGraphView.hiddenNodeClass, false);
        d3.selectAll("."+GraphView.BaseGraphView.hiddenNodeLabelClass).classed(GraphView.BaseGraphView.hiddenNodeLabelClass, false);
        d3.selectAll("."+GraphView.BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass).classed(GraphView.BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass, false);
        
        // this.individualConceptFilter.updateFilterUI();
        // this.ontologyFilter.updateFilterUI();
        this.expansionSetFilter.updateFilterUI();
        this.nestedOntologyConceptFilter.updateFilterUI();
        
        // this.individualConceptFilter.checkmarkAllCheckboxes();
        // this.ontologyFilter.checkmarkAllCheckboxes();
        this.expansionSetFilter.checkmarkAllCheckboxes();
        this.nestedOntologyConceptFilter.checkmarkAllCheckboxes();
        
        this.runCurrentLayout(true);
    }
    
    sortConceptNodesCentralOntologyName(){
        var outerThis = this;
        return this.conceptGraph.graphD3Format.nodes.sort(
            function(a: ConceptGraph.Node, b: ConceptGraph.Node) {
                if(a.rawConceptUri === b.rawConceptUri){
                    // Exact same unqiue identifiers?
                    return 0;
                }
                
                // Is one of these the central node?
                if(a.rawConceptUri === outerThis.conceptGraph.centralConceptUri){
                    return -1;
                } else if(b.rawConceptUri === outerThis.conceptGraph.centralConceptUri){
                    return 1;
                }
                
                // Put central node ontologies above non-central ontologies, rest ontologies alphabetical
                if(a.ontologyAcronym !== b.ontologyAcronym){
                    if(a.ontologyAcronym === outerThis.centralOntologyAcronym){
                        return -1;
                    } else if(b.ontologyAcronym === outerThis.centralOntologyAcronym){
                        return 1;
                    } else {
                        return (a.ontologyAcronym < b.ontologyAcronym) ? -1 : 1;
                    }
                }
                
                // Alphabetical on concept names within ontologies
                return (a.name < b.name) ? -1 : 1;
            }
        );
    }
    
}

