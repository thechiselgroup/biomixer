///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "./ConceptNodeFilterWidget", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./ConceptPathsToRoot", "./ConceptGraph"], function(require, exports, ConceptFilterWidget) {
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
                // Unhide those that are checked, as well as edges with both endpoints visible
                // Also, we will re-check any checkboxes for individual nodes in that ontology.
                $.each(setOfHideCandidates, function (i, node) {
                    if (node.ontologyAcronym !== acronym) {
                        return;
                    }
                    outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
            } else {
                // Hide those that are unchecked, as well as edges with no endpoints visible
                // Also, we will un-check any checkboxes for individual nodes in that ontology.
                $.each(setOfHideCandidates, function (i, node) {
                    if (node.ontologyAcronym !== acronym) {
                        return;
                    }
                    outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0);
                    affectedNodes.push(node);
                });
            }
            outerThis.pathToRootView.refreshOtherFilterCheckboxStates(affectedNodes, this);
        };

        /**
        * Synchronize checkboxes with changes made via other checkboxes.
        * Will make the ontology checkboxes less opaque if any of the individual
        * nodes in the ontology differ in their state from the most recent toggled
        * state of this checkbox. That is, if all were hidden or shown, then one
        * was shown or hidden, the ontology checkbox will be changed visually
        * to indicate inconsistent state.
        */
        OntologyConceptFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            var outerThis = this;
            $.each(affectedNodes, function (i, node) {
                var checkId = outerThis.implementation.computeCheckId(node.ontologyAcronym);
                if (null == checkId) {
                    return;
                }

                // Won't uncheck in this case, but instead gets transparent to indicate
                // mixed state
                $("#" + checkId).addClass(OntologyConceptFilter.SOME_SELECTED_CSS);
            });
        };

        OntologyConceptFilter.prototype.getHoverNeedsAdjacentHighlighting = function () {
            return false;
        };
        OntologyConceptFilter.SUB_MENU_TITLE = "Ontologies Displayed";
        return OntologyConceptFilter;
    })(ConceptFilterWidget.AbstractConceptNodeFilterWidget);
    exports.OntologyConceptFilter = OntologyConceptFilter;
});
