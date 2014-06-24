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

export class OntologyConceptFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link> {
    
    static SUB_MENU_TITLE = "Ontologies Rendered";
    
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    constructor(
        conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(OntologyConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
        this.implementation = this;
        this.pathToRootView = graphView;
    }
    
    generateCheckboxLabel(node: ConceptGraph.Node): string {
        return String(node.ontologyAcronym);
    }
    
    generateColoredSquareIndicator(node: ConceptGraph.Node): string {
        return "<span style='font-size: large; color: "+node.nodeColor+";'>\u25A0</span>";
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
        var affectedNodes: ConceptGraph.Node[] = [];
        checkbox.removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
        if (checkbox.is(':checked')) {
            // Unhide those that are checked, as well as edges with both endpoints visible
            // Also, we will re-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== checkboxContextData.ontologyAcronym){
                        return;
                    }
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        } else {
            // Hide those that are unchecked, as well as edges with no endpoints visible
            // Also, we will un-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== checkboxContextData.ontologyAcronym){
                        return;
                    }
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        }
        outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
    }
    
    /**
     * Synchronize checkboxes with changes made via other checkboxes.
     * Will make the ontology checkboxes less opaque if any of the individual
     * nodes in the ontology differ in their state from the most recent toggled
     * state of this checkbox. That is, if all were hidden or shown, then one
     * was shown or hidden, the ontology checkbox will be changed visually
     * to indicate inconsistent state. 
     */
    updateCheckboxStateFromView(affectedNodes: ConceptGraph.Node[]){
        var outerThis = this;
        $.each(affectedNodes, function(i, node: ConceptGraph.Node){
                var checkId = outerThis.implementation.computeCheckId(node);
                // Won't uncheck in this case, but instead gets transparent to indicate
                // mixed state
                $("#"+checkId).addClass(OntologyConceptFilter.SOME_SELECTED_CSS);
            }
        );
    }
    
        
    getHoverNeedsAdjacentHighlighting(): boolean{
        return false;
    }
    
}