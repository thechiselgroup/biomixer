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
    
export class CherryPickConceptFilter extends FilterWidget.AbstractNodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> {
    
    static SUB_MENU_TITLE = "Concepts Rendered";
    
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    constructor(
        private conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot,
        private centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(CherryPickConceptFilter.SUB_MENU_TITLE, graphView);
        this.implementation = this;
        this.pathToRootView = graphView;
    }
    
    generateCheckboxLabel(node: ConceptGraph.Node): string {
        return node.name+" ("+node.ontologyAcronym+")";
    }
    
    generateColoredSquareIndicator(node: ConceptGraph.Node): string{
        return "<span style='font-size: large; color: "+node.nodeColor+"'>\u25A0</span>";
    }
    
    styleAsCentralNode(node: ConceptGraph.Node): boolean {
        return node.rawConceptUri === this.conceptGraph.centralConceptUri;
    }
    
    computeCheckId(node: ConceptGraph.Node): string {
        return this.getClassName()+"_for_"+String(node.conceptUriForIds)
    }
    
    computeCheckboxElementDomain(node: ConceptGraph.Node): Array<ConceptGraph.Node>{
        return [node];
    }
    
    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery){
        // The checkbox domain here is known to be the single node associated with the checkbox.
        if (checkbox.is(':checked')) {
            // Unhide those that are checked, as well as edges with both endpoints visible
            this.graphView.unhideNodeLambda(this.graphView)(checkboxContextData, 0);
        } else {
            // Hide those that are unchecked, as well as edges with no endpoints visible
            this.graphView.hideNodeLambda(this.graphView)(checkboxContextData, 0);
        }
        // The ontology checkboxes need to be updated based on changes in visibility 
        this.pathToRootView.refreshOntologyCheckboxState([checkboxContextData]);
    }
    
    checkboxHoveredLambda(setOfHideCandidates: Array<ConceptGraph.Node>): (event: JQueryMouseEventObject)=>void{
        var outerThis = this;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse enter behavior.
            outerThis.graphView.highlightHoveredNodeLambda(outerThis.graphView)(setOfHideCandidates[0], 0);
        }
    }
    
    checkboxUnhoveredLambda(setOfHideCandidates: Array<ConceptGraph.Node>): (event: JQueryMouseEventObject)=>void{
        var outerThis = this;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse leave behavior.
            outerThis.graphView.unhighlightHoveredNodeLambda(outerThis.graphView)(setOfHideCandidates[0], 0);
        };
    }
    
    /**
     * Synchronize checkboxes with changes made via other checkboxes.
     */
    updateCheckboxStateFromView(affectedNodes: ConceptGraph.Node[]){
        var outerThis = this;
        $.each(affectedNodes, function(i, node: ConceptGraph.Node){
                var checkId = outerThis.implementation.computeCheckId(node);
                $("#"+checkId).prop("checked", !outerThis.graphView.isNodeHidden(node));
            }
        );
    }
    
}