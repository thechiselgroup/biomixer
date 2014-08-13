///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="../ExpansionSets" />

import FilterWidget = require("../NodeFilterWidget");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
import GraphView = require("../GraphView");
import ExpansionSets = require("../ExpansionSets");

/**
 * Vaguely resembles the sibling node filtering classes, with similarly named method names, but the
 * requirements are different enough that it doesn't share specialized behaviors with them.
 */
export class ExpansionSetFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget<ExpansionSets.ExpansionSet<ConceptGraph.Node>>
    implements FilterWidget.INodeFilterWidget<ExpansionSets.ExpansionSet<ConceptGraph.Node>, ConceptGraph.Node>
    {
    
    static SUB_MENU_TITLE: string = "Node Expansion Sets Displayed";
        
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    constructor(
        conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot
        ){
        super(ExpansionSetFilter.SUB_MENU_TITLE, graphView, conceptGraph);
        this.implementation = this;
        this.pathToRootView = graphView;
    }

    generateCheckboxLabel(expSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>): string {
        if(expSet == undefined){
            return "undefined";
        }
        return expSet.id.displayId;
    }
    
    generateColoredSquareIndicator(node: ExpansionSets.ExpansionSet<ConceptGraph.Node>): string {
        // Node need be nothing.
        // Constant. No color to associate with a set, right?
        return "<span style='font-size: large; color: #223344'>\u25A0</span>";
    }
    
    computeCheckId(expSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>): string {
        return this.getClassName()+"_for_"+expSet.id.internalId;

    }
    
    computeParentingCheckId(expSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>): string {
        return this.getClassName()+"_for_"+expSet.id.internalId;
    }
    
    /**
     * So except for parents, nodes will always be hidden in the deletion-ignoring way described above.
     * If a node comes back in via a different expansion, the original expansion will co-own the node, and
     * they get to fight about whether it is visible or not.
     */
    computeCheckboxElementDomain(expSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>): Array<ConceptGraph.Node>{
        var setNodes = expSet.nodes;
        // We filter out any nodes that are (currently) deleted from the graph.
        // We always need to hold on to nodes that are deleted, since they could be re-added
        // via an undo.
        // TODO A gotchya here is that if a node is added via a different expansion operation,
        // then this expansion set things it has the node, while the new expansion set thinks it
        // has the node (and should) and the node will think it belongs to the new expansion set.
        // Making an issue...but not sure when I will deal with it.
        var setNodesInGraph = setNodes.filter(
            (node: ConceptGraph.Node, index: number)=>{
                return this.pathToRootView.conceptGraph.nodeIsInIdMap(node);
            }
        );
        return setNodesInGraph;
    }
    
    getFilterTargets(): Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>{
        // We have multiple command types in the undo stack, but we are picking out the expansion sets only.
        // If all of the elements have been *deleted* (not undone), then we may want to test for any rendered
        // members prior to instantiating the filter checkbox.
        return this.conceptGraph.expMan.getActiveExpansionSets();
    }

    /**
     * The expansion set implementation of this is particularly convoluted since we want parent nodes
     * to stay visible when their own expansion sets are hidden, and we want to hide them if both their
     * parent and child sets are hidden.
     */
    checkboxChanged(checkboxContextData: ExpansionSets.ExpansionSet<ConceptGraph.Node>, setOfHideCandidates: Array<ConceptGraph.Node>, checkboxIsChecked: JQuery): void {
        var outerThis = this;
        var affectedNodes: ConceptGraph.Node[] = [];
        // For this one, we pull a trick: we don't want to hide the parent node when we hide the expansion, but we sure
        // want it to stay around when we show the expansion. It's trickier to keep the parent around
        // when other expansion sets hide it, so we can leave that be.
        
        var expSet = checkboxContextData;
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
                    if(node.expansionSetAsParent !== undefined){
                        // Convoluted.
                        // TODO Re-implement this logic, now invalid. See lower down as well.
//                        var anotherCheckbox = outerThis.computeParentingCheckId(node);
//                        if($("#"+anotherCheckbox).is(":checked")){
//                            // If the node is a parent of a *visible* set, do not hide it with the rest of
//                            // its expansion set.
//                            safeToHide = false;
//                        }
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
                // TODO Re-implement this logic, now invalid. See higher up as well.
//                var anotherCheckbox = outerThis.computeCheckId(parentNode); // Yes, the normal checkbox id of the parent node
//                if(!$("#"+anotherCheckbox).is(":checked")){
//                    // If the parent node is a member of a *hidden* set, we will hide it along with its child set.
//                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(parentNode, 0);
//                    affectedNodes.push(parentNode);
//                }
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
        $.each(this.getFilterTargets(), function(i, expSet){
            $.each(affectedNodes,
                function(i, node: ConceptGraph.Node){
                    if(expSet.nodes.indexOf(node) !== -1){
                        var checkId = outerThis.implementation.computeCheckId(expSet);
                        if(null == checkId){
                            return;
                        }
                        // Won't uncheck in this case, but instead gets transparent to indicate
                        // mixed state
                        $("#"+checkId).addClass(ExpansionSetFilter.SOME_SELECTED_CSS);
                    }
                }
            );
        });
    }
            
    getHoverNeedsAdjacentHighlighting(): boolean{
        return false;
    }
    
}