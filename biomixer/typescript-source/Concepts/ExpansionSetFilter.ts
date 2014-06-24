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
    
    computeParentingCheckId(node: ConceptGraph.Node): string {
        if(null == node){
            return null;
        }
        if(node.expansionSetIdentifierAsMemberAsParent == undefined){
            return null;
        }
        return this.getClassName()+"_for_"+node.expansionSetIdentifierAsMemberAsParent.internalId;
    }
    
    computeCheckboxElementDomain(node: ConceptGraph.Node): Array<ConceptGraph.Node>{
        return this.expRegistry.findExpansionSet(node.getExpansionSetId()).nodes;
    }

    /**
     * The expansion set implementation of this is particularly convoluted since we want parent nodes
     * to stay visible when their own expansion sets are hidden, and we want to hide them if both their
     * parent and child sets are hidden.
     */
    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkboxIsChecked: JQuery): void {
        var outerThis = this;
        var affectedNodes: ConceptGraph.Node[] = [];
        // For this one, we pull a trick: we don't want to hide the parent node when we hide the expansion, but we sure
        // want it to stay around when we show the expansion. It's trickier to keep the parent around
        // when other expansion sets hide it, so we can leave that be.
        
        var expSet = this.expRegistry.findExpansionSet(checkboxContextData.getExpansionSetId());
        var parentNode = expSet.parentNode;
        
        checkboxIsChecked.removeClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS);
        if (checkboxIsChecked.is(":checked")) {
            // Unhide those that are checked, as well as edges with both endpoints visible
            // Also, we will re-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                }
            );
            // When unhiding, we want to ensure that the parent node is itself is uhidden.
            if(null != parentNode){
                outerThis.graphView.unhideNodeLambda(outerThis.graphView)(parentNode, 0);
                affectedNodes.push(parentNode);
            }
            
        } else {
            // Hide those that are unchecked, as well as edges with no endpoints visible
            // Also, we will un-check any checkboxes for individual nodes in that ontology.
            $.each(setOfHideCandidates,
                function(i, node: ConceptGraph.Node){
                    // When hiding, we also need to check to see if the nodes we are hiding are parents
                    // of other visible expansion sets. If so, we don't hide them.
                    var safeToHide = true;
                    if(node.expansionSetIdentifierAsMemberAsParent !== undefined){
                        // Convoluted.
                        var anotherCheckbox = outerThis.computeParentingCheckId(node);
                        if($("#"+anotherCheckbox).is(":checked")){
                            // If the node is a parent of a *visible* set, do not hide it with the rest of
                            // its expansion set.
                            safeToHide = false;
                        }
                    }
                    
                    if(safeToHide){
                        outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                        affectedNodes.push(node);
                    }
                }
            );
            // When hiding expansion sets, we have to hide the parent if the expansion set that it
            // belongs to is itself hidden (it was kept visible due to being a parent of this current
            // expansion set).
            // Convoluted.
            if(null !== parentNode){
                var anotherCheckbox = outerThis.computeCheckId(parentNode); // Yes, the normal checkbox id of the parent node
                if(!$("#"+anotherCheckbox).is(":checked")){
                    // If the parent node is a member of a *hidden* set, we will hide it along with its child set.
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(parentNode, 0);
                    affectedNodes.push(parentNode);
                }
            }
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