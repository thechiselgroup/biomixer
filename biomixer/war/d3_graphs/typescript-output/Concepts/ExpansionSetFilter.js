///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="../ExpansionSets" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./ConceptPathsToRoot", "./ConceptGraph", "../GraphView", "../ExpansionSets"], function (require, exports, FilterWidget, ConceptFilterWidget) {
    /**
     * Vaguely resembles the sibling node filtering classes, with similarly named method names, but the
     * requirements are different enough that it doesn't share specialized behaviors with them.
     */
    var ExpansionSetFilter = (function (_super) {
        __extends(ExpansionSetFilter, _super);
        function ExpansionSetFilter(conceptGraph, graphView) {
            _super.call(this, ExpansionSetFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.implementation = this;
            this.pathToRootView = graphView;
        }
        ExpansionSetFilter.prototype.generateCheckboxLabel = function (expSet) {
            if (expSet == undefined) {
                return "undefined";
            }
            return expSet.getFullDisplayId();
        };
        ExpansionSetFilter.prototype.generateColoredSquareIndicator = function (node) {
            // Node need be nothing.
            // Constant. No color to associate with a set, right?
            return "<span style='font-size: large; color: #223344'>\u25A0</span>";
        };
        ExpansionSetFilter.prototype.computeCheckId = function (expSet) {
            return this.getClassName() + "_for_" + expSet.id.internalId;
        };
        /**
         * So except for parents, nodes will always be hidden in the deletion-ignoring way described above.
         * If a node comes back in via a different expansion, the original expansion will co-own the node, and
         * they get to fight about whether it is visible or not.
         */
        ExpansionSetFilter.prototype.computeCheckboxElementDomain = function (expSet) {
            var _this = this;
            var setNodes = expSet.nodes;
            // We filter out any nodes that are (currently) deleted from the graph.
            // We always need to hold on to nodes that are deleted, since they could be re-added
            // via an undo.
            // TODO A gotchya here is that if a node is added via a different expansion operation,
            // then this expansion set things it has the node, while the new expansion set thinks it
            // has the node (and should) and the node will think it belongs to the new expansion set.
            // Making an issue...but not sure when I will deal with it.
            var setNodesInGraph = setNodes.filter(function (node, index) {
                return _this.pathToRootView.conceptGraph.nodeIsInIdMap(node);
            });
            return setNodesInGraph;
        };
        ExpansionSetFilter.prototype.getFilterTargets = function () {
            // We have multiple command types in the undo stack, but we are picking out the expansion sets only.
            // If all of the elements have been *deleted* (not undone), then we may want to test for any rendered
            // members prior to instantiating the filter checkbox.
            return this.conceptGraph.expMan.getActiveExpansionSets();
        };
        /**
         * The expansion set implementation of this is particularly convoluted since we want parent nodes
         * to stay visible when their own expansion sets are hidden, and we want to hide them if both their
         * parent and child sets are hidden.
         */
        ExpansionSetFilter.prototype.checkboxChanged = function (checkboxContextData, setOfHideCandidates, checkboxIsChecked) {
            var outerThis = this;
            var affectedNodes = [];
            // For this one, we pull a trick: we don't want to hide the parent node when we hide the expansion, but we sure
            // want it to stay around when we show the expansion. It's trickier to keep the parent around
            // when other expansion sets hide it, so we can leave that be.
            // Also, when the checkbox is grey due to expansion parents being kept, clicking it will
            // turn it to be checked, rather than unchecked.
            var expSet = checkboxContextData;
            var parentNode = expSet.parentNode;
            if (checkboxIsChecked.is(":checked") || checkboxIsChecked.hasClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS)) {
                checkboxIsChecked.removeClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS);
                // Unhide those that are checked, as well as edges with both endpoints visible
                // Also, we will re-check any checkboxes for individual nodes in that ontology.
                $.each(setOfHideCandidates, function (i, node) {
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
                // When unhiding, we want to ensure that the parent node is itself is uhidden.
                if (null != parentNode) {
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(parentNode, 0);
                    affectedNodes.push(parentNode);
                }
            }
            else {
                checkboxIsChecked.removeClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS);
                // Hide those that are unchecked, as well as edges with no endpoints visible
                // Also, we will un-check any checkboxes for individual nodes in that ontology.
                $.each(setOfHideCandidates, function (i, node) {
                    // We don't hide child nodes that happen to be parent nodes of other visible sets.
                    var safeToHide = true;
                    // If the child node is the parent of a *visible* set, do not hide it
                    var parentExpSets = outerThis.conceptGraph.expMan.getExpansionSetsThatNodeIsParentOf(node);
                    for (var es = 0; es < parentExpSets.length; es++) {
                        var parentSet = parentExpSets[es];
                        var anotherCheckbox = outerThis.computeCheckId(parentSet);
                        if ($("#" + anotherCheckbox).is(":checked")) {
                            safeToHide = false;
                        }
                    }
                    if (safeToHide) {
                        outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                        affectedNodes.push(node);
                    }
                });
                if (null !== parentNode) {
                    // Parent nodes are only hidden if all of their owned sets are hidden (they are not children
                    // nor parents of any other set).
                    var safeToHide = true;
                    // If the parent node is the child of a *visible* set, do not hide it.
                    var childExpSets = outerThis.conceptGraph.expMan.getExpansionSetsThatNodeIsChildOf(parentNode);
                    for (var childIndex = 0; childIndex < childExpSets.length; childIndex++) {
                        var childSet = childExpSets[childIndex];
                        var anotherCheckbox = outerThis.computeCheckId(childSet);
                        if ($("#" + anotherCheckbox).is(":checked")) {
                            safeToHide = false;
                        }
                    }
                    // If the parent node is a parent of a different *visible* set, do not hide it.
                    var parentExpSets = outerThis.conceptGraph.expMan.getExpansionSetsThatNodeIsParentOf(parentNode);
                    for (var parentIndex = 0; parentIndex < parentExpSets.length; parentIndex++) {
                        var parentSet = parentExpSets[parentIndex];
                        var anotherCheckbox = outerThis.computeCheckId(parentSet);
                        if ($("#" + anotherCheckbox).is(":checked")) {
                            safeToHide = false;
                        }
                    }
                    if (safeToHide) {
                        outerThis.graphView.hideNodeLambda(outerThis.graphView)(parentNode, 0);
                        affectedNodes.push(parentNode);
                    }
                }
            }
            outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
            return affectedNodes;
        };
        /**
         * Synchronize checkboxes with changes made via other checkboxes.
         * Will make the ontology checkboxes less opaque if any of the individual
         * nodes in the ontology differ in their state from the most recent toggled
         * state of this checkbox. That is, if all were hidden or shown, then one
         * was shown or hidden, the ontology checkbox will be changed visually
         * to indicate inconsistent state.
         */
        ExpansionSetFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            var outerThis = this;
            // Let's make the greyed-out checkbox go back to normal if all nodes of an ontology are
            // now visible. We need to track the ontologies affected by the affected nodes, then check
            // this for each of them.
            var touchedExpansionSets = {};
            $.each(affectedNodes, function (i, node) {
                var nodeExpansionSet = node.getExpansionSet();
                var checkId = outerThis.implementation.computeCheckId(nodeExpansionSet);
                if (null == checkId) {
                    return;
                }
                touchedExpansionSets[String(nodeExpansionSet.id)] = nodeExpansionSet;
            });
            for (var nodeExpansionSetId in touchedExpansionSets) {
                var nodeExpansionSet = touchedExpansionSets[nodeExpansionSetId];
                var conceptsOfSet = this.computeCheckboxElementDomain(nodeExpansionSet);
                var currentTotal = 0;
                var currentVisible = 0;
                $.each(conceptsOfSet, function (i, node) {
                    if (outerThis.graphView.conceptGraph.containsNode(node)) {
                        currentTotal++;
                    }
                    if (!outerThis.graphView.isNodeHidden(node)) {
                        currentVisible++;
                    }
                });
                var checkId = outerThis.implementation.computeCheckId(nodeExpansionSet);
                if (currentTotal === currentVisible) {
                    $("#" + checkId).removeClass(ExpansionSetFilter.SOME_SELECTED_CSS);
                    $("#" + checkId).prop("checked", true);
                }
                else if (currentVisible === 0) {
                    $("#" + checkId).removeClass(ExpansionSetFilter.SOME_SELECTED_CSS);
                    // Also, uncheck it; the entire ontology has been hidden.
                    $("#" + checkId).prop("checked", false);
                }
                else {
                    $("#" + checkId).addClass(ExpansionSetFilter.SOME_SELECTED_CSS);
                    $("#" + checkId).prop("checked", true);
                }
            }
        };
        ExpansionSetFilter.prototype.getHoverNeedsAdjacentHighlighting = function () {
            return false;
        };
        ExpansionSetFilter.SUB_MENU_TITLE = "Node Expansion Sets Displayed";
        return ExpansionSetFilter;
    })(ConceptFilterWidget.AbstractConceptNodeFilterWidget);
    exports.ExpansionSetFilter = ExpansionSetFilter;
});
