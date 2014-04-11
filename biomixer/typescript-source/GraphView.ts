///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

export class GraphDataForD3<N extends BaseNode, L extends BaseLink> {
    public nodes: Array<N> = [];
    public links: Array<L> = [];
}

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
}

export class BaseLink {
    source: D3.Layout.GraphNode;
    target: D3.Layout.GraphNode;
}

export interface Graph {
    
}

// Notes on usage with this pattern:
// If I don't extend and implement both, I have to define things I want implemented in the base class,
// and I won't be forced to define things declared in the interface. Using the interface as the
// type later leads to a full contract of behavior; the doubling up of interface and base class
// here is only important for implementations.
// Example of class using these begins like so:
// export class OntologyMappingOverview extends GraphView.BaseGraphView implements GraphView.GraphView { ...
// Also see "Mixins and Multiple Inheritance" here: http://www.sitepen.com/blog/2013/12/31/definitive-guide-to-typescript/
export class BaseGraphView {
// TODO Review this interface. A lot fo this should probably be made more
// listener orietented rather than direct call. But the system is shallow now,
// so maybe this is what we want.
    
    //var defaultNodeColor = "#496BB0";
    defaultNodeColor = "#000000";
    defaultLinkColor = "#999";
    nodeHighlightColor = "#FC6854";
    
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
    
    updateStartWithoutResume(){
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
    runCurrentLayout: {()}; // ()=>void; is newer syntax?
}

export interface GraphView<N extends BaseNode, L extends BaseLink> extends BaseGraphView {
    visWidth(): number;
    visHeight(): number;
    linkMaxDesiredLength(): number;
    // needs to contain the onTick listener function
    onLayoutTick(): {()} ;
    
    populateNewGraphElements(data: GraphDataForD3<N, L>, expectingNew: boolean);
    populateNewGraphEdges(links: Array<L>);
    populateNewGraphNodes(nodes: Array<N>);
    removeMissingGraphElements(data: GraphDataForD3<N, L>);
    filterGraphOnMappingCounts();
    updateDataForNodesAndLinks(newDataSubset: GraphDataForD3<N, L>);
    createNodePopupTable(nodeSvg, nodeData);
}