///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

///<amd-dependency path="ExpansionSets" />
///<amd-dependency path="UndoRedo/UndoRedoManager" />
///<amd-dependency path="Utils" />
///<amd-dependency path="LayoutProvider" />

import ExpansionSets = require("./ExpansionSets");
import UndoRedoManager = require("./UndoRedo/UndoRedoManager");
import Utils = require("./Utils");
import LayoutProvider = require("./LayoutProvider");
import Menu = require("./Menu");
import PrintSvg = require("./ExportSvgToImage");

export class GraphDataForD3<N extends BaseNode, L extends BaseLink<any>> {
    public nodes: Array<N> = [];
    public links: Array<L> = [];
}

// The silly extends are to facilitate specialized typing to the inheriting classes.
// In Java, it'd be more like ? extends in the things that need.
// If we don't like it, we need to change the expansionSet references in nodes
// to something like strings, which can be used in a registry. This was what I changed
// from because it was an annoying pattern; annoying generics in class defs are better.
//export class BaseNode<SubN extends BaseNode<any>> implements D3.Layout.GraphNode {
export class BaseNode implements D3.Layout.GraphNode {
    id: number;
    index: number;
    name: string;
    px: number;
    py: number;
    size: number;
    weight: number;
    x: number;
    y: number;
    subindex: number;
    startAngle: number;
    endAngle: number;
    value: number;
    fixed: boolean;
    children: D3.Layout.GraphNode[];
    _children: D3.Layout.GraphNode[];
    parent: D3.Layout.GraphNode;
    depth: number;
    nodeColor: string;
        
    getEntityId(): string{
        return "Error, must override this method.";
    }
    
    // Used to have these as string ids that went to a registry that the caller would have to use
    // to get the axtual expansion set. This was forcing callers to use the type of an object
    // they had to track...while usable, I prefer this directness plus casting when necessary.
    //NB the <any> is actually the Typescript idiom for Java's '?' in generics.
    expansionSetAsMember: ExpansionSets.ExpansionSet<any>;
    expansionSetAsParent: ExpansionSets.ExpansionSet<any>;
    
    getExpansionSet(): ExpansionSets.ExpansionSet<any>{
        return this.expansionSetAsMember;
    }
    

    
}

export class BaseLink<N extends BaseNode> {
    source: N;
    target: N;
}

export interface Graph<N extends BaseNode>  {
   addNodes(newNodes: Array<N>, expansionSet: ExpansionSets.ExpansionSet<N>);
   removeNodes(nodesToRemove: Array<N>);
   containsNode(node: N): boolean;
   getNumberOfPotentialNodesToExpand(incomingNode: N, nodeInteraction: UndoRedoManager.NodeInteraction): number;
    
   getLayoutProvider(): LayoutProvider.ILayoutProvider;
   setLayoutProvider(layoutProvider: LayoutProvider.ILayoutProvider);
   findNodesByName(substring: string): Array<N>;
   addNodeToGraph(newNodeId: any); // could be string, really
}



// Notes on usage with this pattern:
// If I don't extend and implement both GraphView and BaseGraphView, I have to define things I want implemented in the base class,
// and I won't be forced to define things declared in the interface. Using the interface as the
// type later leads to a full contract of behavior; the doubling up of interface and base class
// here is only important for implementations.
// Example of class using these begins like so:
// export class OntologyMappingOverview extends GraphView.BaseGraphView implements GraphView.GraphView { ...
// Also see "Mixins and Multiple Inheritance" here: http://www.sitepen.com/blog/2013/12/31/definitive-guide-to-typescript/
export interface GraphView<N extends BaseNode, L extends BaseLink<BaseNode>> extends BaseGraphView<N, L> {
    visWidth(): number;
    visHeight(): number;
    linkMaxDesiredLength(): number;
    // needs to contain the onTick listener function
    onLayoutTick(): {()} ;
    
    populateNewGraphElements(data: GraphDataForD3<N, L>);
    populateNewGraphEdges(links: Array<L>, temporaryEdges?: boolean);
    populateNewGraphNodes(nodes: Array<N>);
    removeMissingGraphElements(data: GraphDataForD3<N, L>);
    filterGraphOnMappingCounts();
    updateDataForNodesAndLinks(newDataSubset: GraphDataForD3<N, L>);
    createNodePopupTable(nodeSvg, nodeData);
    sortConceptNodesCentralOntologyName(): Array<N>;
}

export class BaseGraphView<N extends BaseNode, L extends BaseLink<BaseNode>> {
// TODO Review this interface. A lot of this should probably be made more
// listener orietented rather than direct call. But the system is shallow now,
// so maybe this is what we want.
        
    public undoRedoBoss: UndoRedoManager.UndoRedoManager;
    
    constructor(
        attachScreenshotButton: boolean
        ){
        this.undoRedoBoss = new UndoRedoManager.UndoRedoManager(false, true);
        
        if(attachScreenshotButton){
            this.attachScreenshotButton();
        }
    }
    
    //var defaultNodeColor = "#496BB0";
    defaultNodeColor = "#000000";
    defaultLinkColor = "#999";
    nodeHighlightColor = "#FC6854";
    
    static nodeSvgClassSansDot = "node";
    static nodeInnerSvgClassSansDot = "inner_node"; // Needed for ontology double-node effect
    static nodeGSvgClassSansDot = "node_g";
    static nodeSubGSvgClassSansDot = "node_sub_g";
    static nodeLabelSvgClassSansDot = "nodetext";
    static linkSvgClassSansDot = "link";
    static linkSubGSvgClassSansDot = "sub_link";
    static linkMarkerSvgClassSansDot = "linkmarker";
    static linkLabelSvgClassSansDot = "linktext";
    static linkClassSelectorPrefix = "link_";
    
    static ontologyNodeSvgClassSansDot = "ontologyNode";
    static ontologyLinkSvgClassSansDot = "ontologyMappingLink"
    
    static conceptNodeSvgClassSansDot = "conceptNode";
    static conceptLinkSvgClassSansDot = "conceptLink"
    
    static hiddenNodeClass: string = "hiddenNode";
    static hiddenNodeLabelClass: string = "hiddenNodeLabel";
    static hiddenLinkBecauseOfHiddenNodeLabelClass = "hiddenBecauseOfNodeLink"
    
    static temporaryEdgeClass: string = "temporaryLink";
    
    static nodeSvgClass = "."+BaseGraphView.nodeSvgClassSansDot;
    static nodeInnerSvgClass = "."+BaseGraphView.nodeInnerSvgClassSansDot;
    static nodeGSvgClass = "."+BaseGraphView.nodeGSvgClassSansDot;
    static nodeLabelSvgClass = "."+BaseGraphView.nodeLabelSvgClassSansDot;
    static linkSvgClass = "."+BaseGraphView.linkSvgClassSansDot;
    static linkMarkerSvgClass = "."+BaseGraphView.linkMarkerSvgClassSansDot;
    static linkLabelSvgClass = "."+BaseGraphView.linkLabelSvgClassSansDot;

    
    alphaCutoff: number = 0.01; // used to stop the layout early in the tick() callback
    forceLayout: D3.Layout.ForceLayout = undefined;
    dragging = false;
    
    visWidth(){ return $("#chart").width(); }
    visHeight(){ return $("#chart").height(); }
    linkMaxDesiredLength(){ return Math.min(this.visWidth(), this.visHeight())/2 - 50; }
    
    resizedWindowLambda  = () => {
        d3.select("#graphRect")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        d3.select("#graphSvg")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        // TODO Layouts not relying on force need additional support here.
         // This might need to call back into an instance method named something like "layoutResized"
        if(this.forceLayout){
            this.forceLayout.size([this.visWidth(), this.visHeight()]).linkDistance(this.linkMaxDesiredLength());
            // If needed, move all the nodes towards the new middle here.
            // this.forceLayout.resume(); // wasn't doing the trick
            this.currentLambda(true); // direct retrigger the current layout.
        }  
    }
    
    lastTimeChange = new Date().getTime();
    stampTimeGraphModified(){
        // Things like temporary edges, etc, indicate that the caller must control this.
        this.lastTimeChange = new Date().getTime();
    }
    
    getTimeStampLastGraphModification(): number {
        return this.lastTimeChange;
    }
    
    updateStartWithoutResume(): void{
        // When start(0 is called, the last thing it does is to call resume(),
        // which calls alpha(.1). I need this to not occur...
        var resume = this.forceLayout.resume;
        this.forceLayout.resume = ()=>{ return this.forceLayout; };
        this.forceLayout.start();
        this.forceLayout.resume = resume;
    }
    
    // These are needed to do a refresh of popups when new data arrives and the user has the popup open
    lastDisplayedTipsy = null;
    lastDisplayedTipsyData = null;
    lastDisplayedTipsySvg = null;

    /**
     * Set this function to whichever function has most recently been
     * used or is about to be used. Allows refreshing or resetting of layouts.
     */
    runCurrentLayout: LayoutProvider.LayoutRunner;
    currentLambda: LayoutProvider.LayoutRunner;
    
    layoutTimer = null;
    setCurrentLayout(layoutLambda: {(refreshLayout?: boolean):void}) {
        // This timer delay plus time stamp system cut from 56 calls down to 6 calls in a 5 node 6 arc graph load.
        var outerLayoutTimer = this.layoutTimer;
        var outerThis = this;
        var layoutLastCalled = null;
        var timerWait = 100;
        this.currentLambda = layoutLambda;
        this.runCurrentLayout =
            function(refreshLayoutInner?: boolean){
                // We only allow one layout request to run at a time, and with
                // a short delay between requests. Ok, it's always single threaded,
                // but the point is to avoid hitting a layout because we added one
                // node or edge, only to hit it again milliseconds later. Using the
                // timer lets the next few edges or nodes to come in before making
                // the call, thus thinning out layour refreshes.
                if(outerLayoutTimer == null && (layoutLastCalled == null || outerThis.getTimeStampLastGraphModification() > layoutLastCalled)){
                    outerLayoutTimer = setTimeout(
                        function() {
                            // console.log("calling");
                            clearTimeout(outerLayoutTimer);
                            outerLayoutTimer = null;
                            layoutLastCalled = new Date().getTime();
                            outerThis.currentLambda(refreshLayoutInner);
                        }
                    , timerWait);
                }
            };
    }
//    
//    immediateLayoutRun(layoutLambda: {(refreshLayout?: boolean):void}){
//        layoutLambda();
//    }
    
    getAdjacentLinks(node: N){
        return d3.selectAll(BaseGraphView.linkSvgClass)
        .filter(
            function(d: L, i) {
                    return d.source === node || d.target === node;
            }
        );
    }
    
    getNodeElement(node: N): JQuery {
        // TODO Refactor these #node_g_ constants! There's an issue for this.
        return $("#node_g_"+Utils.escapeIdentifierForId(node.getEntityId()));
    }
    
    isNodeHidden(node: N): boolean {
        // TODO Refactor these #node_g_ constants! There's an issue for this.
        var element = d3.select("#node_g_"+Utils.escapeIdentifierForId(node.getEntityId()));
        if(null == element[0][0]){
            // Already deleted
            return true;
        } else if (element.classed(BaseGraphView.hiddenNodeClass)){
            // hidden...but...is this the old way of deleting, before I changed it to remove
            // nodes entirely from the graph?
            return true;
        } else {
            return false;
        }
    }
    
    getUnhiddenNodes(): JQuery{
        return $(".node_g:not(.hiddenNode)");
    }
    
    highlightHoveredLinkLambda(outerThis: BaseGraphView<N, L>){
        return function(linkLine: L, i){
            if(outerThis.dragging){
                return;
            }
                      
            d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                .classed("highlightedNodeLabel", true)
                .filter(function(aText: N, i){return (null !== linkLine.source && null !== linkLine.target)
                    && ( aText.getEntityId() === linkLine.source.getEntityId() || aText.getEntityId() === linkLine.target.getEntityId() );
                })
                .classed("dimmedNodeLabel", false)
                .classed("highlightedNodeLabel", true)
                ;
            
            // TODO change the getEntityId to accessing the source and target uri,
            // because sometimes links have null on either point due to race conditions,
            // when the user moves off of a node onto a temporary arc, just as it is to
            // be removed.
            d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                .classed("highlightedNode", true)
                .filter(function(aNode: N, i){return (null !== linkLine.source && null !== linkLine.target)
                    && ( aNode.getEntityId() === linkLine.source.getEntityId() || aNode.getEntityId() === linkLine.target.getEntityId() );
                })
                .classed("dimmedNode", false)
                .classed("highlightedNode", true)
                ;
                            
            d3.selectAll(BaseGraphView.linkSvgClass)
                .classed("dimmedLink", true)
                ;
            
            // if we ever use this method attached to anything other than a link hover over, it won't
            // work, because the "this" reference below won't be a line rendered, but whatever we
            // attached the method to.
            // d3.select(this)
            // Defensively, I changed it to grab the correct link via d3.select().filter() instead.
            d3.selectAll(BaseGraphView.linkSvgClass)
                .filter(function(d: L, i){ return d === linkLine; })
                .classed("dimmedLink", false)
                .classed("highlightedLink", true)
                ;
        }
    }
    
    highlightHoveredNodeLambda(outerThis: BaseGraphView<N, L>, highlightAdjacentNodes: boolean){
        return function(nodeData: N, i){
            if(outerThis.dragging){
                return;
            }            
            outerThis.beforeNodeHighlight(nodeData);
            
            // In a previous pass, we may have highlighted a link. Don't clobber it!
            d3.selectAll(BaseGraphView.linkSvgClass)
            .classed("dimmedLink", function(aLink: L, i){
                    return !d3.select(this).classed("highlightedLink");
                }
            );
            
            d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
            .classed("dimmedNode", function(aNode: N, i){
                    return !d3.select(this).classed("highlightedNode");
                });
                
            d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                .classed("dimmedNodeLabel", function(aNode: N, i){
                    return !d3.select(this).classed("highlightedNodeLabel");
                })
                // But highlight the central label
                .filter(function(aText: N, i){return aText.getEntityId() === nodeData.getEntityId();})
                .classed("dimmedNodeLabel", false)
                .classed("highlightedNodeLabel", true);
            
            // D3 doesn't have a way to get from bound data to what it is bound to?
            // Doing it thsi way isntead of d3.select(this) so I can re-use this method with things like
            // checkboxes outside the graph, which will trigger graph behaviors.
            var sourceNode: D3.Selection = d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                .filter(function(d: N, i){return d === nodeData; });
            
            sourceNode
             .classed("highlightedNode", true)
             .classed("dimmedNode", false);
            
            // Get the hovered node to the top of the SVG render stack.
            // This is important for node menu rendering, so that it will inherit
            // the z-order of the node, and be above other nodes rather than beneath.
            d3.select("#node_container").selectAll('.node_g')
                .sort(function(a: N, b: N) {
                    if (a.getEntityId() === nodeData.getEntityId()){ //sourceNode.attr("id")) {
                        return 1;
                    } else {
                        if (b.getEntityId() === nodeData.getEntityId()){ //sourceNode.attr("id")) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
            );
            
            // There must be a less loopy, data oriented way to achieve this.
            // I recently modified it to *not* use x and y coordinates to identify ndoes and edges, which was heinous.
            // Looping over everything is just as ugly (but fast enough in practice).
            var adjacentLinks = outerThis.getAdjacentLinks(nodeData);
                
            if(highlightAdjacentNodes){
                adjacentLinks.each(function(aLink: L){
                    d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                    .filter(function(otherNode: N, i){return aLink.source.getEntityId() === otherNode.getEntityId() || aLink.target.getEntityId() === otherNode.getEntityId();})
                    .classed("dimmedNode", false)
                    .classed("highlightedNode", true)
                    .each(function(aNode: N){
                        d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                        .filter(function(text: N, i){return aNode.getEntityId() === text.getEntityId()})
                        .classed("dimmedNodeLabel", false)
                        .classed("highlightedNodeLabel", true)
                        ;
                    });
                });
            }
            
            // Hide all edges, then show those that have both endpoints shown
            adjacentLinks
                .classed("dimmedLink", false)
                .classed("highlightedLink", function(aLink: L, i){
                        // Would use JQuery, but across diff node types we could have future problems with ID creation.
                        // This is more future proof.
                        var firstEndpoints = d3.selectAll(BaseGraphView.nodeSvgClass)
                            .filter(
                                function(otherNode: N, i){
                                    return aLink.source.getEntityId() === otherNode.getEntityId();
                                }
                            );
                        var secondEndpoint = d3.selectAll(BaseGraphView.nodeSvgClass)
                            .filter(
                                function(otherNode: N, i){
                                    return aLink.target.getEntityId() === otherNode.getEntityId();
                                }
                            );
                        return firstEndpoints.classed("highlightedNode") && secondEndpoint.classed("highlightedNode");
                    }
                );
            
        }
    }
    
    unhighlightHoveredLinkLambda(outerThis: BaseGraphView<N, L>){
        return function(linkData, i){
            outerThis.removeAllNodeHighlighting();
            outerThis.removeAllLinkHighlighting();
        }
    }
    
    unhighlightHoveredNodeLambda(outerThis: BaseGraphView<N, L>, hoverAdjacent: boolean){
        return function(nodeData, i){
            outerThis.removeAllNodeHighlighting();
            outerThis.removeAllLinkHighlighting();
            outerThis.afterNodeUnhighlight(nodeData);
        }
    }
    
    removeAllNodeHighlighting(){
        d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
            .classed("dimmedNode", false)
            .classed("highlightedNode", false)
            ;
        d3.selectAll(BaseGraphView.nodeLabelSvgClass)
            .classed("dimmedNodeLabel", false)
            .classed("highlightedNodeLabel", false)
            ;
    }
    
    removeAllLinkHighlighting() {
        d3.selectAll(BaseGraphView.linkSvgClass)
            .classed("dimmedLink", false)
            .classed("highlightedLink", false)
            ;
    }
    
    hideNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            outerThis.nodeHider(nodeData, true);
        }
    
    }
    
    unhideNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            outerThis.nodeHider(nodeData, false);
        }
    }
    
    toggleHideNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            outerThis.nodeHider(nodeData, undefined);
        }
    }
    
    // Returns "Node" from JQuery/HTML, not a graph node model object
    private findSubNode(nodeData: N): Node{
        var subnode = d3.selectAll(BaseGraphView.nodeGSvgClass)
            .filter(function(d: N, i){ return d === nodeData; })
            .node();
        
        if(subnode === null){
            // When we have deleted nodes (which can be re-done), then hide an expansion
            // set that included some deleted nodes, we need to fall out fo this. There
            // might be other occassions where this happens, and I don't want expansion
            // sets, nor ontology filter systems, to be aware of deletion status, so
            // I will recover from the error here.
            return null;
        }
        
        return subnode;
    }
    
    private nodeHider(nodeData: N, hiding: boolean){
        
        // Hide the node and label away first
        var sourceGNode = this.findSubNode(nodeData);
        
        // When we have deleted nodes (which can be re-done), then hide an expansion
        // set that included some deleted nodes, we need to fall out fo this. There
        // might be other occassions where this happens, and I don't want expansion
        // sets, nor ontology filter systems, to be aware of deletion status, so
        // I will recover from the error here.
        if(null == sourceGNode){
            return;   
        }
        
        if(hiding == null){
            hiding = !(d3.select(sourceGNode).classed(BaseGraphView.hiddenNodeClass));
        }
        
        // In order to hide any baggage (like expander menu indicators), we need to grab the parent
        d3.select(sourceGNode)
            .classed(BaseGraphView.hiddenNodeClass, hiding);
        
        d3.selectAll(BaseGraphView.nodeLabelSvgClass)
            .filter(function(d: N, i){ return d === nodeData;})
            .classed(BaseGraphView.hiddenNodeLabelClass, hiding);
        
        // Hide edges too
        var adjacentLinks = this.getAdjacentLinks(nodeData);
        adjacentLinks
            .classed(BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass,
                function(linkData: L, i){
                    // Look at both endpoints of link, see if both are hidden
                    var source: D3.Selection = d3.selectAll(BaseGraphView.nodeGSvgClass)
                        .filter(function(d: N, i){ return d === linkData.source; });
                    var target: D3.Selection = d3.selectAll(BaseGraphView.nodeGSvgClass)
                        .filter(function(d: N, i){ return d === linkData.target; });
                    // if hiding, we hide the link no matter what
                    // if not hiding, then we pass false if either node is hidden
                    return hiding || source.classed(BaseGraphView.hiddenNodeClass) || target.classed(BaseGraphView.hiddenNodeClass);
                })
        ;
    }
    
    hideLinks(links: D3.Selection){
        this.linkHider(links, true);
    }
    
    unhideLinks(links: D3.Selection){
        this.linkHider(links, false);
    }
    
    private linkHider(links: D3.Selection, hiding: boolean){
        // Different style from the node hider() above, but I don't mind much.
        links.classed("hiddenLink", hiding);
    }
    
    beforeNodeHighlight(targetNodeData){
        // Nothing by default
    }
    
    afterNodeUnhighlight(targetNodeData){
        // Nothing by default
    }
    
    animateHighlightNodesDeactivate(){
        
    }
    
    animateHighlightNodesActivate(matchingNodes: Array<N>){
        var particle = (d: N) => {
            // console.log(d);
            d3.select("#graphSvg").append("circle") //insert("circle", "rect")
              .attr("cx", d.x)
              .attr("cy", d.y)
              .attr("r", 1e-6)
              .style("stroke", d.nodeColor)
              .style("stroke-width", 3)
              .style("stroke-opacity", 1)
              .style("fill-opacity", 0)
            .transition()
              .duration(2000)
              .ease(Math.sqrt)
              .attr("r", 100)
              .style("stroke-opacity", 1e-6)
              .remove()
                ;
        };
        
//        console.log(matchingNodes);
//        
//        console.log(d3.selectAll(".node_g")
//        .filter((d)=>{return matchingNodes.indexOf(d) !== -1; }));
        
        d3.selectAll(".node_g")
        .filter((d)=>{return matchingNodes.indexOf(d) !== -1; })
//        .transition()
//        .duration(2000)
//        .ease(Math.sqrt)
//        .each(pulse);
        .each(particle)
//        .tween("circle", function() {
//            return function(t) {
//              context.strokeStyle = c + (1 - t) + ")";
//              context.beginPath();
//              context.arc(x, y, r * t, 0, 2 * Math.PI);
//              context.stroke();
//            };
//          })
        ;
        
//        function pulse() {
//          var rect = d3.select(this);
//          (function loop() {
//            rect = rect.transition()
//                .duration(750)
//                .style("fill", color(Math.random() * 5 | 0))
//                .each("end", function() { if (this.__transition__.count < 2) loop(); });
//          })();
        
        /////////
        
//        d3.timer(function() {
//  context.clearRect(0, 0, width, height);
//
//  var z = d3.hsl(++i % 360, 1, .5).rgb(),
//      c = "rgba(" + z.r + "," + z.g + "," + z.b + ",",
//      x = x0 += (x1 - x0) * .1,
//      y = y0 += (y1 - y0) * .1;
//
//  d3.select({}).transition()
//      .duration(2000)
//      .ease(Math.sqrt)
//      .tween("circle", function() {
//        return function(t) {
//          context.strokeStyle = c + (1 - t) + ")";
//          context.beginPath();
//          context.arc(x, y, r * t, 0, pithing);
//          context.stroke();
//        };
//      });
//});
        
    }
    
    attachScreenshotButton(){
        var screenshotButton = $("<label>")
            .attr("id", "graphToJpegButton")
            .attr("class", "nodeCommandButton")
            .addClass("unselectable")
            .addClass(Menu.Menu.topBarButtonClass)
            .text("Screenshot")
        ;
        
        $(Menu.Menu.menuBarSelector).append(screenshotButton);
        
        screenshotButton.click((event)=>{ event.stopPropagation(); PrintSvg.ExportSvgToImage.exportSvgAsPng("graphSvg"); });
    }

    attachFullscreenButton(){
        var fullScreenButton = $("<label>")
            .attr("id", "iframeToFullscreenButton")
            .attr("class", "nodeCommandButton")
            .addClass("unselectable")
            .addClass(Menu.Menu.topBarButtonClass)
            .text("Fullscreen")
        ;
        
        $(Menu.Menu.menuBarSelector).append(fullScreenButton);
        
        fullScreenButton.click(
            (event)=>{
                event.stopPropagation();
                // The "*" means I don't care what the origin of the receiving window is. For this request,
                // no data is moving across, so anything works.
                window.top.postMessage("biomixer_full_screen_request", '*')
            }
        );
        
        window.onmessage = (e: any)=>{
            if (e.data === "biomixer_full_screen_request") {
                console.log("Full sreen button pressed, when Biomixer loaded as main frame.");
            }
        };
        
        
        
    }
    
    computeStrokeAndFillLinkEndpoints(sourceX: number, sourceY: number, targetX: number, targetY: number, desiredEdgeThickness: number, extraOffset: number = 0)
        : {sourceX: number; sourceY: number; targetX: number; targetY: number;
           sourceXb: number; sourceYb: number; targetXb: number; targetYb: number;}
    {

        var pointsObj = {sourceX: sourceX, sourceY: sourceY, targetX: targetX, targetY: targetY,
           sourceXb: sourceX, sourceYb: sourceY, targetXb: targetX, targetYb: targetY};
        
        // Get orthogonal vector, by changing x and y and flipping sign on first component (x).
        // We'll want the vector relative to source, then the same repeated for target...but since
        // we know the target orthogonal vector is parallel to the source orthogonal vector, we can
        // infer it.
        // We need it separately for the offset and line thickness, since the offset applies to both legs
        // of the polyline with the same sign, and the thickness applies to both with opposite sign.
        
        
        // Make is_a and has_a arcs move away from each other by enough that we can see them both
        // for when both relations exist ilinkData.source.xn a pair of nodes
        
        // Do it for the offset
        // Kick the special arcs (composition) a couple pixels away
        var xDistOffset = 0;
        var yDistOffset = 0;
        if(extraOffset !== 0){
            // Pretty much same logic as below, but diff variable names.
            // Could route it through itself recursively, but is that easier to maintain??
            var targetVectorXOffset = pointsObj.targetX - pointsObj.sourceX;
            var targetVectorYOffset = pointsObj.targetY - pointsObj.sourceY;
            targetVectorXOffset += (targetVectorXOffset === 0) ? 1 : 0;
            targetVectorYOffset += (targetVectorYOffset === 0) ? 1 : 0;
            var normOffset = Math.sqrt(targetVectorXOffset*targetVectorXOffset + targetVectorYOffset * targetVectorYOffset);
            var targetOrthVectorXOffset = -1 * targetVectorYOffset / normOffset;
            var targetOrthVectorYOffset = targetVectorXOffset / normOffset;
            xDistOffset = extraOffset * targetOrthVectorXOffset;
            yDistOffset = extraOffset * targetOrthVectorYOffset;
        }
    
        // Now do it for the arc thickness
        var halfEdgeThickness = desiredEdgeThickness/2;
        // Now, make the switchbacks, that will make the polyline into a box. This way we can
        // have transparent edges that can be moused over, and opaque centers that can be seen.
        var targetVectorX = pointsObj.targetX - pointsObj.sourceX;
        var targetVectorY = pointsObj.targetY - pointsObj.sourceY;
        targetVectorX += (targetVectorX === 0) ? 1 : 0;
        targetVectorY += (targetVectorY === 0) ? 1 : 0;
        var norm = Math.sqrt(targetVectorX*targetVectorX + targetVectorY * targetVectorY);
        var targetOrthVectorX = -1 * targetVectorY / norm;
        var targetOrthVectorY = targetVectorX / norm;
        var xDist = halfEdgeThickness * targetOrthVectorX;
        var yDist = halfEdgeThickness * targetOrthVectorY;
        
        // Apply to points object. Note signs of these.
        pointsObj.sourceXb += +xDist + xDistOffset;
        pointsObj.sourceYb += +yDist + yDistOffset;
        pointsObj.targetXb += +xDist + xDistOffset;
        pointsObj.targetYb += +yDist + yDistOffset;
        
        pointsObj.sourceX += -xDist + xDistOffset;
        pointsObj.sourceY += -yDist + yDistOffset;
        pointsObj.targetX += -xDist + xDistOffset;
        pointsObj.targetY += -yDist + yDistOffset;
        
        return pointsObj;
    }
        
    computeStrokeAndFillLinkEndpointsString(sourceX: number, sourceY: number, targetX: number, targetY: number, desiredEdgeThickness: number, extraOffset: number = 0)
    : string
    {
        
        var pointsObj = this.computeStrokeAndFillLinkEndpoints(sourceX, sourceY, targetX, targetY, desiredEdgeThickness, extraOffset)
        
        // Create starting point
         var points =
           pointsObj.sourceX+","+pointsObj.sourceY+" "
         + pointsObj.targetX+","+pointsObj.targetY+" "
        ;
        
        // Add the segment for the fill thickness
        points += pointsObj.targetXb+","+pointsObj.targetYb+" "
        
        // Add back in reverse order
        points += pointsObj.targetXb+","+pointsObj.targetYb+" "
          + pointsObj.sourceXb+","+pointsObj.sourceYb+" ";
        
        // Add the other segment for the fill thickness
        points += pointsObj.sourceX+","+pointsObj.sourceY+" ";
        
        return points;
    }
}