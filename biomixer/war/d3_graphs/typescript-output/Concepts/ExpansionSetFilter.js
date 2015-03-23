var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./ConceptPathsToRoot", "./ConceptGraph", "../GraphView", "../ExpansionSets"], function (require, exports, FilterWidget, ConceptFilterWidget) {
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
            return "<span style='font-size: large; color: #223344'>\u25A0</span>";
        };
        ExpansionSetFilter.prototype.computeCheckId = function (expSet) {
            return this.getClassName() + "_for_" + expSet.id.internalId;
        };
        ExpansionSetFilter.prototype.computeCheckboxElementDomain = function (expSet) {
            var _this = this;
            var setNodes = expSet.nodes;
            var setNodesInGraph = setNodes.filter(function (node, index) {
                return _this.pathToRootView.conceptGraph.nodeIsInIdMap(node);
            });
            return setNodesInGraph;
        };
        ExpansionSetFilter.prototype.getFilterTargets = function () {
            return this.conceptGraph.expMan.getActiveExpansionSets();
        };
        ExpansionSetFilter.prototype.checkboxChanged = function (checkboxContextData, setOfHideCandidates, checkboxIsChecked) {
            var outerThis = this;
            var affectedNodes = [];
            var expSet = checkboxContextData;
            var parentNode = expSet.parentNode;
            checkboxIsChecked.removeClass(FilterWidget.AbstractNodeFilterWidget.SOME_SELECTED_CSS);
            if (checkboxIsChecked.is(":checked")) {
                $.each(setOfHideCandidates, function (i, node) {
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
                if (null != parentNode) {
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(parentNode, 0);
                    affectedNodes.push(parentNode);
                }
            }
            else {
                $.each(setOfHideCandidates, function (i, node) {
                    var safeToHide = true;
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
                    var safeToHide = true;
                    var childExpSets = outerThis.conceptGraph.expMan.getExpansionSetsThatNodeIsChildOf(parentNode);
                    for (var childIndex = 0; childIndex < childExpSets.length; childIndex++) {
                        var childSet = childExpSets[childIndex];
                        var anotherCheckbox = outerThis.computeCheckId(childSet);
                        if ($("#" + anotherCheckbox).is(":checked")) {
                            safeToHide = false;
                        }
                    }
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
        ExpansionSetFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            var outerThis = this;
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
