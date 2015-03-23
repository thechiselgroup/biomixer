define(["require", "exports", "../Utils", "../GraphView", "Concepts/ConceptGraph"], function (require, exports) {
    var ConceptRangeSliders = (function () {
        function ConceptRangeSliders(graph, graphView, centralConceptUri) {
            this.graph = graph;
            this.graphView = graphView;
            this.centralConceptUri = centralConceptUri;
            this.rangeSliderSlideEvent = function (event, ui) {
            };
        }
        ConceptRangeSliders.prototype.addMenuComponents = function (menuSelector, softNodeCap) {
        };
        ConceptRangeSliders.prototype.updateTopMappingsSliderRange = function () {
        };
        return ConceptRangeSliders;
    })();
    exports.ConceptRangeSliders = ConceptRangeSliders;
});
