///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />
///<amd-dependency path="../GraphView" />

import FilterWidget = require("../NodeFilterWidget");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
import GraphView = require("../GraphView");

/**
 * Vaguely resembles the sibling node filtering classes, with similarly named method names, but the
 * requirements are different enough that it doesn't share specialized behaviors with them.
 */
export class ExpansionSetFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget
    implements FilterWidget.INodeFilterWidget<ConceptGraph.Node, ConceptGraph.Link>
    {
    
    static SUB_MENU_TITLE: string = "Expansion Sets";
    
    expRegistry: GraphView.ExpansionSetRegistry<ConceptGraph.Node>;
    
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    constructor(
        conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot
        ){
        super(ExpansionSetFilter.SUB_MENU_TITLE, graphView, conceptGraph);
        this.implementation = this;
        this.pathToRootView = graphView;
        this.expRegistry = this.graphView.expSetReg;
    }

    generateCheckboxLabel(node: ConceptGraph.Node): string {
        var expSetLabel = node.getExpansionSetId();
        if(expSetLabel == undefined){
            return "undefined";
        }
        return expSetLabel.displayId;
    }
    
    generateColoredSquareIndicator(node: ConceptGraph.Node): string {
        // Node need be nothing.
        // Constant. No color to associate with a set, right?
        return "<span style='font-size: large; color: #223344'>\u25A0</span>";
    }
    
    computeCheckId(node: ConceptGraph.Node): string {
        if(node.getExpansionSetId() == undefined){
            return null;
        }
        return this.getClassName()+"_for_"+node.getExpansionSetId().internalId;
    }
    
    computeCheckboxElementDomain(node: ConceptGraph.Node): Array<ConceptGraph.Node>{
        return this.expRegistry.findExpansionSet(node.getExpansionSetId()).nodes;
    }

    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkboxIsChecked: JQuery): void {
        var outerThis = this;
        var affectedNodes: ConceptGraph.Node[] = [];
        checkboxIsChecked.removeClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS);
        if (checkboxIsChecked.is(':checked')) {
            // Unhide those that are checked, as well as edges with both endpoints visible
            // Also, we will re-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        } else {
            // Hide those that are unchecked, as well as edges with no endpoints visible
            // Also, we will un-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
        }
        outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
    }
    
    /**
     * Synchronize checkboxes with changes made via other checkboxes.
     * Will make the expansion set checkboxes less opaque if any of the individual
     * nodes in the differ in their state from the most recent toggled
     * state of this checkbox. That is, if all were hidden or shown, then one
     * was shown or hidden, the ontology checkbox will be changed visually
     * to indicate inconsistent state. 
     */
    updateCheckboxStateFromView(affectedNodes: ConceptGraph.Node[]){
        var outerThis = this;
        $.each(affectedNodes, function(i, node: ConceptGraph.Node){
                var checkId = outerThis.implementation.computeCheckId(node);
                if(null == checkId){
                    return;
                }
                // Won't uncheck in this case, but instead gets transparent to indicate
                // mixed state
                $("#"+checkId).addClass(ExpansionSetFilter.SOME_SELECTED_CSS);
            }
        );
    }
            
    getHoverNeedsAdjacentHighlighting(): boolean{
        return false;
    }
    
}