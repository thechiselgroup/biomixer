///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="../Menu" />
///<amd-dependency path="Concepts/ConceptGraph" />

import FilterWidget = require("../NodeFilterWidget");
import Menu = require("../Menu");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
    
export class OntologyConceptFilter extends FilterWidget.AbstractNodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> {
    
    static SUB_MENU_TITLE = "Ontologies Rendered";
    
    constructor(
        public conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(OntologyConceptFilter.SUB_MENU_TITLE, graphView);
        this.implementation = this;
    }
    
    generateCheckboxLabel(node: ConceptGraph.Node): string {
        return String(node.ontologyAcronym);
    }
    
    generateColoredSquareIndicator(node: ConceptGraph.Node): string {
        return "<span style='font-size: large; color: "+node.nodeColor+";'>\u25A0</span>";
    }
    
    styleAsCentralNode(node: ConceptGraph.Node): boolean {
        return node.rawConceptUri === this.conceptGraph.centralConceptUri;
    }
    
    computeCheckId(node: ConceptGraph.Node): string {
        return this.getClassName()+"_for_"+String(node.ontologyAcronym);
    }
    
    computeCheckboxElementDomain(node: ConceptGraph.Node): Array<ConceptGraph.Node> {
        return this.graphView.sortConceptNodesCentralOntologyName()
            .filter(
                function(d: ConceptGraph.Node, i: number){
                    return d.ontologyAcronym === node.ontologyAcronym;
                }
            );
    }
    
    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery){
        var outerThis = this;
        if (checkbox.is(':checked')) {
            // Unhide those that are checked, as well as edges with both endpoints visible
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== checkboxContextData.ontologyAcronym){
                        return;
                    }
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                }
            );
        } else {
            // Hide those that are unchecked, as well as edges with no endpoints visible
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== checkboxContextData.ontologyAcronym){
                        return;
                    }
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                }
            );
        }
    }
    
    checkboxHoveredLambda(setOfHideCandidates: Array<ConceptGraph.Node>): (event: JQueryMouseEventObject)=>void {
        // TODO Do we want multi-node hover for ontology checkboxes? Maybe?
        var graphView = this.graphView;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse enter behavior.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    graphView.highlightHoveredNodeLambda(graphView)(node, 0);
                }
            );
        }
    }
    
    checkboxUnhoveredLambda(setOfHideCandidates: Array<ConceptGraph.Node>): (event: JQueryMouseEventObject)=>void{
        // TODO Do we want multi-node hover for ontology checkboxes? Maybe?
        var graphView = this.graphView;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse leave behavior.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    graphView.unhighlightHoveredNodeLambda(graphView)(node, 0);
                }
            );
        };
    }
}