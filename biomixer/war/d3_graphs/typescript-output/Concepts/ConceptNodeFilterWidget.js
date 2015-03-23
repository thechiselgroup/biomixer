var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../NodeFilterWidget", "../Utils", "../FilterWidget", "../Menu", "../GraphView", "./ConceptPathsToRoot", "./ConceptGraph"], function (require, exports, FilterWidget) {
    var AbstractConceptNodeFilterWidget = (function (_super) {
        __extends(AbstractConceptNodeFilterWidget, _super);
        function AbstractConceptNodeFilterWidget(subMenuTitle, graphView, conceptGraph) {
            _super.call(this, subMenuTitle, graphView);
            this.graphView = graphView;
            this.conceptGraph = conceptGraph;
        }
        AbstractConceptNodeFilterWidget.prototype.checkboxHoveredLambda = function (filterTargetRelatedToCheckbox) {
            var graphView = this.graphView;
            var outerThis = this;
            return function (eventObject) {
                var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(filterTargetRelatedToCheckbox);
                $.each(nodeHideCandidates, function (i, node) {
                    graphView.highlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                });
            };
        };
        AbstractConceptNodeFilterWidget.prototype.checkboxUnhoveredLambda = function (filterTargetRelatedToCheckbox) {
            var graphView = this.graphView;
            var outerThis = this;
            return function (eventObject) {
                var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(filterTargetRelatedToCheckbox);
                $.each(nodeHideCandidates, function (i, node) {
                    graphView.unhighlightHoveredNodeLambda(graphView, outerThis.implementation.getHoverNeedsAdjacentHighlighting())(node, 0);
                });
            };
        };
        AbstractConceptNodeFilterWidget.prototype.deleteSelectedCheckboxesLambda = function (computeNodesToDeleteFunc) {
            return this.graphView.deleteSelectedCheckboxesLambda(function () {
                return computeNodesToDeleteFunc();
            });
        };
        return AbstractConceptNodeFilterWidget;
    })(FilterWidget.AbstractNodeFilterWidget);
    exports.AbstractConceptNodeFilterWidget = AbstractConceptNodeFilterWidget;
});
