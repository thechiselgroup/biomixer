///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />

import FilterWidget = require("../NodeFilterWidget");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");

export class OntologyConceptFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget<ConceptGraph.RawAcronym> implements FilterWidget.INodeFilterWidget<ConceptGraph.RawAcronym, ConceptGraph.Node> {
    
    static SUB_MENU_TITLE = "Ontologies Displayed";
    
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
    
    generateCheckboxLabel(acronym: ConceptGraph.RawAcronym): string {
        return String(acronym);
    }
    
    generateColoredSquareIndicator(acronym: ConceptGraph.RawAcronym): string {
        return "<span style='font-size: large; color: "+this.conceptGraph.nextNodeColor(acronym)+";'>\u25A0</span>";
    }
    
    computeCheckId(acronym: ConceptGraph.RawAcronym): string {
        return this.getClassName()+"_for_"+acronym;
    }
    
    computeCheckboxElementDomain(acronym: ConceptGraph.RawAcronym): Array<ConceptGraph.Node> {
        return this.graphView.sortConceptNodesCentralOntologyName()
            .filter(
                function(d: ConceptGraph.Node, i: number){
                    return d.ontologyAcronym === acronym;
                }
            );
    }
    
    getFilterTargets(): Array<ConceptGraph.RawAcronym>{
        return this.conceptGraph.getOntologiesInGraph();
    }
    
    checkboxChanged(checkboxContextData: ConceptGraph.RawAcronym, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery): Array<ConceptGraph.Node>{
        var outerThis = this;
        var acronym = checkboxContextData;
        var affectedNodes: ConceptGraph.Node[] = [];
        if (checkbox.is(':checked') || checkbox.hasClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS)) {
            checkbox.removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
            // Unhide those that are checked, as well as edges with both endpoints visible
            // Also, we will re-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== acronym){
                        return;
                    }
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        } else {
            checkbox.removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
            // Hide those that are unchecked, as well as edges with no endpoints visible
            // Also, we will un-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    if(node.ontologyAcronym !== acronym){
                        return;
                    }
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        }
        outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
        return affectedNodes;
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
        
        // Let's make the greyed-out checkbox go back to normal if all nodes of an ontology are
        // now visible. We need to track the ontologies affected by the affected nodes, then check
        // this for each of them.
        // I don't currently use the counter, but I need a map anyway, so that will be the value type.
        var ontologyCounts: {[ontologyAcronym: string]: number} = {};
        
        $.each(affectedNodes,
            function(i, node: ConceptGraph.Node){
                var checkId = outerThis.implementation.computeCheckId(node.ontologyAcronym);
                if(null == checkId){
                    return;
                }
                if(undefined == ontologyCounts[String(node.ontologyAcronym)]){
                    ontologyCounts[String(node.ontologyAcronym)] = 0;
                }
                ontologyCounts[String(node.ontologyAcronym)] += 1;
            }
        );
        
        for(var ontologyAcronym in ontologyCounts){
            var conceptsOfOntology = this.computeCheckboxElementDomain(ontologyAcronym);
            var currentTotal = 0;
            var currentVisible = 0;
            $.each(conceptsOfOntology, (i: number, node: ConceptGraph.Node)=>{
                currentTotal++;
                if(!outerThis.graphView.isNodeHidden(node)){
                    currentVisible++;
                }    
            });
            
            var checkId = outerThis.implementation.computeCheckId(ontologyAcronym);
            if(currentTotal === currentVisible){
                $("#"+checkId).removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                $("#"+checkId).prop("checked", true);
            } else if(currentVisible === 0){
                $("#"+checkId).removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                // Also, uncheck it; the entire ontology has been hidden.
                $("#"+checkId).prop("checked", false);
            } else {
                $("#"+checkId).addClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                $("#"+checkId).prop("checked", true);
            }
        }
    }
    
    getHoverNeedsAdjacentHighlighting(): boolean{
        return false;
    }
    
}