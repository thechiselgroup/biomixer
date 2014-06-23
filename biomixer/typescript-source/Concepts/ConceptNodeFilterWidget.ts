///<reference path="headers/require.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../FilterWidget" />
///<amd-dependency path="../Menu" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="./ConceptGraph" />

import Utils = require("../Utils");
import FilterWidget = require("../NodeFilterWidget");
import Menu = require("../Menu");
import GraphView = require("../GraphView");
import ConceptGraph = require("./ConceptGraph");

/**
 * A more manfiest abstract class, for ConceptNode. I was going to refactor to support only this
 * node type, but decided that a simple abstraact extension and wrapping would serve us better in
 * case we have different node types later.
 */
export class AbstractConceptNodeFilterWidget extends FilterWidget.AbstractNodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> {
    
    constructor(
        subMenuTitle: string,
        public graphView: GraphView.GraphView<ConceptGraph.Node, ConceptGraph.Link>,
        public conceptGraph: ConceptGraph.ConceptGraph
        ){
        super(subMenuTitle, graphView);
    }
    
     styleAsCentralNode(node: ConceptGraph.Node): boolean {
        return node.rawConceptUri === this.conceptGraph.centralConceptUri;
    }
    
    checkboxHoveredLambda(nodeRelatedToCheckbox: ConceptGraph.Node): (event: JQueryMouseEventObject)=>void{
        var graphView = this.graphView;
        var outerThis = this;
        return function(eventObject: JQueryMouseEventObject){
            var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(nodeRelatedToCheckbox);
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse enter behavior.
            $.each(nodeHideCandidates,
                function(i, node: ConceptGraph.Node){
                    graphView.highlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                }
            );
        }
    }
    
    checkboxUnhoveredLambda(nodeRelatedToCheckbox: ConceptGraph.Node): (event: JQueryMouseEventObject)=>void{
        // TODO Do we want multi-node hover for ontology checkboxes? Maybe?
        var graphView = this.graphView;
        var outerThis = this;
        return function(eventObject: JQueryMouseEventObject){
            var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(nodeRelatedToCheckbox);
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse leave behavior.
            $.each(nodeHideCandidates,
                function(i, node: ConceptGraph.Node){
                    graphView.unhighlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                }
            );
        };
    }
}