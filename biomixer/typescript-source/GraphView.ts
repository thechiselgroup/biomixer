///<reference path="headers/require.d.ts" />

///<amd-dependency path="OntologyGraph" />


import Graph = require('./OntologyGraph');

// TODO Get the GraphDataForD3, Node, and Link root classes in here, I think.

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
    
    
    visWidth(){ return $("#chart").width(); }
    visHeight(){ return $("#chart").height(); }
    linkMaxDesiredLength(){ return Math.min(this.visWidth(), this.visHeight())/2 - 50; }
    closeMenu(){return function(){ $(this.menuSelector).hide()};}
    
    menuSelector: string = 'div#hoveringGraphMenu';
    
    // These are needed to do a refresh of popups when new data arrives and the user has the popup open
    lastDisplayedTipsy = null;
    lastDisplayedTipsyData = null;
    lastDisplayedTipsyCircle = null;

    
    /**
     * Set this function to whichever function has most recently been
     * used or is about to be used. Allows refreshing or resetting of layouts.
     */
    runCurrentLayout: {()}; // ()=>void; is newer syntax?
}

export interface GraphView extends BaseGraphView {
    visWidth(): number;
    visHeight(): number;
    linkMaxDesiredLength(): number;
    closeMenu();
    
    populateGraph(data: Graph.GraphDataForD3, expectingNew: boolean);
    removeGraphPopulation();
    filterGraphOnMappingCounts();
    updateDataForNodesAndLinks(newDataSubset: Graph.GraphDataForD3);
}