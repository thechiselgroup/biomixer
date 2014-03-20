///<reference path="headers/require.d.ts" />

///<amd-dependency path="OntologyGraph" />


import Graph = require('./OntologyGraph');

// TODO Get the GraphDataForD3, Node, and Link root classes in here, I think.

export class BaseGraphView {
// TODO Review this interface. A lot fo this should probably be made more
// listener orietented rather than direct call. But the system is shallow now,
// so maybe this is what we want.
    
    
    visWidth(){ return $("#chart").width(); }
    visHeight(){ return $("#chart").height(); }
    linkMaxDesiredLength(){ return Math.min(this.visWidth(), this.visHeight())/2 - 50; }
    
    menuSelector: string = 'div#hoveringGraphMenu';
    closeMenu(){return function(){ $(this.menuSelector).hide()};}
    
    // These are needed to do a refresh of popups when new data arrives and the user has the popup open
    lastDisplayedTipsy = null;
    lastDisplayedTipsyData = null;
    lastDisplayedTipsyCircle = null;

    
    /**
     * Set this function to whichever function has most recently been
     * used or is about to be used. Allows refreshing or resetting of layouts.
     */
    runCurrentLayout: {()};
}

export interface GraphView extends BaseGraphView {
    removeGraphPopulation();
    visWidth(): number;
    visHeight(): number;
    linkMaxDesiredLength(): number;
    
    populateGraph(data: Graph.GraphDataForD3, expectingNew: boolean);
    filterGraphOnMappingCounts();
    updateDataForNodesAndLinks(newDataSubset: Graph.GraphDataForD3);
    
    closeMenu();
}