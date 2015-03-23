var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "./ConceptNodeFilterWidget", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./ConceptPathsToRoot", "./ConceptGraph"], function (require, exports, ConceptFilterWidget) {
    var OntologyConceptFilter = (function (_super) {
        __extends(OntologyConceptFilter, _super);
        function OntologyConceptFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, OntologyConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.centralConceptUri = centralConceptUri;
            this.implementation = this;
            this.pathToRootView = graphView;
        }
        OntologyConceptFilter.prototype.generateCheckboxLabel = function (acronym) {
            return String(acronym);
        };
        OntologyConceptFilter.prototype.generateColoredSquareIndicator = function (acronym) {
            return "<span style='font-size: large; color: " + this.conceptGraph.nextNodeColor(acronym) + ";'>\u25A0</span>";
        };
        OntologyConceptFilter.prototype.computeCheckId = function (acronym) {
            return this.getClassName() + "_for_" + acronym;
        };
        OntologyConceptFilter.prototype.computeCheckboxElementDomain = function (acronym) {
            return this.graphView.sortConceptNodesCentralOntologyName().filter(function (d, i) {
                return d.ontologyAcronym === acronym;
            });
        };
        OntologyConceptFilter.prototype.getFilterTargets = function () {
            return this.conceptGraph.getOntologiesInGraph();
        };
        OntologyConceptFilter.prototype.checkboxChanged = function (checkboxContextData, setOfHideCandidates, checkbox) {
            var outerThis = this;
            var acronym = checkboxContextData;
            var affectedNodes = [];
            checkbox.removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
            if (checkbox.is(':checked')) {
                $.each(setOfHideCandidates, function (i, node) {
                    if (node.ontologyAcronym !== acronym) {
                        return;
                    }
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
            }
            else {
                $.each(setOfHideCandidates, function (i, node) {
                    if (node.ontologyAcronym !== acronym) {
                        return;
                    }
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
            }
            outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
            return affectedNodes;
        };
        OntologyConceptFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            var outerThis = this;
            var ontologyCounts = {};
            $.each(affectedNodes, function (i, node) {
                var checkId = outerThis.implementation.computeCheckId(node.ontologyAcronym);
                if (null == checkId) {
                    return;
                }
                if (undefined == ontologyCounts[String(node.ontologyAcronym)]) {
                    ontologyCounts[String(node.ontologyAcronym)] = 0;
                }
                ontologyCounts[String(node.ontologyAcronym)] += 1;
            });
            for (var ontologyAcronym in ontologyCounts) {
                var conceptsOfOntology = this.computeCheckboxElementDomain(ontologyAcronym);
                var currentTotal = 0;
                var currentVisible = 0;
                $.each(conceptsOfOntology, function (i, node) {
                    currentTotal++;
                    if (!outerThis.graphView.isNodeHidden(node)) {
                        currentVisible++;
                    }
                });
                var checkId = outerThis.implementation.computeCheckId(ontologyAcronym);
                if (currentTotal === currentVisible) {
                    $("#" + checkId).removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                    $("#" + checkId).prop("checked", true);
                }
                else if (currentVisible === 0) {
                    $("#" + checkId).removeClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                    $("#" + checkId).prop("checked", false);
                }
                else {
                    $("#" + checkId).addClass(OntologyConceptFilter.SOME_SELECTED_CSS);
                    $("#" + checkId).prop("checked", true);
                }
            }
        };
        OntologyConceptFilter.prototype.getHoverNeedsAdjacentHighlighting = function () {
            return false;
        };
        OntologyConceptFilter.SUB_MENU_TITLE = "Ontologies Displayed";
        return OntologyConceptFilter;
    })(ConceptFilterWidget.AbstractConceptNodeFilterWidget);
    exports.OntologyConceptFilter = OntologyConceptFilter;
});
