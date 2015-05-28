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
    
   getLayoutProvider(): LayoutProvider.ILayoutProvider;
   setLayoutProvider(layoutProvider: LayoutProvider.ILayoutProvider);
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
        ){
        this.undoRedoBoss = new UndoRedoManager.UndoRedoManager(false, true);
    }
    
    //var defaultNodeColor = "#496BB0";
    defaultNodeColor = "#000000";
    defaultLinkColor = "#999";
    nodeHighlightColor = "#FC6854";
    
    static nodeSvgClassSansDot = "node";
    static nodeInnerSvgClassSansDot = "inner_node"; // Needed for ontology double-node effect
    static nodeGSvgClassSansDot = "node_g";
    static nodeLabelSvgClassSansDot = "nodetext";
    static linkSvgClassSansDot = "link";
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
            this.forceLayout.resume();
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
    
    isNodeHidden(node: N): boolean {
        // TODO Refactor these #node_g_ constants! There's an issue for this.
        return d3.select("#node_g_"+Utils.escapeIdentifierForId(node.getEntityId())).classed(BaseGraphView.hiddenNodeClass);
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
                .filter(function(aText: N, i){return aText.getEntityId() ===linkLine.source.getEntityId() || aText.getEntityId() ===linkLine.target.getEntityId();})
                .classed("dimmedNodeLabel", false)
                .classed("highlightedNodeLabel", true)
                ;
            
            d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                .classed("highlightedNode", true)
                .filter(function(aNode: N, i){return aNode.getEntityId() === linkLine.source.getEntityId() || aNode.getEntityId() === linkLine.target.getEntityId();})
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
             .classed("highlightedNode", true);
            
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
    
    redraw() {
       var outerThis = this;
       return function(){
            if(outerThis.dragging){
                return;
            }      
       
            d3.select("#link_container")
                .attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
            d3.select("#node_container")
                .attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
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
    
    private nodeHider(nodeData: N, hiding: boolean){
        // Hide the node and label away first
        var subnode = d3.selectAll(BaseGraphView.nodeSvgClass)
            .filter(function(d: N, i){ return d === nodeData; })
            .node()
        
        if(subnode === null){
            // When we have deleted nodes (which can be re-done), then hide an expansion
            // set that included some deleted nodes, we need to fall out fo this. There
            // might be other occassions where this happens, and I don't want expansion
            // sets, nor ontology filter systems, to be aware of deletion status, so
            // I will recover from the error here.
            return;
        }
        
        var sourceGNode = subnode.parentNode;
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
    
}