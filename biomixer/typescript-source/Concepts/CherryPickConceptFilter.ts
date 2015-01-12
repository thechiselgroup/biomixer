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
    
// The generics look odd, but the node is both the FilterTarget for the filter widget, as well as the node class.
export class CherryPickConceptFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget<ConceptGraph.Node> implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Node> {
    
    static SUB_MENU_TITLE = "Concepts Displayed";
    
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
    
    getFilterTargets(): Array<ConceptGraph.Node>{
        return this.graphView.sortConceptNodesCentralOntologyName();
    }
    
    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery): Array<ConceptGraph.Node>{
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
        
        return [checkboxContextData];
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