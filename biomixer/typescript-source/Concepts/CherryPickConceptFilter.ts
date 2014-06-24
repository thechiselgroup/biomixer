///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />

import FilterWidget = require("../NodeFilterWidget");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
    
export class CherryPickConceptFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> {
    
    static SUB_MENU_TITLE = "Concepts Rendered";
    
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    constructor(
        conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot,
        private centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(CherryPickConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
        this.implementation = this;
        this.pathToRootView = graphView;
    }
    
    generateCheckboxLabel(node: ConceptGraph.Node): string {
        return node.name+" ("+node.ontologyAcronym+")";
    }
    
    generateColoredSquareIndicator(node: ConceptGraph.Node): string{
        return "<span style='font-size: large; color: "+node.nodeColor+"'>\u25A0</span>";
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
        this.pathToRootView.refreshOtherFilterCheckboxStates([checkboxContextData], this);
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
    
    getHoverNeedsAdjacentHighlighting(): boolean{
        return true;
    }
    
}