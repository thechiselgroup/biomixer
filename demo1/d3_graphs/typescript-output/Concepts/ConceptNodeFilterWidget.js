///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../NodeFilterWidget", "../Utils", "../FilterWidget", "../Menu", "../GraphView", "./ConceptGraph"], function(require, exports, FilterWidget) {
    /**
    * A more manfiest abstract class, for ConceptNode. I was going to refactor to support only this
    * node type, but decided that a simple abstraact extension and wrapping would serve us better in
    * case we have different node types later.
    */
    var AbstractConceptNodeFilterWidget = (function (_super) {
        __extends(AbstractConceptNodeFilterWidget, _super);
        function AbstractConceptNodeFilterWidget(subMenuTitle, graphView, conceptGraph) {
            _super.call(this, subMenuTitle, graphView);
            this.graphView = graphView;
            this.conceptGraph = conceptGraph;
        }
        //     styleAsCentralNode(node: ConceptGraph.Node): boolean {
        //        return node.rawConceptUri === this.conceptGraph.centralConceptUri;
        //    }
        AbstractConceptNodeFilterWidget.prototype.checkboxHoveredLambda = function (filterTargetRelatedToCheckbox) {
            var graphView = this.graphView;
            var outerThis = this;
            return function (eventObject) {
                var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(filterTargetRelatedToCheckbox);

                // Technically, the span over the checkbox is the element
                // Find the graph node that corresponds, and fire its mouse enter behavior.
                $.each(nodeHideCandidates, function (i, node) {
                    graphView.highlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                });
            };
        };

        AbstractConceptNodeFilterWidget.prototype.checkboxUnhoveredLambda = function (filterTargetRelatedToCheckbox) {
            // TODO Do we want multi-node hover for ontology checkboxes? Maybe?
            var graphView = this.graphView;
            var outerThis = this;
            return function (eventObject) {
                var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(filterTargetRelatedToCheckbox);

                // Technically, the span over the checkbox is the element
                // Find the graph node that corresponds, and fire its mouse leave behavior.
                $.each(nodeHideCandidates, function (i, node) {
                    graphView.unhighlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                });
            };
        };
        return AbstractConceptNodeFilterWidget;
    })(FilterWidget.AbstractNodeFilterWidget);
    exports.AbstractConceptNodeFilterWidget = AbstractConceptNodeFilterWidget;
});
